package data;

import models.*;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TBinaryProtocol;

/**
 * Created by gustavovm on 5/21/17.
 */
public class SDDBClient {
    public static void main(String [] args) {
        try{
            TTransport transport = new TSocket("localhost",9080);
            transport.open();

            TProtocol protocol = new TBinaryProtocol(transport);
            //Protocolo para utilizar concorrência com apenas 1 thread
            //TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
            Operations.Client client = new Operations.Client(protocol);

            //Operações
            if(client.criarVertice(1,2,"Teste1",1)){
                System.out.println("Criado um vertice");
            }else{
                System.out.println("Erro ao criar!");
            }
            if(client.criarVertice(1,2,"Teste2",1)){
                System.out.println("Criado um vertice");
            }else{
                System.out.println("Erro ao criar!");
            }
            if(client.criarVertice(2,2,"Teste3",1)){
                System.out.println("Criado um vertice");
            }else{
                System.out.println("Erro ao criar!");
            }
            if(client.criarAresta(1,2,1,(short)1,"TesteAresta")){
                System.out.println("Criado uma Aresta");
            }else{
                System.out.println("Erro ao criar!");
            }
            if(client.criarAresta(1,3,1,(short)1,"TesteAresta2")){
                System.out.println("Criado uma Aresta");
            }else{
                System.out.println("Erro ao criar!");
            }
            try{
                Thread.sleep(10000);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            System.out.println(client.exibirGrafo());

            transport.close();
        }catch (TException x){
            x.printStackTrace();
        }
    }
}
