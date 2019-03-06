package com.mdeiml.richard

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.app.FragmentTransaction
import android.support.v4.app.NavUtils
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View.OnClickListener
import android.view.View
import android.widget.Button
import android.widget.TextView
import kotlinx.android.synthetic.main.match.*

class MatchActivity : AppCompatActivity() {
    
    private lateinit var dbHelper: SavedMatchesDbHelper
    lateinit var match: Match
        private set
    private var matchFragment: MatchFragment? = null
    private var statisticsFragment: StatisticsFragment? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.match)

        setSupportActionBar(toolbar)

        dbHelper = SavedMatchesDbHelper(this)

        val i = getIntent()
        if (savedInstanceState != null && savedInstanceState.containsKey("match")) {
            val id = savedInstanceState.getLong("match")
            match = dbHelper.loadMatch(id) ?: throw IllegalArgumentException("Match with id $id not found")
        } else if(i.hasExtra("match_id")) {
            val id = i.getLongExtra("match_id", 0)
            match = dbHelper.loadMatch(id) ?: throw IllegalArgumentException("Match with id $id not found")
        } else {
            val pref = getSharedPreferences("com.mdeiml.richard", MODE_PRIVATE)
            val nameI = i.getStringExtra("nameI").trim().let { if (it.isEmpty()) null else it }
            val nameJ = i.getStringExtra("nameJ").trim().let { if (it.isEmpty()) null else it }

            val pmean = pref.getFloat("pmean", 0.6f)
            val (pi, pj) = MarkovMatrix.approxP(pmean.toDouble(), i.getDoubleExtra("m", 0.5), 3, true)
            match = Match(nameI, nameJ, pi, pj)
        }

        matchPager.setAdapter(object : FragmentStatePagerAdapter(getSupportFragmentManager()) {
            override fun getItem(i: Int) = when (i) {
                0 -> MatchFragment().also { matchFragment = it }
                1 -> StatisticsFragment().also { statisticsFragment = it }
                else -> null
            }

            override fun getCount() = 2

            override fun getPageTitle(i: Int) = getApplicationContext().getResources().getStringArray(R.array.tab_titles)[i]
        })

        tabs.setupWithViewPager(matchPager)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = getMenuInflater()
        inflater.inflate(R.menu.match, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            R.id.undo -> {
                match.removePoint()
                redraw()
                return true
            }
            else -> return false
        }
    }

    fun redraw() {
        matchFragment?.redraw()
        statisticsFragment?.redraw()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.i("MatchActivity", "Match saved")
        dbHelper.saveMatch(match)
        outState.putLong("match", match.matchId!!)
    }

    override fun onBackPressed() {
        Log.i("MatchActivity", "Match saved")
        dbHelper.saveMatch(match)
        NavUtils.navigateUpFromSameTask(this)
    }
    
}
