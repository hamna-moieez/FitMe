package com.example.fitmeandroid.ui.profile;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;


import com.example.fitmeandroid.CallbackReader;
import com.example.fitmeandroid.R;
import com.example.fitmeandroid.User;
import com.example.fitmeandroid.UserManager;
import com.example.fitmeandroid.databinding.FragmentProfileBinding;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Calendar;


public class ProfileFragment extends Fragment {
    public static TextView dateTxt;
    public AutoCompleteTextView mac1, mac2, mac3, mac4;
    private TextInputLayout genderMenu, calorieMenu, weightMenu, heightMenu;
    private String[] gender, calorie, weight, height;
    private Button datePicker;
    private FragmentProfileBinding binding;
    private UserManager uManager;
    private ArrayList<String> userInfoObj;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        dateTxt = root.findViewById(R.id.date_txt);
        mac1 = root.findViewById(R.id.menuAutoComplete1);
        mac2 = root.findViewById(R.id.menuAutoComplete2);
        mac3 = root.findViewById(R.id.menuAutoComplete3);
        mac4 = root.findViewById(R.id.menuAutoComplete4);

        showUserInfo();

        genderMenu = root.findViewById(R.id.gender_menu);
        genderMenu.setOnClickListener(view -> modifyGender(root));

        calorieMenu = root.findViewById(R.id.calorie_menu);
        calorieMenu.setOnClickListener(view -> modifyCalorie(root));

        weightMenu = root.findViewById(R.id.weight_menu);
        weightMenu.setOnClickListener(view -> modifyWeight(root));

        heightMenu = root.findViewById(R.id.height_menu);
        heightMenu.setOnClickListener(view -> modifyHeight(root));

        datePicker = root.findViewById(R.id.date_picker);
        datePicker.setOnClickListener(view -> showDatePickerDialog(root));
        return root;
    }

    private void showUserInfo() {
        uManager = new UserManager();
        uManager.getUserInfo(user -> {
            dateTxt.setText(user.getDob());
            mac1.setText(user.getGender());
            mac2.setText(user.getCalorie());
            mac3.setText(user.getWeight());
            mac4.setText(user.getHeight());
        });
    }

    private void modifyGender(View v) {

        gender = getResources().getStringArray(R.array.gender_list);
        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(getActivity(), R.layout.dropdown_menu, gender);
        mac1.setAdapter(adapter1);
    }

    private void modifyCalorie(View v) {
        calorie = getResources().getStringArray(R.array.calorie_list);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(getActivity(), R.layout.dropdown_menu, calorie);
        mac2.setAdapter(adapter2);
    }

    private void modifyWeight(View v) {
        weight = getResources().getStringArray(R.array.weight_list);
        ArrayAdapter<String> adapter3 = new ArrayAdapter<String>(getActivity(), R.layout.dropdown_menu, weight);
        mac3.setAdapter(adapter3);
    }

    private void modifyHeight(View v) {
        height = getResources().getStringArray(R.array.height_list);
        ArrayAdapter<String> adapter4 = new ArrayAdapter<>(getActivity(), R.layout.dropdown_menu, height);
        mac4.setAdapter(adapter4);
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
        DialogFragment newFragment = new ProfileFragment.DatePickerFragment();
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        newFragment.show(fragmentManager, "datePicker");
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}