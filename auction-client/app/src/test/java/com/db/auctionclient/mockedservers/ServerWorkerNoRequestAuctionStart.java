package com.db.auctionclient.mockedservers;

import com.esotericsoftware.kryonet.Connection;

import java.util.concurrent.TimeUnit;

import auctionplatform.protocol.AuctionConfiguration;
import auctionplatform.protocol.AuctionReady;
import auctionplatform.protocol.Register;
import auctionplatform.protocol.RequestConnectionData;
import auctionplatform.protocol.ResponseConnectionData;
import frescoauction.util.Utils;

/**
 * Server-worker, which shuts down after the smpc-set-up-finish state and does no send a
 * {@link auctionplatform.protocol.RequestAuctionStart} message.
 */
public class ServerWorkerNoRequestAuctionStart extends AbstractTestServerWorker {
    /** Waiting time before shutting down (seconds).*/
    private int wait;
    private int frescoPort;

    public ServerWorkerNoRequestAuctionStart(int hostPort, int wait, int frescoPort) {
        super(hostPort);
        this.wait = wait;
        this.frescoPort = frescoPort;
    }

    @Override
    public void received(Connection connection, Object object) {
        if(object instanceof Register){
            connection.sendTCP(new RequestConnectionData());
        }else if(object instanceof ResponseConnectionData){
            int clientFrescoPort = ((ResponseConnectionData) object).frescoPort;
            String server = String.format("1:localhost:%d", frescoPort);
            String client = String.format("2:localhost:%d", clientFrescoPort);
            AuctionConfiguration configuration = new AuctionConfiguration(
                    2, Utils.AuctionType.SealedFirstPrice,
                    Utils.ProtocolSuite.DummyArithmetic, Utils.Preprocessing.Dummy,
                    new String[]{server, client});
            connection.sendTCP(configuration);
        } else if(object instanceof AuctionReady){
            try {
                TimeUnit.SECONDS.sleep(wait);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            signal.countDown();
        }
    }
}
