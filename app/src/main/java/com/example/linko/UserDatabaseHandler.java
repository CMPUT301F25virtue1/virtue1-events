package com.example.linko;

import android.content.Context;
import android.provider.Settings;
import android.service.autofill.UserData;
import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.List;

/**
 * Handles all the logic for dealing with the user in the Firebase database. Is called whenever we
 * need to use, edit, add, or delete a user within the database
 */
public class UserDatabaseHandler {
    private FirebaseFirestore db;
    private CollectionReference usersRef;

    public UserDatabaseHandler() {
        this.db = FirebaseFirestore.getInstance();
        this.usersRef = db.collection("users");
    }

    /**'
     * Logic for adding user to database
     * @param userToAdd User to add to database
     * @param added A UserDatabaseHandler object used to tell the caller if the user was successfully
     *              added or if there was an error
     */
    public void addUser(User userToAdd, UserAdded added) {
        usersRef.document(userToAdd.getUserId()).set(userToAdd).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                added.userAdd();
            }
            else {
                Log.e("Firestore", "Error adding user to database", task.getException());
                added.userFailedToAdd(task.getException());
            }
        });
    }

    /**
     * This contains the logic for getting the info for the current devices user.
     * @param context Androids built in context object. Typically caller uses "this" here
     * @param fetched A UserDatabaseHandler object used to tell the caller if the user is actually in
     *                the Firebase database.
     */
    public void getCurrentUser(Context context, UserFetched fetched) {
        String userId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        DocumentReference userRef = usersRef.document(userId);
        userRef.get().addOnCompleteListener(task -> {
            DocumentSnapshot snapshot = task.getResult();

            if (task.isSuccessful()) {
                if (snapshot.exists()) {
                    User currentUser = snapshot.toObject(User.class);
                    fetched.userLoaded(currentUser);
                }
                else {
                    fetched.userLoaded(null);
                }
            }
            else {
                Log.e("Firestore", "Error fetching the current user", task.getException());
                fetched.userLoaded(null);
            }
        });
    }

    /**
     * This contains the logic for deleting the current user from the Firebase database
     * @param context Androids built in context object. Typically caller uses "this" here
     * @param deleted A UserDatabaseHandler object used to tell the caller if the current user was
     *                deleted or if there was an error in the deletion
     */
    public void deleteCurrentUser(Context context, UserDeleted deleted) {
        String userId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        DocumentReference userRef = usersRef.document(userId);
        userRef.delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                deleted.userDelete();
            } else {
                Log.e("Firestore", "Error deleting the current user", task.getException());
            }
        });
    }

    /**
     * Fetches the desired user from the Firebase database
     * @param userId User ID in the database
     * @param fetched Catches errors if theres an error or fetches the user
     */
    public void fetchUserById(String userId, UserDatabaseHandler.UserFetchedFromId fetched) {
        DocumentReference docRef = usersRef.document(userId);

        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                User user = documentSnapshot.toObject(User.class);
                fetched.userFetch(user);
            } else {
                Log.e("Firestore", "Error fetching user: " + userId, task.getException());
                fetched.userFetchFailed(task.getException());
            }
        });
    }

    /**
     * Deleted the desired user from the Firebase database
     * @param userId The user ID we want to delete
     * @param deleted Either confirms the user was deleted or throws an exception
     */
    public void deleteUserById(String userId, UserDatabaseHandler.UserDeletedFromId deleted) {
        DocumentReference userRef = usersRef.document(userId);
        fetchUserById(userId, new UserFetchedFromId() {
            @Override
            public void userFetch(User user) {
                // remove the user pfp from firebase storage
                if (user.getProfileUrl() != null && !user.getProfileUrl().isEmpty()) {
                    FirebaseStorage.getInstance().getReferenceFromUrl(user.getProfileUrl())
                            .delete()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Log.d("Storage", "User profile picture deleted");
                                } else {
                                    Log.e("Storage", "Error deleting profile picture", task.getException());
                                }
                            });
                }

                userRef.delete().addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.e("Firestore", "Error deleting user", task.getException());
                        deleted.userDeleteFailed(task.getException());
                        return;
                    }
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    CollectionReference eventsRef = db.collection("events");

                    // update event lists (ONLY
                    eventsRef.get().addOnCompleteListener(eventsTask -> {
                        if (!eventsTask.isSuccessful() || eventsTask.getResult() == null) {
                            Log.e("Firestore", "Error fetching events", eventsTask.getException());
                            deleted.userDeleteFailed(eventsTask.getException());
                            return;
                        }

                        for (QueryDocumentSnapshot snapshot : eventsTask.getResult()) {
                            Event event = snapshot.toObject(Event.class);

                            // remove userId from all event entrant lists
                            List<String> entrants = event.getEntrants();
                            if (entrants.contains(userId)) {
                                entrants.remove(userId);
                                event.setEntrants(entrants);
                            }

                            List<String> cancelledEntrants = event.getCancelledEntrants();
                            if (cancelledEntrants.contains(userId)) {
                                cancelledEntrants.remove(userId);
                                event.setCancelledEntrants(cancelledEntrants);
                            }

                            List<String> invitedEntrants = event.getInvitedEntrants();
                            if (invitedEntrants.contains(userId)) {
                                invitedEntrants.remove(userId);
                                event.setInvitedEntrants(invitedEntrants);
                            }

                            List<String> signedUpEntrants = event.getSignedUpEntrants();
                            if (signedUpEntrants.contains(userId)) {
                                signedUpEntrants.remove(userId);
                                event.setSignedUpEntrants(signedUpEntrants);
                            }

                            EventDatabaseHandler eventUpdateHelper = new EventDatabaseHandler();
                            eventUpdateHelper.update(event, new EventDatabaseHandler.EventUpdated() {
                                @Override
                                public void eventUpdate() {
                                    Log.d("Databasedelete", "Event entrant lists updated");
                                }

                                @Override
                                public void eventUpdateFailed(Exception e) {
                                    Log.e("Databasedelete", "Event entrant lists failed to update", e);
                                }
                            });
                        }
                        deleted.userDelete();
                    });
                });
            }

            @Override
            public void userFetchFailed(Exception e) {
                deleted.userDeleteFailed(e);
            }
        });

    }


    public interface UserFetched {
        void userLoaded(User user);
    }

    public interface UserFetchedFromId {
        void userFetch(User user);
        void userFetchFailed(Exception e);
    }

    public interface UserDeleted {
        void userDelete();
    }

    public interface UserAdded {
        void userAdd();
        void userFailedToAdd(Exception e);
    }

    public interface UserDeletedFromId {
        void userDelete();
        void userDeleteFailed(Exception e);
    }
}
