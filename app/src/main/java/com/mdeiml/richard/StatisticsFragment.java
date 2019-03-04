package com.mdeiml.richard;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.SpannableStringBuilder;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.Locale;

public class StatisticsFragment extends Fragment {

    private ChartView chartWinprob;
    private ChartView chartImp;
    private ChartView chartImpWin;
    private TextView serves1;
    private TextView serves2;
    private TextView breaks1;
    private TextView breaks2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        super.onCreateView(inflater, parent, savedInstanceState);
        View root = inflater.inflate(R.layout.statistics_fragment, parent, false);

        chartWinprob = root.findViewById(R.id.chart_winprob);
        chartImp = root.findViewById(R.id.chart_imp);
        chartImp.setType(ChartView.TYPE_IMPORTANCE);
        chartImpWin = root.findViewById(R.id.chart_impwin);
        chartImpWin.setType(ChartView.TYPE_IMPORTANCE_WIN);

        serves1 = root.findViewById(R.id.stats_serves1);
        serves2 = root.findViewById(R.id.stats_serves2);
        breaks1 = root.findViewById(R.id.stats_breaks1);
        breaks2 = root.findViewById(R.id.stats_breaks2);

        String serves = getContext().getString(R.string.serve_points);
        String breaks = getContext().getString(R.string.breaks);
        String player1 = getMatch().player1 == null ? getResources().getString(R.string.player_a) : getMatch().player1;
        String player2 = getMatch().player2 == null ? getResources().getString(R.string.player_b) : getMatch().player2;
        Object spanRed = new ForegroundColorSpan(getResources().getColor(R.color.primaryRedLight));
        Object spanBlue = new ForegroundColorSpan(getResources().getColor(R.color.primaryLight));
        SpannableString player1S = new SpannableString(player1);
        player1S.setSpan(spanRed, 0, player1S.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        SpannableString player2S = new SpannableString(player2);
        player2S.setSpan(spanBlue, 0, player2S.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);

        ((TextView)root.findViewById(R.id.stats_label_serves1)).setText(
            new SpannableStringBuilder(serves)
            .append(' ')
            .append(player1S)
        );
        ((TextView)root.findViewById(R.id.stats_label_serves2)).setText(
            new SpannableStringBuilder(serves)
            .append(' ')
            .append(player2S)
        );
        ((TextView)root.findViewById(R.id.stats_label_breaks1)).setText(
            new SpannableStringBuilder(breaks)
            .append(' ')
            .append(player1S)
        );
        ((TextView)root.findViewById(R.id.stats_label_breaks2)).setText(
            new SpannableStringBuilder(breaks)
            .append(' ')
            .append(player2S)
        );

        redraw();
        return root;
    }

    public void redraw() {
        if(chartWinprob != null) {
            chartWinprob.drawMatch(getMatch());
            chartImp.drawMatch(getMatch());
            chartImpWin.drawMatch(getMatch());
            int[] points = getMatch().totalPoints();
            String perc1 = points[2] + points[5] > 0 ? String.format(Locale.getDefault(), "%d", 100 * points[2] / (points[2] + points[5])) : "-";
            serves1.setText(String.format(Locale.getDefault(), "%d (%s%%)", points[2], perc1));
            String perc2 = points[4] + points[3] > 0 ? String.format(Locale.getDefault(), "%d", 100 * points[4] / (points[4] + points[3])) : "-";
            serves2.setText(String.format(Locale.getDefault(), "%d (%s%%)", points[4], perc2));
            int[] breaks = getMatch().breaks();
            breaks1.setText(String.format(Locale.getDefault(), "%d", breaks[0]));
            breaks2.setText(String.format(Locale.getDefault(), "%d", breaks[1]));
        }
    }

    private Match getMatch() {
        return ((MatchActivity)getActivity()).getMatch();
    }

}
