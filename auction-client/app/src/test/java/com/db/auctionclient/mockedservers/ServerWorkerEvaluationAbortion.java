package com.db.auctionclient.mockedservers;

import com.esotericsoftware.kryonet.Connection;

import auctionplatform.protocol.AuctionConfiguration;
import auctionplatform.protocol.AuctionReady;
import auctionplatform.protocol.Register;
import auctionplatform.protocol.RequestAuctionStart;
import auctionplatform.protocol.RequestConnectionData;
import auctionplatform.protocol.ResponseAuctionStart;
import auctionplatform.protocol.ResponseConnectionData;
import frescoauction.util.Utils;

/**
 * Server-worker, which shuts down after receiving the {@link ResponseAuctionStart} message.
 * This should cause the client-worker to abort the auction evaluation, since the server-worker
 * did not start the evaluation.
 */
public class ServerWorkerEvaluationAbortion extends AbstractTestServerWorker {
    private int frescoPort;

    public ServerWorkerEvaluationAbortion(int hostPort, int frescoPort) {
        super(hostPort);
        this.frescoPort = frescoPort;
    }

    @Override
    public void received(Connection connection, Object object) {
        if(object instanceof Register){
            connection.sendTCP(new RequestConnectionData());
        }else if(object instanceof ResponseConnectionData){
            int clientSmpcPort = ((ResponseConnectionData) object).frescoPort;
            String server = String.format("1:localhost:%d", frescoPort);
            String client = String.format("2:localhost:%d", clientSmpcPort);
            AuctionConfiguration configuration = new AuctionConfiguration(
                    2, Utils.AuctionType.SealedFirstPrice,
                    Utils.ProtocolSuite.DummyArithmetic, Utils.Preprocessing.Dummy,
                    new String[]{server, client});
            connection.sendTCP(configuration);
        } else if(object instanceof AuctionReady){
            connection.sendTCP(new RequestAuctionStart());
        }else if(object instanceof ResponseAuctionStart){
            signal.countDown();
        }
    }
}
