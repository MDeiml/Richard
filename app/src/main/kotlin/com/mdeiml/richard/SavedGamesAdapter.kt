package com.mdeiml.richard

import android.graphics.Typeface
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.content.Context
import android.widget.CursorAdapter
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.TableRow
import android.util.TypedValue

public class SavedGamesAdapter(
        context: Context,
        dbHelper: SavedMatchesDbHelper
) : CursorAdapter(
        context,
        SavedGamesAdapter.createCursor(dbHelper.getReadableDatabase(), context),
        false
) {
    
    private val idIndex = cursor.getColumnIndexOrThrow("_id")
    private val player1Index = cursor.getColumnIndexOrThrow("match_player1")
    private val player2Index = cursor.getColumnIndexOrThrow("match_player2")
    private val winnerIndex = cursor.getColumnIndexOrThrow("match_winner")
    private val dbHelper = dbHelper
    private var runningHeaderIndex: Int? = null
    private var overHeaderIndex: Int? = null

    companion object {
        fun createCursor(db: SQLiteDatabase, context: Context): Cursor {
            return db.rawQuery(context.getString(R.string.sql_saved_matches), arrayOf())
        }
    }

    override fun getViewTypeCount() = 2

    override fun getItemViewType(position: Int): Int {
        if (position == runningHeaderIndex || position == overHeaderIndex) {
            return 0
        }

        cursor.moveToPosition(position)
        val winner = cursor.getInt(winnerIndex)
        when (winner) {
            -1 -> {
                runningHeaderIndex = position
                return 0
            }
            1 -> {
                overHeaderIndex = position
                return 0
            }
            else -> return 1
        }
    }

    override fun isEnabled(position: Int) = getItemViewType(position) != 0

    override fun bindView(view: View, context: Context, cursor: Cursor) {
        val maxSets = 3 // TODO
        val locale = context.resources.configuration.locales[0]

        val winner = cursor.getInt(winnerIndex)
        when (winner) {
            -1 -> (view as TextView).setText(context.getString(R.string.game_running_heading))
            1 -> (view as TextView).setText(context.getString(R.string.game_over_heading))
            else -> {
                val matchId = cursor.getLong(idIndex)

                val sgPlayer1 = view.findViewById(R.id.sgPlayer1) as TextView
                val sgPlayer2 = view.findViewById(R.id.sgPlayer2) as TextView
                val sgPoints1 = view.findViewById(R.id.sgPoints1) as TextView
                val sgPoints2 = view.findViewById(R.id.sgPoints2) as TextView
                val sgSets1 = view.findViewById(R.id.sgSets1) as ViewGroup
                val sgSets2 = view.findViewById(R.id.sgSets2) as ViewGroup

                val match = dbHelper.loadMatch(matchId)!!
                sgPlayer1.text = match.player1 ?: context.getString(R.string.player_a)
                sgPlayer2.text = match.player2 ?: context.getString(R.string.player_b)

                val numSets = match.currentSetNr + if (match.currentSet.tiebreak) 0 else 1
                val games = match.gameScore
                for (i in 0..numSets-1) {
                    val tv0 = sgSets1.getChildAt(i + 1) as TextView
                    tv0.text = String.format(locale, "%d", games[i].first)
                    tv0.setTextColor(context.resources.getColor(R.color.textContent))
                    tv0.visibility = View.VISIBLE
                    
                    val tv1 = sgSets2.getChildAt(i + 1) as TextView
                    tv1.text = String.format(locale, "%d", games[i].second)
                    tv0.setTextColor(context.getResources().getColor(R.color.textContent))
                    tv1.visibility = View.VISIBLE
                }

                for (i in numSets..maxSets-1) {
                    sgSets1.getChildAt(i + 1).visibility = View.INVISIBLE
                    sgSets2.getChildAt(i + 1).visibility = View.INVISIBLE
                }

                val (s1, s2) = match.currentSet.currentGame.stringScores
                if(s1 != "0" || s2 != "0") {
                    sgPoints1.text = s1
                    sgPoints2.text = s2
                }
            }
        }
    }
    
    override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
        val winner = cursor.getInt(winnerIndex)
        when (winner) {
            -1, 1 -> {
                val heading = TextView(context)
                heading.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20.0f)
                heading.setTextColor(0xff707070.toInt())
                heading.setTypeface(null, Typeface.BOLD)
                return heading
            }
            else -> {
                val maxSets = 3 // TODO
                val inflater = LayoutInflater.from(context)
                val view = inflater.inflate(R.layout.saved_game, parent, false)

                val sgSets1 = view.findViewById(R.id.sgSets1) as ViewGroup
                val sgSets2 = view.findViewById(R.id.sgSets2) as ViewGroup
                for (i in 0..maxSets-1) {
                    val tv0 = TextView(context)
                    tv0.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20.0f)
                    tv0.layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.0f)
                    sgSets1.addView(tv0, i + 1)
                    
                    val tv1 = TextView(context)
                    tv1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20.0f)
                    tv1.layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.0f)
                    sgSets2.addView(tv1, i + 1)
                }
                return view
            }
        }
    }
    
    fun updateCursor(context: Context) {
        val db = dbHelper.getReadableDatabase()
        val cursor = createCursor(db, context)
        changeCursor(cursor)
        runningHeaderIndex = null
        overHeaderIndex = null
    }
}
