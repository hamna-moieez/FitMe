package com.example.fitmeandroid.ui.stats;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import com.example.fitmeandroid.R;
import com.example.fitmeandroid.databinding.FragmentStatsBinding;
import com.example.fitmeandroid.ui.profile.ProfileFragment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatsFragment extends Fragment {


    private FragmentStatsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStatsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        // Write a message to the database
//        FirebaseDatabase database = FirebaseDatabase.getInstance("https://fitme-98d28-default-rtdb.europe-west1.firebasedatabase.app/");
//        DatabaseReference myRef = database.getReference("message");
//        myRef.setValue("Hello, World!");
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}