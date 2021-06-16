package com.example.fitmeandroid;
import com.github.shchurov.horizontalwheelview.HorizontalWheelView;

import com.google.android.material.textfield.TextInputLayout;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import androidx.appcompat.app.AppCompatActivity;

import static com.github.shchurov.horizontalwheelview.HorizontalWheelView.SCROLL_STATE_IDLE;

public class CalorieActivity extends AppCompatActivity {

    private HorizontalWheelView horizontalWheelView;
    private TouchHandler touchHandler;
    private TextView tvAngle;
    private Button calCount;
    private String defaultCal = "0.0";
    private String foodChoice;
    private int choiceId;
    private ImageView foodImage;
    private HashMap<String, Integer> cls2id = new HashMap<>();
    private DatabaseManager dbManager;
    private HashMap<String, String> foodTypes = new HashMap<>();
    private AutoCompleteTextView mac_food, mac_serving;
    private String foodName;
    private Double foodCals, servingSize, totalCals;

    List<String> foodList;
    ArrayList<String> similar_cals;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calorie);

        fillCls2Id();
        getTopChoice();
        foodImage = findViewById(R.id.img_holder);
        final int [] categoryArray = {R.drawable.ic_meat, R.drawable.ic_pasta,
                R.drawable.ic_icecream, R.drawable.ic_fish, R.drawable.ic_chicken,
                R.drawable.ic_burger, R.drawable.ic_fruit , R.drawable.ic_noodle,
                R.drawable.ic_rice};
        for (int i=0; i<categoryArray.length; i++){
            if (i == choiceId){
                foodImage.setImageResource(categoryArray[i]);
            }
        }
        mac_food = findViewById(R.id.mac_food);
        dbManager = new DatabaseManager();
        dbManager.retrieveFromFirebase(foodChoice, new SimilarCal() {
            @Override
            public void onCallback(HashMap<String, String> similar_vals) {
                similar_cals = new ArrayList<>(similar_vals.values());
                foodList = new ArrayList<>(similar_vals.keySet());
                ArrayAdapter<String> adapter_food = new ArrayAdapter<>(getApplicationContext(), R.layout.dropdown_menu, foodList);
                mac_food.setAdapter(adapter_food);
            }
        });

        mac_serving = findViewById(R.id.mac_serving);
        String[] serving = getResources().getStringArray(R.array.serving_list);
        ArrayAdapter<String> adapter_serving = new ArrayAdapter<String>(this, R.layout.dropdown_menu, serving);
        mac_serving.setAdapter(adapter_serving);

        calCount = findViewById(R.id.add_cal_count);
        mac_food.setOnItemClickListener((adapterView, view, i, l) -> {
            foodCals = Double.valueOf(similar_cals.get(i));
        });

        mac_serving.setOnItemClickListener((adapterView, view, i, l) -> {
            servingSize = Double.valueOf(mac_serving.getText().toString());
            totalCals = calorieCalculator(foodCals, servingSize);
            touchHandler = new TouchHandler(horizontalWheelView);
            touchHandler.playSettlingAnimation(totalCals/100.0);
            setupListeners();
            updateText();
            touchHandler.updateScrollStateIfRequired(SCROLL_STATE_IDLE);
        });

        initViews();

    }

    private Double calorieCalculator(Double foodCals, Double servingSize) {
        return (foodCals/100.0) * servingSize;

    }


    private void getTopChoice(){
        foodChoice = getIntent().getStringExtra("foodChoice");
        if(foodChoice.equals("cat")){
            foodChoice = "chicken";
        }
        choiceId = cls2id.get(foodChoice);
    }

    private void fillCls2Id(){
        cls2id.put("meat", 0);
        cls2id.put("pasta", 1);
        cls2id.put("icecream", 2);
        cls2id.put("fish", 3);
        cls2id.put("chicken", 4); // TODO : replace with chicken
        cls2id.put("dog", 5); // TODO : replace with burger
        cls2id.put("fruit", 6);
        cls2id.put("noodle", 7);
        cls2id.put("rice", 8);
//        cls2id.put("pizza", 9); // TODO: add pizza class
    }


    private void initViews() {
        horizontalWheelView = (HorizontalWheelView) findViewById(R.id.horizontalWheelView);
        tvAngle = (TextView) findViewById(R.id.tvAngle);
        tvAngle.setText(defaultCal);

    }

    private void setupListeners() {
        horizontalWheelView.setListener(new HorizontalWheelView.Listener() {
            @Override
            public void onRotationChanged(double radians) {
                updateText();
            }
        });
    }

    private void updateText() {
        String text = String.format(Locale.US, "%.0f", totalCals);
        tvAngle.setText(text);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        updateText();
    }

}