package com.example.fitmeandroid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import com.example.fitmeandroid.ui.login.LoginActivity;

import java.util.Calendar;

public class AboutActivity extends AppCompatActivity{
    public static TextView dateTxt;
    public AutoCompleteTextView mac1, mac2, mac3, mac4;
    private String[] gender, calorie, weight, height;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        dateTxt = findViewById(R.id.date_txt);
        mac1 = findViewById(R.id.menuAutoComplete1);
        mac2 = findViewById(R.id.menuAutoComplete2);
        mac3 = findViewById(R.id.menuAutoComplete3);
        mac4 = findViewById(R.id.menuAutoComplete4);

        gender = getResources().getStringArray(R.array.gender_list);
        calorie = getResources().getStringArray(R.array.calorie_list);
        weight = getResources().getStringArray(R.array.weight_list);
        height = getResources().getStringArray(R.array.height_list);

        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this, R.layout.dropdown_menu, gender);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, R.layout.dropdown_menu, calorie);
        ArrayAdapter<String> adapter3 = new ArrayAdapter<String>(this, R.layout.dropdown_menu, weight);
        ArrayAdapter<String> adapter4 = new ArrayAdapter<String>(this, R.layout.dropdown_menu, height);

        mac1.setAdapter(adapter1);
        mac2.setAdapter(adapter2);
        mac3.setAdapter(adapter3);
        mac4.setAdapter(adapter4);

//        Button button2 = (Button) findViewById(R.id.continue_forward);
//        button2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//                view.getContext().startActivity(intent);}
//        });

    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user

            String date = day + "/" + month + "/" + year;
            dateTxt.setText(date);
        }

    }
    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }
    public void sendMessage(View v) {
        // Do something in response to button
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}