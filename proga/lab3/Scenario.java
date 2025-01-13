import java.util.ArrayList;
import java.util.List;

public class Scenario {
    private final List<Illegal> characters;
    public Scenario() {
        characters = new ArrayList<>();
    }
    public void addCharacter(Illegal character) {
        characters.add(character);
    }
    public void play(){
        Dunno neznaika = (Dunno) characters.get(0);
        Goatling goatling = (Goatling) characters.get(1);

        neznaika.moveTo(Locate.SHORE);
        goatling.moveTo(Locate.SHORE);
        neznaika.hide(Locate.OVER_BRIDGE);
        goatling.hide(Locate.OVER_BRIDGE);
        goatling.say();
        neznaika.insist();
        neznaika.tryboots("ботинки Козлика", "малы.");
        goatling.move();
        neznaika.sit(Locate.OVER_BRIDGE, "шляпа", "Есть");

    }
}
