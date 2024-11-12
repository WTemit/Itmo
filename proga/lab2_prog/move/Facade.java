package move;

import ru.ifmo.se.pokemon.*;

public class Facade extends PhysicalMove {
    public Facade(double power, double acc) {
        super(Type.NORMAL, power, acc);
    }

    @Override
    protected String describe() {
        return "does Facade";
    }

// TODO: implement effect
// Inflicts regular damage.  If the user is burned, paralyzed, or poisoned, this move has double power.

}