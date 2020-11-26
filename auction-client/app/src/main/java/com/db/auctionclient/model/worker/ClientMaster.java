package com.db.auctionclient.model.worker;

import com.db.auctionclient.model.AuctionRepository;
import com.db.auctionclient.model.entities.AuctionTask;

import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Manages instances of {@link ClientWorker}.
 */
public class ClientMaster implements WorkerListener{
    /** Ports, which are used by client-workers for auction evaluation (fresco application).*/
    private final Deque<Integer> frescoPortPool = new ConcurrentLinkedDeque<>();
    /** Client-workers mapped to their corresponding auctionIds.*/
    private final Map<Integer, ClientWorker> workers = new ConcurrentHashMap<>();
    /** Configuration, which should be passed to the client-workers.*/
    private final ClientConfiguration config;

    /**
     * @param config Configuration, which should be passed to the client-workers..
     */
    public ClientMaster(ClientConfiguration config) {
        this.config = config;
        // set up fresco port pool
        for (Integer port : this.config.getFrescoPortPool()) frescoPortPool.push(port);
    }

    /**
     * Requests a client-worker to join an auction.
     * @param auctionId id of the auction, which should be joined.
     * @param hostIp IP address of the host, which hosts the auction.
     * @param hostPort port of the host, which hosts the auction.
     * @param bid bid.
     * @param auctionRepository auction repository.
     * @return true, if client-workers are available and auction is not processed at the moment or
     * false otherwise.
     */
    public boolean joinAuction(int auctionId, String hostIp, int hostPort, int bid, AuctionRepository auctionRepository){
        boolean joined = false;
        if(clientWorkersAvailable() && !workers.containsKey(auctionId)){
            int smpcPort = frescoPortPool.pop();
            AuctionTask task = new AuctionTask(auctionId,
                    hostIp, hostPort, smpcPort,
                    bid);
            auctionRepository.addAuctionTask(task);
            ClientWorker worker = new ClientWorker(config, task, auctionRepository, this);
            new Thread(worker).start();
            workers.put(task.getAuctionId(), worker);
            joined = true;
        }
        return joined;
    }

    /**
     * Requests the corresponding client-worker to leave the auction with the given id.
     * @param auctionId id of the auction, which should be leaved.
     */
    public void leaveAuction(int auctionId){
        ClientWorker worker = workers.get(auctionId);
        if(worker != null){
            worker.leaveAuction();
        }
    }

    /**
     * Requests the corresponding client-worker to change bid.
     * @param auctionId id of the auction.
     * @param bid new bid.
     */
    public void changeBid(int auctionId, int bid){
        ClientWorker worker = workers.get(auctionId);
        if(worker != null){
            worker.setBid(bid);
        }
    }

    /**
     * Checks, if any client-workers are available.
     * @return true, if at least one client worker is available or false otherwise.
     */
    public boolean clientWorkersAvailable(){
        return frescoPortPool.size() > 0;
    }

    @Override
    public void onCompleteTask(AuctionTask task) {
        workers.remove(task.getAuctionId());
        frescoPortPool.push(task.getSmpcPort());
    }
}
