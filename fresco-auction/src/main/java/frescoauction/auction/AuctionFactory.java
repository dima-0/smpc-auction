package frescoauction.auction;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import frescoauction.util.Utils;

/**
 * Contains helper methods for constructing auctions.
 */
public class AuctionFactory {

    /**
     * Constructs a fresco application of the corresponding auction.
     * @param auctionType auction type.
     * @param bid secret bid (should not be lower than 1).
     * @param networkConfiguration network configuration (number of parties should not be lower than 2).
     * @return a new instance of the fresco application.
     */
    public static Application<Auction.AuctionResult, ProtocolBuilderNumeric> getAuction(Utils.AuctionType auctionType, int bid, NetworkConfiguration networkConfiguration){
        Auction auction = null;
        switch (auctionType){
            case SealedFirstPrice:
                auction = new SealedFirstPriceAuction(bid, networkConfiguration);
                break;
            case SealedSecondPrice:
                auction = new SealedSecondPriceAuction(bid, networkConfiguration);
                break;
            case CalcSum:
                auction = new CalcSum(bid, networkConfiguration);
                break;
        }
        return auction;
    }

}
