package com.db.auctionclient;


import com.db.auctionclient.mockedservers.ServerWorkerNoAuctionConfiguration;
import com.db.auctionclient.mockedservers.ServerWorkerNoRequestConnectionData;
import com.db.auctionclient.mockedservers.ServerWorkerNoRequestAuctionStart;
import com.db.auctionclient.mockedservers.ServerWorkerEvaluationAbortion;
import com.db.auctionclient.mockedservers.AbstractTestServerWorker;
import com.db.auctionclient.model.worker.ClientConfiguration;
import com.db.auctionclient.model.worker.ClientWorker;
import com.db.auctionclient.model.entities.AuctionPhase;
import com.db.auctionclient.model.entities.AuctionTask;

import org.junit.After;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Testing {@link ClientWorker} in exceptional cases, where the server-worker does not behave as he should.
 */
public class ClientWorkerExceptionTest {
    /** Number of available threads.*/
    private static final int THREAD_POOL = 2;
    private static final int AUCTION_ID = 1;
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 8888;
    private static final int SERVER_SMPC_PORT = 9000;
    private static final int CLIENT_SMPC_PORT = 50000;
    /**Configuration of the client-worker.*/
    private static final ClientConfiguration CLIENT_CONFIG = new ClientConfiguration(
            1, false, new int[]{CLIENT_SMPC_PORT},
            5, 5, 5);
    private ExecutorService executorService;
    private TestAuctionRepository testRepository;
    private TestWorkerListener workerListener;

    /**
     * Starts the server-worker and the client-worker and waits for both of them to finish.
     * @param serverWorker mocked server-worker.
     * @param clientWorker client-worker.
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private void startAndWait(AbstractTestServerWorker serverWorker, ClientWorker clientWorker) throws ExecutionException, InterruptedException {
        Future<?> server = executorService.submit(serverWorker);
        Future<?> client = executorService.submit(clientWorker);
        server.get();
        client.get();
    }

    /**
     * Creates a new {@link AuctionTask} instance and stores it in {@link #testRepository}.
     * @return a new instance of {@link AuctionTask}.
     */
    private AuctionTask initializeAuctionTask(int bid){
        AuctionTask task = new AuctionTask(AUCTION_ID ,SERVER_IP, SERVER_PORT, CLIENT_SMPC_PORT, bid);
        testRepository.addAuctionTask(task);
        return task;
    }

    @Before
    public void setUp() throws Exception {
        testRepository = new TestAuctionRepository();
        workerListener = new TestWorkerListener();
        executorService = Executors.newFixedThreadPool(THREAD_POOL);
    }

    @After
    public void tearDown() throws Exception {
        testRepository = null;
        workerListener = null;
        executorService.shutdown();
        executorService = null;
    }

    /**
     * Tests the client-worker in a case, in which no connection to the server-worker can be established.
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testClientWorkerNoConnectionToServer() throws ExecutionException, InterruptedException {
        // initialize server worker and client worker
        ClientWorker clientWorker = new ClientWorker(CLIENT_CONFIG, initializeAuctionTask(42), testRepository, workerListener);
        Future<?> client = executorService.submit(clientWorker);
        client.get();
        AuctionTask finishedTask = testRepository.getAuctionTaskDatabase().get(AUCTION_ID);
        assertNotNull(finishedTask);
        assertEquals(AuctionPhase.Abortion, finishedTask.getLocalPhase());
        assertEquals("Connection could not be established.", finishedTask.getErrorMessage());
    }

    /**
     * Tests the client-worker in a case, in which the server-worker does not
     * send a {@link auctionplatform.protocol.RequestConnectionData} message.
     * Uses mocked server-worker: {@link ServerWorkerNoRequestConnectionData}
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testClientWorkerReceiveNoRequestConnectionData() throws ExecutionException, InterruptedException {
        // initialize server worker and client worker
        AbstractTestServerWorker abstractTestServerWorker = new ServerWorkerNoRequestConnectionData(SERVER_PORT, CLIENT_CONFIG.getRegistrationDuration() + 1);
        ClientWorker clientWorker = new ClientWorker(CLIENT_CONFIG, initializeAuctionTask(42), testRepository, workerListener);
        startAndWait(abstractTestServerWorker, clientWorker);
        // check results
        AuctionTask finishedTask = testRepository.getAuctionTaskDatabase().get(AUCTION_ID);
        assertNotNull(finishedTask);
        assertEquals(AuctionPhase.Abortion, finishedTask.getLocalPhase());
        assertEquals("RequestConnectionData timeout.", finishedTask.getErrorMessage());
    }

    /**
     * Tests the client-worker in a case, in which the server-worker does not
     * send a {@link auctionplatform.protocol.AuctionConfiguration} message.
     * Uses mocked server-worker: {@link ServerWorkerNoAuctionConfiguration}
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testClientWorkerReceiveNoAuctionConfiguration() throws ExecutionException, InterruptedException {
        // initialize server worker and client worker
        AbstractTestServerWorker abstractTestServerWorker = new ServerWorkerNoAuctionConfiguration(SERVER_PORT, CLIENT_CONFIG.getSmpcSetUpDuration() + 1);
        ClientWorker clientWorker = new ClientWorker(CLIENT_CONFIG, initializeAuctionTask(42), testRepository, workerListener);
        startAndWait(abstractTestServerWorker, clientWorker);
        // check results
        AuctionTask finishedTask = testRepository.getAuctionTaskDatabase().get(AUCTION_ID);
        assertNotNull(finishedTask);
        assertEquals(AuctionPhase.Abortion, finishedTask.getLocalPhase());
        assertEquals("AuctionConfiguration timeout.", finishedTask.getErrorMessage());
    }

    /**
     * Tests the client-worker in a case, in which the server-worker does not
     * send a {@link auctionplatform.protocol.RequestAuctionStart} message.
     * Uses mocked server-worker: {@link ServerWorkerNoRequestAuctionStart}
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testClientWorkerNoRequestAuctionStart() throws ExecutionException, InterruptedException {
        // initialize server worker and client worker
        AbstractTestServerWorker abstractTestServerWorker = new ServerWorkerNoRequestAuctionStart(SERVER_PORT, CLIENT_CONFIG.getSmpcSetUpFinishDuration() + 1, SERVER_SMPC_PORT);
        ClientWorker clientWorker = new ClientWorker(CLIENT_CONFIG, initializeAuctionTask(42), testRepository, workerListener);
        startAndWait(abstractTestServerWorker, clientWorker);
        // check results
        AuctionTask finishedTask = testRepository.getAuctionTaskDatabase().get(AUCTION_ID);
        assertNotNull(finishedTask);
        assertEquals(AuctionPhase.Abortion, finishedTask.getLocalPhase());
        assertEquals("RequestAuctionStart timeout.", finishedTask.getErrorMessage());
    }

    /**
     * Tests the client-worker in a case, in which the server-worker does not
     * start the auction evaluation.
     * Uses mocked server-worker: {@link ServerWorkerEvaluationAbortion}
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testClientWorkerErrorDuringEvaluation() throws ExecutionException, InterruptedException {
        // initialize server worker and client worker
        AbstractTestServerWorker abstractTestServerWorker = new ServerWorkerEvaluationAbortion(SERVER_PORT, SERVER_SMPC_PORT);
        ClientWorker clientWorker = new ClientWorker(CLIENT_CONFIG, initializeAuctionTask(42), testRepository, workerListener);
        startAndWait(abstractTestServerWorker, clientWorker);
        // check results
        AuctionTask finishedTask = testRepository.getAuctionTaskDatabase().get(AUCTION_ID);
        assertNotNull(finishedTask);
        assertEquals(AuctionPhase.Abortion, finishedTask.getLocalPhase());
        assertEquals("Auction evaluation aborted.", finishedTask.getErrorMessage());
    }
}