package auctionplatform.worker.mockedclients;

import com.esotericsoftware.kryonet.Connection;
import auctionplatform.protocol.RequestConnectionData;
import auctionplatform.protocol.ResponseConnectionData;
import auctionplatform.protocol.AuctionConfiguration;

/**
 * Client-worker, which follows the protocol to the smpc-set-up-finish state.
 * Shuts down after receiving {@link AuctionConfiguration}.
 */
public class ClientWorkerNoAuctionReady extends AbstractTestClientWorker {

    public ClientWorkerNoAuctionReady(int clientId, String hostIp, int hostPort) {
        super(clientId, hostIp, hostPort);
    }

    @Override
    public void received(Connection connection, Object object) {
        if(object instanceof RequestConnectionData){
            connection.sendTCP(new ResponseConnectionData(5000));
        }else if(object instanceof AuctionConfiguration){
            signal.countDown();
        }
    }
}
