package com.example.fitmeandroid.ui.stats;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import com.example.fitmeandroid.CallbackCalorie;
import com.example.fitmeandroid.CalorieConsumption;
import com.example.fitmeandroid.CalorieManager;
import com.example.fitmeandroid.R;
import com.example.fitmeandroid.UserManager;
import com.example.fitmeandroid.databinding.FragmentStatsBinding;
import com.example.fitmeandroid.ui.profile.ProfileFragment;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class StatsFragment extends Fragment {


    private FragmentStatsBinding binding;
    Date date = Calendar.getInstance().getTime();
    String day = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(date);
    private ProgressBar progressBar;
    private TextView goalPercent;
    private UserManager userManager;
    private CalorieManager calorieManager;
    private Double goalProgress;
    private Double userGoal;
    private Double progressPercentage;

    private com.github.mikephil.charting.charts.HorizontalBarChart chart;
    private static final String SET_LABEL = "Weekly Calorie Consumption";
    private static final String[] DAYS = { "SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT" };



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStatsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        progressBar = root.findViewById(R.id.progress_bar);
        goalPercent = root.findViewById(R.id.goal_percent);

        userManager = new UserManager();
        userManager.getUserInfo(user -> {

            String calGoal = user.getCalorie().replace("kcal", "");
            userGoal = Double.valueOf(calGoal);
            Log.i("USER GOAL", user.getCalorie());
        });
        calorieManager = new CalorieManager();
        calorieManager.readDailyCalorie(day.toLowerCase(), new CallbackCalorie() {
            @Override
            public void onCallback(CalorieConsumption cal) { }
            @Override
            public void calCallback(Double calorie) {
                goalProgress = calorie;
                Log.i("GOAL_GET", String.valueOf(goalProgress));
                if (goalProgress > userGoal){
                    Toast.makeText(getActivity().getApplicationContext(), "Calorie goal exceeded", Toast.LENGTH_LONG).show();
                }
                progressPercentage = (goalProgress/userGoal)*100.0;
                createProgressIndicator(progressPercentage);
            }
        });

        chart = root.findViewById(R.id.bar_chart);

        createChartData();
        return root;
    }

    private void createProgressIndicator(Double progressPercentage) {
        int pp = (int)Math.round(progressPercentage);
        ObjectAnimator animation = ObjectAnimator.ofInt(progressBar, "progress", 0, pp); // see this max value coming back here, we animate towards that value
        animation.setDuration(5000); // in milliseconds
        animation.setInterpolator(new DecelerateInterpolator());
        animation.start();
        progressBar.clearAnimation();
        String goalDisplay = pp+"%";
        Log.i("GOAL PERCENT", goalDisplay);
        goalPercent.setText(goalDisplay);
    }

    private void configureChartAppearance() {
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.getAxisLeft().setDrawLabels(false);
        chart.getAxisRight().setDrawLabels(false);
        chart.getXAxis().setDrawGridLines(false);
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getAxisRight().setDrawGridLines(false);
        chart.setDrawGridBackground(false);
        chart.setDrawBorders(false);
        chart.getXAxis().setCenterAxisLabels(false);
        chart.getXAxis().setTextColor(Color.parseColor("#E84545"));
        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return DAYS[(int) value];
            }
        });
    }

    private void prepareChartData(BarData data) {
        data.setValueTextSize(12f);
        chart.setData(data);
        chart.invalidate();
    }


    private void createChartData() {
        ArrayList<BarEntry> values = new ArrayList<>();
        ArrayList<Float> calCounts = new ArrayList<>();
        calorieManager.readCalorieConsumption(new CallbackCalorie() {
            @Override
            public void onCallback(CalorieConsumption cal) {
                calCounts.clear();
                calCounts.add(Float.valueOf(String.valueOf(cal.getSunday())));
                calCounts.add(Float.valueOf(String.valueOf(cal.getMonday())));
                calCounts.add(Float.valueOf(String.valueOf(cal.getTuesday())));
                calCounts.add(Float.valueOf(String.valueOf(cal.getWednesday())));
                calCounts.add(Float.valueOf(String.valueOf(cal.getThursday())));
                calCounts.add(Float.valueOf(String.valueOf(cal.getFriday())));
                calCounts.add(Float.valueOf(String.valueOf(cal.getSaturday())));
                Log.i("BAR_GRAPH", String.valueOf(calCounts));
                for (int i = 0; i < calCounts.size(); i++) {
                    float x = i;
                    float y = calCounts.get(i);
                    values.add(new BarEntry(x, y));
                }
                BarDataSet set1 = new BarDataSet(values, SET_LABEL);
                set1.setColor(Color.parseColor("#E84545"));
                set1.setValueTextColor(Color.parseColor("#FFFFFF"));
                ArrayList<IBarDataSet> dataSets = new ArrayList<>();
                dataSets.add(set1);
                BarData data = new BarData(dataSets);
                configureChartAppearance();
                prepareChartData(data);
                chart.animateXY(3000, 3000);

            }

            @Override
            public void calCallback(Double calorie) {}
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}