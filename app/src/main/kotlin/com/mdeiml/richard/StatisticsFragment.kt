package com.mdeiml.richard

import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.SpannableStringBuilder
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import java.util.Locale
import kotlinx.android.synthetic.main.statistics_fragment.*

class StatisticsFragment : Fragment() {

    val matchActivity
        get() = this.activity as MatchActivity
    val match
        get() = this.matchActivity.match

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, parent, savedInstanceState)
        return inflater.inflate(R.layout.statistics_fragment, parent, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val serves = this.resources.getString(R.string.serve_points)
        val breaks = this.resources.getString(R.string.breaks)
        val player1 = match.player1 ?: this.resources.getString(R.string.player_a)
        val player2 = match.player2 ?: this.resources.getString(R.string.player_b)
        val spanRed = ForegroundColorSpan(this.resources.getColor(R.color.primaryRedLight))
        val spanBlue = ForegroundColorSpan(this.resources.getColor(R.color.primaryLight))
        val player1S = SpannableString(player1)
        player1S.setSpan(spanRed, 0, player1S.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        val player2S = SpannableString(player2)
        player2S.setSpan(spanBlue, 0, player2S.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)

        serves1Label.text = SpannableStringBuilder(serves).append(' ').append(player1S)
        serves2Label.text = SpannableStringBuilder(serves).append(' ').append(player2S)
        breaks1Label.text = SpannableStringBuilder(breaks).append(' ').append(player1S)
        breaks2Label.text = SpannableStringBuilder(breaks).append(' ').append(player2S)

        chartWinprob.type = ChartView.Type.WIN
        chartImp.type = ChartView.Type.IMPORTANCE
        chartImpWin.type = ChartView.Type.IMPORTANCE_WIN

        redraw()
    }

    fun redraw() {
        if (chartWinprob != null) {
            val locale = this.resources.configuration.locales[0]

            chartWinprob.drawMatch(match)
            chartImp.drawMatch(match)
            chartImpWin.drawMatch(match)
            val points = match.totalPoints
            val perc1 = if (points[2] + points[5] > 0) String.format(locale, "%d", 100 * points[2] / (points[2] + points[5])) else "-"
            serves1.text = String.format(locale, "%d (%s%%)", points[2], perc1)
            val perc2 = if (points[4] + points[3] > 0) String.format(locale, "%d", 100 * points[4] / (points[4] + points[3])) else "-"
            serves2.text = String.format(locale, "%d (%s%%)", points[4], perc2)
            val (b1, b2) = match.breaks
            breaks1.text = String.format(locale, "%d", b1)
            breaks2.text = String.format(locale, "%d", b2)
        }
    }

}
