package move;

import ru.ifmo.se.pokemon.*;

public class IcyWind extends SpecialMove {
    public IcyWind(double power, double acc) {
        super(Type.ICE, power, acc);
    }

    @Override
    protected String describe() {
        return "does Icy Wind";
    }

// TODO: implement effect
// Inflicts regular damage.  Has a 100% chance to lower the target's Speed by one stage.

}