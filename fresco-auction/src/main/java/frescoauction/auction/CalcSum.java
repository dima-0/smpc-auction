package frescoauction.auction;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.value.SInt;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * NOT AN AUCTION (This fresco application was created for testing purposes).
 * Calculates the sum of all input data. The result is stored in {@link AuctionResult#getFinalPrice()}.
 */
public class CalcSum extends Auction {

    /**
     * @param input user input.
     * @param networkConfiguration network configuration (number of parties should not be lower than 2).
     */
    public CalcSum(int input, NetworkConfiguration networkConfiguration) {
        super(input, networkConfiguration);
    }

    @Override
    public DRes<AuctionResult> buildComputation(ProtocolBuilderNumeric builder) {
        return builder.seq(seq -> {
            Numeric numeric = seq.numeric();
            List<DRes<SInt>> closedInputs = new ArrayList<>();
            for(int id = 1; id <= networkConfiguration.noOfParties(); id++){
                if (id == networkConfiguration.getMyId()) {
                    DRes<SInt> myInput = numeric.input(secretBid, networkConfiguration.getMyId());
                    closedInputs.add(myInput);
                } else {
                    DRes<SInt> otherInput = numeric.input(null, id);
                    closedInputs.add(otherInput);
                }
            }
            return () -> closedInputs;
        }).seq((seq, inputs) -> {
            DRes<SInt> sum = seq.advancedNumeric().sum(inputs);
            return () -> sum;
        }).seq((seq, sum) -> {
            DRes<BigInteger> openedSum = seq.numeric().open(sum);
            return () -> openedSum;
        }).seq((seq, r) -> {
            return () -> new AuctionResult(-1, r.out().intValue());
        });
    }
}
