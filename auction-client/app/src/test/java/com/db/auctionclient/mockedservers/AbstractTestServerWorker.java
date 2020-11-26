package com.db.auctionclient.mockedservers;

import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import auctionplatform.protocol.NetworkHelper;

/**
 * Simple server-worker for testing purposes. Concrete behavior should be
 * implemented by concrete server-workers.
 */
public abstract class AbstractTestServerWorker extends Listener implements Runnable{
    /** Kryo server, which receives messages from the client-worker (his kryo client).*/
    private Server kryoServer;
    /** Single use signal, which is used for notifying the running thread to shutdown.*/
    protected CountDownLatch signal = new CountDownLatch(1);
    private int hostPort;

    public AbstractTestServerWorker(int hostPort) {
        this.hostPort = hostPort;
    }

    @Override
    public void run() {
        ExecutorService es = Executors.newSingleThreadExecutor();
        try {
            kryoServer = new Server();
            NetworkHelper.register(kryoServer);
            kryoServer.addListener(new ThreadedListener(this, es));
            kryoServer.bind(hostPort);
            kryoServer.start();
            // Blocks until the signal is triggered.
            signal.await();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }finally {
            kryoServer.stop();
            es.shutdown();
        }
    }
}
