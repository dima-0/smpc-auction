package auctionplatform.worker;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import frescoauction.util.Utils;
import auctionplatform.protocol.AuctionConfiguration;
import auctionplatform.protocol.RequestAuctionStart;
import auctionplatform.protocol.RequestConnectionData;
import auctionplatform.protocol.ResponseAuctionStart;
import auctionplatform.worker.mockedclients.*;

import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testing {@link ServerWorker} in exceptional cases, where the client-worker does not behave as he should.
 */
public class ServerWorkerExceptionsTest {
    /** Number of available threads.*/
    private static final int THREAD_POOL = 2;
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 8888;
    private static final int SERVER_FRESCO_PORT = 9000;
    /** Configuration of the server-worker.*/
    private static final ServerConfiguration SERVER_CONFIG = new ServerConfiguration(
            1, 1, Utils.AuctionType.SealedFirstPrice,
            5, 5, SERVER_IP, SERVER_PORT, SERVER_FRESCO_PORT,
            5, 5, Utils.ProtocolSuite.DummyArithmetic, Utils.Preprocessing.Dummy);
    private ExecutorService executorService;
    private ServerWorkerTestListener workerListener;

    @BeforeEach
    void setUp() {
        executorService = Executors.newFixedThreadPool(THREAD_POOL);
        workerListener = new ServerWorkerTestListener();
    }

    @AfterEach
    void tearDown() {
        executorService.shutdownNow();
        executorService = null;
        workerListener = null;
    }

    /**
     * Starts the server-worker and the client-worker and waits for both of them to finish.
     * @param serverWorker server-worker.
     * @param clientWorker mocked client-worker.
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private void startAndWait(ServerWorker serverWorker, AbstractTestClientWorker clientWorker) throws ExecutionException, InterruptedException {
        Future<?> server = executorService.submit(serverWorker);
        TimeUnit.SECONDS.sleep(1);
        Future<?> client = executorService.submit(clientWorker);
        server.get();
        client.get();
    }

    /**
     * Tests server-workers' behavior, when no clients join the auction.
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testServerWorkerAuctionWithNoClientWorkers() throws ExecutionException, InterruptedException {
        ServerWorker serverWorker = new ServerWorker(SERVER_CONFIG, workerListener);
        executorService.submit(serverWorker).get();
        assertFalse(workerListener.completedSuccessfully());
        assertTrue(workerListener.errorOccurred());
        assertEquals("No clients registered.", workerListener.getErrorMessage());
    }

    /**
     * Tests server-workers' behavior, when a client-worker does
     * not respond to {@link RequestConnectionData}.<br>
     * Uses mocked client-worker: {@link ClientWorkerNoResponseConnectionData}
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testServerWorkerReceiveNoConnectionData() throws ExecutionException, InterruptedException {
        ServerWorker serverWorker = new ServerWorker(SERVER_CONFIG, workerListener);
        AbstractTestClientWorker clientWorker = new ClientWorkerNoResponseConnectionData(1, SERVER_IP, SERVER_PORT);
        startAndWait(serverWorker, clientWorker);
        assertFalse(workerListener.completedSuccessfully());
        assertTrue(workerListener.errorOccurred());
        assertEquals("At least one client does not respond to RequestConnectionData.", workerListener.getErrorMessage());
    }

    /**
     * Tests server-workers' behavior, when a client-worker does
     * not respond to {@link AuctionConfiguration}.<br>
     * Uses mocked client-worker: {@link ClientWorkerNoAuctionReady}
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testServerWorkerReceiveNoAuctionReady() throws ExecutionException, InterruptedException {
        ServerWorker serverWorker = new ServerWorker(SERVER_CONFIG, workerListener);
        AbstractTestClientWorker clientWorker = new ClientWorkerNoAuctionReady(1, SERVER_IP, SERVER_PORT);
        startAndWait(serverWorker, clientWorker);
        assertFalse(workerListener.completedSuccessfully());
        assertTrue(workerListener.errorOccurred());
        assertEquals("At least one client is not ready for auction evaluation.", workerListener.getErrorMessage());
    }

    /**
     * Tests server-workers' behavior, when a client-worker does
     * not start SMPC and not respond to {@link RequestAuctionStart}.<br>
     * Uses mocked client-worker: {@link ClientWorkerNoResponseAuctionStart}
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testServerWorkerReceiveNoResponseAuctionStart() throws ExecutionException, InterruptedException {
        ServerWorker serverWorker = new ServerWorker(SERVER_CONFIG, workerListener);
        AbstractTestClientWorker clientWorker = new ClientWorkerNoResponseAuctionStart(1, SERVER_IP, SERVER_PORT);
        startAndWait(serverWorker, clientWorker);
        assertFalse(workerListener.completedSuccessfully());
        assertTrue(workerListener.errorOccurred());
        assertEquals("Auction evaluation is not started by all parties.", workerListener.getErrorMessage());
    }

    /**
     * Tests server-workers' behavior, when a client-worker does
     * response with {@link ResponseAuctionStart} after receiving{@link RequestAuctionStart},
     * but not start SMPC.<br>
     * Uses mocked client-worker: {@link ClientWorkerEvaluationAbortion}
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testServerWorkerErrorDuringEvaluation() throws ExecutionException, InterruptedException {
        ServerWorker serverWorker = new ServerWorker(SERVER_CONFIG, workerListener);
        AbstractTestClientWorker clientWorker = new ClientWorkerEvaluationAbortion(1, SERVER_IP, SERVER_PORT);
        startAndWait(serverWorker, clientWorker);
        assertFalse(workerListener.completedSuccessfully());
        assertTrue(workerListener.errorOccurred());
        assertEquals("Auction evaluation aborted.", workerListener.getErrorMessage());
    }
}
