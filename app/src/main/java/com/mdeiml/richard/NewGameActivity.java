package com.mdeiml.richard;

import android.app.Dialog;
import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.Toolbar;
import android.text.TextWatcher;
import android.text.Editable;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.util.Log;
import java.util.Locale;
import java.util.HashMap;

public class NewGameActivity extends AppCompatActivity {
    
    private Button start;
    private AutoCompleteTextView nameI;
    private AutoCompleteTextView nameJ;
    private AppCompatSeekBar matchProp;
    private TextView matchPropI;
    private TextView matchPropJ;
    private View expandHeader;
    private View advancedSettings;
    private Spinner matchType;

    private SavedMatchesDbHelper dbHelper;

    private CalculateEloTask eloTask;
    private HashMap<String, Double> elos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newgame);

        dbHelper = new SavedMatchesDbHelper(this);
        elos = new HashMap<>();

        start = (Button)findViewById(R.id.start);
        nameI = (AutoCompleteTextView)findViewById(R.id.nameI);
        nameJ = (AutoCompleteTextView)findViewById(R.id.nameJ);
        matchProp = (AppCompatSeekBar)findViewById(R.id.matchProp);
        matchPropI = (TextView)findViewById(R.id.matchPropI);
        matchPropJ = (TextView)findViewById(R.id.matchPropJ);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        expandHeader = findViewById(R.id.expand_header);
        advancedSettings = findViewById(R.id.advanced_settings);
        matchType = (Spinner) findViewById(R.id.match_type);

        setSupportActionBar(toolbar);

        matchProp.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar p1) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar p1) {
            }

            @Override
            public void onProgressChanged(SeekBar s, int progress, boolean b) {
                matchPropI.setText(String.format(Locale.getDefault(), "%d%%", progress));
                matchPropJ.setText(String.format(Locale.getDefault(), "%d%%", (100-progress)));
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

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                null,
                new String[] { "_id" },
                new int[] { android.R.id.text1 }
            );
        adapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {
                return getNamesLike(constraint + "%");
            }
        });
        adapter.setStringConversionColumn(0);
        nameI.setAdapter(adapter);
        nameJ.setAdapter(adapter);
        nameI.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            public void afterTextChanged(Editable e) {
                Cursor c = getNamesLike(e.toString());
                if (c.getCount() > 0) {
                    nameI.setTextColor(0xffff0000);
                    if (elos.containsKey(e.toString())) {
                        if (elos.containsKey(nameJ.getText().toString())) {
                            double elo1 = elos.get(nameI.getText().toString());
                            double elo2 = elos.get(nameJ.getText().toString());
                            double prob = 1 / (1 + Math.pow(10, elo2 - elo1));
                            matchProp.setProgress((int) (prob * 100));
                        }
                    } else {
                        if (eloTask != null) {
                            eloTask.cancel(true);
                        }
                        eloTask = new CalculateEloTask(NewGameActivity.this);
                        eloTask.execute(e.toString());
                    }
                } else {
                    nameI.setTextColor(0xff000000);
                }
            }
        });
        nameJ.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            public void afterTextChanged(Editable e) {
                Cursor c = getNamesLike(e.toString());
                if (c.getCount() > 0) {
                    nameJ.setTextColor(0xffff0000);
                    if (elos.containsKey(nameI.getText().toString()) && elos.containsKey(nameJ.getText().toString())) {
                        double elo1 = elos.get(nameI.getText().toString());
                        double elo2 = elos.get(nameJ.getText().toString());
                        double prob = 1 / (1 + Math.pow(10, (elo2 - elo1) / 400));
                        matchProp.setProgress((int) (prob * 100));
                    }
                } else {
                    nameJ.setTextColor(0xff000000);
                }
            }
        });

        expandHeader.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (advancedSettings.getVisibility() == View.GONE) {
                    advancedSettings.setVisibility(View.VISIBLE);
                    advancedSettings.animate().setDuration(200).alpha(1.0f);
                } else {
                    advancedSettings.animate().setDuration(200).alpha(0.0f).withEndAction(new Runnable() {
                        public void run() {
                            advancedSettings.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });

        ArrayAdapter matchTypeAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, new String[] { "Best of 3 - Matchtiebreak", "Best of 3", "Best of 5" });
        matchTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        matchType.setAdapter(matchTypeAdapter);
    }

    public void addElos(HashMap<String, Double> newElos) {
        elos.putAll(newElos);
        if (elos.containsKey(nameI.getText().toString()) && elos.containsKey(nameJ.getText().toString())) {
            double elo1 = elos.get(nameI.getText().toString());
            double elo2 = elos.get(nameJ.getText().toString());
            double prob = 1 / (1 + Math.pow(10, (elo2 - elo1) / 400));
            matchProp.setProgress((int) (prob * 100));
        }
    }

    public Cursor getNamesLike(String name) { // TODO: Escape %
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.rawQuery(
                "SELECT match_player1 AS _id FROM matches WHERE match_player1 LIKE ? UNION SELECT match_player2 AS _id FROM matches WHERE match_player2 LIKE ?",
                new String[] { name, name }
            );
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
    
    public static class PMeanDialog extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final SharedPreferences pref = getContext().getSharedPreferences("com.mdeiml.richard.PREFS", MODE_PRIVATE);
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            final View v = inflater.inflate(R.layout.pmean_dialog, null);
            builder.setView(v)
                   .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {
                           EditText pmeanInput = (EditText)v.findViewById(R.id.pmean_input);
                           int p = Integer.parseInt(pmeanInput.getText().toString());
                           SharedPreferences.Editor edit = pref.edit();
                           edit.putFloat("pmean", p / 100f);
                           edit.apply();
                       }
                   })
                   .setNegativeButton("Abbruch", new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {
                           getDialog().cancel();
                       }
                   });
            EditText pInput = (EditText)v.findViewById(R.id.pmean_input);
            pInput.setText(String.format(Locale.getDefault(), "%d", (int)(pref.getFloat("pmean", 0.6f) * 100)));
            return builder.create();
        }
        
    }
    
}
