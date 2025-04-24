package utility;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.lang.IllegalStateException;

public class StandardConsole implements Console {
	private static final String P1 = "$ ";
	private static BufferedReader fileReader = null;
	private static BufferedReader defReader = new BufferedReader(new InputStreamReader(System.in));

	/**
	 * Выводит obj.toString() в консоль
	 * @param obj Объект для печати
	 */
	public void print(Object obj) {
		System.out.print(obj);
	}

	/**
	 * Выводит obj.toString() + \n в консоль
	 * @param obj Объект для печати
	 */
	public void println(Object obj) {
		System.out.println(obj);
	}

	/**
	 * Выводит ошибка: obj.toString() в консоль
	 * @param obj Ошибка для печати
	 */
	public void printError(Object obj) {
		System.err.println("Error(err): " + obj);
		System.out.println("Error(out): " + obj);
	}

	public String readln() throws NoSuchElementException, IllegalStateException {
		try {
			String line = (fileReader != null ? fileReader : defReader).readLine();
			if (line == null) {
				throw new NoSuchElementException();
			}
			return line;
		} catch (IOException e) {
			throw new IllegalStateException("Ошибка при чтении строки");
		}
	}

	public boolean isCanReadln() throws IllegalStateException {
		try {
			if (fileReader != null) {
				// Mark the current position in the reader
				fileReader.mark(1);
				// Try to read a character
				int nextChar = fileReader.read();
				// Reset to the marked position
				fileReader.reset();
				// If nextChar is -1, we've reached the end of the stream
				return nextChar != -1;
			} else {
				// For console input, assume we can always read
				return defReader.ready();
			}
		} catch (IOException e) {
			throw new IllegalStateException("Ошибка при проверке доступности строки");
		}
	}

	/**
	 * Выводит таблицу из 2 колонок
	 * @param elementLeft Левый элемент колонки.
	 * @param elementRight Правый элемент колонки.
	 */
	public void printTable(Object elementLeft, Object elementRight) {
		System.out.printf(" %-35s%-1s%n", elementLeft, elementRight);
	}

	/**
	 * Выводит prompt1 текущей консоли
	 */
	public void prompt() {
		print(P1);
	}

	/**
	 * @return prompt1 текущей консоли
	 */
	public String getPrompt() {
		return P1;
	}

	public void selectFileReader(BufferedReader reader) {
		this.fileReader = reader;
	}

	public void selectConsoleReader() {
		this.fileReader = null;
	}
}