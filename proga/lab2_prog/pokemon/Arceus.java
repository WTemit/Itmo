package pokemon;

import ru.ifmo.se.pokemon.Pokemon;
import ru.ifmo.se.pokemon.Type;

import move.Psychic;
import move.DragonClaw;
import move.Will_O_Wisp;
import move.Snarl;

public class Arceus extends Pokemon {
    public Arceus(String name, int level) {
        super(name, level);

        super.setType(Type.NORMAL);

        super.setStats(120,120,120,120,120,120);
        
        Psychic psy = new Psychic(90,100);
        super.addMove(psy);
        
        DragonClaw dra = new DragonClaw(80,100);
        super.addMove(dra);
        
        Will_O_Wisp wil = new Will_O_Wisp(0,85);
        super.addMove(wil);
        
        Snarl sna = new Snarl(55,95);
        super.addMove(sna);
        
    }
}