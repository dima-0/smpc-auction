package auctionplatform.protocol;

/**
 * A Message, which is sent by a client-worker to a server-worker in order to register for an auction.
 */
public class Register {
    /** The Id of the client, who sends the message.*/
    public int clientId;

    public Register() {
    }

    public Register(int clientId) {
        this.clientId = clientId;
    }
}
