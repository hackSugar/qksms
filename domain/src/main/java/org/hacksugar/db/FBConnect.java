package org.hacksugar.db;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class FBConnect {
    DatabaseReference ref;
    public FBConnect() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();
        System.out.println("WRIING");
        ref = myRef;
    }
    public void addNumber(String key, String hash) {
        ref.child("keys").child(hash).setValue(key);
    }
    public static boolean checkNums(List<String> nums) {
        List<String> newNums = new ArrayList<>();
        for(String number : nums) {
            if (number.contains("+")) {
                newNums.add(number.replace("+", ""));
            }
        }
        for(String number : newNums) {

        }
        return false;
    }
}
