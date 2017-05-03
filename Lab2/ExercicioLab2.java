import java.util.Random;

public class ExercicioLab2 extends Thread {
 	 static String message;
 	 static Thread ts[] = new Thread[30];
	
	public synchronized void run () {
	    char teste;
	    if(encontraMinuscula(message) != -1){
	        System.out.println(encontraMinuscula(message));
	        teste = Character.toUpperCase(message.charAt(encontraMinuscula(message)));
	        System.out.println(teste);
	         message = replaceCharAt(message,encontraMinuscula(message),teste);
	         System.out.println(message);
	    }
	}
	
	public static String replaceCharAt(String s, int pos, char c){
        return s.substring(0, pos) + c + s.substring(pos + 1);
    }

	
	public int encontraMinuscula(String str) {        
        for(int i=0; i<str.length(); i++) {
            if(Character.isLowerCase(str.charAt(i))) {
                return i;
            }
        }
        return -1;
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
		System.out.println("Menssagem Inicial: "+message);
		
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
