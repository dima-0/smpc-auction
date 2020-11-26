package auctionplatform.worker.mockedclients;

import com.esotericsoftware.kryonet.Connection;
import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkConfigurationImpl;
import frescoauction.auction.Auction;
import auctionplatform.protocol.*;
import frescoauction.util.AuctionEvaluation;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Client worker, which follows the protocol and behaves correct during the auction.
 */
public class ClientWorker extends AbstractTestClientWorker {
    private int bid;
    private int frescoPort;
    private AuctionConfiguration configuration;
    private NetworkConfiguration networkConfiguration;
    private Auction.AuctionResult result;

    public ClientWorker(int clientId, String hostIp, int hostPort, int bid, int frescoPort) {
        super(clientId, hostIp, hostPort);
        this.bid = bid;
        this.frescoPort = frescoPort;
    }

    @Override
    public void received(Connection connection, Object object) {
        if(object instanceof RequestConnectionData){
            connection.sendTCP(new ResponseConnectionData(frescoPort));
        }else if(object instanceof AuctionConfiguration){
            AuctionConfiguration config = (AuctionConfiguration) object;
            setUpParties(config);
            connection.sendTCP(new AuctionReady());
        }else if(object instanceof RequestAuctionStart){
            connection.sendTCP(new ResponseAuctionStart());
            ExecutorService es = Executors.newSingleThreadExecutor();
            AuctionEvaluation task = new AuctionEvaluation(
                    configuration.auctionType, bid,
                    configuration.protocolSuite, configuration.preprocessing, networkConfiguration);
            Future<Auction.AuctionResult> futureResult = es.submit(task);
            try {
                TimeUnit.SECONDS.sleep(1);
                connection.sendTCP(new ResponseAuctionStart());
                result = futureResult.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }finally {
                es.shutdown();
                signal.countDown();
            }
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

    public Auction.AuctionResult getResult(){
        return result;
    }
}
