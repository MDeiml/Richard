package com.mdeiml.richard

import android.database.*
import android.database.sqlite.*
import android.content.Context
import android.os.AsyncTask
import java.lang.ref.WeakReference
import java.util.LinkedList
import org.apache.commons.math3.linear.OpenMapRealMatrix
import org.apache.commons.math3.linear.ArrayRealVector
import org.apache.commons.math3.linear.RealVector
import org.apache.commons.math3.linear.SingularValueDecomposition

class CalculateEloTask(activity: NewGameActivity) : AsyncTask<String, Void, HashMap<String, Double>>() {

    private val dbHelper = SavedMatchesDbHelper(activity)
    private val sqlPlayerStats = activity.getResources().getString(R.string.sql_player_stats)
    private val ref = WeakReference(activity)
    
    override fun doInBackground(vararg players: String): HashMap<String, Double> {
        val db = dbHelper.getReadableDatabase()

        val queue = LinkedList<String>()
        val connectedPlayers = ArrayList<String>()
        val connections = HashMap<Pair<String, String>, Pair<Int, Double>>()

        queue.addAll(players)
        while (!queue.isEmpty()) {
            val player = queue.removeAt(0)
            val cursor = db.rawQuery(sqlPlayerStats, arrayOf(player, player))
            val otherI = cursor.getColumnIndexOrThrow("player")
            val countI = cursor.getColumnIndexOrThrow("count")
            val winsI = cursor.getColumnIndexOrThrow("wins")
            while (cursor.moveToNext()) {
                val other = cursor.getString(otherI)
                if (connectedPlayers.contains(other)) {
                    continue
                }
                val count = cursor.getInt(countI)
                if (count == 0) {
                    continue
                }
                val wins = cursor.getInt(winsI)
                val prob = (wins + 0.5) / (count + 1.0) // Add one draw to account for to little data
                val res = 400.0 * Math.log10(1.0 / prob - 1.0)
                connections.put(Pair(player, other), Pair(count, res))

                queue.add(other)
            }
            cursor.close()
            connectedPlayers.add(player)
        }

        val rows = connectedPlayers.size * (connectedPlayers.size + 1) / 2
        val matrix = OpenMapRealMatrix(rows, connectedPlayers.size)
        val b = ArrayRealVector(rows)

        var row = 0
        for ((i, name1) in connectedPlayers.withIndex()) {
            for ((j, name2) in connectedPlayers.drop(i).withIndex()) {
                val connection = connections.get(Pair(name1, name2)) ?: connections.get(Pair(name2, name1)) ?: Pair(1, 0.0)
                val (w, r) = connection
                matrix.setEntry(row, i, -w.toDouble())
                matrix.setEntry(row, j, w.toDouble())
                b.setEntry(row, w * r)
                row++
            }
        }
        
        val svd = SingularValueDecomposition(matrix)
        val x = svd.getSolver().solve(b)

        val result = HashMap<String, Double>()
        for ((i, name) in connectedPlayers.withIndex()) {
            result.put(name, x.getEntry(i))
        }
        
        return result
    }

    override fun onPostExecute(result: HashMap<String, Double>) {
        val a = ref.get()
        if (a != null) {
            a.addElos(result)
        }
    }
}
