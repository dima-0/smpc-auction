package frescoauction.util;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticResourcePool;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import frescoauction.auction.Auction;
import frescoauction.auction.AuctionFactory;
import frescoauction.configuration.ProtocolConfiguration;
import frescoauction.configuration.dummy.DummyArithmeticConfiguration;
import frescoauction.configuration.spdz.SpdzConfiguration;

import java.time.Duration;
import java.util.concurrent.Callable;

/**
 * A task for auction evaluation.
 * The max bit length of the arithmetic suites is set to 31.
 * The timeout of the computation is set to 2 hours.
 */
public class AuctionEvaluation implements Callable<Auction.AuctionResult> {
    /** Max bit length, which should be used by the suite.*/
    private static final int BIT_LENGTH = 31;
    /** Max duration of the evaluation (hours).*/
    private static final int TIMEOUT = 2;

    private final Utils.AuctionType auctionType;
    private final int bid;
    private final Utils.ProtocolSuite protocolSuite;
    private final Utils.Preprocessing preprocessing;
    private final NetworkConfiguration networkConfiguration;


    /**
     * @param auctionType auction type.
     * @param bid secret bid (should not be lower than 1).
     * @param preprocessing pre processing strategy.
     * @param networkConfiguration network configuration (number of parties should not be lower than 2).
     * @return an instance of {@link frescoauction.auction.Auction.AuctionResult} if the computation finishes
     * successfully or null otherwise.
     */
    public AuctionEvaluation(Utils.AuctionType auctionType, int bid,
                             Utils.ProtocolSuite protocolSuite, Utils.Preprocessing preprocessing,
                             NetworkConfiguration networkConfiguration) {
        this.auctionType = auctionType;
        this.bid = bid;
        this.protocolSuite = protocolSuite;
        this.preprocessing = preprocessing;
        this.networkConfiguration = networkConfiguration;
    }

    @Override
    public Auction.AuctionResult call() throws Exception {
        Application<Auction.AuctionResult, ProtocolBuilderNumeric> auction = AuctionFactory.getAuction(auctionType, bid, networkConfiguration);
        ProtocolConfiguration config = null;
        Auction.AuctionResult result = null;
        switch (protocolSuite){
            case Spdz:
                config = SpdzConfiguration.builder()
                        .preprocessingStrategy(preprocessing)
                        .maxBitLength(BIT_LENGTH)
                        .build();
                result = new SMPC<SpdzResourcePool, ProtocolBuilderNumeric, Auction.AuctionResult>(config, auction, networkConfiguration)
                        .startComputation(Duration.ofHours(TIMEOUT));
                break;
            case DummyArithmetic:
                config = DummyArithmeticConfiguration.builder()
                        .maxBitLength(BIT_LENGTH)
                        .build();
                result = new SMPC<DummyArithmeticResourcePool, ProtocolBuilderNumeric, Auction.AuctionResult>(config, auction, networkConfiguration)
                        .startComputation(Duration.ofHours(TIMEOUT));
                break;
            default:
                throw new IllegalArgumentException("Unsupported suite.");
        }
       return result;
    }
}
