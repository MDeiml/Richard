package com.mdeiml.richard

import android.app.Dialog
import android.content.*
import android.database.*
import android.database.sqlite.*
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatSeekBar
import android.support.v7.widget.Toolbar
import android.text.TextWatcher
import android.text.Editable
import android.view.*
import android.view.View.OnClickListener
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import android.util.Log
import java.util.Locale
import kotlinx.android.synthetic.main.newgame.*

class NewGameActivity : AppCompatActivity() {
    
    lateinit var dbHelper: SavedMatchesDbHelper
    lateinit var elos: HashMap<String, Double>
    var eloTask: CalculateEloTask? = null

    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.newgame)

        dbHelper = SavedMatchesDbHelper(this)
        elos = HashMap<String, Double>()

        setSupportActionBar(toolbar)

        matchProp.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStartTrackingTouch(s: SeekBar) {}

            override fun onStopTrackingTouch(s: SeekBar) {}

            override fun onProgressChanged(s: SeekBar, progress: Int, b: Boolean) {
                matchPropI.setText(String.format(Locale.getDefault(), "%d%%", progress))
                matchPropJ.setText(String.format(Locale.getDefault(), "%d%%", (100-progress)))
            }
        })
        start.setOnClickListener {
            val i = Intent(this, MatchActivity::class.java)
            i.putExtra("nameI", nameI.getText().toString())
            i.putExtra("nameJ", nameJ.getText().toString())
            i.putExtra("m", matchProp.getProgress()/100.0)
            startActivity(i)
        }

        val adapter = SimpleCursorAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                null,
                arrayOf("_id"),
                intArrayOf(android.R.id.text1)
            )
        adapter.setFilterQueryProvider { getNamesLike("$it%") }
        adapter.setStringConversionColumn(0)
        nameI.setAdapter(adapter)
        nameJ.setAdapter(adapter)
        nameI.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun afterTextChanged(e: Editable) {
                val c = getNamesLike(e.toString())
                if (c.getCount() > 0) {
                    if (!updateElos()) {
                        eloTask?.cancel(true)
                        eloTask = CalculateEloTask(this@NewGameActivity)
                        eloTask?.execute(e.toString())
                    }
                }
            }
        })
        nameJ.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun afterTextChanged(e: Editable) {
                updateElos()
            }
        })

        expandHeader.setOnClickListener {
            if (advancedSettings.getVisibility() == View.GONE) {
                advancedSettings.setVisibility(View.VISIBLE)
                advancedSettings.animate().setDuration(200).alpha(1.0f)
            } else {
                advancedSettings.animate().setDuration(200).alpha(0.0f).withEndAction { advancedSettings.setVisibility(View.GONE) }
            }
        }

        val matchTypeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayOf("Best of 3 - Matchtiebreak", "Best of 3", "Best of 5"))
        matchTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        matchType.setAdapter(matchTypeAdapter)
    }

    fun addElos(newElos: HashMap<String, Double>) {
        elos.putAll(newElos)
        updateElos()
    }

    fun updateElos(): Boolean {
        val elo1 = elos.get(nameI.getText().toString())
        if (elo1 != null) {
            val elo2 = elos.get(nameJ.getText().toString())
            if (elo2 != null) {
                val prob = 1.0 / (1.0 + Math.pow(10.0, (elo2 - elo1) / 400.0))
                matchProp.setProgress((prob * 100.0).toInt())
                return true
            }
        }
        return false
    }

    fun getNamesLike(name: String): Cursor {
        val db = dbHelper.getReadableDatabase()
        return db.rawQuery(
                "SELECT match_player1 AS _id FROM matches WHERE match_player1 LIKE ? UNION SELECT match_player2 AS _id FROM matches WHERE match_player2 LIKE ?",
                arrayOf(name, name)
            )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = getMenuInflater()
        inflater.inflate(R.menu.newgame, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            R.id.set_pmean -> {
                val dialog = PMeanDialog()
                dialog.show(getSupportFragmentManager(), "pmean")
                return true
            }
            else -> return false
        }
    }
    
    class PMeanDialog : DialogFragment() {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val pref = getContext()!!.getSharedPreferences("com.mdeiml.richard.PREFS", MODE_PRIVATE)
            val builder = AlertDialog.Builder(getContext()!!)
            val inflater = LayoutInflater.from(getActivity())
            val v = inflater.inflate(R.layout.pmean_dialog, null)
            builder.setView(v)
                   .setPositiveButton("OK") {_: DialogInterface, _: Int ->
                       val pmeanInput = v.findViewById(R.id.pmean_input) as EditText
                       val p = Integer.parseInt(pmeanInput.getText().toString())
                       val edit = pref.edit()
                       edit.putFloat("pmean", p / 100f)
                       edit.apply()
                   }
                   .setNegativeButton("Abbruch") {_: DialogInterface, _: Int ->
                       getDialog().cancel()
                   }
            val pInput = v.findViewById(R.id.pmean_input) as EditText
            pInput.setText(String.format(Locale.getDefault(), "%d", (pref.getFloat("pmean", 0.6f) * 100.0).toInt()))
            return builder.create()
        }
        
    }
    
}
