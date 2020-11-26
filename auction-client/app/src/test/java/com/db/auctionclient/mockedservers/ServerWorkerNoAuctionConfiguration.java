package com.db.auctionclient.mockedservers;

import com.esotericsoftware.kryonet.Connection;

import java.util.concurrent.TimeUnit;

import auctionplatform.protocol.Register;
import auctionplatform.protocol.RequestConnectionData;
import auctionplatform.protocol.ResponseConnectionData;

/**
 * Server-worker, which shuts down after the smpc-set-up state and does no send a
 * {@link auctionplatform.protocol.AuctionConfiguration} message.
 */
public class ServerWorkerNoAuctionConfiguration extends AbstractTestServerWorker {
    /** Waiting time before shutting down (seconds).*/
    private int wait;

    public ServerWorkerNoAuctionConfiguration(int hostPort, int wait) {
        super(hostPort);
        this.wait = wait;
    }

    @Override
    public void received(Connection connection, Object object) {
        if(object instanceof Register){
            connection.sendTCP(new RequestConnectionData());
        }else if(object instanceof ResponseConnectionData){
            try {
                TimeUnit.SECONDS.sleep(wait);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            signal.countDown();
        }
    }
}
