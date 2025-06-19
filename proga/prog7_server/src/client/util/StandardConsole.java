package client.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.lang.IllegalStateException;
import java.util.Scanner;

/**
 * Стандартная реализация консоли для ввода/вывода.
 * Использует System.in, System.out, System.err.
 * Управляет чтением из консоли и (устаревшим способом) из файла.
 */
public class StandardConsole {
	private static final String P1 = "$ "; // Приглашение командной строки
	// Scanner для чтения из консоли (System.in) - теперь предпочтительнее BufferedReader
	private static final Scanner consoleScanner = new Scanner(System.in);
	// Логика переключения ввода теперь управляется в client.util.Ask
	private static BufferedReader fileReader = null;
	private boolean fileMode = false; // Флаг для определения источника

	/**
	 * Выводит объект в консоль без перевода строки.
	 * @param obj Объект для вывода (используется obj.toString()).
	 */
	public void print(Object obj) {
		System.out.print(obj);
	}

	/**
	 * Выводит объект в консоль с переводом строки.
	 * @param obj Объект для вывода (используется obj.toString()).
	 */
	public void println(Object obj) {
		System.out.println(obj);
	}

	/**
	 * Выводит сообщение об ошибке в стандартный поток ошибок и стандартный поток вывода.
	 * Добавляет префикс "Error: ".
	 * @param obj Объект/сообщение ошибки для вывода.
	 */
	public void printError(Object obj) {
		// Выводим в stderr и stdout для видимости в разных средах
		System.err.println("Error: " + obj);
		// Дублируем в stdout, чтобы ошибка была видна, даже если stderr перенаправлен
		System.out.println("Error(out): " + obj);
	}

	/**
	 * Читает строку текста из текущего источника ввода (консоль или файл).
	 * ПРИМЕЧАНИЕ: client.util.Ask.readLine() теперь является основным методом
	 * для чтения ввода пользователя/скрипта, так как он обрабатывает
	 * переключение Scanner и эхо скрипта. Этот метод может использоваться реже напрямую.
	 * @return Прочитанная строка.
	 * @throws NoSuchElementException если строка не найдена (конец ввода).
	 * @throws IllegalStateException если произошла ошибка ввода-вывода или Scanner закрыт.
	 */
	public String readln() throws NoSuchElementException, IllegalStateException {
		if (fileMode && fileReader != null) {
			// Устаревшая логика чтения из файла через BufferedReader
			try {
				String line = fileReader.readLine();
				if (line == null) {
					throw new NoSuchElementException("Достигнут конец файлового потока.");
				}
				return line;
			} catch (IOException e) {
				throw new IllegalStateException("Ошибка чтения строки из файла: " + e.getMessage());
			}
		} else {
			// Чтение из консоли через Scanner
			if (!consoleScanner.hasNextLine()) { // Проверка перед чтением
				throw new NoSuchElementException("Нет следующей строки в консольном вводе.");
			}
			return consoleScanner.nextLine();
		}
	}

	/**
	 * Выводит приглашение командной строки.
	 */
	public void prompt() {
		print(P1);
	}

	/**
	 * @return Строка приглашения командной строки.
	 */
	public String getPrompt() {
		return P1;
	}


}