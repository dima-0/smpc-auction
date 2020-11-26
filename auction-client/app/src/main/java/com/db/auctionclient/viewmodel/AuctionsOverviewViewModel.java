package com.db.auctionclient.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.db.auctionclient.model.db.AppDatabase;
import com.db.auctionclient.model.entities.Auction;
import com.db.auctionclient.model.AuctionRepository;
import com.db.auctionclient.model.DBAuctionRepository;

import java.util.List;

/**
 * View model, which is used by {@link com.db.auctionclient.view.auctionsoverview.AuctionsOverviewFragment} to retrieve data.
 */
public class AuctionsOverviewViewModel extends AndroidViewModel {
    private AuctionRepository auctionRepository;

    public AuctionsOverviewViewModel(@NonNull Application application) {
        super(application);
        AppDatabase appDatabase = AppDatabase.getInstance(application);
        auctionRepository = new DBAuctionRepository(appDatabase);
    }

    /**
     * Retrieves a list of auctions as an observable. The observers will be notified
     * about changes (asynchronous).
     * @return a list of auctions as an observable.
     */
    public LiveData<List<Auction>> getAllAuctions(){
        return auctionRepository.getAllAuctions();
    }
}
