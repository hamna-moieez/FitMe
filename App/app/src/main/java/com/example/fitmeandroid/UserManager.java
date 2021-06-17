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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UserManager {

    private DatabaseReference ref;

    public String[] getCurrentUser(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // User is signed in
            String uid = user.getUid();
            String email = user.getEmail();
            String [] userCredentials = {uid, email};
            return userCredentials;
        } else {
            // No user is signed in
            Log.i("LogIn_error", "No user is signed in");
        }
        return new String[0];
    }

    public DatabaseReference dbReference(String table_name) {
        ref = FirebaseDatabase.getInstance().getReference(table_name);
        return ref;
    }

    public void addUserInfo(String dob, String gender, String cal, String height, String weight) {
        ref = dbReference("users");
        String [] authInfo = getCurrentUser();
        String userId = authInfo[0];
        String email = authInfo[1];
        ref = ref.child(userId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User newUser = new User(email, dob, gender, cal, weight, height);
                newUser.setEmail(email);
                newUser.setDob(dob);
                newUser.setGender(gender);
                newUser.setCalorie(cal);
                newUser.setWeight(weight);
                newUser.setHeight(height);

                ref.setValue(newUser);

                Log.i("FB_ADD", "User information was successfully added");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("FB_error", "could not write user data");
            }
        });
    }

    public void getUserInfo(CallbackReader mycallback){
        ref = dbReference("users");
        String [] authInfo = getCurrentUser();
        String userId = authInfo[0];
        ref = ref.child(userId);
        ref.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                mycallback.onCallback(user);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("FB_error", "could not read user data");
            }
        });

    }



}
