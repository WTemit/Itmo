package utility;
import java.io.BufferedReader;

public interface Console {
	void print(Object obj);
	void println(Object obj);
	String readln();
	boolean isCanReadln();
	void printError(Object obj);
	void printTable(Object obj1, Object obj2);
	void prompt();
	String getPrompt();
	void selectFileReader(BufferedReader reader);
	void selectConsoleReader();
}