package managers;

import java.util.HashSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import models.Worker;
import java.time.LocalDateTime;

/**
 * Оперирует коллекцией.
 */
public class CollectionManager {
	private long currentId = 1;
	private Map<Long, Worker> workers = new HashMap<>();
	private HashSet<Worker> collection = new HashSet<>();
	private LocalDateTime lastInitTime;
	private LocalDateTime lastSaveTime;
	private final DumpManager dumpManager;

	public CollectionManager(DumpManager dumpManager) {
		this.lastInitTime = null;
		this.lastSaveTime = null;
		this.dumpManager = dumpManager;
	}

	/**
	 * @return коллекция.
	 */
	public HashSet<Worker> getCollection() {
		return collection;
	}

	/**
	 * @return Последнее время инициализации.
	 */
	public LocalDateTime getLastInitTime() {
		return lastInitTime;
	}

	/**
	 * @return Последнее время сохранения.
	 */
	public LocalDateTime getLastSaveTime() {
		return lastSaveTime;
	}

	/**
	 * Сохраняет коллекцию в файл
	 */
	public void saveCollection() {
		dumpManager.writeCollection(collection);
		lastSaveTime = LocalDateTime.now();
	}

	/**
	 * Получить рабочего по ID
	 */
	public Worker byId(Long id) {
		return workers.get(id);
	}

	/**
	 * Содержит ли колекции рабочего
	 */
	public boolean isСontain(Worker e) {
		return e == null || byId(e.getId()) != null;
	}

	/**
	 * Получить свободный ID
	 */
	public long getFreeId() {
		while (byId(currentId) != null)
			if (++currentId < 0)
				currentId = 1;
		return currentId;
	}

	/**
	 * Добавляет работника
	 */
	public boolean add(Worker d) {
		if (isСontain(d)) return false;
		workers.put(d.getId(), d);
		collection.add(d);
		return true;
	}

	public boolean remove(long id) {
		var a = byId(id);
		if (a == null) return false;
		workers.remove(a.getId());
		collection.remove(a);
		return true;
	}

	/**
	 * Получить отсортированную коллекцию
	 * @return отсортированный список элементов коллекции
	 */
	public List<Worker> getSortedCollection() {
		List<Worker> sortedList = new ArrayList<>(collection);
		Collections.sort(sortedList);
		return sortedList;
	}

	public boolean loadCollection() {
		workers.clear();
		collection = dumpManager.readCollection();
		lastInitTime = LocalDateTime.now();

		for (Worker e : collection) {
			if (byId(e.getId()) != null) {
				collection.clear();
				workers.clear();
				return false;
			} else {
				if (e.getId() > currentId) currentId = e.getId();
				workers.put(e.getId(), e);
			}
		}
		return true;
	}

	@Override
	public String toString() {
		if (collection.isEmpty()) return "Коллекция пуста!";

		StringBuilder info = new StringBuilder();
		List<Worker> sortedWorkers = getSortedCollection();
		for (Worker worker : sortedWorkers) {
			info.append(worker).append("\n\n");
		}
		return info.toString().trim();
	}
}