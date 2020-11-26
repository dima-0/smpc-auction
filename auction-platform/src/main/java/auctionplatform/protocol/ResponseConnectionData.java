package auctionplatform.protocol;

/**
 * The message should be sent by the client to the server as a response to {@link RequestConnectionData}.
 * A message, which is sent by a client-worker to the server-worker. It contains a port at which the client-worker
 * will be accepting connections (fresco application).
 */
public class ResponseConnectionData {
    /** Port, which is used by the client-worker during the auction evaluation (fresco application).*/
    public int frescoPort;

    public ResponseConnectionData() {
    }

    public ResponseConnectionData(int frescoPort) {
        this.frescoPort = frescoPort;
    }
}
