package auctionplatform.worker.mockedclients;

import com.esotericsoftware.kryonet.Connection;
import auctionplatform.protocol.*;

/**
 * Client-worker, which follows the protocol to smpc-running state. Shuts down after
 * receiving {@link RequestAuctionStart}.
 */
public class ClientWorkerNoResponseAuctionStart extends AbstractTestClientWorker {
    public ClientWorkerNoResponseAuctionStart(int clientId, String serverIp, int serverPort) {
        super(clientId, serverIp, serverPort);
    }

    @Override
    public void received(Connection connection, Object object) {
        if(object instanceof RequestConnectionData){
            connection.sendTCP(new ResponseConnectionData(5000));
        }else if(object instanceof AuctionConfiguration){
            connection.sendTCP(new AuctionReady());
        }else if(object instanceof RequestAuctionStart){
            signal.countDown();
        }
    }
}
