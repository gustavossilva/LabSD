package data;

import models.*;

import org.apache.thrift.TException;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * Created by gustavovm on 5/21/17.
 */
public class SDDBHandler implements Operations.Iface {

    //private ArrayList<Grafo> grafos = new ArrayList<Grafo>();
    private Grafo G = new Grafo(new ArrayList<Vertice>(),new ArrayList<Aresta>());

    //Parte nova
    @Override
    public synchronized void carregaGrafo(String caminho){
       Object aux = null;

        try{
            FileInputStream restFile = new FileInputStream(caminho);
            ObjectInputStream stream = new ObjectInputStream(restFile);

            aux = stream.readObject();
            if(aux != null){
                G = (Grafo)aux;
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
                FileOutputStream saveFile = new FileOutputStream(caminho);
                ObjectOutputStream stream = new ObjectOutputStream(saveFile);

                stream.writeObject(G);
                stream.close();
            } catch (IOException exc){
                exc.printStackTrace();
            }
        }

    //Fim parte nova
    @Override
    public synchronized boolean criarVertice(int nome, int cor, String descricao, double peso){
        if(G.getV() != null) {
            for (Vertice v : G.V){
                if(v.nome == nome){ //Nome já existente
                    return false;
                }
            }
        }
        G.getV().add(new Vertice (nome,cor,descricao,peso));
        return true;
    }

    @Override
    public synchronized boolean criarAresta(int v1, int v2, double peso, int flag, String descricao){
        int criaControl = 0;
        for(Vertice v:G.V){ //Checagem se ambos os vértices existem
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
                        G.A.add(aux);
                    }
                }
                G.A.add(aux2);
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
    public synchronized boolean delVertice(int nome){
        //for(Aresta a:G.A) {
        for(int i=G.getA().size()-1;i>=0;i--){
            if (G.getA().get(i).v1 == nome || G.getA().get(i).v2 == nome) {
                G.A.remove(i);
                if(G.A.isEmpty()){
                    break;
                }
            }
        }
        for(Vertice v:G.V){
            if (v.nome == nome){
                G.V.remove(v);
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized boolean delAresta(int v1, int v2){
        for(Aresta a:G.A){
            if(a.v1 == v1 && a.v2 == v2){
                G.A.remove(a);
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized boolean updateVertice(int nomeUp, Vertice V){
        if(V == null){
            return false;
        }
        if(nomeUp != V.nome){ //Alteração no Nome do vertice
            return false;
        }
        for(Vertice v:G.getV()){
            if(v.nome == nomeUp){
                v.cor = V.cor;
                v.descricao = V.descricao;
                v.peso = V.peso;
                return true;
            }
        }
        return false; //Não encontrado
    }

    public boolean checaIgualdade(Aresta A){
        for(Aresta a:G.getA()){
            if(a.v1 == A.v1 && a.v2 == A.v2){
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized boolean updateAresta(int nomeV1, int nomeV2, Aresta A){
        if(A == null){
            return false;
        }
        if(nomeV1 != A.v1 || nomeV2 != A.v2){
            return false;
        }
        for(Aresta a:G.getA()){
            if(a.v1 == nomeV1 && a.v2 == nomeV2){
                a.peso = A.peso;
                a.flag = A.flag;
                a.descricao = A.descricao;
                if(A.flag == 2 && a.flag != 2){
                    Aresta aux = new Aresta(A.v2,A.v1,A.peso,A.flag,A.descricao);
                    if(!checaIgualdade(aux)){
                        G.getA().add(aux);
                    }
                }
                return true;
            }
        }
        return false; //Não encontrado
    }

    @Override
    public boolean updateGrafo(java.util.List<Vertice> V, java.util.List<Aresta> A){
        G.V = V;
        G.A = A;
        return true;
    }

    @Override
    public Vertice getVertice(int nome){
        if(!G.getV().isEmpty()) {
            for (Vertice v : G.getV()) {
                if (v.nome == nome) {
                    return v;
                }
            }
        }
        return null;
    }

    @Override
    public Aresta getAresta(int v1, int v2){
        if(!G.getA().isEmpty()) {
            for (Aresta a : G.getA()) {
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
        for(Vertice v:G.getV()){
            exibir = exibir+v.nome+" ,";
        }
        exibir = exibir + "\n";
        exibir = exibir+"Arestas: ";
        for(Aresta a:G.getA()){
            exibir = exibir+"("+a.v1+", "+a.v2+")";
        }
        return exibir;
    }

    @Override
    public String exibirVertice(){
        String exibir = "";
        for (Vertice v:G.getV()){
            exibir = exibir+"Vertice: "+v.nome+" Peso: "+v.peso+" Cor: "+v.cor+" Descrição: "+v.descricao+"\n";
        }
        return exibir;
    }

    @Override
    public String exibirAresta(){
        String exibir = "";
        for (Aresta a:G.getA()){
            exibir = exibir+"Aresta: "+"("+a.v1+", "+a.v2+") Peso: "+a.peso+" Flag: "+a.flag+" Descrição: "+a.descricao+"\n";
        }
        return exibir;
    }

    @Override
    public String listarVerticesArestas(int v1, int v2) {
        return("("+v1+", "+v2+")");
    }

    @Override
    public String listarArestasVertice(int nomeV) {
        String exibir = "Arestas do vértice "+nomeV+": ";
        for(Aresta a:G.getA()){
            if(a.v1 == nomeV || a.v2 == nomeV){
                exibir = exibir+"("+a.v1+", "+a.v2+")";
            }
        }
        return exibir;
    }

    @Override
    public String listarVizinhosVertice(int nomeV) {
        String exibir = "Vizinhos de "+nomeV+" são: \n";

        for (Aresta a : G.getA()) {
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
    //TODO Adicionar Dijkstra para menor caminho ou algum outro algorítmo
    //TODO Mudar a flag do direcionamento dependendo do update, ou da criação de uma nova aresta que cria um bi-direcionamento
    //TODO tratar alguns minor bugs

}

class RWSyncHashSet<E> implements Set<E> {
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
