package com.db.auctionclient.mockedservers;

import com.esotericsoftware.kryonet.Connection;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import auctionplatform.protocol.AuctionConfiguration;
import auctionplatform.protocol.AuctionReady;
import auctionplatform.protocol.Register;
import auctionplatform.protocol.RequestAuctionStart;
import auctionplatform.protocol.RequestConnectionData;
import auctionplatform.protocol.ResponseAuctionStart;
import auctionplatform.protocol.ResponseConnectionData;
import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkConfigurationImpl;
import frescoauction.auction.Auction;
import frescoauction.util.AuctionEvaluation;
import frescoauction.util.Utils;

/**
 * Server-worker, which hosts an auction, follows the protocol
 * and behaves as he should during the auction.
 */
public class ServerWorker extends AbstractTestServerWorker {
    private int startingPrice;
    private Utils.AuctionType auctionType;
    private Utils.ProtocolSuite protocolSuite;
    private Utils.Preprocessing preprocessing;
    private NetworkConfiguration networkConfiguration;
    private Auction.AuctionResult result;
    private int frescoPort;

    public ServerWorker(int hostPort, int frescoPort,
                        int startingPrice, Utils.AuctionType auctionType,
                        Utils.ProtocolSuite protocolSuite, Utils.Preprocessing preprocessing) {
        super(hostPort);
        this.frescoPort = frescoPort;
        this.startingPrice = startingPrice;
        this.auctionType = auctionType;
        this.protocolSuite = protocolSuite;
        this.preprocessing = preprocessing;
    }

    @Override
    public void received(Connection connection, Object object) {
        if(object instanceof Register){
            connection.sendTCP(new RequestConnectionData());
        }else if(object instanceof ResponseConnectionData){
            int clientSmpcPort = ((ResponseConnectionData) object).frescoPort;
            setNetworkConfiguration(clientSmpcPort);
            String server = String.format("1:localhost:%d", frescoPort);
            String client = String.format("2:localhost:%d", clientSmpcPort);
            AuctionConfiguration configuration = new AuctionConfiguration(
                    2, auctionType,
                    protocolSuite, preprocessing,
                    new String[]{server, client});
            connection.sendTCP(configuration);
        } else if(object instanceof AuctionReady){
            connection.sendTCP(new RequestAuctionStart());
        }else if(object instanceof ResponseAuctionStart){
            ExecutorService es = Executors.newSingleThreadExecutor();
            AuctionEvaluation task = new AuctionEvaluation(
                    auctionType, startingPrice,
                    protocolSuite, preprocessing, networkConfiguration);
            Future<Auction.AuctionResult> futureResult = es.submit(task);
            try {
                result = futureResult.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }finally {
                es.shutdown();
                signal.countDown();
            }
        }
    }

    /**
     * Sets up a network configuration, which contains only the server-worker and a single client.
     * @param clientSmpcPort
     */
    public void setNetworkConfiguration(int clientSmpcPort){
        Party serverParty = new Party(1, "localhost", frescoPort);
        Party clientParty = new Party(2, "localhost", clientSmpcPort);
        Map<Integer, Party> parties = new HashMap<>();
        parties.put(1, serverParty);
        parties.put(2, clientParty);
        networkConfiguration = new NetworkConfigurationImpl(1,  parties);
    }
}
