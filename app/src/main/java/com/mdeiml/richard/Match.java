package com.mdeiml.richard;

import android.content.Context;
import android.widget.Toast;
import java.io.Serializable;
import org.apache.commons.math3.linear.RealMatrix;


public class Match implements Serializable {
    
    private byte scoreI;
    private byte scoreJ;
    private byte[] gamesI;
    private byte[] gamesJ;
    
    private int currentSet;
    private int setScoreI;
    private int setScoreJ;
    
    private boolean winI;
    private boolean winJ;
    
    private boolean servePoint;
    private boolean serveGame;
    
    private boolean deuce;
    
    private RealMatrix matGI;
    private RealMatrix matGJ;
    private RealMatrix matMTI;
    private RealMatrix matMTJ;
    private RealMatrix matTI;
    private RealMatrix matTJ;
    private RealMatrix matSI;
    private RealMatrix matSJ;
    private RealMatrix matM;
    
    private Match() {}
    
    public Match(double pi, double pj) {
        scoreI = 0;
        scoreJ = 0;
        gamesI = new byte[] {0,0,0};
        gamesJ = new byte[] {0,0,0};
        
        currentSet = 0;
        setScoreI = 0;
        setScoreJ = 0;
        
        winI = false;
        winJ = false;
        
        servePoint = true;
        serveGame = true;
        
        deuce = false;
        updateProb(pi, pj);
    }
    
    public Score getScore() {
        return new Score(scoreI, scoreJ, gamesI.clone(), gamesJ.clone(), servePoint, deuce);
    }
    
    public void setScore(Score s) {
        scoreI = s.scoreI;
        scoreJ = s.scoreJ;
        gamesI = s.gamesI;
        gamesJ = s.gamesJ;
        currentSet = 0;
        for(int i = 0; i < gamesI.length; i++) {
            if(gamesI[i] != 0 || gamesJ[i] != 0) {
                currentSet = i;
            }
        }
        int gi = gamesI[currentSet];
        int gj = gamesJ[currentSet];
        if(gi == 7 || gj == 7 || ((gi == 6 || gj == 6) && Math.abs(gi - gj) >= 2)) {
            currentSet++;
        }
        setScoreI = 0;
        setScoreJ = 0;
        for(int i = 0; i < currentSet; i++) {
            if(setScoreI > setScoreJ) {
                setScoreI++;
            }else {
                setScoreJ++;
            }
        }
        
        winI = false;
        winJ = false;
        
        if(setScoreI == 2) {
            winI = true;
        }else if(setScoreJ == 2) {
            winJ = true;
        }
        
        servePoint = s.serveI;
        if(currentSet == 2 || (gamesI[currentSet] == 6 && gamesJ[currentSet] == 6)) {
            int serve = (scoreI + scoreJ) % 4;
            serveGame = servePoint == (serve == 0 || serve == 3);
        }else {
            serveGame = servePoint;
        }
        
        deuce = s.deuce;
    }
    
    public void updateProb(double pi, double pj) {
        matGI = MarkovMatrix.getGamePropabilities(pi);
        matGJ = MarkovMatrix.getGamePropabilities(pj);
        matMTI = MarkovMatrix.getMatchTiebreakPropabilities(pi, pj);
        matMTJ = MarkovMatrix.getMatchTiebreakPropabilities(pj, pi);
        matTI = MarkovMatrix.getSetTiebreakPropabilities(pi, pj);
        matTJ = MarkovMatrix.getSetTiebreakPropabilities(pj, pi);
        matSI = MarkovMatrix.getSetPropabilities(matGI.getEntry(0, 0), matGJ.getEntry(0, 0), matTI.getEntry(0, 0));
        matSJ = MarkovMatrix.getSetPropabilities(matGJ.getEntry(0, 0), matGI.getEntry(0, 0), matTI.getEntry(0, 0));
        matM = MarkovMatrix.getMatchPropabilities(matSI.getEntry(0, 0), matMTI.getEntry(0, 0));
    }

    public void setServePoint(boolean servePoint) {
        this.servePoint = servePoint;
    }

    public boolean isServePoint() {
        return servePoint;
    }

    public void setGamesJ(byte[] gamesJ) {
        this.gamesJ = gamesJ;
    }

    public byte[] getGamesJ() {
        return gamesJ;
    }

    public void setGamesI(byte[] gamesI) {
        this.gamesI = gamesI;
    }

    public byte[] getGamesI() {
        return gamesI;
    }

    public void setCurrentSet(int currentSet) {
        this.currentSet = currentSet;
    }

    public int getCurrentSet() {
        return currentSet;
    }

    public void setScoreJ(byte scoreJ) {
        this.scoreJ = scoreJ;
    }

    public int getScoreJ() {
        return scoreJ;
    }
    
    public String getScoreJS() {
        if(gamesI[currentSet] == 6 && gamesJ[currentSet] == 6 || currentSet == 2) {
            return scoreJ+"";
        }else {
            if(deuce) {
                if(scoreJ == 3) {
                    return "A";
                }else {
                    return "40";
                }
            }else {
                switch(scoreJ) {
                    case 0:
                    default:
                        return "0";
                    case 1:
                        return "15";
                    case 2:
                        return "30";
                    case 3:
                        return "40";
                }
            }
        }
    }

    public void setScoreI(byte scoreI) {
        this.scoreI = scoreI;
    }

    public int getScoreI() {
        return scoreI;
    }

    public String getScoreIS() {
        if(gamesI[currentSet] == 6 && gamesJ[currentSet] == 6 || currentSet == 2) {
            return scoreI+"";
        }else {
            if(deuce) {
                if(scoreI == 3) {
                    return "A";
                }else {
                    return "40";
                }
            }else {
                switch(scoreI) {
                    case 0:
                    default:
                        return "0";
                    case 1:
                        return "15";
                    case 2:
                        return "30";
                    case 3:
                        return "40";
                }
            }
        }
    }
    
    public void point(boolean pointI) {
        if(winI || winJ) {
            return;
        }
        if(pointI) {
            scoreI++;
        }else {
            scoreJ++;
        }
        if(currentSet == 2) { // match tiebreak
            if(scoreI >= 10 && (scoreI - scoreJ) >= 2) {
                winI = true;
            }
            if(scoreJ >= 10 && (scoreJ - scoreI) >= 2) {
                winJ = true;
            }
            if((scoreI+scoreJ) % 2 == 1) {
                servePoint = !servePoint;
            }
        }else if(gamesI[currentSet] == 6 && gamesJ[currentSet] == 6) { // set tiebreak
            if(scoreI >= 7 && (scoreI - scoreJ) >= 2) {
                gamesI[currentSet]++;
                setScoreI++;
                currentSet++;
                scoreI = 0;
                scoreJ = 0;
                serveGame = !serveGame;
                servePoint = serveGame;
                if(setScoreI == 2) {
                    winI = true;
                }
            }
            if(scoreJ >= 7 && (scoreJ - scoreI) >= 2) {
                gamesJ[currentSet]++;
                setScoreJ++;
                currentSet++;
                scoreI = 0;
                scoreJ = 0;
                serveGame = !serveGame;
                servePoint = serveGame;
                if(setScoreJ == 2) {
                    winJ = true;
                }
            }
            if((scoreI+scoreJ) % 2 == 1) {
                servePoint = !servePoint;
            }
        }else { // standard game
            if(scoreI == 3 && scoreJ == 3) {
                deuce = true;
                scoreI = 2;
                scoreJ = 2;
            }
            if(scoreI == 4 && scoreJ < 3) {
                gamesI[currentSet]++;
                deuce = false;
                scoreI = 0;
                scoreJ = 0;
                serveGame = !serveGame;
                servePoint = serveGame;
                if(gamesI[currentSet] >= 6 && (gamesI[currentSet] - gamesJ[currentSet]) >= 2) {
                    currentSet++;
                    setScoreI++;
                    if(setScoreI == 2) {
                        winI = true;
                    }
                }
            }
            if(scoreI < 3 && scoreJ == 4) {
                gamesJ[currentSet]++;
                deuce = false;
                scoreI = 0;
                scoreJ = 0;
                serveGame = !serveGame;
                servePoint = serveGame;
                if(gamesJ[currentSet] >= 6 && (gamesJ[currentSet] - gamesI[currentSet]) >= 2) {
                    currentSet++;
                    setScoreJ++;
                    if(setScoreJ == 2) {
                        winJ = true;
                    }
                }
            }
        }
    }
    
    public double winProbI() {
        if(winI) {
            return 1;
        }else if (winJ) {
            return 0;
        }
        if(currentSet == 2) { // match tiebrak
            RealMatrix mti = serveGame ? matMTI : matMTJ;
            int si = serveGame ? scoreI : scoreJ;
            int sj = serveGame ? scoreJ : scoreI;
            if(si > 10) si = 9 + (si-1) % 2;
            if(sj > 10) si = 9 + (sj-1) % 2;
            if(si == 10 && sj == 10) {
                si = 8;
                sj = 8;
            }
            int index;
            if(si < 10 && sj < 10) {
                index = si*10+sj;
            }else {
                if(si == 10) { // 10-9
                    index = 101;
                }else {
                    index = 100;
                }
            }
            double p = mti.getEntry(index, 0);
            if(!serveGame) p = 1-p;
            return p;
        }else if(gamesI[currentSet] == 6 && gamesJ[currentSet] == 6) { // set tiebreak
            RealMatrix mti = serveGame ? matTI : matTJ;
            double ti = mti.getEntry(0, 0);
            ti = serveGame ? ti : 1-ti;
            RealMatrix mmi = matM;
            
            int si = serveGame ? scoreI : scoreJ;
            int sj = serveGame ? scoreJ : scoreI;
            if(si > 7) si = 6 + si % 2;
            if(sj > 7) si = 6 + sj % 2;
            if(si == 7 && sj == 7) {
                si = 6;
                sj = 6;
            }
            int index;
            if(si < 7 && sj < 7) {
                index = si*7+sj;
            }else {
                if(si == 7) { // 7-6
                    index = 50;
                }else {
                    index = 49;
                }
            }
            double pt = mti.getEntry(index, 0);
            if(!serveGame) {
                pt = 1-pt;
            }
            double pwinI = setScoreI == 1 ? 1 : mmi.getEntry(MarkovMatrix.getMatchMatrixIndex(setScoreI+1, setScoreJ), 0);
            double pwinJ = setScoreJ == 1 ? 0 : mmi.getEntry(MarkovMatrix.getMatchMatrixIndex(setScoreI, setScoreJ+1), 0);
            return pt * pwinI + (1-pt) * pwinJ;
        }else { // standard game
            RealMatrix mgi = matGI;
            RealMatrix mgj = matGJ;
            int serve = (gamesI[currentSet] + gamesJ[currentSet]) % 2;
            boolean serveFirstGame = (serve == 0) == serveGame;
            RealMatrix msi = serveFirstGame ? matSI : matSJ;
            RealMatrix mmi = matM;
            
            int si = serveGame ? scoreI : scoreJ;
            int sj = serveGame ? scoreJ : scoreI;
            int index = si*4+sj;
            double pg = (serveGame ? mgi : mgj).getEntry(index, 0);
            if(!serveGame) {
                pg = 1-pg;
            }
            int ssi = serveFirstGame ? gamesI[currentSet] : gamesJ[currentSet];
            int ssj = serveFirstGame ? gamesJ[currentSet] : gamesI[currentSet];
            double pwinI = ssi == 6 || (ssi == 5 && ssj < 5) ? 1 : msi.getEntry(MarkovMatrix.getSetMatrixIndex(ssi+1, ssj), 0);
            double pwinJ = ssj == 6 || (ssj == 5 && ssi < 5) ? 0 : msi.getEntry(MarkovMatrix.getSetMatrixIndex(ssi, ssj+1), 0);
            if(!serveFirstGame) {
                double d = 1-pwinI;
                pwinI = 1-pwinJ;
                pwinJ = d;
            }
            double ps = pg * pwinI + (1-pg) * pwinJ;
            pwinI = setScoreI == 1 ? 1 : mmi.getEntry(MarkovMatrix.getMatchMatrixIndex(setScoreI+1, setScoreJ), 0);
            pwinJ = setScoreJ == 1 ? 0 : mmi.getEntry(MarkovMatrix.getMatchMatrixIndex(setScoreI, setScoreJ+1), 0);
            return ps * pwinI + (1-ps) * pwinJ;
        }
    }
    
    public double importance() {
        Match m1 = cloneMatch();
        Match m2 = cloneMatch();
        m1.point(true);
        m2.point(false);
        double p1 = m1.winProbI();
        double p2 = m2.winProbI();
        return p1 - p2;
    }
    
    public Match cloneMatch() {
        Match m = new Match();
        m.scoreI = scoreI;
        m.scoreJ = scoreJ;
        m.gamesI = gamesI.clone();
        m.gamesJ = gamesJ.clone();
        
        m.currentSet = currentSet;
        m.setScoreI = setScoreI;
        m.setScoreJ = setScoreJ;
        
        m.winI = winI;
        m.winJ = winJ;
        
        m.servePoint = servePoint;
        m.serveGame = serveGame;
        
        m.deuce = deuce;
        
        m.matGI = matGI;
        m.matGJ = matGJ;
        m.matMTI = matMTI;
        m.matMTJ = matMTJ;
        m.matTI = matTI;
        m.matTJ = matTJ;
        m.matSI = matSI;
        m.matSJ = matSJ;
        m.matM = matM;
        return m;
    }
}
