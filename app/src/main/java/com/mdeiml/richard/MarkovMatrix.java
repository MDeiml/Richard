package com.mdeiml.richard;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

public class MarkovMatrix {
    
    public static RealMatrix getGamePropabilities(double p) {
        RealMatrix q = MatrixUtils.createRealMatrix(15, 15);
        for(int i = 0; i < 4; i++) {
            for(int j = 0; j < 4; j++) {
                if(i == 3 && j == 3) { // no 40-40
                    continue;
                }
                int index = i*4+j;
                if(i < 3 && (i < 2 || j < 3)) q.setEntry(index, (i+1)*4+j, p);
                if(j < 3 && (j < 2 || i < 3)) q.setEntry(index, i*4+j+1, 1-p);
            }
        }
        q.setEntry(3*4+2, 2*4+2, 1-p); // 40-30
        q.setEntry(2*4+3, 2*4+2, p); // 30-40;
        RealMatrix r = MatrixUtils.createRealMatrix(15, 2);
        for(int i = 0; i < 3; i++) {
            r.setEntry(3*4+i, 0, p);
            r.setEntry(i*4+3, 1, 1-p);
        }
        return getPropabilities(r, q);
    }
    
    public static RealMatrix getSetTiebreakPropabilities(double pi, double pj) {
        return getTiebreakPropabilities(pi, pj, 7);
    }
    
    public static RealMatrix getMatchTiebreakPropabilities(double pi, double pj) {
        return getTiebreakPropabilities(pi, pj, 10);
    }
    
    public static RealMatrix getTiebreakPropabilities(double pi, double pj, int n) {
        pj = 1-pj;
        RealMatrix q = MatrixUtils.createRealMatrix(n*n+2, n*n+2);
        for(int i = 0; i < n; i++) {
            for(int j = 0; j < n; j++) {
                int index = i*n+j;
                int serve = (i+j)%4;
                double p = serve == 0 || serve == 3 ? pi : pj;
                if(i < n-1) q.setEntry(index, (i+1)*n+j, p);
                if(j < n-1) q.setEntry(index, i*n+j+1, 1-p);
            }
        }
        int serve1 = (n-1+n-1)%4;
        double p1 = serve1 == 0 || serve1 == 3 ? pi : pj;
        q.setEntry((n-1)*n+n-1, n*n, 1-p1); // 6-6 -> 6-7
        q.setEntry((n-1)*n+n-1, n*n+1, p1); // 6-6 -> 7-6
        serve1 = (n+n-1)%4;
        p1 = serve1 == 0 || serve1 == 3 ? pi : pj;
        q.setEntry(n*n, (n-2)*n+(n-2), p1); // 6-7 -> 5-5
        q.setEntry(n*n+1, (n-2)*n+(n-2), 1-p1); // 7-6 -> 5-5
        RealMatrix r = MatrixUtils.createRealMatrix(n*n+2, 2);
        for(int i = 0; i < n-1; i++) {
            int serve = (n-1+i)%4;
            double p = serve == 0 || serve == 3 ? pi : pj;
            r.setEntry((n-1)*n+i, 0, p);
            r.setEntry(i*n+(n-1), 1, 1-p);
        }
        r.setEntry(n*n, 1, 1-p1); // 6-7
        r.setEntry(n*n+1, 0, p1); // 7-6
        return getPropabilities(r, q);
    }
    
    public static RealMatrix getSetPropabilities(double gi, double gj, double ti) {
        gj = 1-gj;
        RealMatrix q = MatrixUtils.createRealMatrix(39, 39);
        for(int i = 0; i < 6; i++) {
            for(int j = 0; j < 6; j++) {
                int index = i*6+j;
                int serve = (i+j) % 2;
                double p = serve == 0 ? gi : gj;
                if(i < 5) q.setEntry(index, (i+1)*6+j, p);
                if(j < 5) q.setEntry(index, i*6+j+1, 1-p);
            }
        }
        q.setEntry(5*6+5, 36, 1-gi); // 5-5 -> 5-6
        q.setEntry(5*6+5, 37, gi); // 5-5 -> 6-5
        q.setEntry(36, 38, gj); // 5-6 -> 6-6
        q.setEntry(37, 38, 1-gj); // 6-5 -> 6-6
        RealMatrix r = MatrixUtils.createRealMatrix(39, 2);
        for(int i = 0; i < 5; i++) {
            int serve = (5+i)%2;
            double p = serve == 0 ? gi : gj;
            r.setEntry(5*6+i, 0, p);
            r.setEntry(i*6+5, 1, 1-p);
        }
        r.setEntry(36, 1, 1-gj); // 5-6 -> 5-7
        r.setEntry(37, 0, gj); // 6-5 -> 7-5
        r.setEntry(38, 0, ti); // 6-6 -> 7-6
        r.setEntry(38, 1, 1-ti); // 6-6 -> 6-7
        return getPropabilities(r, q);
    }
    
    public static RealMatrix getMatchPropabilities(double si, double mi) {
        RealMatrix q = MatrixUtils.createRealMatrix(4, 4);
        q.setEntry(0, 1, 1-si); // 0-0 -> 0-1
        q.setEntry(0, 2, si); // 0-0 -> 1-0
        q.setEntry(1, 3, si); // 0-1 -> 1-1
        q.setEntry(2, 3, 1-si); // 1-0 -> 1-1
        RealMatrix r = MatrixUtils.createRealMatrix(4, 2);
        r.setEntry(1, 1, 1-si); // 0-1 -> 0-2
        r.setEntry(2, 0, si); // 1-0 -> 2-0
        r.setEntry(3, 0, mi); // 1-1 -> 2-1
        r.setEntry(3, 1, 1-mi); // 1-1 -> 2-1
        return getPropabilities(r, q);
    }
    
    public static int getMatchMatrixIndex(int setsI, int setsJ) {
        switch(setsI+"-"+setsJ) {
            case "0-0": return 0;
            case "0-1": return 1;
            case "1-0": return 2;
            case "1-1": return 3;
            default: throw new RuntimeException("FAIL");
        }
    }
    
    public static int getSetMatrixIndex(int gamesI, int gamesJ) {
        if(gamesI < 6 && gamesJ < 6) {
            return gamesI * 6 + gamesJ;
        }else if(gamesI == 5 && gamesJ == 6) {
            return 36;
        }else if(gamesI == 6 && gamesJ == 5) {
            return 37;
        }else {
            return 38;
        }
    }
    
    public static RealMatrix getPropabilities(RealMatrix r, RealMatrix q) {
        return MatrixUtils.inverse(MatrixUtils.createRealIdentityMatrix(q.getColumnDimension()).subtract(q)).multiply(r);
    }
    
    public static double[] approxP(double pm, double m) {
        double pipluspj = 2*pm;
        double piminpj = 0;
        double error = 1;
        int steps = 0;
        double maxerr = 0.0001;
        while(Math.abs(error) > maxerr) {
            steps++;
            if(steps > 100) break;
            double pi = (pipluspj + piminpj) / 2;
            double pj = (pipluspj - piminpj) / 2;
            
            double gi = getGamePropabilities(pi).getEntry(0, 0);
            double gj = getGamePropabilities(pj).getEntry(0, 0);
            double ti = getSetTiebreakPropabilities(pi, pj).getEntry(0, 0);
            double mi = getMatchTiebreakPropabilities(pi, pj).getEntry(0, 0);
            double si = getSetPropabilities(gi, gj, ti).getEntry(0, 0);
            double mapprox = getMatchPropabilities(si, mi).getEntry(0, 0);
            error = (mapprox-m)/m;
            if(Math.abs(error) > maxerr) {
                piminpj += (m-mapprox)*0.1f;
            }
        }
        return new double[] {(pipluspj + piminpj) / 2, (pipluspj - piminpj) / 2};
    }
    
}
