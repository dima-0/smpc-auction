package frescoauction.util;

/**
 * Contains enum classes of supported auction types, protocol suites and preprocessing strategies.
 */
public class Utils {
    /**
     * Supported auction types.
     */
    public enum AuctionType{
        /**
         * Sealed first price auction, which is associated with {@link frescoauction.auction.SealedFirstPriceAuction}.
         */
        SealedFirstPrice,

        /**
         * Sealed first price auction, which  associated with {@link frescoauction.auction.SealedSecondPriceAuction}.
         */
        SealedSecondPrice,

        /**
         * NOT AN AUCTION. Calculation of a sum, which is associated with {@link frescoauction.auction.CalcSum}.
         */
        CalcSum
    }

    /**
     * Supported configurations of protocol suites, which are provided by the fresco framework.
     */
    public enum ProtocolSuite{
        /**
         * Configuration associated with {@link dk.alexandra.fresco.suite.spdz.SpdzProtocolSuite}.
         */
        Spdz,

        /**
         * Configuration associated with {@link dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticProtocolSuite}.
         */
        DummyArithmetic
    }

    /**
     * Supported preprocessing strategies for {@link ProtocolSuite#Spdz}.
     */
    public enum Preprocessing{

        /**
         * Secure preprocessing strategy associated with {@link dk.alexandra.fresco.tools.mascot.Mascot}.
         */
        Mascot,

        /**
         * Not secure preprocessing strategy. Should only be used for testing purposes.
         */
        Dummy
    }
}
