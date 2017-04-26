import java.util.Random;

public class ExercicioLab2 extends Thread {
 	synchronized static String message;
 	synchronized static Thread ts[] = new Thread[30];
	
	public void run () {
				
	}
	
	
	public static void main(String args[]) {
		Random r = new Random();
		StringBuilder sb = new StringBuilder(80);
		
		for (int i = 0; i < 80; i++) {
			if (r.nextInt(2) == 1)
				sb.append((char) ('A' + r.nextInt(26)));
				
			else
				sb.append((char) ('a' + r.nextInt(26)));
		}
		
		message = sb.toString();
	

		
		for (int i = 0; i < 30; i++){
			ts[i] = new ExercicioLab2();
			ts[i].start();
		}
		
		try {
			for (int i = 0; i < 30; i++)
				ts[i].join();
		}
		
		catch (Exception e) {}
	}
}
