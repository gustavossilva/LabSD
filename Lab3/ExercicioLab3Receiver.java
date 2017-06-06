import java.io.*;
import java.net.*;

import java.util.Scanner;
/**
 *
 * @author lycog
 */
public class ExercicioLab3Receiver {
  public static void main(String[] args) {
    MulticastSocket socket = null;
    DatagramPacket inPacket = null;
	DatagramPacket outPacket = null;
	byte[] outBuf;
    byte[] inBuf = new byte[256];
    final int PORT = 8887;
    Scanner leitura = new Scanner(System.in);
    try {
	  String sendmsg;
      //Prepare to join multicast group
      socket = new MulticastSocket(8888);
      InetAddress address = InetAddress.getByName("224.2.2.3");
      socket.joinGroup(address);
 
      while (true) {
		    inPacket = new DatagramPacket(inBuf, inBuf.length);
		    socket.receive(inPacket);
		    String msg = new String(inBuf, 0, inPacket.getLength());
		    System.out.println("From " + inPacket.getAddress() + " Msg : " + msg);
			InetAddress addressSend = InetAddress.getByName("224.2.2.4");
		  	System.out.print("Digite sua mensagem: ");
		  	sendmsg = leitura.nextLine();
		    outBuf = sendmsg.getBytes();
		    outPacket = new DatagramPacket(outBuf, outBuf.length, addressSend, PORT);
		    socket.send(outPacket);	

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
