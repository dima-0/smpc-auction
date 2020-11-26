package auctionplatform.worker;

import frescoauction.auction.Auction;

/**
 * Listens for events, which can occur during the auction.
 */
public interface WorkerListener {
    /**
     * Notifies the listener about successful completion of the auction.
     * @param result result of the auction evaluation.
     * @param clientId id of the client, who won the auction.
     */
    public void onCompletion(Auction.AuctionResult result, int clientId);

    /**
     * Notifies the listener about an error, which occurred during the auction.
     * @param errorMessage error message.
     */
    public void onError(String errorMessage);
}
