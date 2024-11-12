package move;

import ru.ifmo.se.pokemon.*;

public class AerialAce extends PhysicalMove {
    public AerialAce(double power, double acc) {
        super(Type.FLYING, power, acc);
    }

    @Override
    protected String describe() {
        return "does Aerial Ace";
    }

// TODO: implement effect
// Inflicts regular damage.  Ignores accuracy and evasion modifiers.

}