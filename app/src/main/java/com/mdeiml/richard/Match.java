package com.mdeiml.richard;

import java.util.ArrayList;
import org.apache.commons.math3.linear.RealMatrix;
import android.util.Log;

public class Match {

    public static final byte NO_TIEBREAK = 0;
    public static final byte SET_TIEBREAK = 1;
    public static final byte MATCH_TIEBREAK = 2;

    public long matchId;

    public byte winner;
    public final ArrayList<Set> sets;
    public final String player1;
    public final String player2;
    private double p1;
    private double p2;

    private RealMatrix matG1;
    private RealMatrix matG2;
    private RealMatrix matMT1;
    private RealMatrix matMT2;
    private RealMatrix matT1;
    private RealMatrix matT2;
    private RealMatrix matS1;
    private RealMatrix matS2;
    private RealMatrix matM;

    public Match(String player1, String player2, double p1, double p2) {
        this(player1, player2, p1, p2, -1);
    }

    public Match(String player1, String player2, double p1, double p2, long matchId) {
        this.player1 = player1;
        this.player2 = player2;
        this.winner = 0;
        this.sets = new ArrayList<>();
        this.matchId = matchId;
        sets.add(new Set(NO_TIEBREAK, (byte)1));
        updateProb(p1, p2);
        Point p = getCurrentSet().getCurrentGame().getCurrentPoint();
        p.winProb = calcWinProb();
        p.importance = calcImportance();
    }

    public int getCurrentSetNr() {
        return sets.size()-1;
    }

    public boolean servePoint() {
        return getCurrentSet().getCurrentGame().getCurrentPoint().server == 1;
    }

    public double getP1() {
        return p1;
    }

    public double getP2() {
        return p2;
    }

    public byte[][] getGames() {
        byte[][] res = new byte[sets.size()][];
        for(int i = 0; i < sets.size(); i++) {
            res[i] = sets.get(i).totalGames();
        }
        return res;
    }
    
    public void updateProb(double p1, double p2) {
        matG1 = MarkovMatrix.getGamePropabilities(p1);
        matG2 = MarkovMatrix.getGamePropabilities(p2);
        matMT1 = MarkovMatrix.getMatchTiebreakPropabilities(p1, p2);
        matMT2 = MarkovMatrix.getMatchTiebreakPropabilities(p2, p1);
        matT1 = MarkovMatrix.getSetTiebreakPropabilities(p1, p2);
        matT2 = MarkovMatrix.getSetTiebreakPropabilities(p2, p1);
        matS1 = MarkovMatrix.getSetPropabilities(matG1.getEntry(0, 0), matG2.getEntry(0, 0), matT1.getEntry(0, 0));
        matS2 = MarkovMatrix.getSetPropabilities(matG2.getEntry(0, 0), matG1.getEntry(0, 0), matT1.getEntry(0, 0));
        matM = MarkovMatrix.getMatchPropabilities(matS1.getEntry(0, 0), matMT1.getEntry(0, 0));
        this.p1 = p1;
        this.p2 = p2;
    }

    public Set getCurrentSet() {
        return sets.get(sets.size()-1);
    }

    public byte getWinner() {
        if(winner != 0) {
            return winner;
        }
        byte[] totalSets = totalSets();
        if(totalSets[0] == 2) {
            winner = 1;
        }else if(totalSets[1] == 2) {
            winner = 2;
        }
        return winner;
    }

    public byte[] totalSets() {
        byte sets1 = 0;
        byte sets2 = 0;
        for(Set set : sets) {
            byte w = set.getWinner();
            if(w == 1) {
                sets1++;
            }else if(w == 2) {
                sets2++;
            }
        }
        return new byte[] {sets1, sets2};
    }

    public byte point(byte player) {
        return point(player, true);
    }

    private byte point(byte player, boolean calcStatistics) {
        byte w = getCurrentSet().point(player);
        byte w1 = 0;
        if(w != 0) {
            w1 = getWinner();
            if(w1 == 0) {
                byte[] totalSets = totalSets();
                boolean t = totalSets[0] == 1 && totalSets[1] == 1;
                byte server = getCurrentSet().getCurrentGame().getCurrentPoint().server;
                server = server == 1 ? (byte)2 : (byte)1;
                sets.add(new Set(t ? MATCH_TIEBREAK : NO_TIEBREAK, server));
            }
        }
        if(calcStatistics) {
            Point p = getCurrentSet().getCurrentGame().getCurrentPoint();
            p.winProb = calcWinProb();
            p.importance = calcImportance();
            Log.i("Match", p.winProb+"p");
            Log.i("Match", sets.size()+"n");
        }
        return w1;
    }

    public boolean removePoint() {
        if(getCurrentSet().getCurrentGame().getCurrentPoint() == sets.get(0).games.get(0).points.get(0)) {
            return false;
        }
        if(getCurrentSet().removePoint()) {
            sets.remove(sets.size()-1);
            getCurrentSet().getCurrentGame().getCurrentPoint().winner = 0;
            getCurrentSet().getCurrentGame().winner = 0;
            getCurrentSet().winner = 0;
        }
        return true;
    }

    private float calcWinProb() {
        byte w = getWinner();
        if(w == 1) {
            return 1;
        }else if(w == 2) {
            return 0;
        }else {
            byte[] totalSets = totalSets();
            byte s1 = totalSets[0];
            byte s2 = totalSets[1];
            double pm1 = s1 == 1 ? 1 : matM.getEntry(MarkovMatrix.getMatchMatrixIndex(s1+1, s2), 0);
            double pm2 = s2 == 1 ? 0 : matM.getEntry(MarkovMatrix.getMatchMatrixIndex(s1, s2+1), 0);
            double ps = getCurrentSet().getWinProb(matG1, matG2, matT1, matT2, matMT1, matMT2, matS1, matS2);
            return (float)(ps * pm1 + (1 - ps) * pm2);
        }
    }

    private float calcImportance() {
        if(winner != 0) {
            return 0;
        }
        point((byte)1, false);
        float p1 = calcWinProb();
        removePoint();
        point((byte)2, false);
        float p2 = calcWinProb();
        removePoint();
        return p1 - p2;
    }

    public float getWinProb() {
        return getCurrentSet().getCurrentGame().getCurrentPoint().winProb;
    }

    public float importance() {
        return getCurrentSet().getCurrentGame().getCurrentPoint().importance;
    }

    public static class Set {

        public byte winner;
        public final ArrayList<Game> games;
        public final byte tiebreak;

        public Set(byte tiebreak, byte server) {
            this.winner = 0;
            this.games = new ArrayList<>();
            this.tiebreak = tiebreak;
            if(tiebreak == MATCH_TIEBREAK) {
                games.add(new Game(MATCH_TIEBREAK, server));
            }else {
                games.add(new Game(NO_TIEBREAK, server));
            }
        }

        public Set(byte tiebreak) {
            this.tiebreak = tiebreak;
            this.winner = 0;
            this.games = new ArrayList<>();
        }

        public Game getCurrentGame() {
            return games.get(games.size()-1);
        }

        public byte getWinner() {
            if(winner != 0) {
                return winner;
            }
            if(tiebreak == MATCH_TIEBREAK) {
                winner = games.get(0).getWinner();
            }else {
                byte[] totalGames = totalGames();
                if(totalGames[0] == 7 || (totalGames[0] == 6 && totalGames[1] <= 4)) {
                    winner = 1;
                }else if(totalGames[1] == 7 || (totalGames[1] == 6 && totalGames[0] <= 4)) {
                    winner = 2;
                }
            }
            return winner;
        }

        public byte[] totalGames() {
            byte games1 = 0;
            byte games2 = 0;
            for(Game game : games) {
                byte w = game.getWinner();
                if(w == 1) {
                    games1++;
                }else if(w == 2) {
                    games2++;
                }
            }
            return new byte[] {games1, games2};
        }

        public byte point(byte player) {
            byte w = getCurrentGame().point(player);
            if(w != 0) {
                byte w1 = getWinner();
                if(w1 == 0) {
                    byte[] totalGames = totalGames();
                    boolean t = totalGames[0] == 6 && totalGames[1] == 6;
                    byte server = getCurrentGame().getCurrentPoint().server;
                    server = server == 1 ? (byte)2 : (byte)1;
                    games.add(new Game(t ? SET_TIEBREAK : NO_TIEBREAK, server));
                }
                return w1;
            }
            return 0;
        }

        public boolean removePoint() {
            if(getCurrentGame().removePoint()) {
                games.remove(games.size()-1);
                if(games.isEmpty()) {
                    return true;
                }else {
                    getCurrentGame().getCurrentPoint().winner = 0;
                    getCurrentGame().winner = 0;
                    return false;
                }
            }
            return false;
        }

        public double getWinProb(RealMatrix mg1, RealMatrix mg2, RealMatrix mt1, RealMatrix mt2, RealMatrix mmt1, RealMatrix mmt2, RealMatrix ms1, RealMatrix ms2) {
            byte w = getWinner();
            if(w == 1) {
                return 1;
            }else if(w == 2) {
                return 2;
            }else {
                if(tiebreak == MATCH_TIEBREAK) {
                    return getCurrentGame().getWinProb(mg1, mg2, mt1, mt2, mmt1, mmt2);
                }else {
                    byte server = games.get(0).points.get(0).server;
                    RealMatrix ms = server == 1 ? ms1 : ms2;
                    byte[] totalGames = totalGames();
                    byte si = totalGames[server-1];
                    byte sj = totalGames[server%2];
                    double psi = (si == 6 || (si == 5 && sj < 5)) ? 1 : ms.getEntry(MarkovMatrix.getSetMatrixIndex(si+1, sj), 0);
                    double psj = (sj == 6 || (sj == 5 && si < 5)) ? 0 : ms.getEntry(MarkovMatrix.getSetMatrixIndex(si, sj+1), 0);
                    double ps1 = server == 1 ? psi : 1 - psj;
                    double ps2 = server == 1 ? psj : 1 - psi;
                    double pg = getCurrentGame().getWinProb(mg1, mg2, mt1, mt2, mmt1, mmt2);
                    return pg * ps1 + (1 - pg) * ps2;
                }
            }
        }

    }

    public static class Game {

        public byte winner;
        public final ArrayList<Point> points;
        public final byte tiebreak;

        public Game(byte tiebreak, byte server) {
            this.winner = 0;
            this.points = new ArrayList<>();
            this.tiebreak = tiebreak;
            points.add(new Point(server));
        }

        public Game(byte tiebreak) {
            this.winner = 0;
            this.points = new ArrayList<>();
            this.tiebreak = tiebreak;
        }

        public Point getCurrentPoint() {
            return points.get(points.size()-1);
        }

        public byte getWinner() {
            if(winner != 0) {
                return winner;
            }

            byte[] totalPoints = totalPoints();

            if(Math.abs(totalPoints[0] - totalPoints[1]) >= 2) {
                byte minWinPoints = 4;
                if(tiebreak == SET_TIEBREAK) {
                    minWinPoints = 7;
                }else if(tiebreak == MATCH_TIEBREAK) {
                    minWinPoints = 10;
                }
                if(totalPoints[0] >= minWinPoints || totalPoints[1] >= minWinPoints) {
                    if(totalPoints[0] > totalPoints[1]) {
                        winner = 1;
                    }else {
                        winner = 2;
                    }
                }
            }
            return winner;
        }

        public byte[] totalPoints() {
            byte points1 = 0;
            byte points2 = 0;
            for(Point p : points) {
                if(p.winner == 1) {
                    points1++;
                }else if(p.winner == 2) {
                    points2++;
                }
            }
            return new byte[] {points1, points2};
        }

        public byte point(byte player) {
            getCurrentPoint().winner = player;
            byte server = getCurrentPoint().server;
            byte[] totalPoints = totalPoints();
            if(tiebreak != NO_TIEBREAK && (totalPoints[0] + totalPoints[1]) % 2 == 1) {
                server = server == 1 ? (byte)2 : (byte)1;
            }
            byte w = getWinner();
            if(w == 0) {
                points.add(new Point(server));
            }
            return w;
        }

        public boolean removePoint() {
            points.remove(points.size()-1);
            if(points.isEmpty()) {
                return true;
            }else {
                getCurrentPoint().winner = 0;
                return false;
            }
        }

        public double getWinProb(RealMatrix mg1, RealMatrix mg2, RealMatrix mt1, RealMatrix mt2, RealMatrix mmt1, RealMatrix mmt2) {
            byte w = getWinner();
            if(w == 1) {
                return 1;
            }else if(w == 2) {
                return 0;
            }else {
                byte server = points.get(0).server;
                byte[] totalPoints = totalPoints();
                byte si = totalPoints[server-1];
                byte sj = totalPoints[server%2];
                RealMatrix mg = server == 1 ? mg1 : mg2;
                if(tiebreak == SET_TIEBREAK) {
                    mg = server == 1 ? mt1 : mt2;
                }else if(tiebreak == MATCH_TIEBREAK) {
                    mg = server == 1 ? mmt1 : mmt2;
                }
                int index;
                if(tiebreak == NO_TIEBREAK) {
                    if(si >= 3 && sj >= 3) {
                        byte smax = (byte)Math.max(si, sj);
                        si = (byte)(si - smax + 2);
                        sj = (byte)(sj - smax + 2);
                    }
                    index = si * 4 + sj;
                }else if(tiebreak == SET_TIEBREAK) {
                    if(si >= 7 && sj >= 7) {
                        if(si % 2 == 1 && sj == si - 1) {
                            si = (byte)7;
                            sj = (byte)6;
                        }else if(si == sj - 1 && sj % 2 == 1) {
                            si = (byte)6;
                            sj = (byte)7;
                        }else {
                            si = (byte)(5 + (si+1) % 2);
                            sj = (byte)(5 + (sj+1) % 2);
                        }
                    }
                    if(si < 7 && sj < 7) {
                        index = si*7+sj;
                    }else {
                        if(si == 7) { // 7-6
                            index = 50;
                        }else {
                            index = 49;
                        }
                    }
                // }else if(tiebreak == MATCH_TIEBREAK) {
                }else {
                    if(si >= 10 && sj >= 10) {
                        if(si % 2 == 0 && sj == si - 1) {
                            si = (byte)10;
                            sj = (byte)9;
                        }else if(si == sj - 1 && sj % 2 == 0) {
                            si = (byte)9;
                            sj = (byte)10;
                        }else {
                            si = (byte)(8 + si % 2);
                            sj = (byte)(8 + sj % 2);
                        }
                    }
                    if(si < 10 && sj < 10) {
                        index = si*10+sj;
                    }else {
                        if(si == 10) { // 10-9
                            index = 101;
                        }else {
                            index = 100;
                        }
                    }
                }
                double pgi = mg.getEntry(index, 0);
                return server == 1 ? pgi : 1-pgi;
            }
        }

        public String[] stringScores() {
            byte[] totalPoints = totalPoints();
            if(tiebreak == NO_TIEBREAK) {
                boolean deuce = false;
                if(totalPoints[0] >= 4 && totalPoints[1] >= 4) {
                    byte pointsMax = (byte)Math.max(totalPoints[0], totalPoints[1]);
                    totalPoints[0] += 3 - pointsMax;
                    totalPoints[1] += 3 - pointsMax;
                    deuce = true;
                }
                String[] lookup = new String[] {"0", "15", "30", "40", "A"};
                return new String[] {lookup[totalPoints[0]], lookup[totalPoints[1]]};
            }else {
                return new String[] {totalPoints[0]+"", totalPoints[1]+""};
            }
        }

    }

    public static class Point {

        public byte winner;
        public float winProb;
        public float importance;
        public final byte server;

        public Point(byte server) {
            this.server = server;
            this.winner = 0;
        }

    }

}