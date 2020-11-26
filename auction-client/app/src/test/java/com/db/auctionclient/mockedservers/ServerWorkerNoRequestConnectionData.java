package com.db.auctionclient.mockedservers;

import com.esotericsoftware.kryonet.Connection;
import java.util.concurrent.TimeUnit;
import auctionplatform.protocol.Register;

/**
 * Server-worker, which shuts down after the registration state and does no send a
 * {@link auctionplatform.protocol.RequestConnectionData} message.
 */
public class ServerWorkerNoRequestConnectionData extends AbstractTestServerWorker {
    private int registrationDuration;

    public ServerWorkerNoRequestConnectionData(int hostPort, int registrationDuration) {
        super(hostPort);
        this.registrationDuration = registrationDuration;
    }

    @Override
    public void received(Connection connection, Object object) {
        if(object instanceof Register){
            try {
                TimeUnit.SECONDS.sleep(registrationDuration);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            signal.countDown();
        }
    }
}
