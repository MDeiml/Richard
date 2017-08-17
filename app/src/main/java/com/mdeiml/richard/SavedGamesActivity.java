package com.mdeiml.richard;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class SavedGamesActivity extends AppCompatActivity {

    private ListView savedGamesList;
    private SavedMatchesDbHelper dbHelper;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.saved_games);

        dbHelper = new SavedMatchesDbHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("matches", new String[] {"match_id AS _id", "match_player1", "match_player2"}, null, null, null, null, "match_id");
        final int idIndex = cursor.getColumnIndex("_id");
        final int player1Index = cursor.getColumnIndex("match_player1");
        final int player2Index = cursor.getColumnIndex("match_player2");

        savedGamesList = (ListView)findViewById(R.id.saved_games_list);
        savedGamesList.setAdapter(new CursorAdapter(this, cursor) {
            public void bindView(View view, Context context, Cursor cursor) {
                final long matchId = cursor.getLong(idIndex);
                view.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Intent i = new Intent(getApplicationContext(), MatchActivity.class);
                        i.putExtra("match_id", matchId);
                        startActivity(i);
                    }
                });

                TextView sgPlayer1 = (TextView)view.findViewById(R.id.saved_game_player1);
                TextView sgPlayer2 = (TextView)view.findViewById(R.id.saved_game_player2);
                TextView[][] sgSets = new TextView[][] {
                    {
                        (TextView)view.findViewById(R.id.saved_game_set11),
                        (TextView)view.findViewById(R.id.saved_game_set21),
                        (TextView)view.findViewById(R.id.saved_game_set31)
                    },
                    {
                        (TextView)view.findViewById(R.id.saved_game_set12),
                        (TextView)view.findViewById(R.id.saved_game_set22),
                        (TextView)view.findViewById(R.id.saved_game_set32),
                    }
                };
                for(int i = 0; i < sgSets.length; i++) {
                    for(int j = 0; j < sgSets[i].length; j++) {
                        sgSets[i][j].setText("0");
                    }
                }

                sgPlayer1.setText(cursor.getString(player1Index));
                sgPlayer2.setText(cursor.getString(player2Index));
                
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                Cursor setCursor = db.query(
                    "games",
                    new String[] {"COUNT(*) as count", "game_set", "game_winner"},
                    "game_match = ?",
                    new String[] {matchId+""},
                    "game_set, game_winner",
                    null,
                    null
                );
                int countIndex = setCursor.getColumnIndex("count");
                int setIndex = setCursor.getColumnIndex("game_set");
                int winnerIndex = setCursor.getColumnIndex("game_winner");

                while(setCursor.moveToNext()) {
                    if(setCursor.getInt(winnerIndex) == 0)
                        continue;
                    sgSets[setCursor.getInt(winnerIndex)-1][setCursor.getInt(setIndex)].setText(setCursor.getInt(countIndex)+"");
                }
            }
            
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                LayoutInflater inflater = LayoutInflater.from(context);
                return inflater.inflate(R.layout.saved_game, parent, false);
            }
        });
    }

}