package com.mdeiml.richard;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class StatisticsFragment extends Fragment {

    private ChartView chartView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        super.onCreateView(inflater, parent, savedInstanceState);
        View root = inflater.inflate(R.layout.statistics_fragment, parent, false);
        chartView = (ChartView)root.findViewById(R.id.chart);
        return root;
    }

    public void redraw() {
        Log.i("StatisticsFragment", "test");
        chartView.drawMatch(getMatch());
    }

    public Match getMatch() {
        return ((MatchActivity)getActivity()).getMatch();
    }

}