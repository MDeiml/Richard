package com.mdeiml.richard;

import android.database.*;
import android.database.sqlite.*;
import android.content.Context;
import android.os.AsyncTask;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;

public class CalculateEloTask extends AsyncTask<String, Void, HashMap<String, Double>> {

    private SavedMatchesDbHelper dbHelper;
    private String sqlPlayerStats;
    private WeakReference<NewGameActivity> activity;

    public CalculateEloTask(NewGameActivity activity) {
        this.dbHelper = new SavedMatchesDbHelper(activity);
        this.sqlPlayerStats = activity.getResources().getString(R.string.sql_player_stats);
        this.activity = new WeakReference(activity);
    }
    
    protected HashMap<String, Double> doInBackground(String... players) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        LinkedList<String> queue = new LinkedList<>();
        ArrayList<String> connectedPlayers = new ArrayList<>();
        HashMap<Pair<String, String>, Pair<Integer, Double>> connections = new HashMap<>();

        queue.addAll(Arrays.asList(players));
        while (!queue.isEmpty()) {
            String player = queue.remove(0);
            Cursor cursor = db.rawQuery(sqlPlayerStats, new String[] { player, player });
            int otherI = cursor.getColumnIndexOrThrow("player");
            int countI = cursor.getColumnIndexOrThrow("count");
            int winsI = cursor.getColumnIndexOrThrow("wins");
            while (cursor.moveToNext()) {
                String other = cursor.getString(otherI);
                if (connectedPlayers.contains(other)) {
                    continue;
                }
                int count = cursor.getInt(countI);
                if (count == 0) {
                    continue;
                }
                int wins = cursor.getInt(winsI);
                double prob = (wins + 0.5) / (count + 1); // Add one draw to account for to little data
                double res = 400 * Math.log10(1 / prob - 1);
                connections.put(new Pair<>(player, other), new Pair(count, res));

                queue.add(other);
            }
            cursor.close();
            connectedPlayers.add(player);
        }

        int rows = connectedPlayers.size() * (connectedPlayers.size() + 1) / 2;
        OpenMapRealMatrix matrix = new OpenMapRealMatrix(rows, connectedPlayers.size());
        ArrayRealVector b = new ArrayRealVector(rows);

        int row = 0;
        for (int i = 0; i < connectedPlayers.size(); i++) {
            for (int j = i + 1; j < connectedPlayers.size(); j++) {
                String name1 = connectedPlayers.get(i);
                String name2 = connectedPlayers.get(j);
                Pair<Integer, Double> connection = connections.get(new Pair(name1, name2));
                if (connection == null) {
                    connection = connections.get(new Pair(name2, name1));
                    if (connection == null) {
                        connection = new Pair(1, 0.0); // Add one draw
                    }
                }
                int w = connection.getFirst();
                double r = connection.getSecond();
                matrix.setEntry(row, i, -w);
                matrix.setEntry(row, j, w);
                b.setEntry(row, w * r);
                row++;
            }
        }
        
        SingularValueDecomposition svd = new SingularValueDecomposition(matrix);
        RealVector x = svd.getSolver().solve(b);

        HashMap<String, Double> result = new HashMap<>();
        for (int i = 0; i < connectedPlayers.size(); i++) {
            result.put(connectedPlayers.get(i), x.getEntry(i));
        }
        
        return result;
    }

    public void onPostExecute(HashMap<String, Double> result) {
        NewGameActivity a = activity.get();
        if (a != null) {
            a.addElos(result);
        }
    }
}
