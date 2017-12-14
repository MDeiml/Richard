package com.mdeiml.richard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.util.ArrayList;
import android.support.v4.app.NavUtils;

public class MatchActivity extends AppCompatActivity {

    public static final String[] tabTitles = new String[]{"Match", "Statistiken"};
    
    private SavedMatchesDbHelper dbHelper;
    private Match match;
    private String player1;
    private String player2;
    private MatchFragment matchFragment;
    private StatisticsFragment statisticsFragment;
    private ViewPager pager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.match);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        dbHelper = new SavedMatchesDbHelper(this);

        Intent i = getIntent();
        if(savedInstanceState != null && savedInstanceState.containsKey("match")) {
            match = dbHelper.loadMatch(savedInstanceState.getLong("match"));
        }else if(i.hasExtra("match_id")) {
            match = dbHelper.loadMatch(i.getLongExtra("match_id", 0));
        }else {
            SharedPreferences pref = getSharedPreferences("com.mdeiml.richard", MODE_PRIVATE);
            String nameI = i.getStringExtra("nameI").trim();
            String nameJ = i.getStringExtra("nameJ").trim();
            nameI = nameI.isEmpty() ? "Spieler A" : nameI;
            nameJ = nameJ.isEmpty() ? "Spieler B" : nameJ;

            double pmean = pref.getFloat("pmean", 0.6f);
            double[] p = MarkovMatrix.approxP(pmean, i.getDoubleExtra("m", 0.5));
            double pi = p[0];
            double pj = p[1];
            match = new Match(nameI, nameJ, pi, pj);
        }

        pager = (ViewPager)findViewById(R.id.match_pager);
        pager.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int i) {
                switch(i) {
                    case 0:
                        matchFragment = new MatchFragment();
                        return matchFragment;
                    case 1:
                        statisticsFragment = new StatisticsFragment();
                        return statisticsFragment;
                }
                return null;
            }

            @Override
            public int getCount() {
                return 2;
            }

            @Override
            public String getPageTitle(int i) {
                return tabTitles[i];
            }
        });

        TabLayout tabLayout = (TabLayout)findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(pager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.match, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.save_game:
                dbHelper.saveMatch(match);
                return true;
            case R.id.undo:
                match.removePoint();
                redraw();
                return true;
            default:
                return false;
        }
    }

    public Match getMatch() {
        return match;
    }

    public void redraw() {
        if(matchFragment != null) matchFragment.redraw();
        if(statisticsFragment != null) statisticsFragment.redraw();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i("MatchActivity", "Match saved");
        dbHelper.saveMatch(match);
        outState.putLong("match", match.matchId);
    }

    @Override
    public void onBackPressed() {
        Log.i("MatchActivity", "Match saved");
        dbHelper.saveMatch(match);
        NavUtils.navigateUpFromSameTask(this);
    }
    
}
