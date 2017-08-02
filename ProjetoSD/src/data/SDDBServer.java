package data;

import models.Operations;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by gustavovm on 5/21/17.
 */
public class SDDBServer {
    final static int BASE_PORT = 25000;
    final static int BASE_DATA_PORT = 37000;
    public static void main(String [] args){
        final int N_SERVERS = (args.length > 0) ? Integer.parseInt(args[0]) : 3;
        ArrayList<DataServer> servers = new ArrayList<>();
        if (args.length > 1) {
            final int ID = Integer.parseInt(args[1]);

            for(int i=1;i<4;i++){
                servers.add(new DataServer("localhost",BASE_DATA_PORT + (ID+(i*10))));
                servers.get(i-1).initDServer(1,"logs"+ID+".txt");
            }
            try (DataServer data = new DataServer("localhost",BASE_DATA_PORT + ID)) {
                data.initDServer(1, "logs"+ID+".txt");
                //System.out.println("entrou");
                data.killNode();
                servers.get(0).killNode();
                servers.get(1).killNode();
                //System.out.println("saiu");

                try (SDDBHandler handler = new SDDBHandler(ID, N_SERVERS)) {
                    System.out.println("foi");
                    Operations.Processor processor = new Operations.Processor(handler);
                    TServerTransport transport = new TServerSocket(BASE_PORT + ID);
                    new TThreadPoolServer(new TThreadPoolServer.Args(transport).processor(processor)).serve();
                }
            }

            catch (Exception e) { System.out.println(e); }
        }

        else
            for (int i = 0; i < N_SERVERS; i++) {
                try {
                    ProcessBuilder b = new ProcessBuilder("java", "-jar", "Server.jar",
                            Integer.toString(N_SERVERS), Integer.toString(i));
                    b.redirectOutput(new File( String.format("server%d.log", i + 1) )).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
    }
}
