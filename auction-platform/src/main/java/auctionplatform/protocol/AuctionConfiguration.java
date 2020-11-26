package auctionplatform.protocol;


import frescoauction.util.Utils;

/**
 * A message, which contains the configuration of an auction and is sent by a server-worker to a client-worker.
 * It contains all the necessary data to start a {@link frescoauction.util.AuctionEvaluation}.
 */
public class AuctionConfiguration {
    /** Party id, which was assigned to the client-worker.*/
    public int partyId;
    /** Type of the auction which should be evaluated.*/
    public Utils.AuctionType auctionType;
    /** Protocol suite, which should be used for the auction evaluation.*/
    public Utils.ProtocolSuite protocolSuite;
    /** Preprocessing strategy, which should be used, if protocol suite is set to Spdz.*/
    public Utils.Preprocessing preprocessing;
    /** Connection data of all parties (structure of an entry: [partyId]:[ip]:[port]).*/
    public String[] connectionData;

    public AuctionConfiguration() {
    }

    public AuctionConfiguration(int partyId, Utils.AuctionType auctionType, Utils.ProtocolSuite protocolSuite,
                                Utils.Preprocessing preprocessing, String[] connectionData) {
        this.partyId = partyId;
        this.auctionType = auctionType;
        this.protocolSuite = protocolSuite;
        this.preprocessing = preprocessing;
        this.connectionData = connectionData;
    }
}
