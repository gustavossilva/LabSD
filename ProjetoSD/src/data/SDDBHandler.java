package data;

import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;
import io.atomix.copycat.client.CopycatClient;
import models.Aresta;
import models.Operations;
import models.Vertice;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import java.io.Closeable;
import java.security.MessageDigest;
import java.util.*;

import static java.lang.Math.abs;

/**
 * Created by gustavovm on 5/21/17.
 */
public class SDDBHandler implements Operations.Iface, Closeable {
//    private final RWSyncCollection<Aresta> setE = new RWSyncCollection<>();
//    private final RWSyncCollection<Vertice> setV = new RWSyncCollection<>();
    private Operations.Client[] clients;
    private CopycatClient dataClient;
    private TTransport[] transports;
    private int id;

    public SDDBHandler(int id, int total) {
        try {
            final Transport transport = NettyTransport.builder().withThreads(1).build();
            final CopycatClient.Builder dataBuilder = CopycatClient.builder().withTransport(transport);
            final List<Address> cluster = Arrays.asList(new Address("localhost", SDDBServer.BASE_DATA_PORT + id));

            this.id = id;
            this.transports = new TTransport[total];
            this.clients = new Operations.Client[total];
            this.dataClient = dataBuilder.build().connect(cluster).join();

            for (int i = 0; i < this.clients.length; i++) {
                if (i != this.id) {
                    this.transports[i] = new TSocket("localhost", SDDBServer.BASE_PORT + i);
                    final TProtocol protocol = new TBinaryProtocol(this.transports[i]);
                    this.clients[i] = new Operations.Client(protocol);
                } else {
                    this.transports[i] = null;
                    this.clients[i] = null;
                }
            }
        }

        catch (Throwable t) { t.printStackTrace(); }
    }

    private int findResponsible(int i) {
        byte[] theDigest = null;

        try{
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            theDigest = md.digest( Integer.toString(i).getBytes("UTF-8") );
        }

        catch(Exception e){
            e.printStackTrace();
        }

        return abs(theDigest[theDigest.length-1] % this.clients.length);
    }

    private boolean startTransport(int i) {
        if (this.transports[i] == null)
            return false;

        if ( this.transports[i].isOpen() )
            return true;

        try {
            this.transports[i].open();
            return true;
        }

        catch (TTransportException e) {
            return false;
        }
    }

    //Parte nova

    @Override
    public void carregaGrafo(String caminho){
        /*Object aux = null;
        Object aux2 = null;
        try{
            FileInputStream restFile = new FileInputStream(caminho+"A.txt");
            ObjectInputStream stream = new ObjectInputStream(restFile);
            FileInputStream restFile2 = new FileInputStream(caminho+"V.txt");
            ObjectInputStream stream2 = new ObjectInputStream(restFile2);
            aux = stream.readObject();
            aux2 = stream2.readObject();
            if(aux != null || aux2 != null){
                setE = (RWSyncCollection<Aresta>) aux;
                setV = (RWSyncCollection<Vertice>) aux2;
            }
            stream.close();
            stream2.close();
        }catch (Exception e){
            e.printStackTrace();
        }*/
    }
    //Cria um novo grafo caso nenhum tenha sido salvo (ou inicializado ainda)

    //Neste caso a função é usada de inicio (tanto parar inicializar, quanto para salvar)
    @Override
    public synchronized void salvaGrafo(String caminho){
        /*try{
            FileOutputStream saveFile = new FileOutputStream(caminho+"A.txt");
            ObjectOutputStream stream = new ObjectOutputStream(saveFile);
            FileOutputStream saveFile2 = new FileOutputStream(caminho+"V.txt");
            ObjectOutputStream stream2 = new ObjectOutputStream(saveFile2);
            stream.writeObject(setE);
            stream2.writeObject(setV);
            stream2.close();
            stream.close();
        } catch (IOException exc){
            exc.printStackTrace();
        }*/
    }
    //Fim parte nova

    @Override
    public boolean criarVertice(int nome, int cor, String descricao, double peso){
        int responsible = findResponsible(nome);

        System.out.println("[SERVER-" + this.id + "] responsible = " + responsible);

        if (responsible == this.id) {
            return this.dataClient.submit(new CriarVertice(nome, cor, descricao, peso, "")).join();
        }

        else if ( startTransport(responsible) ) {
            try {
                return this.clients[responsible].criarVertice(nome, cor, descricao, peso);
            }

            catch (TException e) {
                return false;
            }
        }

        return true;
    }
    @Override
    public boolean criarAresta(int v1, int v2, double peso, boolean flag, String descricao){
        int responsible1 = findResponsible(v1); //pego onde v1 está

        System.out.println("[ARESTA!SERVER-" + this.id + "] responsible = " + responsible1);

        if (responsible1 == this.id) { //checa se o vertice fonte está nesse servidor, caso o contrário passa para outro
            return this.dataClient.submit(new CriarAresta(v1, v2, peso, flag, descricao)).join();
        }

        else if (startTransport(responsible1)){
            try {
                return this.clients[responsible1].criarAresta(v1, v2, peso, flag, descricao);
            } catch (TException e) {
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean delVertice(int nome){
        int responsible = findResponsible(nome);

        System.out.println("[SERVER-" + this.id + "] responsible = " + responsible);

        if (responsible == this.id) {
            return this.dataClient.submit(new DeletarVertice(nome)).join();
        }

        else if ( startTransport(responsible) ) {
            try {
                return this.clients[responsible].delVertice(nome);
            }

            catch (TException e) {}
        }
        return false;
    }

    @Override
    public boolean delAresta(int v1, int v2){
        int responsible1 = findResponsible(v1);

        if (responsible1 == this.id) {
            Aresta a = this.dataClient.submit(new DeletarAresta(v1, v2)).join();

            if(a.isFlag()){
                int responsible2 = findResponsible(v2);

                if(responsible2 == this.id)
                    this.dataClient.submit(new DeletarAresta(v2, v1)).join();

                else if (startTransport(responsible2)){
                    try{
                        return this.clients[responsible2].delAresta(a.v2,a.v1);
                    }catch(TException e){
                        System.out.println("Erro na comunicação com o servidor" + responsible2);
                    }
                }
            }
        }

        else if(startTransport(responsible1)){
            try{
                return this.clients[responsible1].delAresta(v1,v2);
            }catch(TException e){
                System.out.println("Erro na comunicação com o servidor" + responsible1);
            }
        }
        return false;
    }

    @Override
    public boolean updateVertice(int nomeUp, Vertice V){
        if ((V == null) || (nomeUp != V.nome))
            return false;

        int responsible = findResponsible(nomeUp);

        System.out.println("[SERVER-" + this.id + "] responsible = " + responsible);

        if (responsible == this.id) {
            return this.dataClient.submit(new AtualizarVertice(nomeUp, V)).join();
        }

        else if ( startTransport(responsible) ) {
            try {
                return this.clients[responsible].updateVertice(nomeUp, V);
            }

            catch (TException e) {}
        }

        return false; //Não encontrado
    }

    @Override
    public boolean updateAresta(int nomeV1, int nomeV2, Aresta A) throws TException {
        if(A == null){
            return false;
        }
        if(nomeV1 != A.v1 || nomeV2 != A.v2){
            return false;
        }

        int responsible = findResponsible(nomeV1);

        if(responsible == this.id) {
            return this.dataClient.submit(new AtualizarAresta(nomeV1, nomeV2, A)).join();
        }

        else if(startTransport(responsible)){
            try{
                if(clients[responsible].updateAresta(nomeV1,nomeV2,A)){
                    return true;
                }
            }catch (TException t){
                t.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public Vertice getVertice(int nome){
        int responsible = findResponsible(nome);

        System.out.println("[SERVER-" + this.id + "] responsible = " + responsible);

        if (responsible == this.id) {
            return this.dataClient.submit(new BuscarVertice(nome)).join();
        }

        else if ( startTransport(responsible) ) {
            try {
                return this.clients[responsible].getVertice(nome);
            }

            catch (TException e) {}
        }

        return null;
    }

    @Override
    public Aresta getAresta(int v1, int v2,boolean first){
        Aresta as = this.dataClient.submit(new BuscarAresta(v1, v2)).join();

        if(first) {
            for (Operations.Client client : this.clients) {
                if (client != null) {
                    try {
                        as = client.getAresta(v1, v2, false);
                    } catch (TException e) {}

                }
                if(as != null){
                    return as;
                }
            }
        }
        return null;
    }

    @Override
    public String exibirGrafo(){
        /*String exibir = "Vértices: ";
        for(Vertice v:setV){
            exibir = exibir+v.nome+" ,";
        }
        exibir = exibir + "\n";
        exibir = exibir+"Arestas: ";
        for(Aresta a:setE){
            exibir = exibir+"("+a.v1+", "+a.v2+")";
        }
        return exibir;*/
        return "";
    }

    @Override
    public String exibirVertice(boolean first){
        String exibir = this.dataClient.submit(new ExibirVertice()).join();

        if(first) {
            for (Operations.Client client : this.clients)
                if (client != null) {
                    try {
                        exibir += client.exibirVertice(false);
                    } catch (TException e) {
                    }
                }
        }
        return exibir;
    }

    @Override
    public String exibirAresta(boolean first){
        String exibir = this.dataClient.submit(new ExibirAresta()).join();

        if(first) {
            for (Operations.Client client : this.clients)
                if (client != null) {
                    try {
                        exibir += client.exibirAresta(false);
                    } catch (TException e) {
                    }
                }
        }
        return exibir;
    }

    @Override//Corrigir
    public List<Vertice> listarVerticesArestas(int v1, int v2) {
        ArrayList<Vertice> vertices = new ArrayList<>();
        if(getAresta(v1,v2,true) !=null){
            vertices.add(getVertice(v1));
            vertices.add(getVertice(v2));
        }
        return vertices;
    }

    @Override
    public List<Aresta> listarArestasVertice(int nomeV, boolean first) {
        List<Aresta> arestas = this.dataClient.submit(new ListarArestasVertice(nomeV)).join();

        if(first){
            for(Operations.Client client : this.clients){
                if(client !=null){
                    try{
                        arestas.addAll(client.listarArestasVertice(nomeV,false));
                    }catch(Exception t){
                        t.printStackTrace();
                    }
                }
            }
        }
        return arestas;
    }

    @Override
    public List<Vertice> listarVizinhosVertice(int nomeV) {
        ArrayList<Vertice> vizinhos = new ArrayList<>();
        Vertice aux;
        for (Aresta a : listarArestasVertice(nomeV,true)) {
            if (a.v1 == nomeV) {
                aux = getVertice(a.v2);
                if (!vizinhos.contains(aux)) {
                    vizinhos.add(aux);
                }
            } else if (a.v2 == nomeV) {
                aux = getVertice(a.v1);
                if (!vizinhos.contains(aux)) {
                    vizinhos.add(aux);
                }
            }
        }
        return vizinhos;
    }

    @Override
    public String menorCaminho(int nomeV1, int nomeV2){
        if (getVertice(nomeV1) == null)
            return String.format("Vertice %d não existe!", nomeV1);

        if (getVertice(nomeV2) == null)
            return String.format("Vertice %d não existe!", nomeV2);

        final HashMap<Integer, Double> distances = new HashMap<>();
        final HashMap<Integer, Integer> parents = new HashMap<>();
        final PriorityQueue<Integer> next = new PriorityQueue<>();
        double distance;
        int current;
        Aresta aux;

        next.add(nomeV1);
        distances.put(nomeV1, 0.0);

        while ( !next.isEmpty() ) {
            current = next.poll();

            for (Vertice neighbor : listarVizinhosVertice(current)) {
                aux = getAresta(current, neighbor.getNome(),true);

                if (aux != null) {
                    distance = distances.getOrDefault(current, Double.POSITIVE_INFINITY);
                    distance += aux.getPeso();

                    if (distance < distances.getOrDefault(neighbor.getNome(), Double.POSITIVE_INFINITY)) {
                        next.add( neighbor.getNome() );
                        distances.put(neighbor.getNome(), distance);
                        parents.put(neighbor.getNome(), current);
                    }
                }
            }
        }

        if ( !parents.containsKey(nomeV2) )
            return String.format("Não existe caminho entre %d e %d", nomeV1, nomeV2);

        LinkedList<Integer> caminho = new LinkedList<>();

        for (current = nomeV2; current != nomeV1; current = parents.get(current))
            caminho.addFirst(current);
        caminho.addFirst(nomeV1);

        return caminho.toString();
    }

    @Override
    public List<String> consultaCidade(String cidade) {
        return this.dataClient.submit(new ConsultarCidade(cidade)).join();
    }

    @Override
    public List<String> conhecidosPessoas(List<String> nome, int afinidade) {
        return this.dataClient.submit(new ConsultarConhecidosPessoas(nome, afinidade)).join();
    }

    @Override
    public void close() {
        for (TTransport transport : this.transports) {
            try {
                transport.close();
            }

            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //TODO tratar alguns minor bugs
    //TODO Mudar a flag do direcionamento dependendo do update, ou da criação de uma nova aresta que cria um bi-direcionamento
}
