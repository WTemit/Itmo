package pokemon;

import ru.ifmo.se.pokemon.Pokemon;
import ru.ifmo.se.pokemon.Type;

import move.IcicleCrash;

public class Beartic extends Cubchoo {
    public Beartic(String name, int level) {
        super(name, level);

        super.setType(Type.ICE);

        super.setStats(95,130,80,70,80,50);
        
        IcicleCrash ici = new IcicleCrash(85,90);
        super.addMove(ici);
        
    }
}