package frescoauction.auction;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import frescoauction.util.Utils;

import static frescoauction.auction.TestUtils.getNetworkConfiguration;

/**
 * Testing of {@link AuctionFactory}.
 */
public class AuctionFactoryTest {

    /**
     * Tests {@link AuctionFactory#getAuction(Utils.AuctionType, int, NetworkConfiguration)}
     * with {@link Utils.AuctionType#SealedFirstPrice}.
     */
    @Test
    public void testGetAuctionFirstPrice(){
        NetworkConfiguration networkConfiguration = getNetworkConfiguration(1, 2);
        Utils.AuctionType type = Utils.AuctionType.SealedFirstPrice;
        Application auction = AuctionFactory.getAuction(type, 1, networkConfiguration);
        Assertions.assertTrue(auction instanceof SealedFirstPriceAuction);
    }

    /**
     * Tests {@link AuctionFactory#getAuction(Utils.AuctionType, int, NetworkConfiguration)}
     * with {@link Utils.AuctionType#SealedSecondPrice}.
     */
    @Test
    public void testGetAuctionSecondPrice(){
        NetworkConfiguration networkConfiguration = getNetworkConfiguration(1, 2);
        Utils.AuctionType type = Utils.AuctionType.SealedSecondPrice;
        Application auction = AuctionFactory.getAuction(type, 1, networkConfiguration);
        Assertions.assertTrue(auction instanceof SealedSecondPriceAuction);
    }
}
