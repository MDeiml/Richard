package com.mdeiml.richard;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class StatisticsFragment extends Fragment {

    private ChartView chartWinprob;
    private ChartView chartImp;
    private ChartView chartImpWin;
    private TextView serves1;
    private TextView serves2;

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
        redraw();
        return root;
    }

    public void redraw() {
        if(chartWinprob != null) {
            chartWinprob.drawMatch(getMatch());
            chartImp.drawMatch(getMatch());
            chartImpWin.drawMatch(getMatch());
            int[] points = getMatch().totalPoints();
            String perc1 = points[2] + points[5] > 0 ? (100 * points[2] / (points[2] + points[5])) + "" : "-";
            serves1.setText(points[2]+" ("+perc1+"%)");
            String perc2 = points[4] + points[3] > 0 ? (100 * points[4] / (points[4] + points[3])) + "" : "-";
            serves2.setText(points[4]+" ("+perc2+"%)");
        }
    }

    private Match getMatch() {
        return ((MatchActivity)getActivity()).getMatch();
    }

}
