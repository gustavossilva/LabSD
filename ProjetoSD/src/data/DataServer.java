package data;

import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.local.LocalServerRegistry;
import io.atomix.catalyst.transport.local.LocalTransport;
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

    public DataServer(String ip, int port){
        address = new Address(ip,port);
    }

    public boolean initDServer(int ThreadNum, String fileDir){
        this.builder = CopycatServer.builder(address);
        this.builder.withStateMachine(SDDBStateMachine::new);
        this.builder.withTransport(NettyTransport.builder().withThreads(ThreadNum).build());
        this.builder.withStorage(Storage.builder().withDirectory(new File(fileDir)).withStorageLevel(StorageLevel.DISK).build());

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

}
