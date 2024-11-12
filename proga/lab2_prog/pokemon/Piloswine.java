package pokemon;

import ru.ifmo.se.pokemon.Pokemon;
import ru.ifmo.se.pokemon.Type;

import move.IcyWind;

public class Piloswine extends Swinub {
    public Piloswine(String name, int level) {
        super(name, level);

        super.setType(Type.ICE,Type.GROUND);

        super.setStats(100,100,80,60,60,50);
        
        IcyWind icy = new IcyWind(55,95);
        super.addMove(icy);
        
    }
}