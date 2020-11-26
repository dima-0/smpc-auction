package com.db.auctionclient.model.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.db.auctionclient.model.entities.AuctionTask;

import java.util.List;

/**
 * Provides access to the auction task table.
 */
@Dao
public interface AuctionTaskDao {

    /**
     * Inserts an auction task into the database.
     * @param auctionTask auction task.
     */
    @Insert
    public void addAuctionTask(AuctionTask auctionTask);

    /**
     * Updates an auction task.
     * @param auctionTask auction task, which should be updated.
     */
    @Update
    public void updateAuctionTask(AuctionTask auctionTask);

    /**
     * Retrieves an auction task as an observable. The observers will be notified
     * about changes (asynchronous).
     * @param auctionId id of the auction.
     * @return an auction task as an observable.
     */
    @Query("SELECT * FROM AuctionTask WHERE auctionId =:auctionId")
    public LiveData<AuctionTask> getAuctionTaskById(int auctionId);

    /**
     * Retrieves an auction task as an observable. The observers will be notified
     * about changes (asynchronous).
     * @return an auction task as an observable.
     */
    @Query("SELECT * FROM AuctionTask")
    public LiveData<List<AuctionTask>> getAllOngoingAuctionsTasks();

    /**
     * Removes all auction task from the database.
     */
    @Query("DELETE FROM AuctionTask")
    public void deleteAllAuctionTasks();


}
