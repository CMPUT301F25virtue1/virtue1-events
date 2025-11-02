package com.example.linko;

import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class EventDatabaseHandler {
    private FirebaseFirestore db;
    private CollectionReference eventsRef;

    public EventDatabaseHandler() {
        this.db = FirebaseFirestore.getInstance();
        this.eventsRef = db.collection("events");
    }

    public void addEvent(Event eventToAdd, EventDatabaseHandler.EventAdded added, DocumentReference docRef, String eventId) {
        eventToAdd.setEventId(eventId);
        docRef.set(eventToAdd).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                added.eventAdd();
            }
            else {
                Log.e("Firestore", "Error adding event to database", task.getException());
                added.eventFailedToAdd(task.getException());
            }
        });
    }

    public interface EventAdded {
        void eventAdd();
        void eventFailedToAdd(Exception e);
    }
}
