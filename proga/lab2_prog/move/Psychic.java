package move;

import ru.ifmo.se.pokemon.*;

public class Psychic extends SpecialMove {
    public Psychic(double power, double acc) {
        super(Type.PSYCHIC, power, acc);
    }

    @Override
    protected String describe() {
        return "does Psychic";
    }

// TODO: implement effect
// Inflicts regular damage.  Has a 10% chance to lower the target's Special Defense by one stage.

}