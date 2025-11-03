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
                Log.d("AddEvent", "Event to save: " + eventToAdd.getName() + ", eventId: " + eventToAdd.getEventId());
                added.eventAdd();
            }
            else {
                Log.e("Firestore", "Error adding event to database", task.getException());
                added.eventFailedToAdd(task.getException());
            }
        });
    }

    public void update(Event updatedEvent, EventDatabaseHandler.EventUpdated updated) {
        DocumentReference docRef = db.collection("events").document(updatedEvent.getEventId());
        docRef.set(updatedEvent).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                updated.eventUpdate();
            } else {
                Log.e("Firestore", "Error updating event in database", task.getException());
                updated.eventFailedToUpdate(task.getException());
            }
        });
    }
    public interface EventAdded {
        void eventAdd();
        void eventFailedToAdd(Exception e);
    }

    public interface EventUpdated {
        void eventUpdate();
        void eventFailedToUpdate(Exception e);
    }
}
