package com.mdeiml.richard;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

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
        savedGamesList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        savedGamesList.setAdapter(new CursorAdapter(this, cursor) {
            public void bindView(View view, Context context, Cursor cursor) {
                final long matchId = cursor.getLong(idIndex);
                final int position = cursor.getPosition();
                view.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if(actionMode == null) {
                            Intent i = new Intent(getApplicationContext(), MatchActivity.class);
                            i.putExtra("match_id", matchId);
                            startActivity(i);
                        }else {
                            SparseBooleanArray checked = savedGamesList.getCheckedItemPositions();
                            savedGamesList.setItemChecked(position, !checked.get(position));
                            updateActionMode();
                        }
                    }
                });
                view.setOnLongClickListener(new View.OnLongClickListener() {
                    public boolean onLongClick(View v) {
                        SparseBooleanArray checked = savedGamesList.getCheckedItemPositions();
                        savedGamesList.setItemChecked(position, !checked.get(position));
                        updateActionMode();
                        return true;
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

    private void updateActionMode() {
        SparseBooleanArray checked = savedGamesList.getCheckedItemPositions();
        boolean hasCheckedItem = false;
        for(int i = 0; i < savedGamesList.getAdapter().getCount(); i++) {
            if(checked.get(i)) {
                hasCheckedItem = true;
            }
        }

        if(hasCheckedItem) {
            if(actionMode == null) {
                actionMode = startSupportActionMode(new ModeCallback());
            }
        }else {
            if(actionMode != null) {
                actionMode.finish();
            }
        }
    }

    private final class ModeCallback implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.games_context_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            for(int i = 0; i < savedGamesList.getAdapter().getCount(); i++) {
                savedGamesList.setItemChecked(i, false);
            }
            if(actionMode == mode) {
                actionMode = null;
            }
        }
    };

}
