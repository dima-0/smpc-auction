package auctionplatform.worker;

import frescoauction.auction.Auction;

/**
 * A mocked worker listener, which is used for testing {@link ServerWorker}.
 * The listener safes the result or the error message, in case one occurs.
 */
public class ServerWorkerTestListener implements WorkerListener{
    private int clientId;
    private Auction.AuctionResult result;
    private String errorMessage;

    protected Auction.AuctionResult getAuctionResult(){
        return result;
    }

    protected int getClientId(){
        return clientId;
    }

    protected boolean completedSuccessfully(){
        return result != null;
    }

    protected boolean errorOccurred(){
        return errorMessage != null;
    }

    protected String getErrorMessage(){return errorMessage;}

    @Override
    public void onCompletion(Auction.AuctionResult result, int clientId) {
        this.result = result;
        this.clientId = clientId;
    }

    @Override
    public void onError(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
