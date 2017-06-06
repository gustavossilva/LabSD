import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ExercicioLab3Receiver {
	public static void main(String[] args) {
    	MulticastSocket socket = null;
    	DatagramPacket inPacket = null;
		DatagramPacket outPacket = null;
		byte[] outBuf;
    	byte[] inBuf = new byte[256];
    	final int SENDPORT = 8887;
    	Scanner leitura = new Scanner(System.in);

    	try {
			socket = new MulticastSocket(8888);
			InetAddress address = InetAddress.getByName("224.2.2.3");
      		socket.joinGroup(address);
			String sendmsg;
			String msg;
 
      		while (true) {
				//Receive
				inPacket = new DatagramPacket(inBuf, inBuf.length);
				socket.receive(inPacket);
				msg = new String(inBuf, 0, inPacket.getLength());
				System.out.println("De " + inPacket.getAddress() + " Msg : " + msg);
				//Send
			  	System.out.print("Digite sua mensagem: ");
			  	sendmsg = leitura.nextLine();
				outBuf = sendmsg.getBytes();
				outPacket = new DatagramPacket(outBuf, outBuf.length, address, SENDPORT);
				socket.send(outPacket);	

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
