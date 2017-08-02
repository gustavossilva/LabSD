package data;

import io.atomix.copycat.Command;
import io.atomix.copycat.Query;
import io.atomix.copycat.server.Commit;
import io.atomix.copycat.server.StateMachine;
import models.Aresta;
import models.Vertice;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
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
            Vertice v = new Vertice(cv.nome, cv.cor, cv.descricao, cv.peso, cv.pessoa);

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

    public boolean deletarVertice(Commit<DeletarVertice> commit) {
        try {
            DeletarVertice dv = commit.operation();

            //for(Aresta a:G.A) {
            for(Aresta a:setE){
                if(a.v1 == dv.nome || a.v2 == dv.nome){
                    this.deletarAresta(a.v1,a.v2);
                    if(setE.isEmpty()){
                        break;
                    }
                }
            }
            for(Vertice v:setV){
                if (v.nome == dv.nome){
                    setV.remove(v);
                    return true;
                }
            }

            return false;
        }

        catch (Throwable t) { return false; }
        finally { commit.release(); }
    }

    public Aresta deletarAresta(int v1, int v2) {
        for (Aresta a:setE) {
            if(a.v1 == v1 && a.v2 == v2){
                setE.remove(a);
                return a;
            }
        }

        return null;
    }

    public Aresta deletarAresta(Commit<DeletarAresta> commit) {
        try {
            DeletarAresta da = commit.operation();
            return this.deletarAresta(da.v1, da.v2);
        }

        catch (Throwable t) { return null; }
        finally { commit.release(); }
    }

    public boolean atualizarVertice(Commit<AtualizarVertice> commit) {
        try {
            AtualizarVertice av = commit.operation();

            for(Vertice v:setV){
                if(v.nome == av.nome){
                    v.cor = av.vertice.cor;
                    v.descricao = av.vertice.descricao;
                    v.peso = av.vertice.peso;
                    return true;
                }
            }

            return false;
        }

        catch (Throwable t) { return false; }
        finally { commit.release(); }
    }

    public boolean atualizarAresta(Commit<AtualizarAresta> commit) {
        try {
            AtualizarAresta aa = commit.operation();

            for (Aresta a : setE) {
                if (a.v1 == aa.nomeV1 && a.v2 == aa.nomeV2) {
                    if(!aa.aresta.flag && a.flag){
                        Aresta aux = new Aresta(a.v2, a.v1, a.peso, a.flag, a.descricao);
                        setE.remove(aux);
                        a.flag = false;
                    }
                    a.peso = aa.aresta.peso;
                    a.descricao = aa.aresta.descricao;
                    if(aa.aresta.flag && a.flag){
//                        this.getAresta(aa.aresta.v2, aa.aresta.v1,true).peso = aa.aresta.peso;
//                        this.getAresta(aa.aresta.v2, aa.aresta.v1,true).descricao = aa.aresta.descricao;
                    }
                    if (aa.aresta.flag && !a.flag) {
                        a.flag = true;
                        Aresta aux = new Aresta(aa.aresta.v2, aa.aresta.v1, aa.aresta.peso, aa.aresta.flag, aa.aresta.descricao);
                        /*if(this.getAresta(A.v2,A.v1,true) == null){
                            setE.add(aux);
                        }*/

                    }

                    return true;
                }
            }

            return false;
        }

        catch (Throwable t) { return false; }
        finally { commit.release(); }
    }

    public Vertice buscarVertice(int nome) {
        if (!setV.isEmpty()) {
            for (Vertice v : setV) {
                if (v.nome == nome) {
                    return v;
                }
            }
        }

        return null;
    }

    public Vertice buscarVertice(Commit<BuscarVertice> commit) {
        try {
            BuscarVertice bv = commit.operation();
            return this.buscarVertice(bv.nome);
        }

        catch (Throwable t) { return null; }
        finally { commit.release(); }
    }

    public Aresta buscarAresta(int v1, int v2) {
        if(!setE.isEmpty()){
            for (Aresta a : setE) {
                if (a.v1 == v1 && a.v2 == v2) {
                    return a;
                }
            }
        }

        return null;
    }

    public Aresta buscarAresta(Commit<BuscarAresta> commit) {
        try {
            BuscarAresta ba = commit.operation();
            return this.buscarAresta(ba.nomeV1, ba.nomeV2);
        }

        catch (Throwable t) { return null; }
        finally { commit.release(); }
    }

    public String exibirVertice(Commit<ExibirVertice> commit) {
        try {
            StringBuffer buffer = new StringBuffer();

            for (Vertice v:setV)
                buffer.append("Vertice: ").append(v.nome)
                        .append(" Peso: ").append(v.peso)
                        .append(" Cor: ").append(v.cor)
                        .append(" Descrição: ").append(v.descricao)
                        .append(" Nome: ").append(v.pessoa)
                        .append("\n");

            return buffer.toString();
        }

        catch (Throwable t) { return ""; }
        finally { commit.release(); }
    }

    public String exibirAresta(Commit<ExibirAresta> commit) {
        try {
            StringBuffer buffer = new StringBuffer();

            for (Aresta a: setE)
                buffer.append("Aresta: (").append(a.v1)
                        .append(", ").append(a.v2)
                        .append(") Peso: ").append(a.peso)
                        .append(" Flag: ").append(a.flag)
                        .append(" Descrição: ").append(a.descricao)
                        .append("\n");

            return buffer.toString();
        }

        catch (Throwable t) { return ""; }
        finally { commit.release(); }
    }

    public List<Aresta> listarArestasVertice(Commit<ListarArestasVertice> commit) {
        ArrayList<Aresta> arestas = new ArrayList<>();

        try {
            ListarArestasVertice lav = commit.operation();

            for (Aresta a : setE)
                if (a.v1 == lav.nomeV || a.v2 == lav.nomeV)
                    arestas.add(a);
        }

        finally {
            commit.release();
            return arestas;
        }
    }

    public List<String> consultarCidade(Commit<ConsultarCidade> commit) {
        ArrayList<String> pessoas = new ArrayList<>();

        try {
            ConsultarCidade cc = commit.operation();

            for (Vertice v: setV)
                if ( v.descricao.equals(cc.cidade) )
                    pessoas.add(v.pessoa);
        }

        finally {
            commit.release();
            return pessoas;
        }
    }

    public List<String> consultarConhecidosPessoas(Commit<ConsultarConhecidosPessoas> commit) {
        ArrayList<String> conhecidos = new ArrayList<>();

        try {
            ConsultarConhecidosPessoas ccp = commit.operation();

            for (Aresta a: setE)
                if (a.peso == ccp.nivel) {
                    if ( ccp.pessoas.contains(a.v1) )
                        conhecidos.add(this.buscarVertice(a.v2).pessoa);

                    else if ( ccp.pessoas.contains(a.v2) )
                        conhecidos.add(this.buscarVertice(a.v1).pessoa);
                }
        }

        finally {
            commit.release();
            return conhecidos;
        }
    }
}

class CriarVertice implements Command<Boolean> {
    final int cor;
    final int nome;
    final double peso;
    final String pessoa;
    final String descricao;

    public CriarVertice(int nome, int cor, String descricao, double peso, String pessoa) {
        this.cor = cor;
        this.nome = nome;
        this.peso = peso;
        this.pessoa = pessoa;
        this.descricao = descricao;
    }
}

class CriarAresta implements Command<Boolean> {
    final int v1;
    final int v2;
    final double peso;
    final boolean flag;
    final String descricao;

    public CriarAresta(int v1, int v2, double peso, boolean flag, String descricao) {
        this.v1 = v1;
        this.v2 = v2;
        this.peso = peso;
        this.flag = flag;
        this.descricao = descricao;
    }
}

class DeletarVertice implements Command<Boolean> {
    final int nome;

    public DeletarVertice(int nome) {
        this.nome = nome;
    }
}

class DeletarAresta implements Command<Aresta> {
    final int v1;
    final int v2;

    public DeletarAresta(int v1, int v2) {
        this.v1 = v1;
        this.v2 = v2;
    }
}

class AtualizarVertice implements Command<Boolean> {
    final int nome;
    final Vertice vertice;

    public AtualizarVertice(int nome, Vertice vertice) {
        this.nome = nome;
        this.vertice = vertice;
    }
}

class AtualizarAresta implements Command<Boolean> {
    final int nomeV1;
    final int nomeV2;
    final Aresta aresta;

    public AtualizarAresta(int nomeV1, int nomeV2, Aresta aresta) {
        this.nomeV1 = nomeV1;
        this.nomeV2 = nomeV2;
        this.aresta = aresta;
    }
}

class BuscarVertice implements Query<Vertice> {
    final int nome;

    public BuscarVertice(int nome) {
        this.nome = nome;
    }
}

class BuscarAresta implements Query<Aresta> {
    final int nomeV1;
    final int nomeV2;

    public BuscarAresta(int nomeV1, int nomeV2) {
        this.nomeV1 = nomeV1;
        this.nomeV2 = nomeV2;
    }
}

class ExibirVertice implements Query<String> {}
class ExibirAresta implements Query<String> {}

class ListarArestasVertice implements Query<List<Aresta>> {
    final int nomeV;

    public ListarArestasVertice(int nomeV) {
        this.nomeV = nomeV;
    }
}

class ConsultarCidade implements Query<List<String>> {
    final String cidade;

    public ConsultarCidade(String cidade) {
        this.cidade = cidade;
    }
}

class ConsultarConhecidosPessoas implements Query<List<String>> {
    final int nivel;
    final List<String> pessoas;

    public ConsultarConhecidosPessoas(List<String> pessoas, int nivel) {
        this.pessoas = pessoas;
        this.nivel = nivel;
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
