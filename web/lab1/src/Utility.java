public class Utility {

    public static boolean checkArea(Double x, Double y, Double r){
        if(x >= 0 && y >= 0){
            if (x + 2*y <= r) {
                return true;
            }
        } else if (x < 0 && y >= 0) {
            if(x*x + y*y < (r/2)*(r/2)) {
                return true;
            }
        } else if (x < 0 && y < 0) {
            return false;
        } else if (x > 0 && y < 0) {
            if (x <= r && (-1)*y <= r/2) {
                return true;
            }
        }

        return false;
    }
}
