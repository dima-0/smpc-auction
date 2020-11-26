package frescoauction.auction;

import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import frescoauction.util.AuctionEvaluation;
import frescoauction.util.Utils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static frescoauction.auction.TestUtils.assertAllPartiesGetSameResults;
import static frescoauction.auction.TestUtils.getNetworkConfiguration;

/**
 * Testing the auction evaluation with {@link AuctionEvaluation}.
 */
public class AuctionEvaluationTest {
    private final static int THREAD_POOL = 3;
    private ExecutorService executorService;

    /**
     * Sets up parties for an auction.
     * @param bids bids, which should be used as input by the parties.
     * @param auctionType auction format, which should be run.
     * @param protocolSuite protocol suite, which should be used for the auction evaluation.
     * @param preprocessing pre processing strategy for the offline phase of the Spdz protocol suite.
     * @return parties as {@link Callable}, which can be run by an {@link ExecutorService}.
     */
    private static List<Callable<Auction.AuctionResult>> setUpParties(int[] bids, Utils.AuctionType auctionType,
                                                                      Utils.ProtocolSuite protocolSuite, Utils.Preprocessing preprocessing){
        List<Callable<Auction.AuctionResult>> clients = new ArrayList<>();
        for (int i = 0; i < bids.length; i++) {
            int bid = bids[i];
            int partyId = i + 1;
            NetworkConfiguration networkConfig = getNetworkConfiguration(partyId, bids.length);
            AuctionEvaluation task = new AuctionEvaluation(auctionType, bid,
                    protocolSuite, preprocessing, networkConfig);
            clients.add(task);
        }
        return clients;
    }

    /**
     * Provides party inputs and the expected results for {@link Utils.AuctionType#SealedFirstPrice} and
     * {@link Utils.AuctionType#SealedFirstPrice}. <br>
     * Order: auction type, array of bids, partyId of the expected winner, expected final price
     * @return party inputs with expected results.
     */
    private static List<Arguments> data(){
        return List.of(
                Arguments.of(Utils.AuctionType.SealedFirstPrice, new int[]{1, 1}, 2, 1),
                Arguments.of(Utils.AuctionType.SealedFirstPrice, new int[]{1, 42}, 2, 42),
                Arguments.of(Utils.AuctionType.SealedFirstPrice, new int[]{42, 1}, 1, 42),
                Arguments.of(Utils.AuctionType.SealedFirstPrice, new int[]{Integer.MAX_VALUE, 1}, 1, Integer.MAX_VALUE),
                Arguments.of(Utils.AuctionType.SealedFirstPrice, new int[]{Integer.MAX_VALUE, 42, 1}, 1, Integer.MAX_VALUE),
                Arguments.of(Utils.AuctionType.SealedSecondPrice, new int[]{1, 1}, 2, 1),
                Arguments.of(Utils.AuctionType.SealedSecondPrice, new int[]{1, 42}, 2, 1),
                Arguments.of(Utils.AuctionType.SealedSecondPrice, new int[]{42, 1}, 1, 1),
                Arguments.of(Utils.AuctionType.SealedSecondPrice, new int[]{Integer.MAX_VALUE, 1}, 1, 1),
                Arguments.of(Utils.AuctionType.SealedSecondPrice, new int[]{Integer.MAX_VALUE, 42, 1}, 1, 42)
        );
    }

    /**
     * Provides party inputs and the expected results for {@link Utils.AuctionType#SealedFirstPrice} and
     * {@link Utils.AuctionType#SealedSecondPrice}. Should be only used for tests, which use mascot as
     * preprocessing strategy.<br>
     * Order: auction type, array of bids, partyId of the expected winner, expected final price
     * @return party inputs with expected results.
     */
    private static List<Arguments> dataMascot(){
        return List.of(
                Arguments.of(Utils.AuctionType.SealedFirstPrice, new int[]{1, Integer.MAX_VALUE}, 2, Integer.MAX_VALUE),
                Arguments.of(Utils.AuctionType.SealedSecondPrice, new int[]{1, Integer.MAX_VALUE}, 2, 1)
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
     * Tests the auction evaluation with {@link Utils.ProtocolSuite#DummyArithmetic}.
     * Party inputs and expected results are provided by {@link #data()}.
     * @param auctionType auction type.
     * @param bids  party inputs which should be tested.
     * @param winnerId expected partyId of the highest bidder.
     * @param finalPrice expected final price which should be payed by the winner party.
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @ParameterizedTest
    @MethodSource("data")
    public void testRunAuctionWithDummyArithmetic(Utils.AuctionType auctionType, int[] bids, int winnerId, int finalPrice)
            throws InterruptedException, ExecutionException {
        List<Callable<Auction.AuctionResult>> clients = setUpParties(bids, auctionType,
                Utils.ProtocolSuite.DummyArithmetic, null);
        List<Future<Auction.AuctionResult>> results = executorService.invokeAll(clients);
        assertAllPartiesGetSameResults(results, winnerId, finalPrice);
    }

    /**
     * Tests the auction evaluation with {@link Utils.ProtocolSuite#Spdz} and {@link Utils.Preprocessing#Dummy}.
     * Party inputs and expected results are provided by {@link #data()}.
     * @param auctionType auction type.
     * @param bids  party inputs which should be tested.
     * @param winnerId expected partyId of the highest bidder.
     * @param finalPrice expected final price which should be payed by the winner party.
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @ParameterizedTest
    @MethodSource("data")
    public void testRunAuctionWithSpdzDummy(Utils.AuctionType auctionType, int[] bids, int winnerId, int finalPrice)
            throws InterruptedException, ExecutionException {
        List<Callable<Auction.AuctionResult>> clients = setUpParties(bids, auctionType,
                Utils.ProtocolSuite.Spdz, Utils.Preprocessing.Dummy);
        List<Future<Auction.AuctionResult>> results = executorService.invokeAll(clients);
        assertAllPartiesGetSameResults(results, winnerId, finalPrice);
    }

    /**
     * Tests the auction evaluation with {@link Utils.ProtocolSuite#Spdz} and {@link Utils.Preprocessing#Mascot}.
     * Party inputs and expected results are provided by {@link #dataMascot()}.
     * @param auctionType auction type.
     * @param bids  party inputs which should be tested.
     * @param winnerId expected partyId of the highest bidder.
     * @param finalPrice expected final price which should be payed by the winner party.
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @ParameterizedTest
    @MethodSource("dataMascot")
    public void testRunAuctionWithSpdzMascot(Utils.AuctionType auctionType, int[] bids, int winnerId, int finalPrice) throws InterruptedException, ExecutionException {
        List<Callable<Auction.AuctionResult>> clients = setUpParties(bids, auctionType,
                Utils.ProtocolSuite.Spdz, Utils.Preprocessing.Mascot);
        List<Future<Auction.AuctionResult>> results = executorService.invokeAll(clients);
        assertAllPartiesGetSameResults(results, winnerId, finalPrice);
    }
}
