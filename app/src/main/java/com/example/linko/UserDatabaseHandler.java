package com.example.linko;

import android.content.Context;
import android.provider.Settings;
import android.service.autofill.UserData;
import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

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

    public interface UserFetched {
        void userLoaded(User user);
    }

    public interface UserDeleted {
        void userDelete();
    }

    public interface UserAdded {
        void userAdd();
        void userFailedToAdd(Exception e);
    }

}
