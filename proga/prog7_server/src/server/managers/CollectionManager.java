package server.managers;

import common.dto.User;
import common.models.Worker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.db.DatabaseManager;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Управляет коллекцией Workers в памяти. Потокобезопасный.
 * Синхронизирует свое состояние с базой данных.
 */
public class CollectionManager {
	private static final Logger logger = LogManager.getLogger(CollectionManager.class);

	private final DatabaseManager databaseManager;
	private final Set<Worker> collection = new HashSet<>();
	private final Map<Long, Worker> workersMap = new HashMap<>();
	private LocalDateTime lastInitTime;
	private final Lock collectionLock = new ReentrantLock(true);

	public CollectionManager(DatabaseManager databaseManager) {
		this.databaseManager = databaseManager;
		this.lastInitTime = null;
		logger.info("CollectionManager инициализирован с DatabaseManager.");
	}

	/**
	 * @return Копия текущей коллекции.
	 */
	public Set<Worker> getCollection() {
		collectionLock.lock();
		try {
			return new HashSet<>(collection);
		} finally {
			collectionLock.unlock();
		}
	}

	/**
	 * @return Время последней инициализации коллекции.
	 */
	public LocalDateTime getLastInitTime() {
		return lastInitTime;
	}

	/**
	 * Загружает коллекцию из базы данных. Этот метод следует вызывать при запуске сервера.
	 */
	public void loadCollection() {
		logger.info("Загрузка коллекции из базы данных...");
		collectionLock.lock();
		try {
			collection.clear();
			workersMap.clear();

			Set<Worker> loadedCollection = databaseManager.loadWorkers();
			for (Worker worker : loadedCollection) {
				collection.add(worker);
				workersMap.put(worker.getId(), worker);
			}
			lastInitTime = LocalDateTime.now();
			logger.info("Коллекция успешно загружена из БД. {} элементов.", collection.size());
		} finally {
			collectionLock.unlock();
		}
	}

	/**
	 * Добавляет нового работника в БД, а затем в коллекцию в памяти.
	 * @param worker Работник, которого нужно добавить.
	 * @param user Пользователь, добавляющий работника.
	 * @return ID добавленного работника или -1 в случае неудачи.
	 */
	public long add(Worker worker, User user) {
		collectionLock.lock();
		try {
			long newId = databaseManager.addWorker(worker, user);
			if (newId > 0) {
				worker.setId(newId);
				worker.setOwnerUsername(user.getUsername());
				collection.add(worker);
				workersMap.put(newId, worker);
				logger.info("Работник с ID {} от пользователя '{}' добавлен в БД и коллекцию.", newId, user.getUsername());
				return newId;
			}
			return -1;
		} finally {
			collectionLock.unlock();
		}
	}

	/**
	 * Обновляет работника в БД, а затем в коллекции.
	 * Проверяет право собственности перед выполнением обновления.
	 * @param id ID работника для обновления.
	 * @param newWorkerData Новые данные.
	 * @param user Пользователь, выполняющий операцию.
	 * @return true в случае успеха.
	 */
	public boolean update(long id, Worker newWorkerData, User user) {
		collectionLock.lock();
		try {
			Worker oldWorker = byId(id);
			if (oldWorker == null) {
				logger.warn("Обновление не удалось: Работник с ID {} не найден.", id);
				return false;
			}
			if (!oldWorker.getOwnerUsername().equals(user.getUsername())) {
				logger.warn("Ошибка авторизации: Пользователь '{}' не может обновить работника с ID {}, принадлежащего '{}'.", user.getUsername(), id, oldWorker.getOwnerUsername());
				return false;
			}

			// Сначала обновляем БД. Только в случае успеха обновляем в памяти.
			if (databaseManager.updateWorker(id, newWorkerData, user)) {
				newWorkerData.setId(id);
				newWorkerData.setCreationDate(oldWorker.getCreationDate());
				newWorkerData.setOwnerUsername(user.getUsername());
				collection.remove(oldWorker);
				collection.add(newWorkerData);
				workersMap.put(id, newWorkerData);
				logger.info("Работник с ID {} обновлен пользователем '{}'.", id, user.getUsername());
				return true;
			}
			logger.error("Не удалось обновить работника с ID {} в БД.", id);
			return false;
		} finally {
			collectionLock.unlock();
		}
	}

	/**
	 * Удаляет работника из БД, а затем из коллекции.
	 * Проверяет право собственности.
	 * @param id ID работника для удаления.
	 * @param user Пользователь, выполняющий операцию.
	 * @return true в случае успеха.
	 */
	public boolean remove(long id, User user) {
		collectionLock.lock();
		try {
			Worker workerToRemove = byId(id);
			if (workerToRemove == null) {
				logger.warn("Удаление не удалось: Работник с ID {} не найден.", id);
				return false;
			}
			if (!workerToRemove.getOwnerUsername().equals(user.getUsername())) {
				logger.warn("Ошибка авторизации: Пользователь '{}' не может удалить работника с ID {}, принадлежащего '{}'.", user.getUsername(), id, workerToRemove.getOwnerUsername());
				return false;
			}

			if (databaseManager.removeWorker(id, user)) {
				collection.remove(workerToRemove);
				workersMap.remove(id);
				logger.info("Работник с ID {} удален пользователем '{}'.", id, user.getUsername());
				return true;
			}
			logger.error("Не удалось удалить работника с ID {} из БД.", id);
			return false;
		} finally {
			collectionLock.unlock();
		}
	}

	/**
	 * Очищает коллекцию от объектов, принадлежащих указанному пользователю.
	 * @param user Пользователь, чьи объекты нужно удалить.
	 * @return Количество удаленных элементов.
	 */
	public int clear(User user) {
		collectionLock.lock();
		try {
			int removedCountInDb = databaseManager.clearWorkers(user);
			if (removedCountInDb >= 0) {
				Set<Worker> toRemove = collection.stream()
						.filter(w -> w.getOwnerUsername().equals(user.getUsername()))
						.collect(Collectors.toSet());

				for (Worker w : toRemove) {
					collection.remove(w);
					workersMap.remove(w.getId());
				}
				logger.info("Очищено {} работников, принадлежащих '{}'.", toRemove.size(), user.getUsername());
				return toRemove.size();
			}
			return -1;
		} finally {
			collectionLock.unlock();
		}
	}

	/**
	 * Находит работника по его ID.
	 * @param id ID для поиска.
	 * @return Объект Worker или null, если не найден.
	 */
	public Worker byId(long id) {
		collectionLock.lock();
		try {
			return workersMap.get(id);
		} finally {
			collectionLock.unlock();
		}
	}

	/**
	 * Возвращает тип коллекции.
	 * @return Простое имя класса коллекции.
	 */
	public String getCollectionType() {
		collectionLock.lock();
		try {
			return collection.getClass().getSimpleName();
		} finally {
			collectionLock.unlock();
		}
	}

	/**
	 * Возвращает размер коллекции.
	 * @return Количество элементов в коллекции.
	 */
	public int getCollectionSize() {
		collectionLock.lock();
		try {
			return collection.size();
		} finally {
			collectionLock.unlock();
		}
	}
}