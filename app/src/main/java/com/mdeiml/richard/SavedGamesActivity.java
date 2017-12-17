package com.mdeiml.richard;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.util.Locale;

public class SavedGamesActivity extends AppCompatActivity {

    private ListView savedGamesList;
    private SavedMatchesDbHelper dbHelper;
    private FloatingActionButton newGameButton;
    private ActionMode actionMode;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.saved_games);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        newGameButton = (FloatingActionButton)findViewById(R.id.new_game_button);
        newGameButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(SavedGamesActivity.this, NewGameActivity.class);
                startActivity(i);
            }
        });

        dbHelper = new SavedMatchesDbHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("matches", new String[] {"match_id AS _id", "match_player1", "match_player2"}, null, null, null, null, "match_id");
        final int idIndex = cursor.getColumnIndex("_id");
        final int player1Index = cursor.getColumnIndex("match_player1");
        final int player2Index = cursor.getColumnIndex("match_player2");

        savedGamesList = (ListView)findViewById(R.id.saved_games_list);
        savedGamesList.setItemsCanFocus(false);
        savedGamesList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        savedGamesList.setMultiChoiceModeListener(new ListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {}

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch(item.getItemId()) {
                    case R.id.delete:
                        long[] checkedIds = savedGamesList.getCheckedItemIds();

                        for(int i = 0; i < checkedIds.length; i++) {
                            dbHelper.deleteMatch(checkedIds[i]);
                        }
                        break;
                }
                mode.finish();
                updateAdapter();
                return true;
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.games_context_menu, menu);
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {}

            @Override 
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

        });
        savedGamesList.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getApplicationContext(), MatchActivity.class);
                i.putExtra("match_id", id);
                startActivity(i);
            }
        });
        savedGamesList.setAdapter(new CursorAdapter(this, cursor, false) {
            public void bindView(View view, Context context, Cursor cursor) {
                final long matchId = cursor.getLong(idIndex);
                final int position = cursor.getPosition();
                TextView sgPlayer1 = (TextView)view.findViewById(R.id.saved_game_player1);
                TextView sgPlayer2 = (TextView)view.findViewById(R.id.saved_game_player2);
                TextView[][] sgSets = new TextView[][] {
                    {
                        (TextView)view.findViewById(R.id.saved_game_set11),
                        (TextView)view.findViewById(R.id.saved_game_set21),
                    },
                    {
                        (TextView)view.findViewById(R.id.saved_game_set12),
                        (TextView)view.findViewById(R.id.saved_game_set22)
                    }
                };
                TextView sgPoints1 = view.findViewById(R.id.saved_game_points1);
                TextView sgPoints2 = view.findViewById(R.id.saved_game_points2);
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
                    "game_winner != 0 AND game_match = ?",
                    new String[] {matchId+""},
                    "game_set, game_winner",
                    null,
                    null
                );
                int countIndex = setCursor.getColumnIndex("count");
                int setIndex = setCursor.getColumnIndex("game_set");
                int winnerIndex = setCursor.getColumnIndex("game_winner");

                while(setCursor.moveToNext()) {
                    int w = setCursor.getInt(winnerIndex)-1;
                    int s = setCursor.getInt(setIndex);
                    if(s < sgSets[w].length) {
                        sgSets[w][s].setText(String.format(Locale.getDefault(), "%d", setCursor.getInt(countIndex)));
                    }
                }

                setCursor.close();

                Cursor gameCursor = db.query(
                    "games",
                    new String[] {"game_set", "game_nr", "game_tiebreak"},
                    "game_winner = 0 AND game_match = ?",
                    new String[] {matchId+""},
                    null,
                    null,
                    "game_set ASC, game_nr ASC",
                    "1"
                );

                if(gameCursor.moveToNext()) {
                    int gameSet = gameCursor.getInt(gameCursor.getColumnIndexOrThrow("game_set"));
                    int gameNr = gameCursor.getInt(gameCursor.getColumnIndexOrThrow("game_nr"));
                    int tiebreak = gameCursor.getInt(gameCursor.getColumnIndexOrThrow("game_tiebreak"));
                    gameCursor.close();

                    gameCursor = db.query(
                        "points",
                        new String[] {"COUNT(*) as count", "point_winner"},
                        "point_winner != 0 AND point_match = ? AND point_set = ? AND point_game = ?",
                        new String[] {matchId+"", gameSet+"", gameNr+""},
                        "point_winner",
                        null,
                        null
                    );

                    int points1 = 0;
                    int points2 = 0;
                    winnerIndex = gameCursor.getColumnIndex("point_winner");
                    countIndex = gameCursor.getColumnIndex("count");
                    while(gameCursor.moveToNext()) {
                        if(gameCursor.getInt(winnerIndex) == 1) {
                            points1 = gameCursor.getInt(countIndex);
                        }else {
                            points2 = gameCursor.getInt(countIndex);
                        }
                    }
                    if(points1 != 0 || points2 != 0) {
                        if(tiebreak == Match.NO_TIEBREAK) {
                            if(points1 >= 4 && points2 >= 4) {
                                byte pointsMax = (byte)Math.max(points1, points2);
                                points1 += 3 - pointsMax;
                                points2 += 3 - pointsMax;
                            }
                            sgPoints1.setText(Match.POINT_NAMES[points1]);
                            sgPoints2.setText(Match.POINT_NAMES[points2]);
                        }else {
                            sgPoints1.setText(String.format(Locale.getDefault(), "%d", points1));
                            sgPoints2.setText(String.format(Locale.getDefault(), "%d", points2+""));
                        }
                    }
                }
            }
            
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                LayoutInflater inflater = LayoutInflater.from(context);
                return inflater.inflate(R.layout.saved_game, parent, false);
            }
        });
    }
    
    private void updateAdapter() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("matches", new String[] {"match_id AS _id", "match_player1", "match_player2"}, null, null, null, null, "match_id");
        ((CursorAdapter)savedGamesList.getAdapter()).changeCursor(cursor);
    }

}
