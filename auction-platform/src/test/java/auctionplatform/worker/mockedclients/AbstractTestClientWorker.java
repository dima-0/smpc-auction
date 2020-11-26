package auctionplatform.worker.mockedclients;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import auctionplatform.protocol.Register;
import auctionplatform.protocol.NetworkHelper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Simple client-worker for testing purposes. Concrete behavior should be
 * implemented by concrete client-workers.
 * A {@link Register} is send after the initialization.
 */
public abstract class AbstractTestClientWorker extends Listener implements Runnable{
    private int clientId;
    private String hostIp;
    private int hostPort;
    /** Kryo-client, which receives messages from the server-worker (his kryo-server).*/
    private Client kryoClient;
    /** Single use signal for notifying the running thread to shutdown.*/
    protected CountDownLatch signal = new CountDownLatch(1);

    public AbstractTestClientWorker(int clientId, String hostIp, int hostPort) {
        this.clientId = clientId;
        this.hostIp = hostIp;
        this.hostPort = hostPort;
    }

    @Override
    public void disconnected(Connection connection) {
        signal.countDown();
    }

    @Override
    public void run() {
        ExecutorService es = Executors.newSingleThreadExecutor();
        try {
            kryoClient = new Client();
            kryoClient.start();
            NetworkHelper.register(kryoClient);
            kryoClient.connect(5000, hostIp, hostPort);
            kryoClient.addListener(new ThreadedListener(this, es));
            kryoClient.sendTCP(new Register(clientId));
            // blocks until the signal is triggered
            signal.await();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }finally {
            kryoClient.stop();
            es.shutdown();
        }
    }
}
