package auctionplatform.worker.mockedclients;

import com.esotericsoftware.kryonet.Connection;
import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkConfigurationImpl;
import auctionplatform.protocol.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Client-worker, which follows the protocol to the phase, where smpc should be started. After receiving {@link RequestAuctionStart},
 * the client sends a {@link ResponseAuctionStart}, but does not start smpc and shuts down instead.
 * This should lead to an abortion of the execution of the auction evaluation on the server side.
 */
public class ClientWorkerEvaluationAbortion extends AbstractTestClientWorker {
    private AuctionConfiguration configuration;
    private NetworkConfiguration networkConfiguration;

    public ClientWorkerEvaluationAbortion(int clientId, String serverIp, int serverPort) {
        super(clientId, serverIp, serverPort);
    }

    @Override
    public void received(Connection connection, Object object) {
        if(object instanceof RequestConnectionData){
            connection.sendTCP(new ResponseConnectionData(5000));
        }else if(object instanceof AuctionConfiguration){
            AuctionConfiguration config = (AuctionConfiguration) object;
            setUpParties(config);
            connection.sendTCP(new AuctionReady());
        }else if(object instanceof RequestAuctionStart){
            connection.sendTCP(new ResponseAuctionStart());
            signal.countDown();
        }
    }

    private void setUpParties(AuctionConfiguration config){
        configuration = config;
        Map<Integer, Party> parties = new HashMap<>();
        for(String string : config.connectionData){
            String[] data = string.split(":");
            int id = Integer.parseInt(data[0]);
            String ip = data[1];
            int port = Integer.parseInt(data[2]);
            Party party = new Party(id, ip, port);
            parties.put(id, party);
        }
        networkConfiguration = new NetworkConfigurationImpl(config.partyId, parties);
    }
}
