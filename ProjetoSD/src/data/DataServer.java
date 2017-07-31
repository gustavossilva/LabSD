package data;

import io.atomix.catalyst.transport.Address;
import io.atomix.copycat.server.CopycatServer;
import io.atomix.copycat.server.StateMachine;

/**
 * Created by gustavovm on 7/30/17.
 */
public class DataServer {
    private Address address;

    public DataServer(String ip, int port){
        address = new Address(ip,port);
    }

    public boolean initDServer(){
        CopycatServer.Builder builder = CopycatServer.builder(address);
        builder.withStateMachine(sm::new);
    }

}
