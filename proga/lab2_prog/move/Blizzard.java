package move;

import ru.ifmo.se.pokemon.*;

public class Blizzard extends SpecialMove {
    public Blizzard(double power, double acc) {
        super(Type.ICE, power, acc);
    }

    @Override
    protected String describe() {
        return "does Blizzard";
    }

// TODO: implement effect
// Inflicts regular damage.  Has a 10% chance to freeze the target.
// 
// During hail, this move has 100% accuracy.  It also has a (100 - accuracy)% chance to break through the protection of protect and detect.

}