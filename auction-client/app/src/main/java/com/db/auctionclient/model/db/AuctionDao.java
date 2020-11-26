package com.db.auctionclient.model.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import com.db.auctionclient.model.entities.Auction;
import com.db.auctionclient.model.entities.AuctionWithTask;

import java.util.List;

/**
 * Provides access to the auction table.
 */
@Dao
public interface AuctionDao {

    /**
     * Inserts multiple auctions into the database.
     * @param auctions array of auctions.
     */
    @Insert
    public void addAuctions(Auction... auctions);

    /**
     * Retrieves a list of auctions as an observable. The observers will be notified
     * about changes (asynchronous).
     * @return a list of auctions as an observable.
     */
    @Query("SELECT * FROM Auction")
    public LiveData<List<Auction>> getAllAuctions();

    /**
     * Retrieves an auction as an observable. The observers will be notified
     * about changes (asynchronous).
     * @return an auction as an observable.
     */
    @Query("SELECT * FROM Auction WHERE id =:auctionId")
    public LiveData<Auction> getAuctionById(int auctionId);

    /**
     * Removes all auctions from the database.
     */
    @Query("DELETE FROM Auction")
    public void deleteAll();

    /**
     * Retrieves an auction with the corresponding task as an observable. The observers will be notified
     * about changes (asynchronous).
     * @param auctionId id of the auction.
     * @return an auction with the corresponding task as an observable.
     */
    @Transaction
    @Query("SELECT * FROM Auction WHERE id =:auctionId")
    public LiveData<AuctionWithTask> getAuctionWithTaskById(int auctionId);

    /**
     * Retrieves a list of auctions with the corresponding tasks as an observable. The observers will be notified
     * about changes (asynchronous).
     * @return a list of auctions with the corresponding tasks as an observable.
     */
    @Transaction
    @Query("SELECT * FROM Auction")
    public LiveData<List<AuctionWithTask>> getAuctionsWithTasks();
}
