package com.db.auctionclient.model.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.Gson;

import java.io.FileReader;
import java.io.IOException;

import lombok.Getter;

/**
 * Contains information about the auction.
 */
@Getter
@Entity
public class Auction {
    /** Auction id.*/
    @PrimaryKey
    private int id;
    /** Title of the auction.*/
    private String title;
    /** Description of the auction. */
    private String description;
    /** Starting price.*/
    private int startPrice;
    /** Date, when the registration phase should begin. */
    private long startDate;
    /** IP address of the server-worker, which is hosting this auction. */
    private String hostIp;
    /** Port, which is used by the server-worker for the communication. */
    private int hostPort;
    /** Auction phase. */
    private AuctionPhase phase;
    /** Type of the auction.*/
    private String auctionType;

    /**
     * @param id auction id.
     * @param title title of the auction.
     * @param description description of the auction.
     * @param startPrice starting price.
     * @param startDate date, when the registration phase should begin.
     * @param hostIp IP address of the server-worker, which is hosting this auction.
     * @param hostPort port, which is used by the server-worker for the communication.
     * @param phase auction phase.
     * @param auctionType type of the auction.
     */
    public Auction(int id, String title, String description, int startPrice, long startDate,
                   String hostIp, int hostPort, AuctionPhase phase, String auctionType) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.startPrice = startPrice;
        this.startDate = startDate;
        this.hostIp = hostIp;
        this.hostPort = hostPort;
        this.phase = phase;
        this.auctionType = auctionType;
    }

    /**
     * Loads an array of auctions from a json file.
     * @param path path to the json file.
     * @return an array of instances of {@link Auction}.
     * @throws IOException
     */
    public static Auction[] loadFromJson(String path) throws IOException {
        Gson gson = new Gson();
        FileReader reader = new FileReader(path);
        Auction[] config = gson.fromJson(reader, Auction[].class);
        reader.close();
        return config;
    }
}


