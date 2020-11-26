package frescoauction.auction;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import lombok.Getter;

/**
 * A fresco application for running secure auctions.
 * Concrete auction types should implement {@link Application#buildComputation(ProtocolBuilder)}.
 * The result of the auction should be returned as {@link AuctionResult}.
 */
public abstract class Auction implements Application<Auction.AuctionResult, ProtocolBuilderNumeric> {
    /** Secret bid provided by the user.*/
    protected final int secretBid;
    /** Network configuration used by the fresco application. */
    protected final NetworkConfiguration networkConfiguration;

    /**
     * @param secretBid secret bid (should not be lower than 1).
     * @param networkConfiguration network configuration (number of parties should not be lower than 2).
     * @throws IllegalArgumentException is thrown if secretBid is lower than 1 or number of parties is lower than 2.
     */
    public Auction(int secretBid, NetworkConfiguration networkConfiguration) throws IllegalArgumentException{
        if(secretBid < 1) throw new IllegalArgumentException("Bid cannot be lower than 1.");
        if(networkConfiguration.noOfParties() < 2) throw new IllegalArgumentException("There must be at least 2 parties.");
        this.secretBid = secretBid;
        this.networkConfiguration = networkConfiguration;
    }

    /**
     * Contains the computed result of the auction.
     */
    public final static class AuctionResult {
        /** PartyId of the highest bidder.*/
        @Getter private final int winnerId;
        /** Final price, which the highest bidder has to pay.*/
        @Getter private final int finalPrice;

        /**
         * @param winnerId partyId of the highest bidder.
         * @param finalPrice final price, which the highest bidder has to pay.
         */
        public AuctionResult(int winnerId, int finalPrice) {
            this.winnerId = winnerId;
            this.finalPrice = finalPrice;
        }
    }
}
