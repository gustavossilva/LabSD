package data;

import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.netty.NettyTransport;
import io.atomix.copycat.server.CopycatServer;
import io.atomix.copycat.server.storage.Storage;
import io.atomix.copycat.server.storage.StorageLevel;

import java.io.File;
import java.util.concurrent.CompletableFuture;

/**
 * Created by gustavovm on 7/30/17.
 */
public class DataServer {
    private Address address;
    private CopycatServer.Builder builder;
    private CopycatServer server;
    private CompletableFuture<CopycatServer> future;
    //private Collection<Address> cluster;

    public DataServer(String ip, int port){
        address = new Address(ip,port);
    }

    public boolean initDServer(int ThreadNum, String fileDir){
        this.builder = CopycatServer.builder(address);
        this.builder.withStateMachine(SDDBStateMachine::new);
        this.builder.withTransport(NettyTransport.builder().withThreads(ThreadNum).build());
        this.builder.withStorage(Storage.builder().withDirectory(new File(fileDir)).withStorageLevel(StorageLevel.DISK).build());
/*
        cluster = Arrays.asList(
                new Address("localhost",this.address.port()+10),
                new Address("localhost",this.address.port()+20),
                new Address("localhost",this.address.port()+30));
*/

        //its possible to add a new cluster, just pass a new list to server.join(newClusterList).join();
        try{
            this.server = this.builder.build();
            this.future = server.bootstrap();
            future.join();
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
/*    public void killNode(){
        System.out.println("Teste");
        server.cluster().members().forEach(member ->{
            System.out.println("Cluster "+member.address().host()+", "+member.address().port());
        });
        //Member membro = server.cluster().member();
*//*        System.out.println(membro.type().toString());
        membro.remove().whenComplete((result,error)->{
            if(error == null){
                System.out.println("Cluster removido");
            }else{
                System.out.println("Erro ao remover cluster");
            }
        });
        server.cluster().onLeave(member ->{
            System.out.println(member.address()+ " left the cluster");
        });*//*
*//*        server.cluster().member(new Address("localhost",this.address.port()+10)).remove();
        server.onStateChange(state -> {
           if(state == CopycatServer.State.LEADER){
               System.out.println("Leader changed");
           }else{
               System.out.println("Something changed");
           }
        });*//*
    }*/
}