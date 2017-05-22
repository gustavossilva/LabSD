package data;

import models.*;

import org.apache.thrift.TException;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by gustavovm on 5/21/17.
 */
public class SDDBHandler implements Operations.Iface {

    //private ArrayList<Grafo> grafos = new ArrayList<Grafo>();
    private Grafo G = new Grafo(new ArrayList<Vertice>(),new ArrayList<Aresta>());

    @Override
    public boolean criarVertice(int nome, int cor, java.lang.String descricao, double peso){
        if(G.getV() != null) {
            for (Vertice v : G.V){
                if(v.nome == nome){ //Nome já existente
                    return false;
                }
            }
        }
        System.out.println("Cheguei aqui!");
        G.getV().add(new Vertice (nome,cor,descricao,peso));
        return true;
    }
    @Override
    public boolean criarAresta(int v1, int v2, double peso, short flag, java.lang.String descricao){
        int criaControl = 0;
        for(Vertice v:G.V){ //Checagem se ambos os vértices existem
            if(v.nome == v1 || v.nome == v2){
                criaControl++;
            }
        }
        if(criaControl > 1) {
            G.A.add(new Aresta(v1, v2, peso, flag, descricao));
            return true;
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
        for(Aresta a:G.A) {
            if (a.v1 == nome || a.v2 == nome) {
                G.A.remove(a);
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
    public boolean delAresta(int v1, int v2){
        for(Aresta a:G.A){
            if(a.v1 == v1 && a.v2 == v2){
                G.A.remove(a);
                return true;
            }
        }
        return false;
    }
    @Override
    public boolean updateVertice(int nomeUp, Vertice V){
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
    @Override
    public boolean updateAresta(int nomeV1, int nomeV2, Aresta A){
        if(A == null){
            return false;
        }
        for(Aresta a:G.getA()){
            if(a.v1 == nomeV1 && a.v2 == nomeV2){
                a.v1 = A.v1;
                a.v2 = A.v2;
                a.peso = A.peso;
                a.flag = A.flag;
                a.descricao = A.descricao;
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
        for(Vertice v:G.getV()) {
            if (v.nome == nome) {
                return v;
            }
        }
        return null;
    }
    @Override
    public Aresta getAresta(int v1, int v2){
        for(Aresta a:G.getA()) {
            if (a.v1 == v1 && a.v2 == v2) {
                return a;
            }
        }
        return null;
    }

    @Override
    public String exibirGrafo(){
        String exibir;
        exibir = "Vértices: ";
        for(Vertice v:G.getV()){
            exibir = exibir+v.nome+" ,";
        }
        exibir = exibir+"Arestas: ";
        for(Aresta a:G.getA()){
            exibir = exibir+"("+a.v1+", "+a.v2+")";
        }
        return exibir;
    }

    @Override
    public void listarVerticesArestas(Aresta A) {

    }
    @Override
    public void listarArestasVertice(int nomeV) {

    }
    @Override
    public void listarVizinhosVertice(int nomeV) {

    }
}
