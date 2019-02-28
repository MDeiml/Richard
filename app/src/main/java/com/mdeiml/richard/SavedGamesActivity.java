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
import android.widget.TableRow;
import android.util.TypedValue;

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
        Cursor cursor = db.query("matches", new String[] {"match_id AS _id"}, null, null, null, null, "match_id");
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
                TextView sgPoints1 = view.findViewById(R.id.saved_game_points1);
                TextView sgPoints2 = view.findViewById(R.id.saved_game_points2);
                TableRow sgSets1 = (TableRow)view.findViewById(R.id.saved_game_sets1);
                TableRow sgSets2 = (TableRow)view.findViewById(R.id.saved_game_sets2);

                Match match = dbHelper.loadMatch(matchId);
                sgPlayer1.setText(match.player1);
                sgPlayer2.setText(match.player2);

                while (sgSets1.getChildCount() > 2) {
                    sgSets1.removeViewAt(1);
                }
                while (sgSets2.getChildCount() > 2) {
                    sgSets2.removeViewAt(1);
                }

                int numSets = match.getCurrentSetNr()+1;
                byte[][] games = match.getGames();
                for(int i = 0; i < numSets; i++) {
                    TextView tv0 = new TextView(SavedGamesActivity.this);
                    tv0.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                    tv0.setText(String.format(Locale.getDefault(), "%d", games[i][0]));
                    tv0.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
                    sgSets1.addView(tv0, i+1);
                    
                    TextView tv1 = new TextView(SavedGamesActivity.this);
                    tv1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                    tv1.setText(String.format(Locale.getDefault(), "%d", games[i][1]));
                    tv1.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
                    sgSets2.addView(tv1, i+1);
                }

                String[] scores = match.getCurrentSet().getCurrentGame().stringScores();
                if(scores[0] != "0" || scores[1] != "0") {
                    sgPoints1.setText(scores[0]);
                    sgPoints2.setText(scores[1]);
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
