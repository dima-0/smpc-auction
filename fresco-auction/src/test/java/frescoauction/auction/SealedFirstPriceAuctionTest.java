package frescoauction.auction;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticResourcePool;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import frescoauction.util.SMPC;
import frescoauction.util.Utils;
import frescoauction.configuration.ProtocolConfiguration;
import frescoauction.configuration.dummy.DummyArithmeticConfiguration;
import frescoauction.configuration.spdz.SpdzConfiguration;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static frescoauction.auction.TestUtils.assertAllPartiesGetSameResults;
import static frescoauction.auction.TestUtils.getNetworkConfiguration;

/**
 * Testing the implementation of {@link SealedFirstPriceAuction}.
 * It contains test cases for all suites, which are included in {@link Utils.ProtocolSuite}.
 */
public class SealedFirstPriceAuctionTest {
    /** Number of available threads. */
    private final static int THREAD_POOL = 10;
    private ExecutorService executorService;

    /**
     * Sets up parties for {@link SealedFirstPriceAuction}.
     * @param bids bids, which should be used as input by the parties.
     * @param config protocol suite configuration.
     * @return parties as {@link Callable}, which can be run by an {@link ExecutorService}.
     */
    private static List<Callable<Auction.AuctionResult>> setUpParties(int[] bids, ProtocolConfiguration config){
        List<Callable<Auction.AuctionResult>> clients = new ArrayList<>();
        for(int i = 0; i < bids.length; i++){
            int bid = bids[i];
            int partyId = i + 1;
            NetworkConfiguration networkConfig = getNetworkConfiguration(partyId, bids.length);
            Application<Auction.AuctionResult, ProtocolBuilderNumeric> application =
                    new SealedFirstPriceAuction(bid, networkConfig);
            Callable<Auction.AuctionResult> task = null;
            if(config instanceof DummyArithmeticConfiguration){
                SMPC<DummyArithmeticResourcePool, ProtocolBuilderNumeric, Auction.AuctionResult> smpc =
                        new SMPC<>(config, application, networkConfig);
                task = () -> smpc.startComputation(Duration.ofMinutes(5));
            }else if(config instanceof SpdzConfiguration){
                SMPC<SpdzResourcePool, ProtocolBuilderNumeric, Auction.AuctionResult> smpc =
                        new SMPC<>(config, application, networkConfig);
                task = () -> smpc.startComputation(Duration.ofMinutes(5));
            }
            clients.add(task);
        }
        return clients;
    }

    /**
     * Provides party inputs and the expected results.<br>
     * Order: array of bids, partyId of the expected winner, expected final price
     * @return party inputs with expected results.
     */
    private static List<Arguments> data(){
        return List.of(
                Arguments.of(new int[]{1, 42}, 2, 42),
                Arguments.of(new int[]{42, 1}, 1, 42),
                Arguments.of(new int[]{Integer.MAX_VALUE, 1}, 1, Integer.MAX_VALUE),
                Arguments.of(new int[]{Integer.MAX_VALUE, 42, 1}, 1, Integer.MAX_VALUE),
                Arguments.of(new int[]{1, 2, 1}, 2, 2),
                Arguments.of(new int[]{1, 2, 42, 41}, 3, 42),
                Arguments.of(new int[]{42, 42, 42, 1}, 3, 42),
                Arguments.of(new int[]{41, 43, 42, 1}, 2, 43),
                Arguments.of(new int[]{41, 43, 42, 43, 1}, 4, 43),
                Arguments.of(new int[]{14, 15, 12, 10, 13, 20}, 6, 20),
                Arguments.of(new int[]{14, 15, 12, 10, 13, 20, 17}, 6, 20),
                Arguments.of(new int[]{14, 15, 12, 10, 13, 20, 17, 21}, 8, 21),
                Arguments.of(new int[]{25, 14, 15, 12, 10, 13, 20, 17, 21}, 1, 25),
                Arguments.of(new int[]{25, 14, 15, 12, 50, 10, 13, 20, 17, 21}, 5, 50)
        );
    }

    /**
     * Provides party inputs and the expected results (should be used in tests, in which mascot is used).<br>
     * Order: array of bids, partyId of the expected winner, expected final price
     * @return party inputs with expected results.
     */
    private static List<Arguments> mascotData(){
        return List.of(
                Arguments.of(new int[]{1, 42}, 2, 42),
                Arguments.of(new int[]{42, 1, 25}, 1, 42)
        );
    }

    @BeforeEach
    void setUp() {
        executorService = Executors.newFixedThreadPool(THREAD_POOL);
    }

    @AfterEach
    void tearDown() {
        executorService.shutdownNow();
        executorService = null;
    }

    /**
     * Tests correctness of the auction implementation with {@link Utils.ProtocolSuite#DummyArithmetic}.
     * Party inputs and expected results are provided by {@link #data()}.
     * @param bids  party inputs which should be tested.
     * @param winnerId expected partyId of the highest bidder.
     * @param finalPrice expected final price which should be payed by the winner party.
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @ParameterizedTest
    @MethodSource("data")
    public void testDummyArithmeticSuite(int[] bids, int winnerId, int finalPrice) throws InterruptedException, ExecutionException {
        ProtocolConfiguration config = DummyArithmeticConfiguration.builder()
                .maxBitLength(31)
                .build();
        List<Callable<Auction.AuctionResult>> clients = setUpParties(bids, config);
        List<Future<Auction.AuctionResult>> fResults = executorService.invokeAll(clients);
        assertAllPartiesGetSameResults(fResults, winnerId, finalPrice);
    }

    /**
     * Tests correctness of the auction implementation with {@link Utils.ProtocolSuite#Spdz} and
     * {@link Utils.Preprocessing#Dummy} as preprocessing strategy.
     * Party inputs and expected results are provided by {@link #data()}.
     * @param bids party inputs, which should be tested.
     * @param winnerId expected partyId of the highest bidder.
     * @param finalPrice expected final price, which should be payed by the winner party.
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @ParameterizedTest
    @MethodSource("data")
    public void testSpdzWithDummy(int[] bids, int winnerId, int finalPrice) throws InterruptedException, ExecutionException {
        ProtocolConfiguration config = SpdzConfiguration.builder()
                .preprocessingStrategy(Utils.Preprocessing.Dummy)
                .maxBitLength(31)
                .build();
        List<Callable<Auction.AuctionResult>> clients = setUpParties(bids, config);
        List<Future<Auction.AuctionResult>> results = executorService.invokeAll(clients);
        assertAllPartiesGetSameResults(results, winnerId, finalPrice);
    }

    /**
     * Tests correctness of the auction implementation with {@link Utils.ProtocolSuite#Spdz} and
     * {@link Utils.Preprocessing#Mascot} as preprocessing strategy.
     * @param bids party inputs, which should be tested.
     * @param winnerId expected partyId of the highest bidder.
     * @param finalPrice expected final price, which should be payed by the winner party.
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @ParameterizedTest
    @MethodSource("mascotData")
    public void testSpdzWithMascot(int[] bids, int winnerId, int finalPrice) throws InterruptedException, ExecutionException {
        ProtocolConfiguration config = SpdzConfiguration.builder()
                .preprocessingStrategy(Utils.Preprocessing.Mascot)
                .maxBitLength(31)
                .build();
        List<Callable<Auction.AuctionResult>> clients = setUpParties(bids, config);
        List<Future<Auction.AuctionResult>> results = executorService.invokeAll(clients);
        assertAllPartiesGetSameResults(results, winnerId, finalPrice);
    }
}
