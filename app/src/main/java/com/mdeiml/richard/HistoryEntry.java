package com.mdeiml.richard;
import java.io.Serializable;

public class HistoryEntry implements Serializable {
    
    public Score score;
    public float prop;
    public float imp;

    public HistoryEntry(Score score, float prop, float imp) {
        this.score = score;
        this.prop = prop;
        this.imp = imp;
    }
    
}
