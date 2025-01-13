abstract class Illegal implements Movable {

    protected String name;
    protected String state;

    public Illegal(String name) {
        this.name = name;
        this.state = "normal";
    }

    public void moveTo(Locate arg){
        System.out.println("Чтобы не попасть на глаза полицейским, "+ name + " прошёл вдоль " + arg.Type());
    }
    public abstract void hide(Locate location);

    public abstract String getName();
}
