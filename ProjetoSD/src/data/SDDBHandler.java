package data;

import models.*;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import java.io.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Handler;

import static java.lang.Math.abs;


/**
 * Created by gustavovm on 5/21/17.
 */
public class SDDBHandler implements Operations.Iface, Closeable {
    //private ArrayList<Grafo> grafos = new ArrayList<Grafo>();
    //private Grafo G = new Grafo(new ArrayList<Vertice>(),new ArrayList<Aresta>());
    private RWSyncHashSet<Aresta> setE = new RWSyncHashSet<>();
    private RWSyncHashSet<Vertice> setV = new RWSyncHashSet<>();
    private final Operations.Client[] clients;
    private final TTransport[] transports;
    private final int id;

    public SDDBHandler(int id, int total) {
        this.clients = new Operations.Client[total];
        this.transports = new TTransport[total];
        this.id = id;

//        System.out.println(Thread.currentThread().getName() + ": handler " + id);

        for (int i = 0; i < this.clients.length; i++) {
            if (i != this.id) {
                this.transports[i] = new TSocket("localhost", SDDBServer.BASE_PORT + i);
                final TProtocol protocol = new TBinaryProtocol(this.transports[i]);
                this.clients[i] = new Operations.Client(protocol);
            }

            else {
                this.transports[i] = null;
                this.clients[i] = null;
            }
        }
    }

    private int distribute(int[] is) {
        MessageDigest md;
        byte[] bytesOfMessage = null,theDigest = null;
        try{
            md = MessageDigest.getInstance("SHA-1");

            for (int i : is) {
                bytesOfMessage = Integer.toString(i).getBytes("UTF-8");
                md.update(bytesOfMessage);
            }

            theDigest = md.digest();
        }catch(Exception e){
            e.printStackTrace();
        }
        return abs(theDigest[theDigest.length-1]%clients.length);
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
        Object aux = null;
        Object aux2 = null;
        try{
            FileInputStream restFile = new FileInputStream(caminho+"A.txt");
            ObjectInputStream stream = new ObjectInputStream(restFile);
            FileInputStream restFile2 = new FileInputStream(caminho+"V.txt");
            ObjectInputStream stream2 = new ObjectInputStream(restFile2);
            aux = stream.readObject();
            aux2 = stream2.readObject();
            if(aux != null || aux2 != null){
                setE = (RWSyncHashSet<Aresta>) aux;
                setV = (RWSyncHashSet<Vertice>) aux2;
            }
            stream.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //Cria um novo grafo caso nenhum tenha sido salvo (ou inicializado ainda)
    //Neste caso a função é usada de inicio (tanto parar inicializar, quanto para salvar)
    @Override
    public synchronized void salvaGrafo(String caminho){
        try{
            FileOutputStream saveFile = new FileOutputStream(caminho+"A.txt");
            ObjectOutputStream stream = new ObjectOutputStream(saveFile);
            FileOutputStream saveFile2 = new FileOutputStream(caminho+"V.txt");
            ObjectOutputStream stream2 = new ObjectOutputStream(saveFile2);
            stream.writeObject(setE);
            stream.writeObject(setV);
            stream.close();
        } catch (IOException exc){
            exc.printStackTrace();
        }
    }

    //Fim parte nova
    @Override
    public boolean criarVertice(int nome, int cor, String descricao, double peso){
        int responsible = distribute(new int[]{nome});
        Vertice v = new Vertice(nome,cor,descricao,peso);

        System.out.println("[SERVER-" + this.id + "] responsible = " + responsible);

        if (responsible == this.id) {
            if (setV != null) {
                for (Vertice ve : setV) {
                    if (ve.nome == nome) { //Nome já existente
                        return false;
                    }
                }
                setV.add(v);
            }
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
    public boolean criarAresta(int v1, int v2, double peso, int flag, String descricao){
        int criaControl = 0;
        for(Vertice v:setV){ //Checagem se ambos os vértices existem
            if(v.nome == v1 || v.nome == v2){
                criaControl++;
            }
        }
        if(criaControl > 1) {
            Aresta aux2 = new Aresta(v1, v2, peso, flag, descricao);
            if (!checaIgualdade(aux2)) {
                if (flag == 2) {
                    Aresta aux = new Aresta(v2, v1, peso, flag, descricao);
                    if (!checaIgualdade(aux)) {
                        setE.add(aux);
                    }
                }
                setE.add(aux2);
                return true;
            }
        }
        return false;
    }

/*    @Override
    public Grafo criarGrafo(java.util.List<Vertice> V, java.util.List<Aresta> A){
        Grafo g = new Grafo(V,A);
        return g;
    }*/

    @Override
    public boolean delVertice(int nome){
        int responsible = distribute(new int[]{nome});

        System.out.println("[SERVER-" + this.id + "] responsible = " + responsible);

        if (responsible == this.id) {
            //for(Aresta a:G.A) {
            for(Aresta a:setE){
                if(a.v1 == nome || a.v2 == nome){
                    setE.remove(a);
                    if(setE.isEmpty()){
                        break;
                    }
                }
            }
            for(Vertice v:setV){
                if (v.nome == nome){
                    setV.remove(v);
                    return true;
                }
            }
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
        for(Aresta a:setE){
            if(a.v1 == v1 && a.v2 == v2){
                setE.remove(a);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean updateVertice(int nomeUp, Vertice V){
        if ((V == null) || (nomeUp != V.nome))
            return false;

        int responsible = distribute(new int[]{nomeUp});

        System.out.println("[SERVER-" + this.id + "] responsible = " + responsible);

        if (responsible == this.id) {
            for(Vertice v:setV){
                if(v.nome == nomeUp){
                    v.cor = V.cor;
                    v.descricao = V.descricao;
                    v.peso = V.peso;
                    return true;
                }
            }
        }

        else if ( startTransport(responsible) ) {
            try {
                return this.clients[responsible].updateVertice(nomeUp, V);
            }

            catch (TException e) {}
        }

        return false; //Não encontrado
    }
    //Checa se já existe a Aresta
    public boolean checaIgualdade(Aresta A){
        for(Aresta a:setE){
            if(a.v1 == A.v1 && a.v2 == A.v2){
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean updateAresta(int nomeV1, int nomeV2, Aresta A){
        if(A == null){
            return false;
        }
        if(nomeV1 != A.v1 || nomeV2 != A.v2){
            return false;
        }
        for(Aresta a:setE){
            if(a.v1 == nomeV1 && a.v2 == nomeV2){
                a.peso = A.peso;
                a.flag = A.flag;
                a.descricao = A.descricao;
                if(A.flag == 2 && a.flag != 2){ //Se não era Bidirecional e agora é
                    Aresta aux = new Aresta(A.v2,A.v1,A.peso,A.flag,A.descricao);
                    if(!checaIgualdade(aux)){
                        setE.add(aux);
                    }
                }
                return true;
            }
        }
        return false; //Não encontrado
    }

    //Remover do thrift
    @Override
    public boolean updateGrafo(java.util.List<Vertice> V, java.util.List<Aresta> A){
        return true;
    }

    @Override
    public Vertice getVertice(int nome){
        int responsible = distribute(new int[]{nome});

        System.out.println("[SERVER-" + this.id + "] responsible = " + responsible);

        if (responsible == this.id) {
            if (!setV.isEmpty()) {
                for (Vertice v : setV) {
                    if (v.nome == nome) {
                        return v;
                    }
                }
            }
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
    public Aresta getAresta(int v1, int v2){
        if(!setE.isEmpty()) {
            for (Aresta a : setE) {
                if (a.v1 == v1 && a.v2 == v2) {
                    return a;
                }
            }
        }
        return null;
    }

    @Override
    public String exibirGrafo(){
        String exibir = "Vértices: ";
        for(Vertice v:setV){
            exibir = exibir+v.nome+" ,";
        }
        exibir = exibir + "\n";
        exibir = exibir+"Arestas: ";
        for(Aresta a:setE){
            exibir = exibir+"("+a.v1+", "+a.v2+")";
        }
        return exibir;
    }

    @Override
    public String exibirVertice(){
        String exibir = "";
        for (Vertice v:setV){
            exibir = exibir+"Vertice: "+v.nome+" Peso: "+v.peso+" Cor: "+v.cor+" Descrição: "+v.descricao+"\n";
        }

        for (Operations.Client client : this.clients)
            if (client != null) {
                try {
                    exibir += client.exibirVertice();
                }

                catch (TException e) {}
            }

        return exibir;
    }

    @Override
    public String exibirAresta(){
        String exibir = "";
        for (Aresta a:setE){
            exibir = exibir+"Aresta: "+"("+a.v1+", "+a.v2+") Peso: "+a.peso+" Flag: "+a.flag+" Descrição: "+a.descricao+"\n";
        }
        return exibir;
    }

    @Override//Corrigir
    public String listarVerticesArestas(int v1, int v2) {
        return("("+v1+", "+v2+")");
    }

    @Override
    public String listarArestasVertice(int nomeV) {
        String exibir = "Arestas do vértice "+nomeV+": ";
        for(Aresta a:setE){
            if(a.v1 == nomeV || a.v2 == nomeV){
                exibir = exibir+"("+a.v1+", "+a.v2+")";
            }
        }
        return exibir;
    }

    @Override
    public String listarVizinhosVertice(int nomeV) {
        String exibir = "Vizinhos de "+nomeV+" são: \n";

        for (Aresta a : setE) {
            if(a.v1 == nomeV) {
                Vertice v = getVertice(a.v2);
                exibir = exibir+ "Vértice: "+ v.nome +" "+
                        "Cor: "+v.cor +" "+
                        "Peso: "+ v.peso + " "+
                        "Descrição: "+ v.descricao+ "\n";
            } else if(a.v2 == nomeV) {
                Vertice v = getVertice(a.v1);
                exibir = exibir+ "Vértice: "+ v.nome +" "+
                        "Cor: "+v.cor +" "+
                        "Peso: "+ v.peso + " "+
                        "Descrição: "+ v.descricao+ "\n";
            }
        }
        return exibir;
    }

    @Override
    public String menorCaminho(int nomeV1, int nomeV2){
        return "oi";
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
    //TODO Adicionar Dijkstra para menor caminho ou algum outro algorítmo
    //TODO Mudar a flag do direcionamento dependendo do update, ou da criação de uma nova aresta que cria um bi-direcionamento
    //TODO tratar alguns minor bugs

}

 class  RWSyncHashSet<E> implements Set<E>,Serializable {
    private final HashSet<E> s = new HashSet<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock read = lock.readLock();
    private final Lock write = lock.writeLock();

    @Override
    public int size() {
        read.lock();
        try { return s.size(); }
        finally { read.unlock(); }
    }

    @Override
    public boolean isEmpty() {
        read.lock();
        try { return s.isEmpty(); }
        finally { read.unlock(); }
    }

    @Override
    public boolean contains(Object o) {
        read.lock();
        try { return s.contains(o); }
        finally { read.unlock(); }
    }

    @Override
    public Iterator<E> iterator() {
        read.lock();
        try { return s.iterator(); }
        finally { read.unlock(); }
    }

    @Override
    public Object[] toArray() {
        read.lock();
        try { return s.toArray(); }
        finally { read.unlock(); }
    }

    @Override
    public <T> T[] toArray(T[] ts) {
        read.lock();
        try { return s.toArray(ts); }
        finally { read.unlock(); }
    }

    @Override
    public boolean add(E e) {
        write.lock();
        try { return s.add(e); }
        finally { write.unlock(); }
    }

    @Override
    public boolean remove(Object o) {
        write.lock();
        try { return s.remove(o); }
        finally { write.unlock(); }
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        read.lock();
        try { return s.containsAll(collection); }
        finally { read.unlock(); }
    }

    @Override
    public boolean addAll(Collection<? extends E> collection) {
        write.lock();
        try { return s.addAll(collection); }
        finally { write.unlock(); }
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        write.lock();
        try { return s.retainAll(collection); }
        finally { write.unlock(); }
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        write.lock();
        try { return s.removeAll(collection); }
        finally { write.unlock(); }
    }

    @Override
    public void clear() {
        write.lock();
        try { s.clear(); }
        finally { write.unlock(); }
    }
}
