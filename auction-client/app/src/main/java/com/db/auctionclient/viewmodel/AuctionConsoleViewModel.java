package com.db.auctionclient.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.db.auctionclient.model.worker.GlobalSingleton;
import com.db.auctionclient.model.db.AppDatabase;
import com.db.auctionclient.model.entities.AuctionWithTask;
import com.db.auctionclient.model.AuctionRepository;
import com.db.auctionclient.model.worker.ClientMaster;
import com.db.auctionclient.model.DBAuctionRepository;

/**
 * View model, which is used by {@link com.db.auctionclient.view.AuctionConsoleFragment} to retrieve data
 * and communicate with {@link ClientMaster}.
 */
public class AuctionConsoleViewModel extends AndroidViewModel {
    private ClientMaster clientMaster;
    private AuctionRepository auctionRepository;

    public AuctionConsoleViewModel(@NonNull Application application) {
        super(application);
        AppDatabase database = AppDatabase.getInstance(getApplication());
        auctionRepository = new DBAuctionRepository(database);
        clientMaster = GlobalSingleton.getInstance();
    }

    /**
     * Requests the client-master to leave the auction with the given id.
     * @param auctionId id of, the auction, which should be leaved.
     */
    public void leaveAuction(int auctionId){
        clientMaster.leaveAuction(auctionId);
    }

    /**
     * Requests the client-master to change the bid in the auction
     * with the given id.
     * @param auctionId id of the auction.
     * @param bid new bid.
     */
    public void changeBid(int auctionId, int bid){
        clientMaster.changeBid(auctionId, bid);
    }

    /**
     * Retrieves an auction with the corresponding task as an observable. The observers will be notified
     * about changes (asynchronous).
     * @param auctionId id of the auction.
     * @return an auction with the corresponding task as an observable.
     */
    public LiveData<AuctionWithTask> getAuctionWithTaskById(int auctionId) {
        return auctionRepository.getAuctionWithTaskById(auctionId);
    }
}
