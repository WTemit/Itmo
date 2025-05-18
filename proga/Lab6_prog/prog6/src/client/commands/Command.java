package client.commands;

import client.network.ClientNetworkIO;
import client.util.Ask; // Для AskBreak exception
import client.util.StandardConsole; // Или ваш интерфейс консоли
import common.dto.Request;
import common.dto.Response;
import common.interfaces.Describable;
import common.util.ExecutionResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * Абстрактный базовый класс для команд на стороне клиента.
 * Обрабатывает логику взаимодействия, создание запросов и сетевой ввод-вывод.
 */
public abstract class Command implements Describable {
	protected static final Logger logger = LogManager.getLogger(Command.class);
	private final String name;
	private final String description;
	protected final ClientNetworkIO networkIO;
	protected final StandardConsole console; // Для взаимодействия с пользователем (Ask)

	public Command(String name, String description, ClientNetworkIO networkIO, StandardConsole console) {
		this.name = name;
		this.description = description;
		this.networkIO = networkIO;
		this.console = console; // Сохраняем консоль для Ask
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
	 * Выполняет логику команды на стороне клиента:
	 * 1. Собирает аргументы (потенциально используя Ask).
	 * 2. Создает объект Request.
	 * 3. Отправляет Request с помощью networkIO.
	 * 4. Получает Response с помощью networkIO.
	 * 5. Возвращает ExecutionResponse из Response сервера.
	 *
	 * @param arguments Аргументы командной строки (разделенная строка).
	 * @return ExecutionResponse, содержащий результат от сервера или ошибку/статус на стороне клиента.
	 */
	public abstract ExecutionResponse apply(String[] arguments);


	/** Вспомогательный метод для отправки запроса и обработки ответа */
	protected ExecutionResponse sendRequestAndGetResponse(Request request) {
		Response serverResponse = networkIO.sendRequestAndReceiveResponse(request);
		if (serverResponse == null) {
			// NetworkIO, вероятно, залогировал ошибку (тайм-аут, ошибка ввода-вывода, ошибка десериализации)
			return new ExecutionResponse(false, "Не удалось получить ответ от сервера. Проверьте логи.");
		}
		return serverResponse.getExecutionResponse();
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