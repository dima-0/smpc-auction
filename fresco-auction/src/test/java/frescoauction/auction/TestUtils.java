package frescoauction.auction;

import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkConfigurationImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Provides helper methods for setting up and running auction tests.
 */
public class TestUtils {

    /**
     * Sets up a network configuration, which can be used in a fresco application.
     * @param myPartyId id of the party, which should own the configuration.
     * @param numberOfParties number of parties, who participate in the computation.
     * @return network configuration.
     */
    protected static NetworkConfiguration getNetworkConfiguration(int myPartyId, int numberOfParties){
        Map<Integer, Party> parties = new HashMap<>();
        for(int i = 0; i < numberOfParties; i++){
            int partyId = i + 1;
            int port = 5000 + i;
            parties.put(partyId, new Party(partyId, "localhost", port));
        }
        return new NetworkConfigurationImpl(myPartyId, parties);
    }

    /**
     * Asserts that all parties get the same result after running the fresco application.
     * @param parties future results of all parties.
     * @param winnerId expected partyId of the highest bidder.
     * @param finalPrice expected final price which should be payed by the winner party.
     * @throws ExecutionException
     * @throws InterruptedException
     */
    protected static void assertAllPartiesGetSameResults(List<Future<Auction.AuctionResult>> parties,
                                                         int winnerId, int finalPrice) throws ExecutionException, InterruptedException {
        for (Future<Auction.AuctionResult> fResult : parties){
            Auction.AuctionResult result = fResult.get();
            assertEquals(winnerId, result.getWinnerId());
            assertEquals(finalPrice, result.getFinalPrice());
        }
    }
}
