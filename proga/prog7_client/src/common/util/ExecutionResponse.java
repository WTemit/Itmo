package common.util;

import java.io.Serializable;

/**
 * Класс для возврата результата выполнения команды.
 */
public class ExecutionResponse implements Serializable {
	private static final long serialVersionUID = 100L;

	private final boolean exitCode; // Успешность выполнения команды
	private final String message;   // Сообщение для пользователя

	/**
	 * Конструктор для создания объекта ExecutionResponse.
	 *
	 * @param exitCode успешность выполнения команды
	 * @param message  сообщение для пользователя
	 */
	public ExecutionResponse(boolean exitCode, String message) {
		this.exitCode = exitCode;
		this.message = message;
	}

	/**
	 * Упрощённый конструктор для успешного выполнения команды.
	 *
	 * @param message сообщение для пользователя
	 */
	public ExecutionResponse(String message) {
		this(true, message);
	}

	/**
	 * Возвращает успешность выполнения команды.
	 *
	 * @return true, если команда выполнена успешно, иначе false
	 */
	public boolean getExitCode() {
		return exitCode;
	}

	/**
	 * Возвращает сообщение для пользователя.
	 *
	 * @return сообщение
	 */
	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return "ExecutionResponse{" +
				"exitCode=" + exitCode +
				", message='" + message + '\'' +
				'}';
	}
}