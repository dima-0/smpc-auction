package com.db.auctionclient.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.db.auctionclient.model.worker.GlobalSingleton;
import com.db.auctionclient.model.worker.ClientMaster;
import com.db.auctionclient.model.db.AppDatabase;
import com.db.auctionclient.model.AuctionRepository;
import com.db.auctionclient.model.DBAuctionRepository;

/**
 * View model, which is used by {@link com.db.auctionclient.view.SetBidFragment} to retrieve data
 * and communicate with {@link ClientMaster}.
 */
public class SetBidViewModel extends AndroidViewModel {
    private ClientMaster clientMaster;
    private AuctionRepository repository;

    public SetBidViewModel(@NonNull Application application) {
        super(application);
        repository = new DBAuctionRepository(AppDatabase.getInstance(application));
        clientMaster = GlobalSingleton.getInstance();
    }

    /**
     * Checks, if any client-workers are available.
     * @return true, if if at least one client-worker is available or false otherwise.
     */
    public boolean workersAvailable(){
        return clientMaster.clientWorkersAvailable();
    }

    /**
     * Requests a client-master to join an auction.
     * @param auctionId id of the auction, which should be joined.
     * @param hostIp IP address of the host, which hosts the auction.
     * @param hostPort port of the host, which hosts the auction.
     * @param bid bid.
     * @return true, if client-workers are available and the auction is not processed at the moment
     * or false otherwise.
     */
    public boolean joinAuction(int auctionId, String hostIp, int hostPort, int bid){
        return clientMaster.joinAuction(auctionId, hostIp, hostPort, bid, repository);
    }
}
