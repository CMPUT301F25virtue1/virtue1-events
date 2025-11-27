package com.example.linko;

import android.widget.Button;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SampleButtonHandler {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface SampleCallback {
        void onSuccess(int freeSpace, List<String> newInvited, List<String> invited, List<String> signedUp);
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
        List<String> entrants = new ArrayList<>(event.getEntrants());
        List<String> newInvited = new ArrayList<>();

        //If statement for if event has no one signed up
        if (entrants.isEmpty()){
            callback.onFail("No one is signed up, try again later.");
            return;
        }

        //Event capacity - num of invited - num of signed up = amount of free space.
        // to calc how many more entrants to invite
        int freeSpace = event.getEventCapacity() - (signedUp.size() + invited.size());

        if(freeSpace <= 0){
            callback.onFail("Event full");
            return;
        }

        List<String> okToAdd = new ArrayList<>();
        for(String id : entrants){
            if(!invited.contains(id) && !cancelled.contains(id) && !signedUp.contains(id)){
                okToAdd.add(id);
            }
        }

        if(okToAdd.isEmpty()){
            callback.onFail("All entrants already invited!");
            return;
        }

        for(int i = freeSpace; i > 0; i--){
            randomSelector(signedUp, newInvited, invited, okToAdd);
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
    public void randomSelector(List<String> signedUp, List<String> newInvited, List<String> invited, List<String> okToAdd){
        Random rand = new Random();

        if(!okToAdd.isEmpty()) {
            String temp = okToAdd.get(rand.nextInt(okToAdd.size()));
            newInvited.add(temp);
            invited.add(temp);
            okToAdd.remove(temp);
        }

    }
}
