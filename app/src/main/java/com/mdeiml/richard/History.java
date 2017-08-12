package com.mdeiml.richard;

import java.util.ArrayList;
import org.apache.commons.math3.linear.RealMatrix;

public class History {

    public static final byte NO_TIEBREAK = 0;
    public static final byte SET_TIEBREAK = 1;
    public static final byte MATCH_TIEBREAK = 2;

    private byte winner;
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

    public History(String player1, String player2, double p1, double p2) {
        this.player1 = player1;
        this.player2 = player2;
        this.p1 = p1;
        this.p2 = p2;
        this.winner = 0;
        this.sets = new ArrayList<>();
    }

    public double getP1() {
        return p1;
    }

    public double getP2() {
        return p2;
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
        byte w = getCurrentSet().point(player);
        if(w != 0) {
            byte w1 = getWinner();
            if(w1 == 0) {
                byte[] totalSets = totalSets();
                boolean t = totalSets[0] == 1 && totalSets[1] == 1;
                byte server = getCurrentSet().getCurrentGame().getCurrentPoint().server;
                server = server == 1 ? (byte)2 : (byte)1;
                sets.add(new Set(t ? MATCH_TIEBREAK : NO_TIEBREAK, server));
            }
            return getWinner();
        }
        return 0;
    }

    public double getWinProb() {
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
            return ps * pm1 + (1 - ps) * pm2;
        }
    }

    public static class Set {

        private byte winner;
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
                return getWinner();
            }
            return 0;
        }

        public double getWinProb(RealMatrix mg1, RealMatrix mg2, RealMatrix mt1, RealMatrix mt2, RealMatrix mmt1, RealMatrix mmt2, RealMatrix ms1, RealMatrix ms2) {
            byte w = getWinner();
            if(w == 1) {
                return 1;
            }else if(w == 2) {
                return 2;
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

    public static class Game {

        private byte winner;
        public final ArrayList<Point> points;
        public final byte tiebreak;

        public Game(byte tiebreak, byte server) {
            this.winner = 0;
            this.points = new ArrayList<>();
            this.tiebreak = tiebreak;
            points.add(new Point(server));
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
                }else if(tiebreak == SET_TIEBREAK) {
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
            points.add(new Point(server));
            return getWinner();
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
                byte minWinPoints = 4;
                RealMatrix mg = server == 1 ? mg1 : mg2;
                if(tiebreak == SET_TIEBREAK) {
                    minWinPoints = 7;
                    mg = server == 1 ? mt1 : mt2;
                }else if(tiebreak == MATCH_TIEBREAK) {
                    minWinPoints = 10;
                    mg = server == 1 ? mmt1 : mmt2;
                }
                int index;
                if(tiebreak == NO_TIEBREAK) {
                    if(si >= minWinPoints || sj >= minWinPoints) {
                        byte smax = (byte)Math.max(si, sj);
                        si = (byte)(si - smax + minWinPoints - 1);
                        sj = (byte)(sj - smax + minWinPoints - 1);
                    }
                    index = si * minWinPoints + sj;
                }else {
                    if(si >= minWinPoints && sj >= minWinPoints) {
                        byte smax = (byte)Math.max(si, sj);
                        si = (byte)(si - smax + minWinPoints - 1);
                        sj = (byte)(sj - smax + minWinPoints - 1);
                    }
                    if(si == minWinPoints) {
                        index = minWinPoints * minWinPoints + 1;
                    }else if(sj == minWinPoints) {
                        index = minWinPoints * minWinPoints;
                    }else {
                        index = si * minWinPoints + sj;
                    }
                }
                double pgi = mg.getEntry(index, 0);
                return server == 1 ? pgi : 1-pgi;
            }
        }

    }

    public static class Point {

        public byte winner;
        public final byte server;

        public Point(byte server) {
            this.server = server;
            this.winner = 0;
        }

    }

}