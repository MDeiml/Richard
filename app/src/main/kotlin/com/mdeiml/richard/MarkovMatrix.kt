package com.mdeiml.richard

import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.RealMatrix
import org.apache.commons.math3.linear.RealVector

object MarkovMatrix {
    
    fun getGamePropabilities(p: Double): RealVector {
        val q = MatrixUtils.createRealMatrix(15, 15)
        for (i in 0..3) {
            for(j in 0..3) {
                if(i == 3 && j == 3) { // no 40-40
                    continue
                }
                val index = i*4+j
                if(i < 3 && (i < 2 || j < 3)) q.setEntry(index, (i+1)*4+j, p)
                if(j < 3 && (j < 2 || i < 3)) q.setEntry(index, i*4+j+1, 1-p)
            }
        }
        q.setEntry(3*4+2, 2*4+2, 1-p) // 40-30
        q.setEntry(2*4+3, 2*4+2, p) // 30-40
        val r = MatrixUtils.createRealMatrix(15, 2)
        for(i in 0..2) {
            r.setEntry(3*4+i, 0, p)
            r.setEntry(i*4+3, 1, 1-p)
        }
        return getPropabilities(r, q)
    }
    
    fun getSetTiebreakPropabilities(pi: Double, pj: Double) = getTiebreakPropabilities(pi, pj, 7)
    
    fun getMatchTiebreakPropabilities(pi: Double, pj: Double) = getTiebreakPropabilities(pi, pj, 10)
    
    fun getTiebreakPropabilities(pi: Double, pj: Double, n: Int): RealVector {
        val pjI = 1-pj
        val q = MatrixUtils.createRealMatrix(n*n+2, n*n+2)
        for (i in 0..n-1) {
            for (j in 0..n-1) {
                val index = i*n+j
                val serve = (i+j)%4
                val p = if (serve == 0 || serve == 3) pi else pjI
                if(i < n-1) q.setEntry(index, (i+1)*n+j, p)
                if(j < n-1) q.setEntry(index, i*n+j+1, 1-p)
            }
        }
        val serve1 = (n-1+n-1)%4
        val p1 = if (serve1 == 0 || serve1 == 3) pi else pjI
        q.setEntry((n-1)*n+n-1, n*n, 1-p1) // 6-6 -> 6-7
        q.setEntry((n-1)*n+n-1, n*n+1, p1) // 6-6 -> 7-6
        val serve2 = (n+n-1)%4
        val p2 = if (serve2 == 0 || serve2 == 3) pi else pjI
        q.setEntry(n*n, (n-2)*n+(n-2), p2) // 6-7 -> 5-5
        q.setEntry(n*n+1, (n-2)*n+(n-2), 1-p2) // 7-6 -> 5-5
        val r = MatrixUtils.createRealMatrix(n*n+2, 2)
        for(i in 0..n-2) {
            val serve = (n-1+i)%4
            val p = if (serve == 0 || serve == 3) pi else pjI
            r.setEntry((n-1)*n+i, 0, p)
            r.setEntry(i*n+(n-1), 1, 1-p)
        }
        r.setEntry(n*n, 1, 1-p2) // 6-7
        r.setEntry(n*n+1, 0, p2) // 7-6
        return getPropabilities(r, q)
    }
    
    fun getSetPropabilities(gi: Double, gj: Double, ti: Double): RealVector {
        val gjI = 1-gj
        val q = MatrixUtils.createRealMatrix(39, 39)
        for(i in 0..5) {
            for(j in 0..5) {
                val index = i*6+j
                val serve = (i+j) % 2
                val p = if (serve == 0) gi else gjI
                if(i < 5) q.setEntry(index, (i+1)*6+j, p)
                if(j < 5) q.setEntry(index, i*6+j+1, 1-p)
            }
        }
        q.setEntry(5*6+5, 36, 1-gi) // 5-5 -> 5-6
        q.setEntry(5*6+5, 37, gi) // 5-5 -> 6-5
        q.setEntry(36, 38, gjI) // 5-6 -> 6-6
        q.setEntry(37, 38, gj) // 6-5 -> 6-6
        val r = MatrixUtils.createRealMatrix(39, 2)
        for(i in 0..4) {
            val serve = (5+i)%2
            val p = if (serve == 0) gi else gjI
            r.setEntry(5*6+i, 0, p)
            r.setEntry(i*6+5, 1, 1-p)
        }
        r.setEntry(36, 1, gj) // 5-6 -> 5-7
        r.setEntry(37, 0, gjI) // 6-5 -> 7-5
        r.setEntry(38, 0, ti) // 6-6 -> 7-6
        r.setEntry(38, 1, 1-ti) // 6-6 -> 6-7
        return getPropabilities(r, q)
    }
    
    fun getMatchPropabilities(si: Double, mi: Double, sets: Int, tiebreak: Boolean): RealVector {
        if(sets % 2 != 1) throw IllegalArgumentException("Only odd number of sets are allowed")
        val sets0 = sets / 2
        val n = sets0 + 1
        val q = MatrixUtils.createRealMatrix(n * n, n * n)
        val r = MatrixUtils.createRealMatrix(n * n, 2)
        for(i in 0..n-1) {
            for(j in 0..n-1) {
                val index = i * n + j
                if (i < n - 1) {
                    q.setEntry(index, (i+1) * n + j, si) //i-j -> (i+1)-j
                } else {
                    if (tiebreak && j == n - 1) {
                        r.setEntry(index, 0, mi)
                    } else {
                        r.setEntry(index, 0, si)
                    }
                }

                if (j < n - 1) {
                    q.setEntry(index, i * n + (j+1), 1-si) //i-j -> i-(j+1)
                } else {
                    if (tiebreak && i == n - 1) {
                        r.setEntry(index, 1, 1-mi)
                    } else {
                        r.setEntry(index, 1, 1-si)
                    }
                }
            }
        }
        return getPropabilities(r, q)
    }
    
    fun getMatchMatrixIndex(setsI: Int, setsJ: Int, n: Int) = setsI * (n / 2 + 1) + setsJ
    
    fun getSetMatrixIndex(gamesI: Int, gamesJ: Int) = when (Pair(gamesI, gamesJ)) {
        Pair(5, 6) -> 36
        Pair(6, 5) -> 37
        Pair(6, 6) -> 38
        else -> gamesI * 6 + gamesJ
    }

    fun getGameMatrixIndex(si: Int, sj: Int): Int {
        val smax = if (si >= 3 && sj >= 3) Math.max(si, sj) + 2 else 0
        return (si - smax) * 4 + (sj - smax)
    }

    fun getSetTiebreakMatrixIndex(si: Int, sj: Int): Int {
        val (si0, sj0) = if(si >= 7 && sj >= 7) {
            when {
                si % 2 == 1 && sj == si - 1 -> Pair(7, 6)
                si == sj - 1 && sj % 2 == 1 -> Pair(6, 7)
                else -> Pair(5 + (si + 1) % 2, 5 + (sj + 1) % 2)
            }
        } else Pair(si, sj)
        return when {
            si0 < 7 && sj0 < 7 -> si0*7+sj0
            si0 == 7 -> 50 // 7-6
            else -> 49
        }
    }
    
    fun getMatchTiebreakMatrixIndex(si: Int, sj: Int): Int {
        val (si0, sj0) = if (si >= 10 && sj >= 10) {
            when {
                si % 2 == 0 && sj == si - 1 -> Pair(10, 9)
                si == sj - 1 && sj % 2 == 0 -> Pair(9, 10)
                else -> Pair(8 + si % 2, 8 + sj % 2)
            }
        } else Pair(si, sj)
        return when {
            si0 < 10 && sj0 < 10 -> si0*10+sj0
            si0 == 10 -> 101 // 10-9
            else -> 100
        }
    }
    
    fun getPropabilities(r: RealMatrix, q: RealMatrix): RealVector {
        return MatrixUtils.inverse(MatrixUtils.createRealIdentityMatrix(q.getColumnDimension()).subtract(q)).multiply(r).getColumnVector(0)
    }
    
    fun approxP(pm: Double, m: Double, sets: Int, tiebreak: Boolean): Pair<Double, Double> {
        val pipluspj = 2*pm
        var piminpj = 0.0
        val maxerr = 0.0001
        for (steps in 0..99) {
            val pi = (pipluspj + piminpj) / 2
            val pj = (pipluspj - piminpj) / 2
            
            val gi = getGamePropabilities(pi).getEntry(0)
            val gj = getGamePropabilities(pj).getEntry(0)
            val ti = getSetTiebreakPropabilities(pi, pj).getEntry(0)
            val mi = getMatchTiebreakPropabilities(pi, pj).getEntry(0)
            val si = getSetPropabilities(gi, gj, ti).getEntry(0)
            val mapprox = getMatchPropabilities(si, mi, sets, tiebreak).getEntry(0)
            val error = (mapprox-m)/m
            if(Math.abs(error) > maxerr) {
                piminpj += (m-mapprox)*0.1f
            } else {
                break
            }
        }

        return Pair((pipluspj + piminpj) / 2, (pipluspj - piminpj) / 2)
    }
    
}
