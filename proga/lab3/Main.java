//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        Scenario scenario = new Scenario();
        City city = new City();
        People people = new People();
        Dunno neznaika = new Dunno("Незнайка");
        Goatling goatling = new Goatling("Козлик");
        try{
            city.wakeup();
            people.going(Locate.EMBANKMENT);
            scenario.addCharacter(neznaika);
            scenario.addCharacter(goatling);
            scenario.play();
        } catch (HatHaveException e) {
            System.out.println("\u041E\u0428\u0418\u0411\u041A\u0410!!!!" +e.getMessage());
            System.out.println(" без шляпы и босиком");
        }
    }
}