package data;

import models.*;

import org.apache.thrift.server.*;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.server.TServer.Args;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by gustavovm on 5/21/17.
 */
public class SDDBServer {
    private static ArrayList<TServerTransport> serverTransports = new ArrayList<>();
    private static ArrayList<TServer> servers = new ArrayList<>();
    private static int port = 9080;
    private static int qtdServer = 3;

    public static void main(String [] args){
        try{
            SDDBHandler handler = new SDDBHandler();

            Operations.Processor processor = new Operations.Processor(handler);
            for(int i =0;i<qtdServer;i++){
                serverTransports.add(new TServerSocket(port++));
                servers.add(new TThreadPoolServer
                        (new TThreadPoolServer.Args(serverTransports.get(i)).processor(processor)));
            }
            for(int i = 0;i<qtdServer;i++) {
                final int fimi = i;
                System.out.println("Servidor Inicializado...");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        servers.get(fimi).serve();
                    }
                }).start();
            }
        } catch (Exception x){
            x.printStackTrace();
        }
    }
}
