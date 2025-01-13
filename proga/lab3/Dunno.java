import java.util.Objects;

public class Dunno extends Illegal{
    public Dunno(String name) {
        super(name);
    }

    public void insist(){
        System.out.println(name + " настаивал на своём.");
    }
    public void tryboots(String a, String b){
        Item shoesState = new Item(a, b);
        System.out.println(" Из его предложения, однако, ничего не вышло, так как "+ shoesState);
    }

    public void sit(Locate loc, String hat, String h){
        System.out.println(name + " остался сидеть "+loc.Type());
        if (h=="Есть")
            throw new HatHaveException("У "+this.name + " нет шляпы в этом отрывке");
        else
            System.out.print(" без шляпы и босиком");
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
        Dunno dunno = (Dunno) o;
        return (Objects.equals(this.name, dunno.name) && Objects.equals(this.state, dunno.state));
    }


    @Override
    public int hashCode() {
        return Objects.hash(name, state);
    }
}
