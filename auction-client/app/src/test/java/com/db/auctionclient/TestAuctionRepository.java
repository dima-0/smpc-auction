package com.db.auctionclient;

import androidx.lifecycle.LiveData;

import com.db.auctionclient.model.entities.Auction;
import com.db.auctionclient.model.entities.AuctionTask;
import com.db.auctionclient.model.entities.AuctionWithTask;
import com.db.auctionclient.model.AuctionRepository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A map based database, which is used in tests.
 */
public class TestAuctionRepository implements AuctionRepository {
    private Map<Integer, AuctionTask> auctionTaskDatabase = new ConcurrentHashMap<>();

    @Override
    public LiveData<List<Auction>> getAllAuctions() {
        return null;
    }

    @Override
    public LiveData<Auction> getAuctionById(int auctionId) {
        return null;
    }

    @Override
    public LiveData<AuctionWithTask> getAuctionWithTaskById(int auctionId) {
        return null;
    }


    @Override
    public void addAuctionTask(AuctionTask task) {
        auctionTaskDatabase.put(task.getAuctionId(), task);
    }

    @Override
    public void updateAuctionTask(AuctionTask task) {
        auctionTaskDatabase.put(task.getAuctionId(), task);
    }

    public Map<Integer, AuctionTask> getAuctionTaskDatabase(){
        return auctionTaskDatabase;
    }

    @Override
    public LiveData<List<AuctionWithTask>> getAllAuctionsWithTasks() {
        return null;
    }
}
