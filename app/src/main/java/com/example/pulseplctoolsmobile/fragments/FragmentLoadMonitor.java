package com.example.pulseplctoolsmobile.fragments;


import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.example.pulseplctoolsmobile.R;
import com.example.pulseplctoolsmobile.enums.ImpNum;
import com.example.pulseplctoolsmobile.models.DeviceImpExParams;
import com.example.pulseplctoolsmobile.protocol.Commands;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Calendar;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentLoadMonitor extends Fragment {

    //UI
    TextView txtLoad, txtImpCounter, txtLastImpTime, txtTariff;
    GraphView graph;
    Switch swImp1, swImp2;
    //Data
    LineGraphSeries<DataPoint> series;
    //Timers
    private final Handler mHandler = new Handler();
    private Runnable mTimer1, mTimer2;

    Date start;
    Date end;
    long currentTimeMs;

    //Events
    private OnFragmentInteractionListener listener;
    public void setListener(OnFragmentInteractionListener listener) {
        this.listener = listener;
    }

    public FragmentLoadMonitor() {
        // Required empty public constructor
        start = Calendar.getInstance().getTime();
        end = Calendar.getInstance().getTime();
        currentTimeMs = 0;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_load_monitor, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        graph = view.findViewById(R.id.graph);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(30);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(10000);

        series = new LineGraphSeries<>();
        series.setDrawDataPoints(true);
        graph.addSeries(series);

        txtLoad = view.findViewById(R.id.txtLoad);
        txtImpCounter = view.findViewById(R.id.txtImpCounter);
        txtLastImpTime = view.findViewById(R.id.txtLastImpTime);
        txtTariff = view.findViewById(R.id.txtTariff);

        mTimer1 = new Runnable() {
            @Override
            public void run() {
                if(listener != null)
                    listener.onSendCmdRequest(
                            Commands.Read_IMP_extra,
                            ImpNum.IMP1
                    );
                mHandler.postDelayed(this, 500);
            }
        };
        mTimer2 = new Runnable() {
            @Override
            public void run() {
                if(listener != null)
                    listener.onSendCmdRequest(
                            Commands.Read_IMP_extra,
                            ImpNum.IMP2
                    );
                mHandler.postDelayed(this, 500);
            }
        };
        swImp1 = view.findViewById(R.id.swImp1);
        swImp1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    //
                    swImp2.setChecked(false);
                    //
                    clearGraphData();
                    //Запускаем перодический опрос
                    mHandler.postDelayed(mTimer1, 500);
                }
                else {
                    mHandler.removeCallbacks(mTimer1);
                }
            }
        });
        swImp2 = view.findViewById(R.id.swImp2);
        swImp2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    //
                    swImp1.setChecked(false);
                    //
                    clearGraphData();
                    //Запускаем перодический опрос
                    mHandler.postDelayed(mTimer2, 500);
                }
                else {
                    mHandler.removeCallbacks(mTimer2);
                }
            }
        });
    }

    private void clearGraphData() {
        currentTimeMs = 0;
        series.resetData(new DataPoint[]{});
        start = Calendar.getInstance().getTime();
    }

    public void addNewDataPoint(DeviceImpExParams data) {
        if(data == null) return;
        //To textViews
        txtLoad.setText("" + data.getCurrentPower() + " Вт");
        txtImpCounter.setText("" + data.getImpCounter());
        txtLastImpTime.setText("" + data.getTimeMsFromLastImp()/1000 + " сек");
        txtTariff.setText("Тариф " + data.getCurrentTarif());
        //Graph
        end = Calendar.getInstance().getTime();
        currentTimeMs += end.getTime() - start.getTime();
        start = end;
        DataPoint point = new DataPoint(currentTimeMs/1000, data.getCurrentPower());
        series.appendData(point, true, 60);
        graph.onDataChanged(false, false);
    }

    @Override
    public void onPause() {
        mHandler.removeCallbacks(mTimer1);
        mHandler.removeCallbacks(mTimer2);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mHandler.removeCallbacks(mTimer1);
        mHandler.removeCallbacks(mTimer2);
        super.onDestroy();
    }

}
