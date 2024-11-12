package move;

import ru.ifmo.se.pokemon.*;

public class Swagger extends StatusMove {
    public Swagger(double power, double acc) {
        super(Type.NORMAL, power, acc);
    }

    @Override
    protected String describe() {
        return "does Swagger";
    }

// TODO: implement effect
// Raises the target's Attack by two stages, then confuses it.  If the target's Attack cannot be raised by two stages, the confusion is not applied.

}