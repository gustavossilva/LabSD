package data;

import models.Operations;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import java.util.ArrayList;

/**
 * Created by gustavovm on 5/21/17.
 */
public class SDDBServer {
    final static int BASE_PORT = 9080;

    public static void main(String [] args){
        final int N_SERVERS = (args.length > 1) ? Integer.parseInt(args[1]) : 3;

        for(int i =0;i<N_SERVERS;i++){
            final int id = i;

            new Thread(new Runnable() {
                @Override
                public void run() {
//                    System.out.println(Thread.currentThread().getName() + ": server " + id);

                    try {
                        Operations.Processor processor = new Operations.Processor(new SDDBHandler(id, N_SERVERS));
                        TServerTransport transport = new TServerSocket(BASE_PORT + id);
                        new TThreadPoolServer(new TThreadPoolServer.Args(transport).processor(processor)).serve();
                    }

                    catch (TTransportException e) {
                        e.printStackTrace();
                    }

                }
            }).start();
        }
    }
}
