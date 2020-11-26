package com.db.auctionclient;

import com.db.auctionclient.mockedservers.ServerWorker;
import com.db.auctionclient.mockedservers.AbstractTestServerWorker;
import com.db.auctionclient.model.worker.ClientConfiguration;
import com.db.auctionclient.model.worker.ClientWorker;
import com.db.auctionclient.model.entities.AuctionPhase;
import com.db.auctionclient.model.entities.AuctionTask;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import frescoauction.util.Utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Testing the implementation of {@link ClientWorker} participating in an auction, which is
 * hosted by a server-worker, which behave as he should.
 * A mocked implementation of the server-worker is used for testing: {@link ServerWorker}
 */
@RunWith(Parameterized.class)
public class ClientWorkerTest {
    /** Number of available threads.*/
    private static final int THREAD_POOL = 2;
    private static final int AUCTION_ID = 1;
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 8888;
    private static final int SERVER_SMPC_PORT = 9000;
    private static final int CLIENT_SMPC_PORT = 5000;
    /** Configuration of the client-worker.*/
    private static final ClientConfiguration CLIENT_CONFIG = new ClientConfiguration(
            1, false, new int[]{CLIENT_SMPC_PORT},
            5, 5, 5);
    private ExecutorService executorService;
    private TestAuctionRepository testRepository;
    private TestWorkerListener workerListener;

    /**
     * Provides input data for the client-worker and the expected results.
     * Order: auction type, bid, id of expected winner, expected final price
     * @return
     */
    @Parameters
    public static List<Object[]> data(){
        return Arrays.asList(new Object[][]{
                {Utils.AuctionType.SealedFirstPrice, 1, 1, 1},
                {Utils.AuctionType.SealedFirstPrice, 42, 1, 42},
                {Utils.AuctionType.SealedFirstPrice, Integer.MAX_VALUE, 1, Integer.MAX_VALUE},
                {Utils.AuctionType.SealedSecondPrice, 1, 1, 1},
                {Utils.AuctionType.SealedSecondPrice, 42, 1, 1},
                {Utils.AuctionType.SealedSecondPrice, Integer.MAX_VALUE, 1, 1}
        });
    }

    /**
     * Auction type.
     */
    @Parameter(0)
    public Utils.AuctionType auctionType;

    /**
     * Bid, which should be used by the client-worker.
     */
    @Parameter(1)
    public int bid;

    /**
     * Id of the expected winner.
     */
    @Parameter(2)
    public int expectedWinnerId;

    /**
     * Expected final price.
     */
    @Parameter(3)
    public int expectedFinalPrice;

    /**
     * Creates a new {@link AuctionTask} instance and stores it in {@link #testRepository}.
     * @return a new instance of {@link AuctionTask}.
     */
    private AuctionTask initializeAuctionTask(int bid){
        AuctionTask task = new AuctionTask(AUCTION_ID ,SERVER_IP, SERVER_PORT, CLIENT_SMPC_PORT, bid);
        testRepository.addAuctionTask(task);
        return task;
    }

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
     * Tests the client-worker participating in an auction. The auction evaluation is executed
     * with {@link Utils.ProtocolSuite#DummyArithmetic}.
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testClientRunAuctionWithDummyArithmetic() throws ExecutionException, InterruptedException {
        // initialize server worker and client worker
        AbstractTestServerWorker abstractTestServerWorker = new ServerWorker(SERVER_PORT, SERVER_SMPC_PORT,
                1, auctionType,
                Utils.ProtocolSuite.DummyArithmetic, Utils.Preprocessing.Dummy);
        ClientWorker clientWorker = new ClientWorker(CLIENT_CONFIG, initializeAuctionTask(bid), testRepository, workerListener);
        startAndWait(abstractTestServerWorker, clientWorker);
        // check results
        AuctionTask finishedTask = testRepository.getAuctionTaskDatabase().get(AUCTION_ID);
        assertNotNull(finishedTask);
        assertEquals(AuctionPhase.Completion, finishedTask.getLocalPhase());
        assertEquals(expectedFinalPrice, finishedTask.getFinalPrice());
    }

    /**
     * Tests the client-worker participating in an auction. The auction evaluation is executed
     * with {@link Utils.ProtocolSuite#Spdz}, which uses {@link Utils.Preprocessing#Dummy}.
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testClientRunAuctionWithSpdzDummy() throws ExecutionException, InterruptedException {
        // initialize server worker and client worker
        AbstractTestServerWorker abstractTestServerWorker = new ServerWorker(SERVER_PORT, SERVER_SMPC_PORT,
                1, auctionType,
                Utils.ProtocolSuite.Spdz, Utils.Preprocessing.Dummy);
        ClientWorker clientWorker = new ClientWorker(CLIENT_CONFIG, initializeAuctionTask(bid), testRepository, workerListener);
        startAndWait(abstractTestServerWorker, clientWorker);
        // check results
        AuctionTask finishedTask = testRepository.getAuctionTaskDatabase().get(AUCTION_ID);
        assertNotNull(finishedTask);
        assertEquals(AuctionPhase.Completion, finishedTask.getLocalPhase());
        assertEquals(expectedFinalPrice, finishedTask.getFinalPrice());
    }

    /**
     * Tests the client-worker participating in an auction. The auction evaluation is executed
     * with {@link Utils.ProtocolSuite#Spdz}, which uses {@link Utils.Preprocessing#Mascot}.
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testClientRunAuctionWithSpdzMascot() throws ExecutionException, InterruptedException {
        // initialize server worker and client worker
        AbstractTestServerWorker abstractTestServerWorker = new ServerWorker(SERVER_PORT, SERVER_SMPC_PORT,
                1, auctionType,
                Utils.ProtocolSuite.Spdz, Utils.Preprocessing.Mascot);
        ClientWorker clientWorker = new ClientWorker(CLIENT_CONFIG, initializeAuctionTask(bid), testRepository, workerListener);
        startAndWait(abstractTestServerWorker, clientWorker);
        // check results
        AuctionTask finishedTask = testRepository.getAuctionTaskDatabase().get(AUCTION_ID);
        assertNotNull(finishedTask);
        assertEquals(AuctionPhase.Completion, finishedTask.getLocalPhase());
        assertEquals(expectedFinalPrice, finishedTask.getFinalPrice());
    }
}
