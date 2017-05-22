namespace java models

exception KeyNotFound
{
}

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
	4:i16 flag,
	5:string descricao
}

struct Grafo {
    1:list<Vertice> V,
    2:list<Aresta> A
}

service Operations {
    bool criarVertice(1:i32 nome,2:i32 cor,3:string descricao,4:double peso),
    bool criarAresta(1:i32 v1,2:i32 v2,3:double peso,4:i16 flag,5:string descricao),
    //Grafo criarGrafo(1:list<Vertice> V,2:list<Aresta> A),
    bool delVertice(1:i32 nome),
    bool delAresta(1:i32 v1,2:i32 v2),
    bool updateVertice(1:i32 nomeUp,2:Vertice V),
    bool updateAresta(1:i32 nomeV1, 2:i32 nomeV2, 3:Aresta A),
    bool updateGrafo(1:list<Vertice> V,2:list<Aresta> A),
    Vertice getVertice(1:i32 nome),
    Aresta getAresta(1:i32 v1,2:i32 v2),
    string exibirGrafo(),
    string exibirVertice(),
    string exibirAresta(),
    string listarVerticesArestas(1:Aresta A),
    string listarArestasVertice(1:i32 nomeV),
    string listarVizinhosVertice(1:i32 nomeV)
}
