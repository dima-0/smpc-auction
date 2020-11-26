package com.db.auctionclient.model;

import androidx.lifecycle.LiveData;

import com.db.auctionclient.model.db.AppDatabase;
import com.db.auctionclient.model.db.AuctionDao;
import com.db.auctionclient.model.db.AuctionTaskDao;
import com.db.auctionclient.model.entities.Auction;
import com.db.auctionclient.model.entities.AuctionTask;
import com.db.auctionclient.model.entities.AuctionWithTask;

import java.util.List;

/**
 * Implementation of {@link AuctionRepository}, which obtains data from a Room database.
 */
public class DBAuctionRepository implements AuctionRepository{
    /** Provides access to the auction table.*/
    private AuctionDao auctionDao;
    /** Provides access to the auction task table.*/
    private AuctionTaskDao auctionTaskDao;

    public DBAuctionRepository(AppDatabase appDatabase) {
        this.auctionDao = appDatabase.auctionDao();
        this.auctionTaskDao = appDatabase.auctionTaskDao();
    }

    @Override
    public LiveData<List<Auction>> getAllAuctions() {
        return auctionDao.getAllAuctions();
    }

    @Override
    public LiveData<Auction> getAuctionById(int auctionId) {
        return auctionDao.getAuctionById(auctionId);
    }

    @Override
    public LiveData<AuctionWithTask> getAuctionWithTaskById(int auctionId) {
        return auctionDao.getAuctionWithTaskById(auctionId);
    }

    @Override
    public void addAuctionTask(AuctionTask task) {
        AppDatabase.executorService
                .execute(() -> auctionTaskDao.addAuctionTask(task));
    }

    @Override
    public void updateAuctionTask(AuctionTask task) {
        AppDatabase.executorService
                .execute(() -> auctionTaskDao.updateAuctionTask(task));
    }

    @Override
    public LiveData<List<AuctionWithTask>> getAllAuctionsWithTasks() {
        return auctionDao.getAuctionsWithTasks();
    }
}
