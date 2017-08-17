package com.mdeiml.richard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.util.ArrayList;

public class MatchActivity extends AppCompatActivity {

    public static final String[] tabTitles = new String[]{"Match"};
    
    private SavedMatchesDbHelper dbHelper;
    private Match match;
    private String player1;
    private String player2;
    private MatchFragment matchFragment;
    private ViewPager pager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.match);

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
                }
                return null;
            }

            @Override
            public int getCount() {
                return 1;
            }

            @Override
            public String getPageTitle(int i) {
                return tabTitles[i];
            }
        });

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {

            }
            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

            }
            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

            }
        };

        for(int j = 0; j < 1; j++) {
            actionBar.addTab(actionBar.newTab().setText(tabTitles[j]).setTabListener(tabListener));
        }
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
            default:
                return false;
        }
    }

    public Match getMatch() {
        return match;
    }

    public void updateProbs() {
        double p = match.getWinProb();
        double imp = match.importance();
        if(matchFragment != null) matchFragment.updateProbs(p, imp);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i("MatchActivity", "Match saved");
        dbHelper.saveMatch(match);
        outState.putLong("match", match.matchId);
    }
    
}
