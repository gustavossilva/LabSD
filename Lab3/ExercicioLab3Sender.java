import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 * @author lycog
 */
public class ExercicioLab3Sender {
  public static void main(String[] args) {
    MulticastSocket socket = null;
    DatagramPacket outPacket = null;
    DatagramPacket inPacket = null;
	byte[] inBuf = new byte[256];
    byte[] outBuf;
    final int PORT = 8888;
    Scanner leitura = new Scanner(System.in);
 
    try {	
      socket = new MulticastSocket(8887);
      String msg;
	  String receivemsg;
      InetAddress addressRec = InetAddress.getByName("224.2.2.4");
	  socket.joinGroup(addressRec);
 
      while (true) {

		//Envio de msg
      	System.out.print("Digite sua mensagem: ");
      	msg = leitura.nextLine();
        outBuf = msg.getBytes();
        InetAddress address = InetAddress.getByName("224.2.2.3");
        outPacket = new DatagramPacket(outBuf, outBuf.length, address, PORT);
        socket.send(outPacket);

		//Recebimento da msg
		inPacket = new DatagramPacket(inBuf, inBuf.length);
		socket.receive(inPacket);
        receivemsg = new String(inBuf, 0, inPacket.getLength());
        System.out.println("From " + inPacket.getAddress() + " Msg : " + receivemsg);

        try {
			Thread.sleep(500);
        } catch (InterruptedException ie) {
        }
      }
    } catch (IOException ioe) {
      System.out.println(ioe);
    }
  }
}
