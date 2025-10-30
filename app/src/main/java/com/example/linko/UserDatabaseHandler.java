package com.example.linko;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class UserDatabaseHandler {
    private FirebaseFirestore db;
    private CollectionReference usersRef;

    public UserDatabaseHandler() {
        this.db = FirebaseFirestore.getInstance();
        this.usersRef = db.collection("users");
    }

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

    public void deleteCurrentUser(Context context, UserDeleted deleted) {
        String userId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        DocumentReference userRef = usersRef.document(userId);
        userRef.delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                deleted.userDelete();
            }
            else {
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
}
