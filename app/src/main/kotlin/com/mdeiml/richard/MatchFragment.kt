package com.mdeiml.richard

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View.OnClickListener
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.TableLayout
import kotlinx.android.synthetic.main.match_fragment.*

public class MatchFragment : Fragment() {
    
    private var numPointRows = 0
    private lateinit var points: View
    private lateinit var pointsI: TextView
    private lateinit var pointsJ: TextView
    private lateinit var sets: ArrayList<View>
    val matchActivity
        get() = getActivity() as MatchActivity
    val match
        get() = matchActivity.match
    
    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, parent, savedInstanceState)
        return inflater.inflate(R.layout.match_fragment, parent, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sets = ArrayList<View>()

        points = addPointsRow(0)
        pointsI = points.findViewById(R.id.pointsI)
        pointsJ = points.findViewById(R.id.pointsJ)

        buttonI.setOnClickListener {
            match.point(Match.Player.PLAYER_A)
            matchActivity.redraw()
        }
       
        buttonJ.setOnClickListener {
            match.point(Match.Player.PLAYER_B) 
            matchActivity.redraw()
        }

        buttonI.setText(this.match.player1 ?: this.resources.getString(R.string.player_a))
        buttonJ.setText(this.match.player2 ?: this.resources.getString(R.string.player_b))

        matchActivity.redraw()
    }

    fun addPointsRow(i: Int): View {
        val inflater = matchActivity.layoutInflater
        val row = inflater.inflate(R.layout.points, pointsTable, false)
        pointsTable.addView(row, i)
        return row
    }

    fun redraw() {
        val p = match.winProb
        val imp = match.importance
        val piP = p*100
        val pjP = 100-piP
        val w = match.winner
        propI.setText(String.format(this.resources.configuration.locales[0], "%.1f%%", piP))
        propJ.setText(String.format(this.resources.configuration.locales[0], "%.1f%%", pjP))
        if(w == null || match.currentSet.tiebreak) {
            val (s1, s2) = match.currentSet.currentGame.stringScores
            pointsI.setText(s1)
            pointsJ.setText(s2)
            points.setVisibility(View.VISIBLE)
        } else {
            points.setVisibility(View.INVISIBLE)
        }

        var expectedRows = match.currentSetNr + 1
        if (match.currentSet.tiebreak) {
            expectedRows--
        }
        while(expectedRows > numPointRows) {
            sets.add(addPointsRow(numPointRows++))
        }
        while(expectedRows < numPointRows) {
            pointsTable.removeView(sets.removeAt(sets.size-1))
            numPointRows--
        }
        val games: List<Pair<Int, Int>> = match.gameScore
        for (i in 0..numPointRows-1) {
            (sets[i].findViewById(R.id.pointsI) as TextView).text = String.format(this.resources.configuration.locales[0], "%d", games[i].first)
            (sets[i].findViewById(R.id.pointsJ) as TextView).text = String.format(this.resources.configuration.locales[0], "%d", games[i].second)
        }
        importance.text = String.format(this.resources.configuration.locales[0], "%.1f%%", imp*100)
        if(match.servePoint) {
            serveI.setVisibility(View.VISIBLE)
            serveJ.setVisibility(View.INVISIBLE)
        }else {
            serveJ.setVisibility(View.VISIBLE)
            serveI.setVisibility(View.INVISIBLE)
        }
        chart.drawMatch(match)
    }
    
}
