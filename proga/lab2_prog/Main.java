import pokemon.*;
import ru.ifmo.se.pokemon.*;

public class Main {
    public static void main(String[] args) {
        Battle b = new Battle ();
        Arceus p1 = new Arceus("��������������", 5);
        Beartic p2 = new Beartic("������", 5);
        Cubchoo p3 = new Cubchoo("��������", 5);
        Mamoswine p4 = new Mamoswine("������������", 5);
        Piloswine p5 = new Piloswine("������������������", 5);
        Swinub p6 = new Swinub("������������", 5);
        b.addAlly(p1);
        b.addAlly(p2);
        b.addAlly(p3);
        b.addFoe(p4);
        b.addFoe(p5);
        b.addFoe(p6);
        b.go();
    }
}