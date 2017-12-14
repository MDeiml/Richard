package com.mdeiml.richard;

import android.content.*;
import android.view.*;
import android.widget.*;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.Toolbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View.OnClickListener;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.support.v7.widget.Toolbar;

public class NewGameActivity extends AppCompatActivity {
    
    private Button start;
    private EditText nameI;
    private EditText nameJ;
    private AppCompatSeekBar matchProp;
    private TextView matchPropI;
    private TextView matchPropJ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newgame);
        start = (Button)findViewById(R.id.start);
        nameI = (EditText)findViewById(R.id.nameI);
        nameJ = (EditText)findViewById(R.id.nameJ);
        matchProp = (AppCompatSeekBar)findViewById(R.id.matchProp);
        matchPropI = (TextView)findViewById(R.id.matchPropI);
        matchPropJ = (TextView)findViewById(R.id.matchPropJ);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        matchProp.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar p1) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar p1) {
            }

            public void onProgressChanged(SeekBar s, int progress, boolean b) {
                matchPropI.setText(progress+"%");
                matchPropJ.setText((100-progress)+"%");
            }
        });
        start.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(NewGameActivity.this, MatchActivity.class);
                i.putExtra("nameI", nameI.getText().toString());
                i.putExtra("nameJ", nameJ.getText().toString());
                i.putExtra("m", matchProp.getProgress()/100.0);
                startActivity(i);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.newgame, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.set_pmean:
                PMeanDialog dialog = new PMeanDialog();
                dialog.show(getSupportFragmentManager(), "pmean");
                return true;
            default:
                return false;
        }
    }
    
    private class PMeanDialog extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final SharedPreferences pref = getSharedPreferences("com.mdeiml.richard.PREFS", MODE_PRIVATE);
            AlertDialog.Builder builder = new AlertDialog.Builder(NewGameActivity.this);
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            final View v = inflater.inflate(R.layout.pmean_dialog, null);
            builder.setView(v)
                   .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {
                           EditText pmeanInput = (EditText)v.findViewById(R.id.pmean_input);
                           float p = Float.parseFloat(pmeanInput.getText().toString());
                           SharedPreferences.Editor edit = pref.edit();
                           edit.putFloat("pmean", p);
                           edit.commit();
                       }
                   })
                   .setNegativeButton("Abbruch", new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {
                           getDialog().cancel();
                       }
                   });
            EditText pInput = (EditText)v.findViewById(R.id.pmean_input);
            pInput.setText(pref.getFloat("pmean", 0.6f)+"");
            return builder.create();
        }
        
    }
    
}
