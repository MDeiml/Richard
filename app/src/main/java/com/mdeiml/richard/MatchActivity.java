package com.mdeiml.richard;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import java.util.ArrayList;

public class MatchActivity extends AppCompatActivity {
    
    private Match match;
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
    private double pi, pj;
    private View serveI;
    private View serveJ;
    private DiagrammView diagramm;
    
    private ArrayList<HistoryEntry> history;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.match);
        buttonI = (Button)findViewById(R.id.buttonI);
        buttonJ = (Button)findViewById(R.id.buttonJ);
        propI = (TextView)findViewById(R.id.propI);
        propJ = (TextView)findViewById(R.id.propJ);
        pointsI = (TextView)findViewById(R.id.pointsI);
        pointsJ = (TextView)findViewById(R.id.pointsJ);
        gamesI0 = (TextView)findViewById(R.id.gamesI0);
        gamesJ0 = (TextView)findViewById(R.id.gamesJ0);
        gamesI1 = (TextView)findViewById(R.id.gamesI1);
        gamesJ1 = (TextView)findViewById(R.id.gamesJ1);
        set1 = findViewById(R.id.set1);
        importance = (TextView)findViewById(R.id.importance);
        serveI = findViewById(R.id.serveI);
        serveJ = findViewById(R.id.serveJ);
        diagramm = (DiagrammView)findViewById(R.id.diagramm);
        buttonI.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                match.point(true);
                updateProbs();
            }
        });
        buttonJ.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                match.point(false);
                updateProbs();
            }
        });
        
        Intent i = getIntent();
        String nameI = i.getStringExtra("nameI").trim();
        String nameJ = i.getStringExtra("nameJ").trim();
        nameI = nameI.isEmpty() ? "Spieler A" : nameI;
        nameJ = nameJ.isEmpty() ? "Spieler B" : nameJ;
        buttonI.setText(nameI);
        buttonJ.setText(nameJ);
        diagramm.setLabels(nameI, nameJ);
        
        SharedPreferences pref = getSharedPreferences("com.mdeiml.richard", MODE_PRIVATE);
        double pmean = pref.getFloat("pmean", 0.6f);
        double[] p = MarkovMatrix.approxP(pmean, i.getDoubleExtra("m", 0.5));
        pi = p[0];
        pj = p[1];
        
        if(savedInstanceState != null && savedInstanceState.containsKey("match")) {
            match = (Match)savedInstanceState.getSerializable("match");
        }else {
            match = new Match(pi, pj);
        }
        if(savedInstanceState != null && savedInstanceState.containsKey("history")) {
            history = (ArrayList<HistoryEntry>)savedInstanceState.getSerializable("history");
        }else {
            history = new ArrayList<>();
        }
        updateProbs();
    }
    
    public void updateProbs() {
        double p = match.winProbI();
        double piP = p*100;
        double pjP = 100-piP;
        propI.setText(String.format("%.1f", piP)+"%");
        propJ.setText(String.format("%.1f", pjP)+"%");
        pointsI.setText(match.getScoreIS());
        pointsJ.setText(match.getScoreJS());
        if(match.getCurrentSet() == 0) {
            set1.setVisibility(View.INVISIBLE);
        }else {
            set1.setVisibility(View.VISIBLE);
        }
        byte[] gamesI = match.getGamesI();
        byte[] gamesJ = match.getGamesJ();
        gamesI0.setText(gamesI[0]+"");
        gamesJ0.setText(gamesJ[0]+"");
        gamesI1.setText(gamesI[1]+"");
        gamesJ1.setText(gamesJ[1]+"");
        double imp = match.importance();
        importance.setText(String.format("%.1f", imp*100)+"%");
        if(match.isServePoint()) {
            serveI.setVisibility(View.VISIBLE);
            serveJ.setVisibility(View.INVISIBLE);
        }else {
            serveJ.setVisibility(View.VISIBLE);
            serveI.setVisibility(View.INVISIBLE);
        }
        history.add(match.getHistoryEntry());
        diagramm.addValue((float)p);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("match", match);
        outState.putSerializable("history", history);
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
            case R.id.edit_score:
                Intent i = new Intent(this, ScoreActivity.class);
                i.putExtra("nameI", buttonI.getText()+"");
                i.putExtra("nameJ", buttonJ.getText()+"");
                startActivityForResult(i, 0);
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK) {
            Score s = (Score)data.getSerializableExtra("score");
            history.clear();
            match.setScore(s);
            diagramm.clear();
            updateProbs();
        }
    }
    
    

    
    
}
