package move;

import ru.ifmo.se.pokemon.*;

public class RockTomb extends PhysicalMove {
    public RockTomb(double power, double acc) {
        super(Type.ROCK, power, acc);
    }

    @Override
    protected String describe() {
        return "does Rock Tomb";
    }

// TODO: implement effect
// Inflicts regular damage.  Has a 100% chance to lower the target's Speed by one stage.

}