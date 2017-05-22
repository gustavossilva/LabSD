package data;

import models.*;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TBinaryProtocol;

import java.util.Scanner;

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
            int v1 = 0,v2 = 0,cor = 0,v3=0,v4=0;
            String descricao = "";
            double peso = 0;
            Scanner leitura = new Scanner(System.in);

            //Operações
            while(menu!=13) {
                //Reinicialização das variáveis para evitar erros.
                v1 = 0;v2 = 0;v3 = 0; v4 =0; cor = 0; peso = 0; descricao = "";
                System.out.println("Bem-vindo ao BD de Grafos, selecione uma função pelo número: ");
                System.out.println("|||||||||||||||||||||||||");
                System.out.println("1. Inserir Vértice");
                System.out.println("2. Inserir Aresta");
                System.out.println("3. Remover Vértice");
                System.out.println("4. Remover Arestas");
                System.out.println("5. Atualizar Vértice");
                System.out.println("6. Atualizar Arestas");
                System.out.println("7. Exibir Grafo");
                System.out.println("8. Exibir Vertices");
                System.out.println("9. Exibir Arestas");
                System.out.println("10. Exibir Vértice de uma aresta");
                System.out.println("11. Exibir Arestas de um vértice");
                System.out.println("12. Listar Vértices vizinhos de um vértice");
                System.out.println("13. Sair");
                menu = leitura.nextInt();
                switch (menu) {
                    case 1:
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
                        }else{
                            System.out.println("Erro ao criar vértice!");
                        }
                        break;
                    case 2:
                        System.out.println("Entre com o vertice 1 da aresta: ");
                        v1 = leitura.nextInt();
                        System.out.println("Entre com o vertice 2 da aresta: ");
                        v2 = leitura.nextInt();
                        System.out.println("Entre com a flag (Direcionado 1 Bi-Direcionado 2): ");
                        cor = leitura.nextInt();
                        System.out.println("Descrição do vértice: ");
                        leitura.nextLine();
                        descricao = leitura.nextLine();
                        System.out.println("Digite o peso: ");
                        peso = leitura.nextDouble();
                        if(client.criarAresta(v1,v2,peso,(short)cor,descricao)){
                            System.out.println("Aresta criada com sucesso!");
                        }else{
                            System.out.println("Erro ao criar aresta!");
                        }
                        break;
                    case 3:
                        System.out.println("Entre com o vértice que deseja remover: ");
                        v1 = leitura.nextInt();
                        if(client.delVertice(v1)){
                            System.out.println("Removido com sucesso!");
                        }else{
                            System.out.println("Erro ao remover...");
                        }
                        break;
                    case 4:
                        System.out.println("Entre com o vertice 1 da aresta que será removida: ");
                        v1 = leitura.nextInt();
                        System.out.println("Entre com o vertice 2: ");
                        v2 = leitura.nextInt();
                        if(client.delAresta(v1,v2)){
                            System.out.println("Aresta removida com sucesso!");
                        }else{
                            System.out.println("Erro ao remover aresta!");
                        }
                        break;
                    case 5:
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
                        if(client.updateVertice(v1,new Vertice(v2,cor,descricao,peso))){
                            System.out.println("Atualizado com sucesso!");
                        }else{
                            System.out.println("Erro ao atualizar!");
                        }
                        break;
                    case 6:
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
                        cor = leitura.nextInt();
                        if(client.updateAresta(v1,v2,new Aresta(v3,v4,peso,(short)cor,descricao))){
                            System.out.println("Atualizado com sucesso!");
                        }else{
                            System.out.println("Erro ao atualizar!");
                        }
                        break;
                    case 7:
                        System.out.println(client.exibirGrafo());
                        break;
                    case 8:
                        System.out.println(client.exibirVertice());
                        break;
                    case 9:
                        System.out.println(client.exibirAresta());
                        break;
                    case 10:
                        System.out.println(client.listarVerticesArestas(new Aresta()));
                        break;
                    case 11:
                        System.out.println(client.listarArestasVertice(v1));
                        break;
                    case 12:
                        System.out.println(client.listarVizinhosVertice(v1));
                        break;
                    case 13:
                        System.out.println("Saindo do sistema e desconectando do servidor...");
                        break;
                    default:
                        System.out.println("Opção inválida!");
                }
                try{
                    Thread.sleep(5000);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
                leitura.nextLine();
            }
            transport.close();
        }catch (TException x){
            x.printStackTrace();
        }
    }
}
