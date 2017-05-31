import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 * @author lycog
 */
public class ExercicioLab3Sender {
  public static void main(String[] args) {
    DatagramSocket socket = null;
    DatagramPacket outPacket = null;
    byte[] outBuf;
    final int PORT = 8888;
    Scanner leitura = new Scanner(System.in);
 
    try {	
      socket = new DatagramSocket();
      String msg;
 
      while (true) {
      	System.out.println("Digite sua mensagem");
      	msg = leitura.nextLine();
        outBuf = msg.getBytes();
 
        //Send to multicast IP address and port
        InetAddress address = InetAddress.getByName("224.2.2.3");
        outPacket = new DatagramPacket(outBuf, outBuf.length, address, PORT);
 
        socket.send(outPacket);
 
        System.out.println("Server sends : " + msg);
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
