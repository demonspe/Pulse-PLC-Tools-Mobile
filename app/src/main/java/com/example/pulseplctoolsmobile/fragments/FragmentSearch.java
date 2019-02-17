package com.example.pulseplctoolsmobile.fragments;


import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.pulseplctoolsmobile.R;

public class FragmentSearch extends Fragment {

    LinearLayout dContainer;

    public FragmentSearch() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false);
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Поиск устройств...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                addDevice("Счетчик [12345678]", true);
            }
        });

        dContainer = view.findViewById(R.id.dContainer);
        addDevice("Фаза A [12345678]", false);
        addDevice("Фаза B [12345678]", false);
    }

    public void addDevice(String name, boolean mode_counter) {
        Button btn = new Button(getContext());
        btn.setHeight(dpToPx(80));
        btn.setPadding(dpToPx(20), 0,dpToPx(20), 0);
        btn.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        //Текст
        btn.setText("   " + name);

        btn.setTextSize(18);
        //Значки
        Drawable ic_mode;
        if(mode_counter)
            ic_mode = getContext().getResources().getDrawable(R.drawable.ic_mode_counter_24dp );
        else
            ic_mode = getContext().getResources().getDrawable(R.drawable.ic_router_24dp);
        Drawable ic_bt = getContext().getResources().getDrawable(R.drawable.ic_bluetooth_blue_24dp );
        btn.setCompoundDrawablesWithIntrinsicBounds(ic_mode, null, ic_bt, null);
        //Тень
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            btn.setTranslationZ(dpToPx(3));
            btn.setElevation(dpToPx(3));
            btn.setStateListAnimator(null);
        }
        dContainer.addView(btn);
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }
}
