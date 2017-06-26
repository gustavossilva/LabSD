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

import static java.lang.Math.abs;


/**
 * Created by gustavovm on 5/21/17.
 */
public class SDDBHandler implements Operations.Iface, Closeable {
    private RWSyncCollection<Aresta> setE = new RWSyncCollection<>();
    private RWSyncCollection<Vertice> setV = new RWSyncCollection<>();
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
        byte[] theDigest = null;

        try{
            MessageDigest md = MessageDigest.getInstance("SHA-1");

            for (int i : is)
                md.update( Integer.toString(i).getBytes("UTF-8") );

            theDigest = md.digest();
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
                setE = (RWSyncCollection<Aresta>) aux;
                setV = (RWSyncCollection<Vertice>) aux2;
            }
            stream.close();
            stream2.close();
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
            stream2.writeObject(setV);
            stream2.close();
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
    public boolean criarAresta(int v1, int v2, double peso, boolean flag, String descricao){
        int responsible1 = distribute(new int[]{v1}); //pego onde v1 está
        Aresta aux = new Aresta(v1, v2, peso, flag, descricao);
        if (responsible1 == this.id) { //checa se o vertice fonte está nesse servidor, caso o contrário passa para outro
            // o vértice fonte está nesse servidor, então insere
            if (!checaIgualdade(aux)) {
                setE.add(aux);
                if (flag) {
                    int responsible2 = distribute(new int[]{v2}); //pego onde v2 está
                    if(responsible2 == this.id){
                        Aresta aux2 = new Aresta(v2, v1, peso, flag, descricao);
                        if(!checaIgualdade(aux2)){
                            setE.add(aux2);
                            return true;
                        }
                    }
                    else if(startTransport(responsible2)) {//caso de bidirecionado, crio outra aresta no server necessário.
                        try {
                            this.clients[responsible2].criarAresta(v2, v1, peso, flag, descricao);
                        } catch (TException e) {
                            System.out.println(e);
                        }
                    }
                }
                return true;
            }
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
        int responsible = distribute(new int[]{nome});
        System.out.println("[SERVER-" + this.id + "] responsible = " + responsible);
        if (responsible == this.id) {
            //for(Aresta a:G.A) {
            for(Aresta a:setE){
                if(a.v1 == nome || a.v2 == nome){
                    delAresta(a.v1,a.v2);
                    delAresta(a.v2,a.v1);
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
        int responsible1 = distribute(new int[]{v1});

        if (responsible1 == this.id) {
            if(this.setE.isEmpty())
                return false; //se está vazio retorna falso
            for(Aresta a:setE){
                if(a.v1 == v1 && a.v2 == v2){

                    setE.remove(a);
                    //se é uma resta bidimensional, remove também.
                    if(a.isFlag()){
                        int responsible2 = distribute(new int[]{v2});
                        if(responsible2 == this.id) {
                            Aresta aux = new Aresta(v2,v1,a.getPeso(),a.isFlag(),a.getDescricao());
                            setE.remove(aux);
                            return true;
                        }else if (startTransport(responsible2)){
                            try{
                                this.clients[responsible2].delAresta(a.v2,a.v1);
                            }catch(TException e){
                                System.out.println("Erro na comunicação com o servidor" + responsible2);
                            }
                        }
                    }else {
                        return true;
                    }
                }
            }

        }else if(startTransport(responsible1)){
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
                if(A.flag && !a.flag ){ //Se não era Bidirecional e agora é
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
        int responsible = distribute(new int[]{v1});
        if(responsible == this.id){
            if(!setE.isEmpty()) {
                for (Aresta a : setE) {
                    if (a.v1 == v1 && a.v2 == v2) {
                        return a;
                    }
                }
            }
                return null;
        }
        else if ( startTransport(responsible) ) {
            try {
                return this.clients[responsible].getAresta(v1,v2);
            }
            catch (TException e) {}
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
        for (Operations.Client client : this.clients)
            if(client != null){
                try{
                    exibir += client.exibirVertice();

                }
                catch (TException e) {}
            }
        return exibir;
    }

    @Override//Corrigir
    public List<Vertice> listarVerticesArestas(int v1, int v2) {
        ArrayList<Vertice> vertices = new ArrayList<>();
        vertices.add(getVertice(v1));
        vertices.add(getVertice(v2));
        return vertices;
    }

    @Override
    public List<Aresta> listarArestasVertice(int nomeV) {
        ArrayList<Aresta> arestas = new ArrayList<>();
        for(Aresta a:setE){
            if(a.v1 == nomeV || a.v2 == nomeV){
                arestas.add(a);
            }
        }
        return arestas;
    }

    @Override
    public List<Vertice> listarVizinhosVertice(int nomeV) {
        ArrayList<Vertice> vizinhos = new ArrayList<>();

        for (Aresta a : setE) {
            if(a.v1 == nomeV) {
                Vertice v = getVertice(a.v2);
                vizinhos.add(v);
            } else if(a.v2 == nomeV) {
                Vertice v = getVertice(a.v1);
                vizinhos.add(v);
            }
        }
        return vizinhos;
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


class RWSyncCollection<E> implements Collection<E>,Serializable {
    private final ArrayList<E> internal = new ArrayList<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock read = lock.readLock();
    private final Lock write = lock.writeLock();

    @Override
    public int size() {
        this.read.lock();
        try { return this.internal.size(); }
        finally { this.read.unlock(); }
    }

    @Override
    public boolean isEmpty() {
        this.read.lock();
        try { return this.internal.isEmpty(); }
        finally { this.read.unlock(); }
    }

    @Override
    public boolean contains(Object o) {
        this.read.lock();
        try { return this.internal.contains(o); }
        finally { this.read.unlock(); }
    }

    @Override
    public Iterator<E> iterator() {
        this.read.lock();
        try { return this.internal.iterator(); }
        finally { this.read.unlock(); }
    }

    @Override
    public Object[] toArray() {
        this.read.lock();
        try { return this.internal.toArray(); }
        finally { this.read.unlock(); }
    }

    @Override
    public <T> T[] toArray(T[] ts) {
        this.read.lock();
        try { return this.internal.toArray(ts); }
        finally { this.read.unlock(); }
    }

    @Override
    public boolean add(E e) {
        this.write.lock();
        try { return this.internal.add(e); }
        finally { this.write.unlock(); }
    }

    @Override
    public boolean remove(Object o) {
        this.write.lock();
        try { return this.internal.remove(o); }
        finally { this.write.unlock(); }
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        this.read.lock();
        try { return this.internal.containsAll(collection); }
        finally { this.read.unlock(); }
    }

    @Override
    public boolean addAll(Collection<? extends E> collection) {
        this.write.lock();
        try { return this.internal.addAll(collection); }
        finally { this.write.unlock(); }
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        this.write.lock();
        try { return this.internal.retainAll(collection); }
        finally { this.write.unlock(); }
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        this.write.lock();
        try { return this.internal.removeAll(collection); }
        finally { this.write.unlock(); }
    }

    @Override
    public void clear() {
        this.write.lock();
        try { this.internal.clear(); }
        finally { this.write.unlock(); }
    }
}
