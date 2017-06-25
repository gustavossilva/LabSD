package data;

import models.Operations;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransport;

import java.util.ArrayList;

/**
 * Created by gustavovm on 5/21/17.
 */
public class SDDBServer {
    private static int port = 9080;

    public static void main(String [] args){
        final int N_SERVERS = (args.length > 1) ? Integer.parseInt(args[1]) : 3;
        Operations.Processor processor;
        TServerTransport transport;

        try{

            for(int i =0;i<N_SERVERS;i++){
                processor = new Operations.Processor(new SDDBHandler());
                transport = new TServerSocket(port++);
                final TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(transport).processor(processor));

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println(Thread.currentThread().getName() + ": " + server);
                        server.serve();
                    }
                }).start();
            }
        } catch (Exception x){
            x.printStackTrace();
        }
    }
}
