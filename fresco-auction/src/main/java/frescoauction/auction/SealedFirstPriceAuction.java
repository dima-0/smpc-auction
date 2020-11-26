package frescoauction.auction;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumeric;
import dk.alexandra.fresco.framework.builder.numeric.Comparison;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the sealed first price auction,
 * in which the highest bidder pays the price of the highest bid (his own bid).
 * In case there is more than one highest bidder, than the bidder with the highest partyId wins.
 */
public class SealedFirstPriceAuction extends Auction{

    /**
     * @param secretBid secret bid (should not be lower than 1).
     * @param networkConfiguration network configuration (number of parties should not be lower than 2).
     */
    public SealedFirstPriceAuction(int secretBid, NetworkConfiguration networkConfiguration) {
        super(secretBid, networkConfiguration);
    }

    @Override
    public DRes<AuctionResult> buildComputation(ProtocolBuilderNumeric builder) {
        return builder.seq(seq -> {
            Numeric numeric = seq.numeric();
            List<Pair<DRes<SInt>, DRes<SInt>>> closedInputs = new ArrayList<>(networkConfiguration.noOfParties());
            // close inputs and gather inputs of other parties
            for (int otherId = 1; otherId <= networkConfiguration.noOfParties(); otherId++) {
                if (otherId == networkConfiguration.getMyId()) {
                    DRes<SInt> myPartyId = numeric.input(networkConfiguration.getMyId(), networkConfiguration.getMyId());
                    DRes<SInt> mySecretBid = numeric.input(this.secretBid, networkConfiguration.getMyId());
                    closedInputs.add(new Pair<>(myPartyId, mySecretBid));
                } else {
                    DRes<SInt> otherPartyId = numeric.input(null, otherId);
                    DRes<SInt> otherSecretBid = numeric.input(null, otherId);
                    closedInputs.add(new Pair<>(otherPartyId, otherSecretBid));
                }
            }
            return () -> closedInputs;
        }).seq((seq, inputs) -> {
            AdvancedNumeric advancedNumeric = seq.advancedNumeric();
            Comparison comparison = seq.comparison();
            // party id of the highest bidder
            DRes<SInt> highBidderId = inputs.get(0).getFirst();
            // highest bid
            DRes<SInt> highBid = inputs.get(0).getSecond();
            // find highest bid and the party id of the corresponding bidder
            for (int i = 1; i < inputs.size(); i++) {
                DRes<SInt> inputPartyId = inputs.get(i).getFirst();
                DRes<SInt> inputBid = inputs.get(i).getSecond();
                DRes<SInt> highBidIsLowerEquals = comparison.compareLEQ(highBid, inputBid);
                highBid = advancedNumeric.condSelect(highBidIsLowerEquals, inputBid, highBid);
                highBidderId = advancedNumeric.condSelect(highBidIsLowerEquals, inputPartyId, highBidderId);
            }
            Pair<DRes<SInt>, DRes<SInt>> resultPair = new Pair(highBidderId, highBid);
            return () -> resultPair;
        }).seq((seq, resultPair) -> {
            Numeric numeric = seq.numeric();
            // open results for all parties
            DRes<BigInteger> openedHighBidderId = numeric.open(resultPair.getFirst());
            DRes<BigInteger> openedHighBid = numeric.open(resultPair.getSecond());
            Pair<DRes<BigInteger>, DRes<BigInteger>> openedResultPair = new Pair<>(openedHighBidderId, openedHighBid);
            return () -> openedResultPair;
        }).seq((seq, openedResultPair) -> {
            return () -> new AuctionResult(openedResultPair.getFirst().out().intValue(), openedResultPair.getSecond().out().intValue());
        });
    }
}
