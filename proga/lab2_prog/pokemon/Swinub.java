package pokemon;

import ru.ifmo.se.pokemon.Pokemon;
import ru.ifmo.se.pokemon.Type;

import move.Swagger;
import move.Facade;

public class Swinub extends Pokemon {
    public Swinub(String name, int level) {
        super(name, level);

        super.setType(Type.ICE,Type.GROUND);

        super.setStats(50,50,40,30,30,50);
        
        Swagger swa = new Swagger(0,85);
        super.addMove(swa);
        
        Facade fac = new Facade(70,100);
        super.addMove(fac);
        
    }
}