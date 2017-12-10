package com.mdeiml.richard;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class StatisticsFragment extends Fragment {

    private ChartView chartWinprob;
    private ChartView chartImp;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        super.onCreateView(inflater, parent, savedInstanceState);
        View root = inflater.inflate(R.layout.statistics_fragment, parent, false);
        chartWinprob = (ChartView)root.findViewById(R.id.chart_winprob);
        chartImp = (ChartView)root.findViewById(R.id.chart_imp);
        chartImp.setType(ChartView.TYPE_IMPORTANCE);
        Log.i("StatisticsFragment", chartWinprob.toString());
        return root;
    }

    public void redraw() {
        if(chartWinprob != null && chartImp != null) {
            chartWinprob.drawMatch(getMatch());
            chartImp.drawMatch(getMatch());
        }
    }

    private Match getMatch() {
        return ((MatchActivity)getActivity()).getMatch();
    }

}
