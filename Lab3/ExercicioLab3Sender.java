import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ExercicioLab3Sender {
	public static void main(String[] args) {
		MulticastSocket socket = null;
		DatagramPacket outPacket = null;
		DatagramPacket inPacket = null;
		byte[] inBuf = new byte[256];
		byte[] outBuf;
		final int SENDPORT = 8888;
		Scanner leitura = new Scanner(System.in);
 
    	try {	
			socket = new MulticastSocket(8887);
			InetAddress address = InetAddress.getByName("224.2.2.3");
			socket.joinGroup(address);
			String msg;
			String receivemsg;
 
			while (true) {
				//Envio de msg
			  	System.out.print("Digite sua mensagem: ");
			  	msg = leitura.nextLine();
				outBuf = msg.getBytes();
				outPacket = new DatagramPacket(outBuf, outBuf.length, address, SENDPORT);
				socket.send(outPacket);
				//Recebimento da msg
				inPacket = new DatagramPacket(inBuf, inBuf.length);
				socket.receive(inPacket);
				receivemsg = new String(inBuf, 0, inPacket.getLength());
				System.out.println("De " + inPacket.getAddress() + " Msg : " + receivemsg);

				try {
					Thread.sleep(500);
				}catch (InterruptedException ie) {

		    	}
			}
		}catch (IOException ioe) {
			System.out.println(ioe);
    	}
  	}
}
