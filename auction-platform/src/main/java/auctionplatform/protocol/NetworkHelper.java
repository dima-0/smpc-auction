package auctionplatform.protocol;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;
import frescoauction.util.Utils;

/**
 * Helper class for setting up the message protocol.
 */
public class NetworkHelper {

    /**
     * Registers messages, which will be sent during communication between kryo-server and kryo-clients.
     * @param endPoint kryo server/client.
     */
    public static void register(EndPoint endPoint){
        Kryo kryo = endPoint.getKryo();
        kryo.register(String[].class);
        kryo.register(Utils.AuctionType.class);
        kryo.register(Utils.Preprocessing.class);
        kryo.register(Utils.ProtocolSuite.class);
        kryo.register(Register.class);
        kryo.register(RequestConnectionData.class);
        kryo.register(ResponseConnectionData.class);
        kryo.register(AuctionConfiguration.class);
        kryo.register(AuctionReady.class);
        kryo.register(RequestAuctionStart.class);
        kryo.register(ResponseAuctionStart.class);
    }
}
