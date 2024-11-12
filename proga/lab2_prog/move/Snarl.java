package move;

import ru.ifmo.se.pokemon.*;

public class Snarl extends SpecialMove {
    public Snarl(double power, double acc) {
        super(Type.DARK, power, acc);
    }

    @Override
    protected String describe() {
        return "does Snarl";
    }

// TODO: implement effect
// Inflicts regular damage.  Has a 100% chance to lower the target's Special Attack by one stage.

}