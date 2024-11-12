import pokemon.*;
import ru.ifmo.se.pokemon.*;

public class Main {
    public static void main(String[] args) {
        Battle b = new Battle ();
        Arceus p1 = new Arceus("—ветла€Ћошадка", 5);
        Beartic p2 = new Beartic("—лавик", 5);
        Cubchoo p3 = new Cubchoo("—оплежуй", 5);
        Mamoswine p4 = new Mamoswine("—ерьЄзный“ип", 5);
        Piloswine p5 = new Piloswine("—омнительныйћамонт", 5);
        Swinub p6 = new Swinub("—онливый’леб", 5);
        b.addAlly(p1);
        b.addAlly(p2);
        b.addAlly(p3);
        b.addFoe(p4);
        b.addFoe(p5);
        b.addFoe(p6);
        b.go();
    }
}