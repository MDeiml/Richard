package com.mdeiml.richard;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

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
                ((SavedGamesAdapter) savedGamesList.getAdapter()).updateCursor(SavedGamesActivity.this);
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
        savedGamesList.setAdapter(new SavedGamesAdapter(this, dbHelper));
    }

}
