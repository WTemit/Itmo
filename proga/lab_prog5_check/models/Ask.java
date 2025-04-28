package models;

import java.util.Objects;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import models.*;
import utility.Console;
import java.util.NoSuchElementException;
import java.lang.IllegalArgumentException;
import java.util.Scanner;

import managers.CollectionManager;

/**
 * Класс чтения объекта
 */
public class Ask {
	private static Scanner currentScanner = new Scanner(System.in);
	public static class AskBreak extends Exception {
	}

	/**
	 * Устанавливает источник ввода данных
	 * @param scanner Scanner для чтения ввода (System.in для консоли или файловый Scanner для скриптов)
	 */
	public static void setScanner(Scanner scanner) {
		currentScanner = scanner;
	}

	public static Worker askWorker(Console console, long id) throws AskBreak {

		try {
			console.print("name: ");
			String name;
			while (true) {
				name = console.readln().trim();
				if (name.equals("exit")) throw new AskBreak();
				if (!name.equals("")) break;
				console.print("name: ");
			}
			var coordinates = askCoordinates(console);
			console.print("salary: ");
			Double salary;
			while (true) {
				var line = console.readln().trim();
				if (line.equals("exit")) throw new AskBreak();
				if (line.equals("")) {
					salary = null;
					break;
				}
				try {
					salary = Double.parseDouble(line);
					if (salary > 0) break;
				} catch (NumberFormatException e) {
				}
				console.print("salary: ");
			}
			var position = askPosition(console);
			var organization = askOrganization(console);
			return new Worker(id, name, coordinates, salary, position, organization);
		} catch (NoSuchElementException | IllegalStateException e) {
			console.printError("Ошибка чтения");
			return null;
		}
	}

	public static Coordinates askCoordinates(Console console) throws AskBreak {
		try {
			// private Integer x; //Значение поля должно быть больше -485, Поле не может быть null
			// private Double y; //Максимальное значение поля: 907, Поле не может быть null
			console.print("coordinates.x: ");
			Long x;
			while (true) {
				var line = console.readln().trim();
				if (line.equals("exit")) throw new AskBreak();
				if (!line.equals("")) {
					try {
						x = Long.parseLong(line);
						if (x > -472) break;
					} catch (NumberFormatException e) {
					}
				}
				console.print("coordinates.x: ");
			}
			console.print("coordinates.y: ");
			Integer y;
			while (true) {
				var line = console.readln().trim();
				if (line.equals("exit")) throw new AskBreak();
				if (!line.equals("")) {
					try {
						y = Integer.parseInt(line);
						break;
					} catch (NumberFormatException e) {
					}
				}
				console.print("coordinates.y: ");
			}

			return new Coordinates(x, y);
		} catch (NoSuchElementException | IllegalStateException e) {
			console.printError("Ошибка чтения");
			return null;
		}
	}

	public static Position askPosition(Console console) throws AskBreak {
	 try {
	 	console.print("Position ("+Position.names()+"): ");
	 	Position r;
	 	while (true) {
			var line = console.readln().trim();
			if (line.equals("exit")) throw new AskBreak();
			if (!line.equals("")) {
				try {
					r = Position.valueOf(line);
					break;
				} catch (NullPointerException | IllegalArgumentException e) {
				}
			}
			console.print("Posision: ");
		}
		 return r;
	 } catch (NoSuchElementException | IllegalStateException e) {
	 	console.printError("Ошибка чтения");
	 	return null;
	 	}
	 }
	public static Organization askOrganization(Console console) throws AskBreak {
		try {
			/*
				 private Integer annualTurnover; //Поле может быть null, Значение поля должно быть больше 0
    			 private OrganizationType type; //Поле может быть null
    			 private Address officialAddress; //Поле может быть null
			*/
			console.print("organization.annualTurnover(number): ");
			Integer annualTurnover;
			var line = console.readln().trim();
			if (line.equals("exit")) throw new AskBreak();
			if (line.equals("")) return null;
			while(true) {
				if (!line.equals("")) {
					try {
						annualTurnover = Integer.valueOf(line);
						break;
					} catch (NumberFormatException e) {
					}
					console.print("organization.annualTurnover(number): ");
				}
			}

			console.print("organization.type(PUBLIC, TRUST, PRIVATE_LIMITED_COMPANY, OPEN_JOINT_STOCK_COMPANY) : ");
			OrganizationType type;
			while (true) {
				line = console.readln().trim();
				if (line.equals("exit")) throw new AskBreak();
				if (line.equals("")) { type = null; break;}
				try { type = OrganizationType.valueOf(line); break; } catch (NullPointerException | IllegalArgumentException  e) { }
				console.print("organization.type(PUBLIC, TRUST, PRIVATE_LIMITED_COMPANY, OPEN_JOINT_STOCK_COMPANY) : ");
			}


			console.print("person.officialAddress: ");
			var address = askAddress(console);


			return new Organization(annualTurnover, type, address);
		} catch (NoSuchElementException | IllegalStateException e) {
			console.printError("Ошибка чтения");
			return null;
		}
	}

	public static Address askAddress(Console console) throws AskBreak{
		console.print("street : ");
		String street;
		while (true){
			var line = console.readln().trim();
			if (line.equals("exit")) throw new AskBreak();
			if (line.equals("")) { street = null; break;}
			try { street = line; break;} catch (Exception e) { }
		}

		var town = askTown(console);

		return new Address(street, town);
	}

	private static Location askTown(Console console) throws AskBreak {
		console.print("town.x : ");
		Double x;
		while (true) {
			var line = console.readln().trim();
			if (line.equals("exit")) throw new AskBreak();
			if (!line.equals("")) {
				try {
					x = Double.parseDouble(line);
                    break;
				} catch (NumberFormatException e) {
				}
			}
			console.print("town.x: ");
		}
		console.print("town.y: ");
		Integer y;
		while (true) {
			var line = console.readln().trim();
			if (line.equals("exit")) throw new AskBreak();
			if (!line.equals("")) {
				try {
					y = Integer.parseInt(line);
					break;
				} catch (NumberFormatException e) {
				}
			}
			console.print("town.y: ");
		}
		console.print("town.name: ");
		String name;
		while (true) {
			name = console.readln().trim();
			if (name.equals("exit")) throw new AskBreak();
			if (!name.equals("")) break;
			console.print("name: ");
		}
		return new Location(x,y,name);
	}
}