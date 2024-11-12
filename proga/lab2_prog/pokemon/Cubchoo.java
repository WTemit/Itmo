package pokemon;

import ru.ifmo.se.pokemon.Pokemon;
import ru.ifmo.se.pokemon.Type;

import move.Blizzard;
import move.AerialAce;
import move.RockTomb;

public class Cubchoo extends Pokemon {
    public Cubchoo(String name, int level) {
        super(name, level);

        super.setType(Type.ICE);

        super.setStats(55,70,40,60,40,40);
        
        Blizzard bli = new Blizzard(110,70);
        super.addMove(bli);
        
        AerialAce aer = new AerialAce(60,100);
        super.addMove(aer);
        
        RockTomb roc = new RockTomb(60,95);
        super.addMove(roc);
        
    }
}