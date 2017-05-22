package data;

import models.*;

import org.apache.thrift.server.*;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.server.TServer.Args;

import java.util.HashMap;

/**
 * Created by gustavovm on 5/21/17.
 */
public class SDDBServer {
    public static void main(String [] args){
        try{
            SDDBHandler handler = new SDDBHandler();
            Operations.Processor processor = new Operations.Processor(handler);
            TServerTransport serverTransport = new TServerSocket(9080);
            TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));
            //Criar concorrencia com apenas um thread
            //TNonblockingServerSocket serverTransport = new TNonblockingServerSocket(9080);
            //THsHaServer.Args argus = new THsHaServer.Args(serverTransport);
            //argus.processor(processor);
            //argus.transportFactory(new TFramedTransport.Factory());
            //TServer server = new THsHaServer(argus);
            System.out.println("Servidor Inicializado...");
            server.serve();
        } catch (Exception x){
            x.printStackTrace();
        }
    }
}
