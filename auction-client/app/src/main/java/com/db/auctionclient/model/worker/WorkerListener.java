package com.db.auctionclient.model.worker;

import com.db.auctionclient.model.entities.AuctionTask;

/**
 * Listens for events, which can occur during the auction.
 */
public interface WorkerListener {

    /**
     * Notifies the listener about the completion of the auction.
     * @param task corresponding auction task, which contains the local state of the auction.
     */
    public void onCompleteTask(AuctionTask task);
}
