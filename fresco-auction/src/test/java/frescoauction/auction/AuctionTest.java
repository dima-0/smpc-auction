package frescoauction.auction;

import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testing the construction of an auction.
 */
public class AuctionTest {

    /**
     * Tests construction of an action with more than two parties. Both
     * {@link SealedFirstPriceAuction} and {@link SealedSecondPriceAuction} are tested.
     * @param numberOfParties the number of parties.
     */
    @ParameterizedTest
    @ValueSource(ints = {2, 5, 10, 42, 100})
    public void testMoreThanTwoPartiesWithCorrectInput(int numberOfParties){
        int myId = 1;
        int myInput = 42;
        NetworkConfiguration networkConfiguration = TestUtils.getNetworkConfiguration(myId, numberOfParties);
        assertDoesNotThrow(() -> new SealedFirstPriceAuction(myInput, networkConfiguration));
        assertDoesNotThrow(() -> new SealedSecondPriceAuction(myInput, networkConfiguration));
    }

    /**
     * Tests construction of an action with a bid, which is higher than zero. Both
     * {@link SealedFirstPriceAuction} and {@link SealedSecondPriceAuction} are tested.
     * @param input bid.
     */
    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10, 42, Integer.MAX_VALUE})
    public void testTwoPartiesWithInputHigherThanZero(int input){
        int myId = 1;
        int numberOfParties = 2;
        NetworkConfiguration networkConfiguration = TestUtils.getNetworkConfiguration(myId, numberOfParties);
        assertDoesNotThrow(() -> new SealedFirstPriceAuction(input, networkConfiguration));
        assertDoesNotThrow(() -> new SealedSecondPriceAuction(input, networkConfiguration));
    }

    /**
     * Tests construction of an auction with a single party. Both
     * {@link SealedFirstPriceAuction} and {@link SealedSecondPriceAuction} are tested.
     */
    @Test
    public void testSinglePartyException(){
        int myId = 1;
        int myInput = 42;
        int numberOfParties = 1;
        NetworkConfiguration networkConfiguration = TestUtils.getNetworkConfiguration(myId, numberOfParties);
        assertThrows(IllegalArgumentException.class, () -> new SealedFirstPriceAuction(myInput, networkConfiguration));
        assertThrows(IllegalArgumentException.class, () -> new SealedSecondPriceAuction(myInput, networkConfiguration));
    }

    /**
     * Tests construction of an auction with a bid, which is lower than 1.Both
     * {@link SealedFirstPriceAuction} and {@link SealedSecondPriceAuction} are tested.
     * @param input bid.
     */
    @ParameterizedTest
    @ValueSource(ints = {Integer.MIN_VALUE, -42, -1, 0})
    public void testNonPositiveInputException(int input){
        int myId = 1;
        int numberOfParties = 2;
        NetworkConfiguration networkConfiguration = TestUtils.getNetworkConfiguration(myId, numberOfParties);
        assertThrows(IllegalArgumentException.class, () -> new SealedFirstPriceAuction(input, networkConfiguration));
        assertThrows(IllegalArgumentException.class, () -> new SealedSecondPriceAuction(input, networkConfiguration));
    }
}
