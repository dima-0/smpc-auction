package com.db.auctionclient.model.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents a local state of an active auction, in which the client participates.
 */
@Setter
@Getter
@Entity
public class AuctionTask {
    /** Id of the corresponding auction.*/
    @PrimaryKey
    private int auctionId;
    /** IP address of the server-worker, which is hosting this auction.*/
    private String hostIp;
    /** Port, which is used by the server-worker for the communication.*/
    private int hostPort;
    /** Fresco port, which is used for communication during the auction evaluation (fresco application).*/
    private int smpcPort;
    /** Current bid, which is specified by the client.*/
    private int currentBid;
    /** Final price, which is set after the evaluation of the auction (-1 if not set).*/
    private int finalPrice = -1;
    /** Indicates, if the client has won the auction.*/
    private boolean hasWon;
    /** Local phase of the auction, which should is synced.*/
    private AuctionPhase localPhase;
    /** Error message, if an error occurred during the auction.*/
    private String errorMessage;

    /**
     * @param auctionId id of the corresponding auction.
     * @param hostIp IP address of the server-worker, which is hosting this auction.
     * @param hostPort port, which is used by the server-worker for the communication.
     * @param smpcPort fresco port, which is used for communication during the auction evaluation (fresco application).
     * @param currentBid current bid, which is specified by the client.
     */
    public AuctionTask(int auctionId, String hostIp, int hostPort, int smpcPort, int currentBid) {
        this.auctionId = auctionId;
        this.hostIp = hostIp;
        this.hostPort = hostPort;
        this.smpcPort = smpcPort;
        this.currentBid = currentBid;
        localPhase = AuctionPhase.Registration;
    }
}
