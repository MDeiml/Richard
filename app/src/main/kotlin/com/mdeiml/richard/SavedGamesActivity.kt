package com.mdeiml.richard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.ActionMode
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.AbsListView
import kotlinx.android.synthetic.main.saved_games.*

class SavedGamesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.saved_games)

        setSupportActionBar(toolbar)

        new_game_button.setOnClickListener {
            val i = Intent(this, NewGameActivity::class.java)
            startActivity(i)
        }

        val dbHelper = SavedMatchesDbHelper(this)

        savedGamesList.setItemsCanFocus(false)
        savedGamesList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL)
        savedGamesList.setMultiChoiceModeListener(object : AbsListView.MultiChoiceModeListener {
            override fun onItemCheckedStateChanged(mode: ActionMode, position: Int, id: Long, checked: Boolean) {}

            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                when (item.getItemId()) {
                    R.id.delete -> {
                        val checkedIds = savedGamesList.getCheckedItemIds()

                        for (checkedId in checkedIds) {
                            dbHelper.deleteMatch(checkedId)
                        }
                    }
                }
                mode.finish()
                (savedGamesList.getAdapter() as SavedGamesAdapter).updateCursor(this@SavedGamesActivity)
                return true
            }

            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                val inflater = mode.getMenuInflater()
                inflater.inflate(R.menu.games_context_menu, menu)
                return true
            }

            override fun onDestroyActionMode(mode: ActionMode) {}

            override fun onPrepareActionMode(mode: ActionMode, menu: Menu) = false

        })
        savedGamesList.setOnItemClickListener { _: AdapterView<*>, _: View, _: Int, id: Long ->
            val i = Intent(getApplicationContext(), MatchActivity::class.java)
            i.putExtra("match_id", id)
            startActivity(i)
        }
        savedGamesList.setAdapter(SavedGamesAdapter(this, dbHelper))
    }

}
