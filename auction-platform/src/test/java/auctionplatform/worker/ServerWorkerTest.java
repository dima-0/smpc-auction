package auctionplatform.worker;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import frescoauction.util.Utils;
import auctionplatform.worker.mockedclients.ClientWorker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Testing {@link ServerWorker} performing an auction, with clients, which behave as they should.
 * A mocked implementation of the client-worker is used for testing: {@link ClientWorker}.
 */
public class ServerWorkerTest {
    /** Number of available threads.*/
    private static final int THREAD_POOL = 5;
    private static final int AUCTION_ID = 1;
    private static final int STATE_DURATION = 5;
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 8888;
    private static final int FRESCO_PORT = 9000;

    private ExecutorService executorService;
    private ServerWorkerTestListener workerListener;

    /**
     * Sets up and configures mocked client-workers for tests.
     * @param bids bids, which should be used by the client-workers.
     * @return
     */
    private static List<ClientWorker> setUpClients(int bids[]){
        List<ClientWorker> clients = new ArrayList<>();
        for (int i = 0; i < bids.length; i++){
            int clientId = i + 1;
            ClientWorker client = new ClientWorker(clientId, SERVER_IP, SERVER_PORT, bids[i], 5000 + i);
            clients.add(client);
        }
        return clients;
    }

    /**
     * Provides party inputs and the expected results for {@link Utils.AuctionType#SealedFirstPrice} and
     * {@link Utils.AuctionType#SealedFirstPrice}.<br>
     * Order: auction type, starting price, array of bids, partyId of the expected winner, expected final price
     * @return party inputs with expected results.
     */
    private static List<Arguments> data(){
        return List.of(
                Arguments.of(Utils.AuctionType.SealedFirstPrice, 1, new int[]{1}, 1, 1),
                Arguments.of(Utils.AuctionType.SealedFirstPrice, 1, new int[]{1, Integer.MAX_VALUE}, 2, Integer.MAX_VALUE),
                Arguments.of(Utils.AuctionType.SealedFirstPrice, 1, new int[]{1, Integer.MAX_VALUE, 2}, 2, Integer.MAX_VALUE),
                Arguments.of(Utils.AuctionType.SealedFirstPrice, 1, new int[]{1, 42, Integer.MAX_VALUE, 2}, 3, Integer.MAX_VALUE),
                Arguments.of(Utils.AuctionType.SealedSecondPrice, 1, new int[]{1}, 1, 1),
                Arguments.of(Utils.AuctionType.SealedSecondPrice, 1, new int[]{1, Integer.MAX_VALUE}, 2, 1),
                Arguments.of(Utils.AuctionType.SealedSecondPrice, 1, new int[]{1, Integer.MAX_VALUE, 2}, 2, 2),
                Arguments.of(Utils.AuctionType.SealedSecondPrice, 1, new int[]{1, 42, Integer.MAX_VALUE, 2}, 3, 42)
        );
    }

    /**
     * Provides party inputs and the expected results for {@link Utils.AuctionType#SealedFirstPrice} and
     * {@link Utils.AuctionType#SealedSecondPrice}. Should be only used in tests, which run
     * {@link Utils.Preprocessing#Mascot}.<br>
     * Order: auction type, starting price, array of bids, partyId of the expected winner, expected final price
     * @return party inputs with expected results.
     */
    private static List<Arguments> dataMascot(){
        return List.of(
                Arguments.of(Utils.AuctionType.SealedFirstPrice, 1, new int[]{Integer.MAX_VALUE}, 1, Integer.MAX_VALUE),
                Arguments.of(Utils.AuctionType.SealedSecondPrice, 1, new int[]{Integer.MAX_VALUE}, 1, 1)
        );
    }

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
     * Tests the server-worker running the auction evaluation with {@link Utils.ProtocolSuite#DummyArithmetic}.
     * @param auctionType auction type.
     * @param startingPrice starting price, which the server-worker should use.
     * @param bids array of bids, which should be used by the clients.
     * @param expectedClientId id of the client, who is expected to be the highest bidder.
     * @param expectedBid expected highest bid.
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @ParameterizedTest
    @MethodSource("data")
    public void testServerWorkerRunAuctionWithDummyArithmetic(Utils.AuctionType auctionType, int startingPrice, int bids[], int expectedClientId, int expectedBid) throws ExecutionException, InterruptedException {
        // setting up server-worker and client-workers
        ServerConfiguration serverConfig = new ServerConfiguration(
                AUCTION_ID, startingPrice, auctionType,
                STATE_DURATION, STATE_DURATION, SERVER_IP, SERVER_PORT, FRESCO_PORT,
                STATE_DURATION, STATE_DURATION, Utils.ProtocolSuite.DummyArithmetic, Utils.Preprocessing.Dummy);
        ServerWorker serverWorker = new ServerWorker(serverConfig, workerListener);
        Future<?> server = executorService.submit(serverWorker);
        TimeUnit.SECONDS.sleep(1);
        List<Future<ClientWorker>> clients = setUpClients(bids).stream()
                .map(client -> executorService.submit(client, client)) //returns corresponding client-worker instance on finish, so the result can be tested.
                .collect(Collectors.toList());

        // waiting for all client-workers and the server-worker to finish and testing the results
        for(Future<ClientWorker> client : clients){
            ClientWorker c = client.get();
            assertNotNull(c.getResult());
            assertEquals(expectedBid, c.getResult().getFinalPrice());
        }
        server.get();
        assertTrue(workerListener.completedSuccessfully());
        assertEquals(expectedClientId, workerListener.getClientId());
        assertEquals(expectedBid, workerListener.getAuctionResult().getFinalPrice());
    }

    /**
     * Tests the server-worker running the auction evaluation with {@link Utils.ProtocolSuite#Spdz}, which uses
     * {@link Utils.Preprocessing#Dummy}.
     * @param auctionType auction type.
     * @param startingPrice starting price, which the server-worker should use.
     * @param bids array of bids, which should be used by the clients.
     * @param expectedClientId id of the client, who is expected to be the highest bidder.
     * @param expectedBid expected highest bid.
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @ParameterizedTest
    @MethodSource("data")
    public void testServerWorkerRunWithSpdzDummy(Utils.AuctionType auctionType, int startingPrice, int bids[], int expectedClientId, int expectedBid) throws ExecutionException, InterruptedException {
        // setting up server-worker and client-workers
        ServerConfiguration serverConfig = new ServerConfiguration(
                AUCTION_ID, startingPrice, auctionType,
                STATE_DURATION, STATE_DURATION, SERVER_IP, SERVER_PORT, FRESCO_PORT,
                STATE_DURATION, STATE_DURATION, Utils.ProtocolSuite.Spdz, Utils.Preprocessing.Dummy);
        ServerWorker serverWorker = new ServerWorker(serverConfig, workerListener);
        Future<?> server = executorService.submit(serverWorker);
        TimeUnit.SECONDS.sleep(1);
        List<Future<ClientWorker>> clients = setUpClients(bids).stream()
                .map(client -> executorService.submit(client, client)) //returns corresponding client-worker instance on finish, so the result can be tested.
                .collect(Collectors.toList());

        // waiting for all client-workers and server-worker to finish and testing the results
        for(Future<ClientWorker> client : clients){
            ClientWorker c = client.get();
            assertNotNull(c.getResult());
            assertEquals(expectedBid, c.getResult().getFinalPrice());
        }
        server.get();
        assertTrue(workerListener.completedSuccessfully());
        assertEquals(expectedClientId, workerListener.getClientId());
        assertEquals(expectedBid, workerListener.getAuctionResult().getFinalPrice());
    }

    /**
     * Tests the server-worker running the auction evaluation with {@link Utils.ProtocolSuite#Spdz}, which uses
     * {@link Utils.Preprocessing#Mascot}.
     * @param auctionType auction type.
     * @param startingPrice starting price, which the server-worker should use.
     * @param bids array of bids, which should be used by the clients.
     * @param expectedClientId id of the client, who is expected to be the highest bidder.
     * @param expectedBid expected highest bid.
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @ParameterizedTest
    @MethodSource("dataMascot")
    public void testServerWorkerRunWithSpdzMascot(Utils.AuctionType auctionType, int startingPrice, int bids[], int expectedClientId, int expectedBid) throws ExecutionException, InterruptedException {
        // setting up server-worker and client-workers
        ServerConfiguration serverConfig = new ServerConfiguration(
                AUCTION_ID, startingPrice, auctionType,
                STATE_DURATION, STATE_DURATION, SERVER_IP, SERVER_PORT, FRESCO_PORT,
                STATE_DURATION, STATE_DURATION, Utils.ProtocolSuite.Spdz, Utils.Preprocessing.Mascot);
        ServerWorker serverWorker = new ServerWorker(serverConfig, workerListener);
        Future<?> server = executorService.submit(serverWorker);
        TimeUnit.SECONDS.sleep(1);
        List<Future<ClientWorker>> clients = setUpClients(bids).stream()
                .map(client -> executorService.submit(client, client)) //returns corresponding client-worker instance on finish, so the result can be tested.
                .collect(Collectors.toList());

        // waiting for all client-workers and server-worker to finish and testing the results
        for(Future<ClientWorker> client : clients){
            ClientWorker c = client.get();
            assertNotNull(c.getResult());
            assertEquals(expectedBid, c.getResult().getFinalPrice());
        }
        server.get();
        assertTrue(workerListener.completedSuccessfully());
        assertEquals(expectedClientId, workerListener.getClientId());
        assertEquals(expectedBid, workerListener.getAuctionResult().getFinalPrice());
    }
}
