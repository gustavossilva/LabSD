package data;

import models.Aresta;
import models.Operations;
import models.Vertice;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import java.util.Scanner;

//import org.apache.thrift.transport.TFramedTransport;

/**
 * Created by gustavovm on 5/21/17.
 */
public class SDDBClient {

    public static void main(String [] args) {
        try{
            TTransport transport = new TSocket("localhost",9080);
            transport.open();

            TProtocol protocol = new TBinaryProtocol(transport);
            //Protocolo para utilizar concorrência com apenas 1 thread
            //TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
            Operations.Client client = new Operations.Client(protocol);

            int menu = 0;
            int v1,v2,cor,v3,v4;
            String descricao,arquivo;
            double peso;
            boolean flag;
            Scanner leitura = new Scanner(System.in);

            //Operações
            while(menu!=16) {
                //Reinicialização das variáveis para evitar erros.
                v1 = 0;v2 = 0;v3 = 0; v4 =0; cor = 0; peso = 0; descricao = ""; arquivo = "";
                System.out.println("Bem-vindo ao BD de Grafos, selecione uma função pelo número: ");
                System.out.println("|||||||||||||||||||||||||");
                System.out.println("1. Carregar Grafo");
                System.out.println("2. Salvar Grafo");
                System.out.println("3. Inserir Vértice");
                System.out.println("4. Inserir Aresta");
                System.out.println("5. Remover Vértice");
                System.out.println("6. Remover Arestas");
                System.out.println("7. Atualizar Vértice");
                System.out.println("8. Atualizar Arestas");
                System.out.println("9. Exibir Grafo");
                System.out.println("10. Exibir Vertices");
                System.out.println("11. Exibir Arestas");
                System.out.println("12. Exibir Vértice de uma aresta");
                System.out.println("13. Exibir Arestas de um vértice");
                System.out.println("14. Listar Vértices vizinhos de um vértice");
                System.out.println("15. Menor caminho entre 2 vértices");
                System.out.println("16. Sair");
                menu = leitura.nextInt();
                switch (menu) {
                    case 1:
                        System.out.println("Digite o nome do arquivo para carregar: ");
                        leitura.nextLine();
                        arquivo = leitura.nextLine();
                        client.carregaGrafo(arquivo);
                        break;
                    case 2:
                        System.out.println("Digite o nome do arquivo para salvar: ");
                        leitura.nextLine();
                        arquivo = leitura.nextLine();
                        client.salvaGrafo(arquivo);
                        break;
                    case 3:
                        System.out.println("Nome(Número) do vértice: ");
                        v1 = leitura.nextInt();
                        System.out.println("Entre com a cor: ");
                        cor = leitura.nextInt();
                        System.out.println("Descrição do vértice: ");
                        leitura.nextLine();
                        descricao = leitura.nextLine();
                        System.out.println("Digite o peso: ");
                        peso = leitura.nextDouble();
                        if(client.criarVertice(v1,cor,descricao,peso)){
                            System.out.println("Vértice criado com sucesso!");
                            System.out.println(client.exibirVertice(true));
                        }else{
                            System.out.println("Erro ao criar vértice!");
                        }
                        break;
                    case 4:
                        System.out.println("Entre com o vertice 1 da aresta: ");
                        v1 = leitura.nextInt();
                        System.out.println("Entre com o vertice 2 da aresta: ");
                        v2 = leitura.nextInt();
                        System.out.println("Entre com a flag (Direcionado 1 Bi-Direcionado 2): ");
                        flag = leitura.nextBoolean();    // cor = leitura.nextInt();
                        System.out.println("Descrição do vértice: ");
                        leitura.nextLine();
                        descricao = leitura.nextLine();
                        System.out.println("Digite o peso: ");
                        peso = leitura.nextDouble();
                        if(client.criarAresta(v1,v2,peso,flag,descricao)){
                            System.out.println("Aresta criada com sucesso!");
                            System.out.println(client.exibirAresta(true));
                        }else{
                            System.out.println("Erro ao criar aresta!");
                        }
                        break;
                    case 5:
                        System.out.println("Entre com o vértice que deseja remover: ");
                        v1 = leitura.nextInt();
                        if(client.delVertice(v1)){
                            System.out.println("Removido com sucesso!");
                            System.out.println(client.exibirGrafo());
                        }else{
                            System.out.println("Erro ao remover...");
                        }
                        break;
                    case 6:
                        System.out.println("Entre com o vertice 1 da aresta que será removida: ");
                        v1 = leitura.nextInt();
                        System.out.println("Entre com o vertice 2: ");
                        v2 = leitura.nextInt();
                        if(client.delAresta(v1,v2)){
                            System.out.println("Aresta removida com sucesso!");
                            System.out.println(client.exibirAresta(true));
                        }else{
                            System.out.println("Erro ao remover aresta!");
                        }
                        break;
                    case 7:
                        System.out.println("Número do vértice a ser atualizado: ");
                        v1 = leitura.nextInt();
                        System.out.println("Novo número: ");
                        v2 = leitura.nextInt();
                        System.out.println("Entre com a cor: ");
                        cor = leitura.nextInt();
                        System.out.println("Descrição do vértice novo: ");
                        leitura.nextLine();
                        descricao = leitura.nextLine();
                        System.out.println("Digite o peso: ");
                        peso = leitura.nextDouble();
                        if(client.updateVertice(v1,new Vertice(v2,cor,descricao,peso,""))){
                            System.out.println("Atualizado com sucesso!");
                            System.out.println(client.exibirVertice(true));
                        }else{
                            System.out.println("Erro ao atualizar!");
                        }
                        break;
                    case 8:
                        System.out.println("Vertice 1 da aresta que será atualizada: ");
                        v1 = leitura.nextInt();
                        System.out.println("Vertice 2: ");
                        v2 = leitura.nextInt();
                        System.out.println("Novo vertice 1: ");
                        v3 = leitura.nextInt();
                        System.out.println("Novo vertice 2: ");
                        v4 = leitura.nextInt();
                        System.out.println("Atualização da descrição: ");
                        leitura.nextLine();
                        descricao = leitura.nextLine();
                        System.out.println("Digite o peso: ");
                        peso = leitura.nextDouble();
                        System.out.println("Digite o novo flag: ");
                        flag = leitura.nextBoolean();    // cor = leitura.nextInt();
                        if(client.updateAresta(v1,v2,new Aresta(v3,v4,peso,flag,descricao))){
                            System.out.println("Atualizado com sucesso!");
                            System.out.println(client.exibirAresta(true));
                        }else{
                            System.out.println("Erro ao atualizar!");
                        }
                        break;
                    case 9:
                        System.out.println(client.exibirGrafo());
                        break;
                    case 10:
                        System.out.println(client.exibirVertice(true));
                        break;
                    case 11:
                        System.out.println(client.exibirAresta(true));
                        break;
                    case 12:
                        System.out.println("Qual aresta deseja visualizar os vértices? ");
                        System.out.println("Vertíce 1: ");
                        v1 = leitura.nextInt();
                        System.out.println("Vertíce 2: ");
                        v2 = leitura.nextInt();
                        System.out.println(client.listarVerticesArestas(v1,v2));
                        break;
                    case 13:
                        System.out.println("Digite qual vértice deseja visualizar as arestas: ");
                        v1 = leitura.nextInt();
                        System.out.println(client.listarArestasVertice(v1,true));
                        break;
                    case 14:
                        System.out.println("Digite o numero do vertice que deseja verificar os vizinhos: ");
                        v1 = leitura.nextInt();
                        System.out.println(client.listarVizinhosVertice(v1));
                        break;
                    case 15:
                        System.out.println("Digite o primeiro vértice do caminho: ");
                        v1 = leitura.nextInt();
                        System.out.println("Digite o segundo vértice do caminho: ");
                        v2 = leitura.nextInt();
                        System.out.println(client.menorCaminho(v1,v2));
                        break;
                    case 16:
                        System.out.println("Saindo do sistema e desconectando do servidor...");
                        break;
                    default:
                        System.out.println("Opção inválida!");
                        break;
                }
            }
            transport.close();
        }catch (TException x){
            x.printStackTrace();
        }
    }
}
