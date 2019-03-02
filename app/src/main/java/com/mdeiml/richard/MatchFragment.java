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
import java.util.Locale;
import android.widget.TableLayout;

public class MatchFragment extends Fragment {
    
    private Button buttonI;
    private Button buttonJ;
    private TextView propI;
    private TextView propJ;
    private TextView pointsI;
    private TextView pointsJ;
    private TableLayout pointsTable;
    private int numPointRows;
    private View points;
    private ArrayList<View> sets;
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
        pointsTable = (TableLayout)root.findViewById(R.id.points_table);
        importance = (TextView)root.findViewById(R.id.importance);
        serveI = root.findViewById(R.id.serveI);
        serveJ = root.findViewById(R.id.serveJ);
        chart = (ChartView)root.findViewById(R.id.match_chart);

        sets = new ArrayList<>();

        points = addPointsRow(0);
        numPointRows = 0;
        pointsI = (TextView)points.findViewById(R.id.pointsI);
        pointsJ = (TextView)points.findViewById(R.id.pointsJ);

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
        
        buttonI.setText(match.player1 == null ? getResources().getString(R.string.player_a) : match.player1);
        buttonJ.setText(match.player2 == null ? getResources().getString(R.string.player_b) : match.player2);

        updateProbs();
        
        return root;
    }

    public View addPointsRow(int i) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View row = inflater.inflate(R.layout.points, pointsTable, false);
        pointsTable.addView(row, i);
        return row;
    }

    public void redraw() {
        double p = getMatch().getWinProb();
        double imp = getMatch().importance();
        double piP = p*100;
        double pjP = 100-piP;
        byte w = getMatch().getWinner();
        propI.setText(String.format(Locale.getDefault(), "%.1f%%", piP));
        propJ.setText(String.format(Locale.getDefault(), "%.1f%%", pjP));
        if(w == 0 || getMatch().getCurrentSet().tiebreak == Match.MATCH_TIEBREAK) {
            String[] stringScores = getMatch().getCurrentSet().getCurrentGame().stringScores();
            pointsI.setText(stringScores[0]);
            pointsJ.setText(stringScores[1]);
            points.setVisibility(View.VISIBLE);
        } else {
            points.setVisibility(View.INVISIBLE);
        }

        int expectedRows = getMatch().getCurrentSetNr()+1;
        if (getMatch().getCurrentSet().tiebreak == Match.MATCH_TIEBREAK) {
            expectedRows--;
        }
        while(expectedRows > numPointRows) {
            sets.add(addPointsRow(numPointRows++));
        }
        while(expectedRows < numPointRows) {
            pointsTable.removeView(sets.remove(sets.size()-1));
            numPointRows--;
        }
        byte[][] games = getMatch().getGames();
        for(int i = 0; i < numPointRows; i++) {
            ((TextView)sets.get(i).findViewById(R.id.pointsI)).setText(String.format(Locale.getDefault(), "%d", games[i][0]));
            ((TextView)sets.get(i).findViewById(R.id.pointsJ)).setText(String.format(Locale.getDefault(), "%d", games[i][1]));
        }
        importance.setText(String.format(Locale.getDefault(), "%.1f%%", imp*100));
        if(getMatch().servePoint()) {
            serveI.setVisibility(View.VISIBLE);
            serveJ.setVisibility(View.INVISIBLE);
        }else {
            serveJ.setVisibility(View.VISIBLE);
            serveI.setVisibility(View.INVISIBLE);
        }
        chart.drawMatch(getMatch());
    }

    public void updateProbs() {
        ((MatchActivity)getActivity()).redraw();
    }

    private Match getMatch() {
        return ((MatchActivity)getActivity()).getMatch();
    }
    
}
