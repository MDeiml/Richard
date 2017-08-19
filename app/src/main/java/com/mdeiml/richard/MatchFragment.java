package com.mdeiml.richard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import java.util.ArrayList;

public class MatchFragment extends Fragment {
    
    private Button buttonI;
    private Button buttonJ;
    private TextView propI;
    private TextView propJ;
    private TextView pointsI;
    private TextView pointsJ;
    private TextView gamesI0;
    private TextView gamesJ0;
    private TextView gamesI1;
    private TextView gamesJ1;
    private View set1;
    private TextView importance;
    private View serveI;
    private View serveJ;
    private ChartView chart;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        super.onCreateView(inflater, parent, savedInstanceState);
        View root = inflater.inflate(R.layout.match_fragment, parent, false);
        buttonI = (Button)root.findViewById(R.id.buttonI);
        buttonJ = (Button)root.findViewById(R.id.buttonJ);
        propI = (TextView)root.findViewById(R.id.propI);
        propJ = (TextView)root.findViewById(R.id.propJ);
        pointsI = (TextView)root.findViewById(R.id.pointsI);
        pointsJ = (TextView)root.findViewById(R.id.pointsJ);
        gamesI0 = (TextView)root.findViewById(R.id.gamesI0);
        gamesJ0 = (TextView)root.findViewById(R.id.gamesJ0);
        gamesI1 = (TextView)root.findViewById(R.id.gamesI1);
        gamesJ1 = (TextView)root.findViewById(R.id.gamesJ1);
        set1 = root.findViewById(R.id.set1);
        importance = (TextView)root.findViewById(R.id.importance);
        serveI = root.findViewById(R.id.serveI);
        serveJ = root.findViewById(R.id.serveJ);
        chart = (ChartView)root.findViewById(R.id.match_chart);

        buttonI.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                getMatch().point((byte)1);
                updateProbs();
            }
        });
        buttonJ.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                getMatch().point((byte)2); 
                updateProbs();
            }
        });

        Match match = getMatch();
        
        buttonI.setText(match.player1);
        buttonJ.setText(match.player2);

        updateProbs();
        
        return root;
    }

    public void updateProbs() {
        double p = getMatch().getWinProb();
        double imp = getMatch().importance();
        double piP = p*100;
        double pjP = 100-piP;
        propI.setText(String.format("%.1f", piP)+"%");
        propJ.setText(String.format("%.1f", pjP)+"%");
        String[] stringScores = getMatch().getCurrentSet().getCurrentGame().stringScores();
        pointsI.setText(stringScores[0]);
        pointsJ.setText(stringScores[1]);
        if(getMatch().getCurrentSetNr() == 0) {
            set1.setVisibility(View.INVISIBLE);
        }else {
            set1.setVisibility(View.VISIBLE);
        }
        byte[][] games = getMatch().getGames();
        gamesI0.setText(games[0][0]+"");
        gamesJ0.setText(games[0][1]+"");
        if(games.length >= 2) {
            gamesI1.setText(games[1][0]+"");
            gamesJ1.setText(games[1][1]+"");
        }
        importance.setText(String.format("%.1f", imp*100)+"%");
        if(getMatch().servePoint()) {
            serveI.setVisibility(View.VISIBLE);
            serveJ.setVisibility(View.INVISIBLE);
        }else {
            serveJ.setVisibility(View.VISIBLE);
            serveI.setVisibility(View.INVISIBLE);
        }
        chart.drawMatch(getMatch());
    }

    private Match getMatch() {
        return ((MatchActivity)getActivity()).getMatch();
    }
    
}
