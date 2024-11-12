package move;

import ru.ifmo.se.pokemon.*;

public class IcicleCrash extends PhysicalMove {
    public IcicleCrash(double power, double acc) {
        super(Type.ICE, power, acc);
    }

    @Override
    protected String describe() {
        return "does Icicle Crash";
    }

// TODO: implement effect
// Inflicts regular damage.  Has a 30% chance to make the target flinch.

}