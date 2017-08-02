namespace java models

struct Vertice {
	1:i32 nome,
	2:i32 cor,
	3:string descricao,
	4:double peso
}

struct Aresta {
	1:i32 v1,
	2:i32 v2,
	3:double peso,
	4:bool flag,
	5:string descricao
}

service Operations {
    void carregaGrafo(1:string caminho),
    void salvaGrafo(1:string caminho),
    bool criarVertice(1:i32 nome,2:i32 cor,3:string descricao,4:double peso),
    bool criarAresta(1:i32 v1,2:i32 v2,3:double peso,4:bool flag,5:string descricao),
    bool delVertice(1:i32 nome),
    bool delAresta(1:i32 v1,2:i32 v2),
    bool updateVertice(1:i32 nomeUp,2:Vertice V),
    bool updateAresta(1:i32 nomeV1, 2:i32 nomeV2, 3:Aresta A),
    Vertice getVertice(1:i32 nome),
    Aresta getAresta(1:i32 v1,2:i32 v2,3:bool flag),
    string exibirGrafo(),
    string exibirVertice(1:bool flag),
    string exibirAresta(1:bool flag),
    list<Vertice> listarVerticesArestas(1:i32 v1,2:i32 v2),
    list<Aresta> listarArestasVertice(1:i32 nomeV,2:bool flag),
    list<Vertice> listarVizinhosVertice(1:i32 nomeV, bool first),
    string menorCaminho(1:i32 v1,2:i32 v2, bool first)
}
