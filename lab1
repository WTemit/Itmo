public class Lab1 {
	public static void main(String[] args) {
		long[] z = {3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20};
		double min = -6;
		double max = 15;
		float[] x = new float[18];
		
		for (int i = 0; i < 18; i++)  {
			x[i] = (float) (Math.random()*(max - min)+min);
		}
		double[][] array = new double[18][18];
		for (int i = 0; i <18*18; i++){
            int row = i / 18;
            int column = i % 18;
			if (z[row]==10) {
					array[row][column] = Math.log(Math.pow(Math.tan(Math.pow((0.25/(Math.cos(x[column])-0.5)), Math.pow(Math.E,x[column]))),2));
			}
			else if (z[row]== 3 || z[row]== 4|| z[row]== 5|| z[row]== 6|| z[row]== 7|| z[row]== 9 || z[row]== 11 || z[row]== 17|| z[row]== 18)  {
					array[row][column] = Math.log(Math.pow(2*(Math.PI/Math.pow(Math.tan(x[column]),2)),2));
			}
			else {
					array[row][column] = Math.log(Math.abs(Math.pow((1.0/3.0)/Math.pow(Math.log(Math.abs(x[column])+2),2),2)));
			}
        }
		
		for (int i = 0; i <18*18; i++){
            int row = i / 18;
            int column = i % 18;
            System.out.printf("%6.2f ", array[row][column]);
            if ((i+1) % 18 == 0) System.out.println("");
        }

	}
}
