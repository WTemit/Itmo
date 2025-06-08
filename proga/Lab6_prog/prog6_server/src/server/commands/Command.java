package server.commands;

import common.dto.Request;
import common.interfaces.Describable; // Из common
import common.util.ExecutionResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

public abstract class Command implements Describable {
	protected static final Logger logger = LogManager.getLogger(Command.class); // Общий логгер или для каждого свой
	private final String name;
	private final String description;

	public Command(String name, String description) {
		this.name = name;
		this.description = description;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * Выполняет команду, используя данные из запроса.
	 * @param request Объект запроса от клиента.
	 * @return Результат выполнения команды.
	 */
	public abstract ExecutionResponse apply(Request request);

	/**
	 * Старый метод apply - не используется напрямую в сетевой логике.
	 * Можно удалить или оставить для обратной совместимости/тестов,
	 * но он не будет вызываться обработчиком запросов.
	 * @param arguments Массив строк аргументов.
	 * @return Результат выполнения.
	 */
	@Deprecated
	public ExecutionResponse apply(String[] arguments) {
		logger.warn("Вызван устаревший метод apply(String[]) для команды {}", getName());
		return new ExecutionResponse(false, "Некорректный вызов команды на сервере.");
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Command command = (Command) o;
		return Objects.equals(name, command.name) && Objects.equals(description, command.description);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, description);
	}

	@Override
	public String toString() {
		return "Command{" +
				"name='" + name + '\'' +
				", description='" + description + '\'' +
				'}';
	}
}