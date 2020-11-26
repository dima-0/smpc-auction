package com.db.auctionclient.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.db.auctionclient.model.db.AppDatabase;
import com.db.auctionclient.model.entities.AuctionWithTask;
import com.db.auctionclient.model.AuctionRepository;
import com.db.auctionclient.model.DBAuctionRepository;

import java.util.List;

/**
 * View model, which is used by {@link com.db.auctionclient.view.runningauctions.RunningAuctionsFragment} to retrieve data.
 */
public class RunningAuctionsViewModel extends AndroidViewModel {
    private AuctionRepository repository;

    public RunningAuctionsViewModel(@NonNull Application application) {
        super(application);
        repository = new DBAuctionRepository(AppDatabase.getInstance(application));
    }

    /**
     * Retrieves a list of auctions with the corresponding tasks as an observable. The observers will be notified
     * about changes (asynchronous).
     * @return a list of auctions with the corresponding tasks as an observable.
     */
    public LiveData<List<AuctionWithTask>> getAllAuctionWithTasks(){
        return repository.getAllAuctionsWithTasks();
    }
}
