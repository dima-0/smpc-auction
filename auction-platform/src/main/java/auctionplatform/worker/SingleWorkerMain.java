package auctionplatform.worker;

import frescoauction.auction.Auction;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SingleWorkerMain {
    public static void main(String[] args) throws IOException {
        ServerConfiguration config = ServerConfiguration.loadFromJson(args[0]);
        WorkerListener listener = new WorkerListener() {
            @Override
            public void onCompletion(Auction.AuctionResult result, int clientId) {
                System.out.println(String.format("Listener: Auction completed (Winner: clientId=%d (partyId=%d), final price=%d)",
                                clientId, result.getWinnerId(), result.getFinalPrice()));
            }

            @Override
            public void onError(String errorMessage) {
                System.out.println(String.format("Listener: Auction aborted (Error: %s)", errorMessage));
            }
        };
        ExecutorService es = Executors.newSingleThreadExecutor();
        ServerWorker server = new ServerWorker(config, listener);
        es.submit(server);
        es.shutdown();
    }
}
