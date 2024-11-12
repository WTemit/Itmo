package pokemon;

import ru.ifmo.se.pokemon.Pokemon;
import ru.ifmo.se.pokemon.Type;

import move.Facade;

public class Mamoswine extends Piloswine {
    public Mamoswine(String name, int level) {
        super(name, level);

        super.setType(Type.ICE,Type.GROUND);

        super.setStats(110,130,80,70,60,80);
        
        Facade fac = new Facade(70,100);
        super.addMove(fac);
        
    }
}