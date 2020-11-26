package com.db.auctionclient;

import com.db.auctionclient.model.worker.WorkerListener;
import com.db.auctionclient.model.entities.AuctionTask;

/**
 * Worker listener, which is used in tests for retrieving the result of the auction evaluation.
 */
public class TestWorkerListener implements WorkerListener {
    private AuctionTask auctionTask;

    @Override
    public void onCompleteTask(AuctionTask task) {
        auctionTask = task;
    }

    public AuctionTask getAuctionTask() {
        return auctionTask;
    }
}
