package com.db.auctionclient.model.entities;

/**
 * Represents an auction phase.
 */
public enum AuctionPhase {
    Registration("Registration"),
    Running("Running"),
    Completion("Completion"),
    Abortion("Abortion");
    private final String name;

    private AuctionPhase(String name) {
        this.name = name;
    }

    public String getName(){
        return name;
    }

    /**
     * Converts a string to an {@link AuctionPhase}.
     * @param string string, which should be converted.
     * @return an {@link AuctionPhase}, if the given string matches a {@link AuctionPhase#name}. Null otherwise.
     */
    public static AuctionPhase fromString(String string){
        for(AuctionPhase p : values()){
            if(p.getName().matches(string)) return p;
        }
        return null;
    }
}
