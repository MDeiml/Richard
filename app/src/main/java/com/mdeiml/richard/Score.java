package com.mdeiml.richard;
import java.io.Serializable;

public class Score implements Serializable {
    
    public final byte scoreI;
    public final byte scoreJ;
    public final byte[] gamesI;
    public final byte[] gamesJ;
    public final boolean serveI;
    public final boolean deuce;

    // convenience
    public Score(int scoreI, int scoreJ, byte[] gamesI, byte[] gamesJ, boolean serveI, boolean deuce) {
        this((byte)scoreI, (byte)scoreJ, gamesI, gamesJ, serveI, deuce);
    }

    public Score(byte scoreI, byte scoreJ, byte[] gamesI, byte[] gamesJ, boolean serveI, boolean deuce) {
        this.scoreI = scoreI;
        this.scoreJ = scoreJ;
        this.gamesI = gamesI;
        this.gamesJ = gamesJ;
        this.serveI = serveI;
        this.deuce = deuce;
    }
    
}
