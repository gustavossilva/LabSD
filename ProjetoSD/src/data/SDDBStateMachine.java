package data;

import io.atomix.copycat.Command;
import io.atomix.copycat.server.Commit;
import io.atomix.copycat.server.StateMachine;
import models.Aresta;
import models.Vertice;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SDDBStateMachine extends StateMachine {
    private RWSyncCollection<Aresta> setE = new RWSyncCollection<>();
    private RWSyncCollection<Vertice> setV = new RWSyncCollection<>();

    //Checa se já existe a Aresta
    public boolean checaIgualdade(Aresta A){
        for(Aresta a:setE){
            if(a.v1 == A.v1 && a.v2 == A.v2){
                return true;
            }
        }
        return false;
    }

    public boolean criarVertice(Commit<CriarVertice> commit) {
        try {
            CriarVertice cv = commit.operation();
            Vertice v = new Vertice(cv.nome, cv.cor, cv.descricao, cv.peso);

            if (setV != null) {
                for (Vertice ve : setV) {
                    if (ve.nome == cv.nome) {   //Nome já existente
                        return false;
                    }
                }
                setV.add(v);
            }

            return true;
        }

        catch (Throwable t) { return false; }
        finally { commit.release(); }
    }

    public boolean criarAresta(Commit<CriarAresta> commit) {
        try {
            CriarAresta ca = commit.operation();
            Aresta aux = new Aresta(ca.v1, ca.v2, ca.peso, ca.flag, ca.descricao);

            // o vértice fonte está nesse servidor, então insere
            if (!checaIgualdade(aux))
                setE.add(aux);
            if (ca.flag) {
                Aresta aux2 = new Aresta(ca.v2, ca.v1, ca.peso, ca.flag, ca.descricao);
                if (!checaIgualdade(aux2)) {
                    setE.add(aux2);
                    return true;
                }else{
                    setE.remove(aux);
                    return false;
                }
            }
            return true;
        }

        catch (Throwable t) { return false; }
        finally { commit.release(); }
    }
}

class CriarVertice implements Command<Void> {
    public final int cor;
    public final int nome;
    public final double peso;
    public final String descricao;

    public CriarVertice(int nome, int cor, String descricao, double peso) {
        this.cor = cor;
        this.nome = nome;
        this.peso = peso;
        this.descricao = descricao;
    }
}

class CriarAresta implements Command<Void> {
    public final int v1;
    public final int v2;
    public final double peso;
    public final boolean flag;
    public final String descricao;

    public CriarAresta(int v1, int v2, double peso, boolean flag, String descricao) {
        this.v1 = v1;
        this.v2 = v2;
        this.peso = peso;
        this.flag = flag;
        this.descricao = descricao;
    }
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
