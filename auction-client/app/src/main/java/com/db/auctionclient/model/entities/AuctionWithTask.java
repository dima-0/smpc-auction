package com.db.auctionclient.model.entities;

import androidx.room.Embedded;
import androidx.room.Relation;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents a one to one relationship between an auction and its task (needed by Room).
 */
@Setter
@Getter
public class AuctionWithTask {
    @Embedded
    private Auction auction;

    @Relation(
            parentColumn = "id",
            entityColumn = "auctionId"
    )
    private AuctionTask task;
}
