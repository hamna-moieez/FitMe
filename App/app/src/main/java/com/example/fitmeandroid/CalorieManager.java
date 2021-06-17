package com.example.fitmeandroid;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

public class CalorieManager {

    private DatabaseReference ref;

    public String getCurrentUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        return user.getUid();
    }

    public DatabaseReference dbReference(String table_name) {
        ref = FirebaseDatabase.getInstance().getReference(table_name);
        return ref;
    }

    public void writeCalorieConsumption(Double Sun, Double Mon, Double Tue, Double Wed, Double Thu, Double Fri, Double Sat){
        ref = dbReference("calorieConsumption");
        String uid = getCurrentUser();
        ref = ref.child(uid);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                CalorieConsumption cc = new CalorieConsumption(Sun, Mon, Tue, Wed, Thu, Fri, Sat);
                cc.setSunday(Sun);
                cc.setMonday(Mon);
                cc.setTuesday(Tue);
                cc.setWednesday(Wed);
                cc.setThursday(Thu);
                cc.setFriday(Fri);
                cc.setSaturday(Sat);
                ref.setValue(cc);
                Log.i("FB_ADD", "User information was successfully added");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("FB_error", "could not write user data");
            }
        });
    }

    public void readCalorieConsumption(CallbackCalorie calCallback){
        ref = dbReference("calorieConsumption");
        ref = ref.child(getCurrentUser());
        ref.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                CalorieConsumption cal = snapshot.getValue(CalorieConsumption.class);
                calCallback.onCallback(cal);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("FB_error", "could not read user data");
            }
        });

    }

    public void updateDailyCalorie(String day, Double val){
        ref = dbReference("calorieConsumption");
        ref = ref.child(getCurrentUser());
        ref.addListenerForSingleValueEvent(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                ref.child(day).setValue(val);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }

    public void readDailyCalorie(String day, CallbackCalorie cCallback){
        ref = dbReference("calorieConsumption");
        ref = ref.child(getCurrentUser()).child(day);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    Double calorie = ((Long) dataSnapshot.getValue()).doubleValue();
                    cCallback.calCallback(calorie);
                }
                catch (ClassCastException e){
                    Double calorie = (Double) dataSnapshot.getValue();
                    cCallback.calCallback(calorie);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
