package com.example.linko;

import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

/**
 * Handles the logic for dealing with events in the Firebase Database.
 * Called whenever an event needs to be added to the database, edited, deleted, or looked at.
 * @see EditEventActivity
 * @see EventDetailsActivity
 * @see AddEventActivity
 * @see MyEventsActivity
 */
public class EventDatabaseHandler {
    private FirebaseFirestore db;
    private CollectionReference eventsRef;

    public EventDatabaseHandler() {
        this.db = FirebaseFirestore.getInstance();
        this.eventsRef = db.collection("events");
    }

    /**
     * Adds event to the Firebase database
     * @param eventToAdd The event were adding to the database
     * @param added Checks exceptions
     * @param docRef Reference to where the event is in the database. Nessicery to allow us to read, write, and edit out events
     * @param eventId The Firebase Id for the event
     */
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

    /**
     * Updates our event in the database when things change
     * @param updatedEvent Our updated event
     * @param updated Checks exceptions
     */
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

    public void fetchEventById(String eventId, EventDatabaseHandler.EventFetched fetched) {
        DocumentReference docRef = eventsRef.document(eventId);

        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                Event event = documentSnapshot.toObject(Event.class);
                Log.d("eventfetchfunc", "infetcheventbyid" + event.getEventId());
                fetched.eventFetch(event);
            } else {
                Log.e("Firestore", "Error fetching event: " + eventId, task.getException());
                fetched.eventFetchFailed(task.getException());
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

    public interface EventFetched {
        void eventFetch(Event event);
        void eventFetchFailed(Exception e);
    }
}
