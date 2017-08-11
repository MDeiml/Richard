package com.mdeiml.richard;
import android.widget.*;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.NumberPicker.OnValueChangeListener;

public class ScoreActivity extends AppCompatActivity {

    private static final String[] dv = new String[] {"0", "15", "30", "40"};
    
    private NumberPicker gamesI0;
    private NumberPicker gamesI1;
    private NumberPicker scoreI;
    private RadioButton serveI;
    private NumberPicker gamesJ0;
    private NumberPicker gamesJ1;
    private NumberPicker scoreJ;
    private RadioButton serveJ;
    private Button cancel;
    private Button ok;
    private TextView nameI;
    private TextView nameJ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.score);
        gamesI0 = (NumberPicker)findViewById(R.id.gamesI0_in);
        gamesI1 = (NumberPicker)findViewById(R.id.gamesI1_in);
        scoreI = (NumberPicker)findViewById(R.id.scoreI_in);
        serveI = (RadioButton)findViewById(R.id.serveI_in);
        gamesJ0 = (NumberPicker)findViewById(R.id.gamesJ0_in);
        gamesJ1 = (NumberPicker)findViewById(R.id.gamesJ1_in);
        scoreJ = (NumberPicker)findViewById(R.id.scoreJ_in);
        serveJ = (RadioButton)findViewById(R.id.serveJ_in);
        cancel = (Button)findViewById(R.id.scoreCancel);
        ok = (Button)findViewById(R.id.scoreOk);
        nameI = (TextView)findViewById(R.id.scoreNameI);
        nameJ = (TextView)findViewById(R.id.scoreNameJ);
        cancel.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
        ok.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent();
                byte si = (byte)scoreI.getValue();
                byte sj = (byte)scoreJ.getValue();
                byte[] gi = new byte[] {(byte)gamesI0.getValue(), (byte)gamesI1.getValue(), 0};
                byte[] gj = new byte[] {(byte)gamesJ0.getValue(), (byte)gamesJ1.getValue(), 0};
                boolean s = serveI.isChecked();
                i.putExtra("score", new Score(si, sj, gi, gj, s, false));
                setResult(RESULT_OK, i);
                finish();
            }
        });

        gamesI0.setMinValue(0);
        gamesI0.setMaxValue(7);
        gamesI1.setMinValue(0);
        gamesI1.setMaxValue(7);
        scoreI.setMinValue(0);
        scoreI.setMaxValue(3);
        scoreI.setDisplayedValues(dv);
        gamesJ0.setMinValue(0);
        gamesJ0.setMaxValue(7);
        gamesJ1.setMinValue(0);
        gamesJ1.setMaxValue(7);
        scoreJ.setMinValue(0);
        scoreJ.setMaxValue(3);
        scoreJ.setDisplayedValues(dv);

        serveI.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton v, boolean user) {
                    if(user) {
                        serveJ.setChecked(!serveI.isChecked());
                    }
                }
            });
        serveJ.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton v, boolean user) {
                    if(user) {
                        serveI.setChecked(!serveJ.isChecked());
                    }
                }
            });
        serveI.setChecked(true);

        gamesI0.setOnValueChangedListener(new OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker v, int old, int val) {
                    if(val == 7) {
                        gamesJ0.setValue(Math.max(5, Math.min(6, gamesJ0.getValue())));
                    }else if(val < 5) {
                        gamesJ0.setValue(Math.min(6, gamesJ0.getValue()));
                    }
                    if(!isSetDone(val, gamesJ0.getValue())) {
                        gamesI1.setEnabled(false);
                        gamesJ1.setEnabled(false);
                        gamesI1.setValue(0);
                        gamesJ1.setValue(0);
                    }else {
                        gamesI1.setEnabled(true);
                        gamesJ1.setEnabled(true);
                    }
                    updatePoints();
                }
            });
        gamesJ0.setOnValueChangedListener(new OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker v, int old, int val) {
                    if(val == 7) {
                        gamesI0.setValue(Math.max(5, Math.min(6, gamesI0.getValue())));
                    }else if(val < 5) {
                        gamesI0.setValue(Math.min(6, gamesI0.getValue()));
                    }
                    if(!isSetDone(val, gamesI0.getValue())) {
                        gamesI1.setEnabled(false);
                        gamesJ1.setEnabled(false);
                        gamesI1.setValue(0);
                        gamesJ1.setValue(0);
                    }else {
                        gamesI1.setEnabled(true);
                        gamesJ1.setEnabled(true);
                    }
                    updatePoints();
                }
            });
        gamesI1.setOnValueChangedListener(new OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker v, int old, int val) {
                    if(val == 7) {
                        gamesJ1.setValue(Math.max(5, Math.min(6, gamesJ1.getValue())));
                    }else if(val < 5) {
                        gamesJ1.setValue(Math.min(6, gamesJ1.getValue()));
                    }
                    updatePoints();
                }
            });
        gamesJ1.setOnValueChangedListener(new OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker v, int old, int val) {
                    if(val == 7) {
                        gamesI1.setValue(Math.max(5, Math.min(6, gamesI1.getValue())));
                    }else if(val < 5) {
                        gamesI1.setValue(Math.min(6, gamesI1.getValue()));
                    }
                    updatePoints();
                }
            });
            
        gamesI1.setEnabled(false);
        gamesJ1.setEnabled(false);
        Intent i = getIntent();
        nameI.setText(i.getStringExtra("nameI"));
        nameJ.setText(i.getStringExtra("nameJ"));
    }
    
    public void updatePoints() {
        boolean tiebreak = false;
        if(gamesI1.isEnabled()) {
            if(isSetDone(gamesI1.getValue(), gamesJ1.getValue())) {
                scoreI.setDisplayedValues(null);
                scoreI.setMaxValue(10);
                scoreJ.setDisplayedValues(null);
                scoreJ.setMaxValue(10);
                tiebreak = true;
            }else if(gamesI1.getValue() == 6 && gamesJ1.getValue() == 6) {
                scoreI.setDisplayedValues(null);
                scoreI.setMaxValue(7);
                scoreJ.setDisplayedValues(null);
                scoreJ.setMaxValue(7);
                tiebreak = true;
            }
        }else if(gamesI0.getValue() == 6 && gamesJ0.getValue() == 6) {
            scoreI.setDisplayedValues(null);
            scoreI.setMaxValue(7);
            scoreJ.setDisplayedValues(null);
            scoreJ.setMaxValue(7);
            tiebreak = true;
        }
        if(!tiebreak) {
            scoreI.setMaxValue(3);
            scoreI.setDisplayedValues(dv);
            scoreJ.setMaxValue(3);
            scoreJ.setDisplayedValues(dv);
        }
    }

    private static boolean isSetDone(int i, int j) {
        return (i == 7 && j >= 5) || (i >= 5 && j == 7) || (Math.abs(i - j) >= 2 && (i == 6 || j == 6));
    }

}
