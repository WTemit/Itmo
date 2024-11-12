package move;

import ru.ifmo.se.pokemon.*;

public class DragonClaw extends PhysicalMove {
    public DragonClaw(double power, double acc) {
        super(Type.DRAGON, power, acc);
    }

    @Override
    protected String describe() {
        return "does Dragon Claw";
    }

// TODO: implement effect
// Inflicts regular damage.

}