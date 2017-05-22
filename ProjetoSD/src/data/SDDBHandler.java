package data;

import models.*;

import org.apache.thrift.TException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Created by gustavovm on 5/21/17.
 */
public class SDDBHandler implements Operations.Iface {

    //private ArrayList<Grafo> grafos = new ArrayList<Grafo>();
    private Grafo G = new Grafo(new ArrayList<Vertice>(),new ArrayList<Aresta>());

    @Override
    public boolean criarVertice(int nome, int cor, String descricao, double peso){
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
    public boolean criarAresta(int v1, int v2, double peso, int flag, String descricao){
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
    public boolean delVertice(int nome){
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

    public boolean checaIgualdade(Aresta A){
        for(Aresta a:G.getA()){
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
