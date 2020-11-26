package com.db.auctionclient.model.entities;

import androidx.room.TypeConverter;

/**
 * Provides type converters, which are used by Room.
 */
public class Converters {

    /**
     * Converts a string to an auction phase.
     * @param phaseAsString string, which should be converted.
     * @return AuctionPhase, if the given string matches a name or null otherwise.
     */
    @TypeConverter
    public static AuctionPhase PhaseFromString(String phaseAsString){
        return AuctionPhase.fromString(phaseAsString);
    }

    /**
     * Converts an auction phase to a string.
     * @param phase auction phase, which should be converted.
     * @return a string, which represents the auction phase.
     */
    @TypeConverter
    public static String PhaseToString(AuctionPhase phase){
        return phase.getName();
    }
}
