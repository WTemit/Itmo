package client.util; // Правильное имя пакета для клиента

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.lang.IllegalStateException;
import java.util.Scanner; // Импортируем Scanner

/**
 * Стандартная реализация консоли для ввода/вывода.
 * Использует System.in, System.out, System.err.
 * Управляет чтением из консоли и (устаревшим способом) из файла.
 */
public class StandardConsole {
	private static final String P1 = "$ "; // Приглашение командной строки
	// Scanner для чтения из консоли (System.in) - теперь предпочтительнее BufferedReader
	private static final Scanner consoleScanner = new Scanner(System.in);

	// BufferedReader для файла - УСТАРЕВШИЙ ПОДХОД
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
	 * Проверяет, готов ли ввод для чтения без блокировки.
	 * ПРИМЕЧАНИЕ: Менее надежно для System.in. Использование Ask и Scanner.hasNextLine() предпочтительнее.
	 * @return true, если ввод готов, иначе false.
	 * @throws IllegalStateException Если произошла ошибка ввода-вывода.
	 */
	public boolean isCanReadln() throws IllegalStateException {
		if (fileMode && fileReader != null) {
			// Устаревшая логика для BufferedReader
			try {
				return fileReader.ready();
			} catch (IOException e) {
				throw new IllegalStateException("Ошибка проверки готовности файлового ввода: " + e.getMessage());
			}
		} else {
			// Для Scanner System.in метод ready() недоступен.
			// Можно попробовать использовать hasNextLine(), но он может блокироваться.
			// Возвращаем true, предполагая, что консоль всегда готова (не очень точно).
			// Лучше полагаться на логику в Ask.
			return true; // Ненадежно для консоли
			// Или можно выбросить исключение:
			// throw new UnsupportedOperationException("isCanReadln не поддерживается для консольного Scanner");
		}
	}

	/**
	 * Выводит строку таблицы в две колонки с форматированием.
	 * @param elementLeft Объект левой колонки.
	 * @param elementRight Объект правой колонки.
	 */
	public void printTable(Object elementLeft, Object elementRight) {
		// Определяем ширину колонки (можно настроить)
		int leftWidth = 35;
		System.out.printf(" %-" + leftWidth + "s | %s%n", elementLeft, elementRight);
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

	/**
	 * Выбирает BufferedReader для чтения (например, для скриптов).
	 * УСТАРЕЛО: Ask теперь управляет переключением Scanner. Этот метод может быть удален.
	 * @param reader BufferedReader для использования при чтении.
	 */
	@Deprecated
	public void selectFileReader(BufferedReader reader) {
		fileReader = reader;
		fileMode = (reader != null);
	}

	/**
	 * Выбирает консольный Scanner (System.in) для чтения.
	 * УСТАРЕЛО: Ask теперь управляет переключением Scanner. Этот метод может быть удален.
	 */
	@Deprecated
	public void selectConsoleReader() {
		fileReader = null; // Сбрасываем файловый ридер
		fileMode = false;
		// Scanner для консоли всегда доступен через consoleScanner
	}
}