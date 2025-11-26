package com.example.linko;

import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
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
     * @param docRef Reference to where the event is in the database. Necessary to allow us to read, write, and edit out events
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
                added.eventAddFailed(task.getException());
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
                updated.eventUpdateFailed(task.getException());
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

    public void deleteEvent(Event event, EventDeleted deleted) {
        // remove the event poster from firebase storage
        if (event.getEventPosterURL() != null && !event.getEventPosterURL().isEmpty()) {
            FirebaseStorage.getInstance().getReferenceFromUrl(event.getEventPosterURL())
                    .delete()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("Storage", "Event poster deleted");
                        } else {
                            Log.e("Storage", "Error deleting poster", task.getException());
                        }
                    });
        }

        String eventIdToDelete = event.getEventId();

        // delete any notifications from that event
        CollectionReference notifsRef = db.collection("notifications");
        List<String> notifIdsToRemove = new ArrayList<>();
        notifsRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e("NOTIFTEST", "Error fetching notifications", task.getException());
                return;
            }
            for (QueryDocumentSnapshot snapshot : task.getResult()) {
                UserNotification notif = snapshot.toObject(UserNotification.class);

                // delete if event id matches
                if (notif.getEventId().equals(eventIdToDelete)) {
                    snapshot.getReference().delete().addOnSuccessListener(a -> {
                        Log.d("NOTIFTEST", "Deleted notif: " + snapshot.getId());
                    }).addOnFailureListener(e -> {
                        Log.e("NOTIFTEST", "Failed to delete notif", e);
                    });
                    notifIdsToRemove.add(notif.getNotificationId());
                }
            }
        });

        // hard delete it from database, and user event lists
        eventsRef.document(eventIdToDelete).delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // delete event from any event history/registered events list for users
                CollectionReference usersRef = db.collection("users");
                usersRef.get().addOnCompleteListener(deleteTask -> {
                    if (!deleteTask.isSuccessful()) {
                        Log.e("Firestore", "Error fetching users", deleteTask.getException());
                        return;
                    }
                    QuerySnapshot users = deleteTask.getResult();
                    for (QueryDocumentSnapshot snapshot : users) {
                        List<String> userRegisteredEvents = (List<String>) snapshot.get("eventsRegistered");
                        List<String> userEventHistory = (List<String>) snapshot.get("eventHistory");

                        if (userRegisteredEvents.contains(eventIdToDelete)) {
                            userRegisteredEvents.remove(eventIdToDelete);
                        }

                        if (userEventHistory.contains(eventIdToDelete)) {
                            userEventHistory.remove(eventIdToDelete);
                        }
                        User userToUpdate = snapshot.toObject(User.class);
                        userToUpdate.getNotificationList().removeAll(notifIdsToRemove);
                        userToUpdate.setEventsRegistered(userRegisteredEvents);
                        userToUpdate.setEventHistory(userEventHistory);

                        UserDatabaseHandler userDb = new UserDatabaseHandler();
                        userDb.addUser(userToUpdate, new UserDatabaseHandler.UserAdded() {
                            @Override
                            public void userAdd() {
                                Log.d("FirebaseDeleteEvent", "Successfully updated user event lists");
                            }

                            @Override
                            public void userFailedToAdd(Exception e) {
                                Log.e("FirebaseDeleteEvent", "Error updating user event lists" + e.getMessage());
                            }
                        });
                    }
                });
                deleted.eventDelete();
            } else {
                Log.e("Firestore", "Error deleting event: " + event.getName(), task.getException());
                deleted.eventDeleteFailed(task.getException());
            }
        });
    }

    public interface EventAdded {
        void eventAdd();
        void eventAddFailed(Exception e);
    }

    public interface EventUpdated {
        void eventUpdate();
        void eventUpdateFailed(Exception e);
    }

    public interface EventFetched {
        void eventFetch(Event event);
        void eventFetchFailed(Exception e);
    }

    public interface EventDeleted {
        void eventDelete();
        void eventDeleteFailed(Exception e);
    }
}
