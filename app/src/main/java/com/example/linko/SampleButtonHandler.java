package com.example.linko;

import android.widget.Button;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SampleButtonHandler {
    private FirebaseFirestore db;

    public interface SampleCallback {
        void onSuccess(int freeSpace, List<String> invited, List<String> signedUp);
        void onFail(String error);
    }

    public void sampling (Event event, SampleCallback callback ){
        if (event == null){
            callback.onFail("Event error");
            return;
        }


        List<String> invited = new ArrayList<>(event.getInvitedEntrants());
        List<String> signedUp = new ArrayList<>(event.getSignedUpEntrants());
        List<String> cancelled = new ArrayList<>(event.getCancelledEntrants());

        //If statement for if event has no one signed up
        if (invited.isEmpty()){
            callback.onFail("No one is signed up, try again later.");
            return;
        }

        //Event capacity - num of invited = amount of free space
        int freeSpace = event.getEventCapacity() - signedUp.size();

        if(freeSpace <= 0){
            callback.onFail("Event full");
            return;
        }

        for(int i = freeSpace; i > 0; i--){
            randomSelector(signedUp, invited);
        }

        db.collection("events").document(event.getEventId()).update("invitedEntrants", invited, "signedUpEntrants", signedUp)
                .addOnSuccessListener(v -> {
                    callback.onSuccess(freeSpace, invited, signedUp);
                })
                .addOnFailureListener(e -> {
                    callback.onFail(e.getMessage());
                });

    }

    //Random Selector
    public void randomSelector(List<String> signedUp, List<String> invited){
        Random rand = new Random();

        if(!invited.isEmpty()) {
            String temp = invited.get(rand.nextInt(invited.size()));
            signedUp.add(temp);
            invited.remove(temp);
        }

    }
}
