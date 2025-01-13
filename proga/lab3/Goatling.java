import java.util.Objects;

public class Goatling extends Illegal{
    public Goatling(String name) {
        super(name);
    }

    public void move() {
        System.out.println("На добычу пришлось все же отправиться "+ name);
    }

    public void say(){
        System.out.println(name + " ответил, что ему не трудно");
    }
    @Override
    public void hide(Locate location){
        System.out.println(name + " спрятался " + location.Type());
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return this.getName();
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Goatling goatling = (Goatling) o;
        return (Objects.equals(this.name, goatling.name) && Objects.equals(this.state, goatling.state));
    }


    @Override
    public int hashCode() {
        return Objects.hash(name, state);
    }
}
