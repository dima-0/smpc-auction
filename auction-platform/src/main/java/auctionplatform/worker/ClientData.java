package auctionplatform.worker;


import lombok.Getter;
import lombok.Setter;

/**
 * Contains data of the client.
 */
@Getter
@Setter
public class ClientData {
    /** Id of the client.*/
    private final int clientId;
    /** Party id of the client. (-1 if no party id was assigned).*/
    private int frescoPartyId = -1;
    /** Corresponding client-worker is ready for the auction evaluation.*/
    private boolean isReadyForAuctionEvaluation = false;
    /** Port, which is specified by the client. (-1 if no port was specified).*/
    private int frescoPort = -1;

    /**
     * @param clientId id of the client.
     */
    public ClientData(int clientId) {
        this.clientId = clientId;
    }

    /**
     * Checks, if the client-worker has already provided a fresco port.
     * @return true, if the fresco port is bigger than -1 or false otherwise.
     */
    public boolean frescoPortAvailable(){
        return frescoPort > -1;
    }

}
