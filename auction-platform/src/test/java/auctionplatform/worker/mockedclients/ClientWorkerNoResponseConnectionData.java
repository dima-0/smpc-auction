package auctionplatform.worker.mockedclients;

import com.esotericsoftware.kryonet.Connection;
import auctionplatform.protocol.RequestConnectionData;

/**
 * Client-worker, which follows the protocol to the smp-set-up state.
 * Shuts down after receiving {@link RequestConnectionData}.
 */
public class ClientWorkerNoResponseConnectionData extends AbstractTestClientWorker {

    public ClientWorkerNoResponseConnectionData(int clientId, String hostIp, int hostPort) {
        super(clientId, hostIp, hostPort);
    }

    @Override
    public void received(Connection connection, Object object) {
        if(object instanceof RequestConnectionData){
            signal.countDown();
        }
    }
}
