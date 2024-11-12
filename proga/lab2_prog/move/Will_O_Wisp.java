package move;

import ru.ifmo.se.pokemon.*;

public class Will_O_Wisp extends StatusMove {
    public Will_O_Wisp(double power, double acc) {
        super(Type.FIRE, power, acc);
    }

    @Override
    protected String describe() {
        return "does Will-O-Wisp";
    }

// TODO: implement effect
// Burns the target.

}