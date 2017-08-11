package com.mdeiml.richard;
import java.io.Serializable;

public class Score implements Serializable {
    
    public byte scoreI;
    public byte scoreJ;
    public byte[] gamesI;
    public byte[] gamesJ;
    public boolean serveI;
    public boolean deuce;

    public Score(byte scoreI, byte scoreJ, byte[] gamesI, byte[] gamesJ, boolean serveI, boolean deuce) {
        this.scoreI = scoreI;
        this.scoreJ = scoreJ;
        this.gamesI = gamesI;
        this.gamesJ = gamesJ;
        this.serveI = serveI;
        this.deuce = deuce;
    }
    
}
