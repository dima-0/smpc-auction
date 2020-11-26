package auctionplatform.worker;

import com.google.gson.Gson;
import lombok.Getter;
import frescoauction.util.Utils;

import java.io.FileReader;
import java.io.IOException;

/**
 * Contains a set of parameters for configuring a {@link ServerWorker}.
 */
@Getter
public class ServerConfiguration {
    /** Id of the auction.*/
    private int auctionId;
    /** Starting price of the auction.*/
    private int startingPrice;
    /** Auction type, which should be carried out by the server-worker.*/
    private Utils.AuctionType auctionType;
    /** Duration of the registration state (seconds).*/
    private int registrationDuration;
    /** Duration of the smpc-set-up state (seconds).*/
    private int smpcSetUpDuration;
    /** Duration of the smpc-set-up-finish state (seconds).*/
    private int smpcSetUpFinishDuration;
    /** Duration of the closure state (seconds).*/
    private int closureDuration;
    /** IP address of the host.*/
    private String hostIp ;
    /** Port of the host (at which kryo-server should be running).*/
    private int hostPort;
    /** Port, which should be used during the auction evaluation (fresco application).*/
    private int frescoPort;
    /** Protocol suite, which should be used for the action evaluation.*/
    private Utils.ProtocolSuite protocolSuite;
    /** Pre processing strategy, which should be used, if the protocol suite set to spdz.*/
    private Utils.Preprocessing preprocessing;

    /**
     * @param auctionId id of the auction.
     * @param startingPrice starting price of the auction.
     * @param auctionType auction type, which should be carried out by the server-worker.
     * @param registrationDuration duration of the registration state (seconds).
     * @param closureDuration duration of the closure state(seconds).
     * @param hostIp IP address of the host.
     * @param hostPort port of the host (at which kryo-server should be running).
     * @param frescoPort port, which should be used during the auction evaluation (fresco application).
     * @param smpcSetUpDuration duration of the smpc-set-up state (seconds).
     * @param smpcSetUpFinishDuration duration of the smpc-set-up-finish state (seconds).
     * @param protocolSuite protocol suite, which should be used for the action evaluation.
     * @param preprocessing pre processing strategy, which should be used, if the protocol suite set to spdz.
     */
    public ServerConfiguration(int auctionId, int startingPrice, Utils.AuctionType auctionType,
                               int registrationDuration, int closureDuration,
                               String hostIp, int hostPort, int frescoPort,
                               int smpcSetUpDuration, int smpcSetUpFinishDuration,
                               Utils.ProtocolSuite protocolSuite, Utils.Preprocessing preprocessing) {
        this.auctionId = auctionId;
        this.startingPrice = startingPrice;
        this.auctionType = auctionType;
        this.registrationDuration = registrationDuration;
        this.closureDuration = closureDuration;
        this.hostIp = hostIp;
        this.hostPort = hostPort;
        this.frescoPort = frescoPort;
        this.smpcSetUpDuration = smpcSetUpDuration;
        this.smpcSetUpFinishDuration = smpcSetUpFinishDuration;
        this.protocolSuite = protocolSuite;
        this.preprocessing = preprocessing;
    }

    /**
     * Loads the configuration from a json file.
     * @param path path to the json file.
     * @return an instance of {@link ServerConfiguration}.
     * @throws IOException
     */
    public static ServerConfiguration loadFromJson(String path) throws IOException {
        Gson gson = new Gson();
        FileReader reader = new FileReader(path);
        ServerConfiguration config = gson.fromJson(reader, ServerConfiguration.class);
        reader.close();
        return config;
    }
}
