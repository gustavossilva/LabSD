package data;

import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.local.LocalServerRegistry;
import io.atomix.catalyst.transport.local.LocalTransport;
import io.atomix.catalyst.transport.netty.NettyTransport;
import io.atomix.copycat.server.CopycatServer;
import io.atomix.copycat.server.storage.Storage;
import io.atomix.copycat.server.storage.StorageLevel;

import java.io.File;

/**
 * Created by gustavovm on 7/30/17.
 */
public class DataServer {
    private Address address;

    public DataServer(String ip, int port){
        address = new Address(ip,port);
    }

    public boolean initDServer(int ThreadNum, String fileDir){
        CopycatServer.Builder builder = CopycatServer.builder(address);
        builder.withStateMachine(SDDBStateMachine::new);
        builder.withTransport(NettyTransport.builder().withThreads(ThreadNum).build());
        builder.withStorage(Storage.builder().withDirectory(new File(fileDir)).withStorageLevel(StorageLevel.DISK).build());

        try{
            CopycatServer server = builder.build();
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

}
