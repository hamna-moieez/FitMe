package com.example.fitmeandroid;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DatabaseManager {

    public List<String> food_names = new ArrayList<>();
    public List<String> food_calories = new ArrayList<>();
    public List<String> food_ids = new ArrayList<>();
    private HashMap<String, String> similarFoods = new HashMap<>();

    private FirebaseDatabase rootNode;
    private DatabaseReference reference;

    public DatabaseReference initDB(String tableName ){
        rootNode = FirebaseDatabase.getInstance();
        reference = rootNode.getReference(tableName);
        return reference;
    }

    private void dumpToFirebase(String food_name, String calorie, Integer food_id) {
        reference = initDB("foodCalories");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                FoodCalorie newCalorie = new FoodCalorie(food_name, calorie);
                newCalorie.setFood_name(food_name);
                newCalorie.setFood_calorie(calorie);
                reference.child(String.valueOf(food_id)).setValue(newCalorie);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("could not write data");
            }
        });
    }


    public void loadData(InputStream ips) throws IOException {
        Integer food_id = 1;
        BufferedReader reader = new BufferedReader(new InputStreamReader(ips));
        try {
            String line, name, calorie;
            while((line = reader.readLine()) != null){
                String[] strings = TextUtils.split(line, ",");
                if(strings.length == 0){ continue; }
                food_id += 1;
                name = strings[1];
                calorie = strings[2];
                dumpToFirebase(name, calorie, food_id);
            }
        }finally{
            reader.close();
        }
    }


    public void retrieveFromFirebase(){
        reference = initDB("foodCalories");
        reference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                food_calories.clear();
                List<String> keys = new ArrayList<>();
                for(DataSnapshot keyNode : snapshot.getChildren()){
                    keys.add(keyNode.getKey());
                    FoodCalorie foodCalorie = keyNode.getValue(FoodCalorie.class);
                    getDataInForm(foodCalorie, keyNode.getKey());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("could not read data");
            }
        });
    }

    private void getDataInForm(FoodCalorie data, String key){


        String food_name = data.getFood_name();
        String food_calorie = data.getFood_calorie();
        String food_key = key;

        food_names.add(food_name);
        food_ids.add(food_key);
        food_calories.add(food_calorie);
    }

    public HashMap<String, String> match_based_on_result(String anchor, List<String> calories, List<String> names){
        HashMap<String, String> similar_foods = new HashMap<>();
        for(int i = 0; i < names.size(); i++){
            String calorie = calories.get(i);
            String name = names.get(i);
            anchor = anchor.toLowerCase();
            name = name.toLowerCase();
            if (name.contains(anchor)){ // checking if the name is present in the query
                similar_foods.put(name, calorie);
            }
        }
        return similar_foods;
    }

    public Boolean checkDBState(String tableName){
        final Boolean[] flag = {false};
        reference = initDB(tableName);
        Log.i("check_3", "child was null");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.child(tableName).getValue() == null){
                    flag[0] = false;
                    Log.i("check_0", "child was null");
                }
                else flag[0] = true;
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
        return flag[0];
    }
}