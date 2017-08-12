package com.mdeiml.richard;

import android.content.Context;
import android.widget.Toast;
import java.io.Serializable;
import org.apache.commons.math3.linear.RealMatrix;


public class Match implements Serializable {

    private Score s;
    
    private int currentSet;
    private int setScoreI;
    private int setScoreJ;
    
    private boolean winI;
    private boolean winJ;
    
    private boolean serveGame;
    
    private RealMatrix matGI;
    private RealMatrix matGJ;
    private RealMatrix matMTI;
    private RealMatrix matMTJ;
    private RealMatrix matTI;
    private RealMatrix matTJ;
    private RealMatrix matSI;
    private RealMatrix matSJ;
    private RealMatrix matM;

    private double winProbI;
    private double importance;

    private double pi;
    private double pj;
    
    private Match() {}
    
    public Match(double pi, double pj) {
        s = new Score(0, 0, new byte[] {0, 0, 0}, new byte[] {0, 0, 0}, true, false);
        
        currentSet = 0;
        setScoreI = 0;
        setScoreJ = 0;
        
        winI = false;
        winJ = false;
        
        serveGame = true;
        
        updateProb(pi, pj);

        winProbI = -1;
        importance = -1;
    }
    
    public Score getScore() {
        return s;
    }
    
    public void setScore(Score s) {
        this.s = s;
        currentSet = 0;
        for(int i = 0; i < s.gamesI.length; i++) {
            if(s.gamesI[i] != 0 || s.gamesJ[i] != 0) {
                currentSet = i;
            }
        }
        int gi = s.gamesI[currentSet];
        int gj = s.gamesJ[currentSet];
        if(gi == 7 || gj == 7 || ((gi == 6 || gj == 6) && Math.abs(gi - gj) >= 2)) {
            currentSet++;
        }
        setScoreI = 0;
        setScoreJ = 0;
        for(int i = 0; i < currentSet; i++) {
            if(s.gamesI[i] > s.gamesJ[i]) {
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
        
        if(currentSet == 2 || (s.gamesI[currentSet] == 6 && s.gamesJ[currentSet] == 6)) {
            int serve = (s.scoreI + s.scoreJ) % 4;
            serveGame = s.serveI == (serve == 0 || serve == 3);
        }else {
            serveGame = s.serveI;
        }

        winProbI = -1;
        importance = -1;
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
        this.pi = pi;
        this.pj = pj;
    }

    public void setServePoint(boolean servePoint) {
        setScore(new Score(s.scoreI, s.scoreJ, s.gamesI, s.gamesJ, servePoint, s.deuce));
    }

    public boolean isServePoint() {
        return s.serveI;
    }

    public void setGamesJ(byte[] gamesJ) {
       setScore(new Score(s.scoreI, s.scoreJ, s.gamesI, gamesJ, s.serveI, s.deuce));
    }

    public byte[] getGamesJ() {
        return s.gamesJ.clone();
    }

    public void setGamesI(byte[] gamesI) {
        setScore(new Score(s.scoreI, s.scoreJ, gamesI, s.gamesJ, s.serveI, s.deuce));
    }

    public byte[] getGamesI() {
        return s.gamesI.clone();
    }

    public void setScoreJ(byte scoreJ) {
        setScore(new Score(s.scoreI, scoreJ, s.gamesI, s.gamesJ, s.serveI, s.deuce));
    }

    public int getScoreJ() {
        return s.scoreJ;
    }
    
    public String getScoreJS() {
        if(s.gamesI[currentSet] == 6 && s.gamesJ[currentSet] == 6 || currentSet == 2) {
            return s.scoreJ+"";
        }else {
            if(s.deuce) {
                if(s.scoreJ == 3) {
                    return "A";
                }else {
                    return "40";
                }
            }else {
                switch(s.scoreJ) {
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
        setScore(new Score(scoreI, s.scoreJ, s.gamesI, s.gamesJ, s.serveI, s.deuce));
    }

    public int getScoreI() {
        return s.scoreI;
    }

    public String getScoreIS() {
        if(s.gamesI[currentSet] == 6 && s.gamesJ[currentSet] == 6 || currentSet == 2) {
            return s.scoreI+"";
        }else {
            if(s.deuce) {
                if(s.scoreI == 3) {
                    return "A";
                }else {
                    return "40";
                }
            }else {
                switch(s.scoreI) {
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

    public int getCurrentSet() {
        return currentSet;
    }

    public int getWinner() {
        if(winI) {
            return 1;
        }else if(winJ) {
            return 2;
        }else {
            return 0;
        }
    }

    public double[] getProbs() {
        return new double[] {pi, pj};
    }
    
    public void point(boolean pointI) {
        byte scoreI = s.scoreI;
        byte scoreJ = s.scoreJ;
        byte[] gamesI = s.gamesI.clone();
        byte[] gamesJ = s.gamesJ.clone();
        boolean servePoint = s.serveI;
        boolean deuce = s.deuce;
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
        s = new Score(scoreI, scoreJ, gamesI, gamesJ, servePoint, deuce);
        winProbI = -1;
        importance = -1;
    }
    
    public double winProbI() {
        if(winProbI > 0) {
            return winProbI;
        }
        if(winI) {
            winProbI = 1;
        }else if (winJ) {
            winProbI = 0;
        }
        if(currentSet == 2) { // match tiebrak
            RealMatrix mti = serveGame ? matMTI : matMTJ;
            int si = serveGame ? s.scoreI : s.scoreJ;
            int sj = serveGame ? s.scoreJ : s.scoreI;
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
            winProbI = p;
        }else if(s.gamesI[currentSet] == 6 && s.gamesJ[currentSet] == 6) { // set tiebreak
            RealMatrix mti = serveGame ? matTI : matTJ;
            double ti = mti.getEntry(0, 0);
            ti = serveGame ? ti : 1-ti;
            RealMatrix mmi = matM;
            
            int si = serveGame ? s.scoreI : s.scoreJ;
            int sj = serveGame ? s.scoreJ : s.scoreI;
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
            winProbI = pt * pwinI + (1-pt) * pwinJ;
        }else { // standard game
            RealMatrix mgi = matGI;
            RealMatrix mgj = matGJ;
            int serve = (s.gamesI[currentSet] + s.gamesJ[currentSet]) % 2;
            boolean serveFirstGame = (serve == 0) == serveGame;
            RealMatrix msi = serveFirstGame ? matSI : matSJ;
            RealMatrix mmi = matM;
            
            int si = serveGame ? s.scoreI : s.scoreJ;
            int sj = serveGame ? s.scoreJ : s.scoreI;
            int index = si*4+sj;
            double pg = (serveGame ? mgi : mgj).getEntry(index, 0);
            if(!serveGame) {
                pg = 1-pg;
            }
            int ssi = serveFirstGame ? s.gamesI[currentSet] : s.gamesJ[currentSet];
            int ssj = serveFirstGame ? s.gamesJ[currentSet] : s.gamesI[currentSet];
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
            winProbI = ps * pwinI + (1-ps) * pwinJ;
        }
        return winProbI;
    }
    
    public double importance() {
        if(importance > 0) {
            return importance;
        }
        Match m1 = cloneMatch();
        Match m2 = cloneMatch();
        m1.point(true);
        m2.point(false);
        double p1 = m1.winProbI();
        double p2 = m2.winProbI();
        importance = p1 - p2;
        return importance;
    }
    
    public Match cloneMatch() {
        Match m = new Match();
        m.s = s;
        
        m.currentSet = currentSet;
        m.setScoreI = setScoreI;
        m.setScoreJ = setScoreJ;
        
        m.winI = winI;
        m.winJ = winJ;
        
        m.serveGame = serveGame;
        
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

    public HistoryEntry getHistoryEntry() {
        return new HistoryEntry(getScore(), (float)winProbI(), (float)importance());
    }
}
