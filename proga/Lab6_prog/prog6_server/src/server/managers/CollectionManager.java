package server.managers;

import common.models.Worker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * Управляет коллекцией Worker'ов. Потокобезопасен для чтения и записи.
 */
public class CollectionManager {
	private static final Logger logger = LogManager.getLogger(CollectionManager.class);

	private final DumpManager dumpManager;
	private HashSet<Worker> collection = new HashSet<>();
	// Карта для быстрого доступа по ID (ключ - ID, значение - Worker)
	private Map<Long, Worker> workersMap = new HashMap<>();
	private LocalDateTime lastInitTime;
	private LocalDateTime lastSaveTime;
	private long currentId = 1; // Используется для генерации getFreeId

	// Блокировка для обеспечения потокобезопасности при модификации коллекции
	private final ReadWriteLock collectionLock = new ReentrantReadWriteLock(true); // true для 'fair'

	public CollectionManager(DumpManager dumpManager, Logger parentLogger) { // Можно передать логгер, если нужно
		this.dumpManager = dumpManager;
		this.lastInitTime = null;
		this.lastSaveTime = null;
		logger.info("CollectionManager инициализирован.");
	}

	/**
	 * @return Текущая коллекция (копия для безопасности).
	 */
	public HashSet<Worker> getCollection() {
		collectionLock.readLock().lock();
		try {
			return new HashSet<>(collection);
		} finally {
			collectionLock.readLock().unlock();
		}
	}

	/**
	 * @return Время последней инициализации коллекции из файла.
	 */
	public LocalDateTime getLastInitTime() {
		return lastInitTime;
	}

	/**
	 * @return Время последнего сохранения коллекции в файл.
	 */
	public LocalDateTime getLastSaveTime() {
		return lastSaveTime;
	}

	/**
	 * Загружает коллекцию из файла, используя DumpManager.
	 * @return true, если загрузка прошла успешно, иначе false.
	 */
	public boolean loadCollection() {
		logger.info("Загрузка коллекции из файла...");
		collectionLock.writeLock().lock(); // Блокируем на запись перед полной заменой
		try {
			collection.clear(); // Очищаем старые данные
			workersMap.clear();
			currentId = 1; // Сбрасываем счетчик ID

			HashSet<Worker> loadedCollection = dumpManager.readCollection();
			if (loadedCollection == null) {
				logger.error("Ошибка при чтении коллекции из файла. DumpManager вернул null.");
				return false; // dumpManager должен был залогировать детали
			}

			long maxId = 0;
			int duplicates = 0;
			for (Worker worker : loadedCollection) {
				if (worker != null && worker.getId() != null) {
					if (workersMap.containsKey(worker.getId())) {
						logger.warn("Обнаружен дублирующийся ID {} при загрузке. Работник пропущен.", worker.getId());
						duplicates++;
					} else {
						collection.add(worker);
						workersMap.put(worker.getId(), worker);
						if (worker.getId() > maxId) {
							maxId = worker.getId();
						}
					}
				} else {
					logger.warn("Обнаружен null worker или worker с null ID при загрузке. Пропущен.");
				}
			}
			// Устанавливаем currentId на основе максимального загруженного + 1
			currentId = maxId + 1;

			lastInitTime = LocalDateTime.now();
			logger.info("Коллекция загружена. {} элементов добавлено, {} дубликатов пропущено. Текущий свободный ID начнется с {}.",
					collection.size(), duplicates, currentId);
			return true;
		} catch (Exception e) {
			logger.error("Непредвиденная ошибка при загрузке и обработке коллекции: {}", e.getMessage(), e);
			collection.clear(); // Очищаем в случае серьезной ошибки
			workersMap.clear();
			return false;
		} finally {
			collectionLock.writeLock().unlock();
		}
	}

	/**
	 * Сохраняет текущую коллекцию в файл, используя DumpManager.
	 */
	public void saveCollection() {
		logger.info("Сохранение коллекции в файл...");
		collectionLock.readLock().lock(); // Блокируем на чтение, т.к. dumpManager работает с копией
		try {
			dumpManager.writeCollection(new HashSet<>(collection)); // Передаем копию
			lastSaveTime = LocalDateTime.now();
			logger.info("Коллекция успешно сохранена в файл.");
		} catch (Exception e) {
			logger.error("Ошибка при сохранении коллекции: {}", e.getMessage(), e);
			// Здесь можно решить, нужно ли пробрасывать исключение дальше
		} finally {
			collectionLock.readLock().unlock();
		}
	}



	public void clearCollection() { // Метод для полной очистки
		collectionLock.writeLock().lock(); // Блокировка на запись
		try {
			collection.clear();    // Очищаем основной HashSet
			workersMap.clear();    // Очищаем карту ID, если она используется
			// currentId можно не сбрасывать, getFreeId продолжит работать
			logger.info("Коллекция и карта ID были полностью очищены.");
		} finally {
			collectionLock.writeLock().unlock();
		}
	}

	/**
	 * Добавляет нового работника в коллекцию. ID должен быть уже установлен!
	 * @param worker Работник для добавления.
	 * @return true, если работник успешно добавлен, false если ID уже существует.
	 */
	public boolean add(Worker worker) {
		if (worker == null || worker.getId() == null) {
			logger.warn("Попытка добавить null worker или worker с null ID.");
			return false;
		}
		collectionLock.writeLock().lock();
		try {
			if (workersMap.containsKey(worker.getId())) {
				logger.warn("Попытка добавить работника с уже существующим ID: {}", worker.getId());
				return false;
			}
			workersMap.put(worker.getId(), worker);
			collection.add(worker);
			logger.debug("Работник с ID {} добавлен в коллекцию.", worker.getId());
			return true;
		} finally {
			collectionLock.writeLock().unlock();
		}
	}

	/**
	 * Удаляет работника из коллекции по ID.
	 * @param id ID работника для удаления.
	 * @return true, если работник был найден и удален, иначе false.
	 */
	public boolean remove(long id) {
		collectionLock.writeLock().lock();
		try {
			Worker removedWorker = workersMap.remove(id); // Удаляем из карты
			if (removedWorker != null) {
				boolean removedFromSet = collection.remove(removedWorker); // Удаляем из сета
				if (removedFromSet) {
					logger.debug("Работник с ID {} удален из коллекции.", id);
					return true;
				} else {
					// Эта ситуация странная: был в карте, но не в сете?
					logger.error("Работник с ID {} был удален из карты, но не найден в сете!", id);
					// Можно попытаться восстановить консистентность, но лучше разобраться в причине
					return false; // Считаем операцию неуспешной
				}
			} else {
				logger.warn("Попытка удаления несуществующего ID: {}", id);
				return false; // Элемент не найден
			}
		} finally {
			collectionLock.writeLock().unlock();
		}
	}

	/**
	 * Обновляет данные работника с указанным ID.
	 * Заменяет старый объект новым в коллекции и карте.
	 * @param id ID работника для обновления.
	 * @param newWorkerData Новый объект Worker с обновленными данными (ID и дата создания должны быть как у старого).
	 * @return true, если обновление прошло успешно, false если ID не найден.
	 */
	public boolean update(long id, Worker newWorkerData) {
		if (newWorkerData == null || newWorkerData.getId() == null || newWorkerData.getId() != id) {
			logger.warn("Попытка обновления ID {} некорректными данными (null, null ID, или несовпадающий ID).", id);
			return false;
		}
		collectionLock.writeLock().lock();
		try {
			Worker oldWorker = workersMap.get(id);
			if (oldWorker == null) {
				logger.warn("Попытка обновления несуществующего ID: {}", id);
				return false; // ID не найден
			}

			// Удаляем старый объект из сета (важно сделать до добавления нового, т.к. hashCode мог измениться)
			collection.remove(oldWorker);

			// Обновляем карту и сет новым объектом
			workersMap.put(id, newWorkerData);
			collection.add(newWorkerData);

			logger.debug("Работник с ID {} успешно обновлен.", id);
			return true;
		} finally {
			collectionLock.writeLock().unlock();
		}
	}


	/**
	 * Находит работника по его ID.
	 * @param id ID для поиска.
	 * @return Объект Worker или null, если не найден.
	 */
	public Worker byId(long id) {
		collectionLock.readLock().lock();
		try {
			Worker worker = workersMap.get(id);
			// logger.trace("Поиск по ID {}: {}", id, (worker == null ? "не найден" : "найден")); // Для детального лога
			return worker;
		} finally {
			collectionLock.readLock().unlock();
		}
	}

	/**
	 * Генерирует следующий свободный ID для нового элемента.
	 * Потокобезопасен при использовании блокировки.
	 * @return Свободный ID.
	 */
	public long getFreeId() {
		collectionLock.readLock().lock(); // Достаточно блокировки на чтение для проверки containsKey
		try {
			// Простой инкремент, пока не найдем свободный
			// В очень редких случаях может зациклиться, если ID переполнятся
			// и начнутся с 1 заново, но это маловероятно.
			while (workersMap.containsKey(currentId)) {
				if (++currentId <= 0) { // Проверка на переполнение long
					currentId = 1; // Начинаем сначала, если переполнился
					logger.warn("Счетчик ID переполнился и был сброшен на 1.");
				}
			}
			// logger.trace("Сгенерирован свободный ID: {}", currentId);
			return currentId; // Возвращаем найденный свободный ID (не инкрементируем здесь)
		} finally {
			collectionLock.readLock().unlock();
		}
	}


	/**
	 * Возвращает отсортированный список работников (по имени).
	 * @return Отсортированный список List<Worker>.
	 */
	public List<Worker> getSortedByNameCollection() {
		collectionLock.readLock().lock();
		try {
			List<Worker> sortedList = collection.stream()
					.sorted(Comparator.comparing(Worker::getName, String.CASE_INSENSITIVE_ORDER)) // Сортировка по имени без учета регистра
					.collect(Collectors.toList());
			// logger.trace("Возвращен отсортированный по имени список из {} элементов.", sortedList.size());
			return sortedList;
		} finally {
			collectionLock.readLock().unlock();
		}
	}

	/**
	 * Представление коллекции в виде строки (для команды info или логов).
	 * @return Строковое представление.
	 */
	@Override
	public String toString() {
		collectionLock.readLock().lock();
		try {
			if (collection.isEmpty()) {
				return "Коллекция пуста!";
			}
			// Используем сортированный по имени список для вывода
			StringBuilder info = new StringBuilder("Элементы коллекции (" + collection.size() + "):\n");
			List<Worker> sortedWorkers = getSortedByNameCollection(); // Уже использует блокировку
			for (Worker worker : sortedWorkers) {
				info.append(worker.toString()).append("\n---\n"); // Разделитель для читаемости
			}
			return info.toString().trim();
		} finally {
			collectionLock.readLock().unlock();
		}
	}
}