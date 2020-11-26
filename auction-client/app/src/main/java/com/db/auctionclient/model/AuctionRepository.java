package com.db.auctionclient.model;

import androidx.lifecycle.LiveData;

import com.db.auctionclient.model.entities.Auction;
import com.db.auctionclient.model.entities.AuctionTask;
import com.db.auctionclient.model.entities.AuctionWithTask;

import java.util.List;

/**
 * Provides methods for database access.
 */
public interface AuctionRepository {
    /**
     * Retrieves a list of auctions as an observable. The observers will be notified
     * about changes (asynchronous).
     * @return a list of auctions as an observable.
     */
    public LiveData<List<Auction>> getAllAuctions();

    /**
     * Retrieves an auction as an observable. The observers will be notified
     * about changes (asynchronous).
     * @return an auction as an observable.
     */
    public LiveData<Auction> getAuctionById(int auctionId);

    /**
     * Retrieves an auction with the corresponding task as an observable. The observers will be notified
     * about changes (asynchronous).
     * @param auctionId id of the auction.
     * @return an auction with the corresponding task as an observable.
     */
    public LiveData<AuctionWithTask> getAuctionWithTaskById(int auctionId);

    /**
     * Inserts an auction task into the database.
     * @param task auction task.
     */
    public void addAuctionTask(AuctionTask task);

    /**
     * Updates an auction task.
     * @param task auction task, which should be updated.
     */
    public void updateAuctionTask(AuctionTask task);

    /**
     * Retrieves a list of auctions with the corresponding tasks as an observable. The observers will be notified
     * about changes (asynchronous).
     * @return a list of auctions with the corresponding tasks as an observable.
     */
    public LiveData<List<AuctionWithTask>> getAllAuctionsWithTasks();
}
