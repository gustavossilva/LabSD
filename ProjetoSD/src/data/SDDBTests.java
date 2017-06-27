package data;

import models.Operations;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

/**
 * Created by luiz on 26/06/17.
 */
public final class SDDBTests {
    private SDDBTests() {}

    public static void main(String[] args) {
        try (TTransport transport = new TSocket("localhost",9080)) {
            transport.open();

            Operations.Client client = new Operations.Client(new TBinaryProtocol(transport));

            try {
                assert client.criarVertice(1, 0, "A", 0);
                assert client.criarVertice(2, 0, "B", 0);
                assert client.criarVertice(3, 0, "C", 0);
                assert client.criarVertice(4, 0, "D", 0);
                assert client.criarVertice(5, 0, "E", 0);

                assert client.criarAresta(1, 2, 1, false, "A -> B");
                assert client.criarAresta(2, 3, 2, false, "B -> C");
                assert client.criarAresta(3, 4, 4, false, "C -> D");
                assert client.criarAresta(4, 5, 8, false, "D -> E");
            }

            catch (AssertionError e) {}

            System.out.println( client.exibirVertice(false) );
            System.out.println( client.exibirAresta(false) );

            System.out.println("Menor caminho entre A e B: " + client.menorCaminho(1, 2));
            System.out.println("Menor caminho entre A e C: " + client.menorCaminho(1, 3));
            System.out.println("Menor caminho entre A e D: " + client.menorCaminho(1, 4));
            System.out.println("Menor caminho entre A e E: " + client.menorCaminho(1, 5));
        }

        catch (TException e) {
            e.printStackTrace(System.err);
        }

    }
}
