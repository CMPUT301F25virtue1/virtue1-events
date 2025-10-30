package com.example.linko;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
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
        usersRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", error.toString());
            }

            boolean userExists = false;
            if (value != null && !value.isEmpty()) {
                for (QueryDocumentSnapshot snapshot : value) {
                    if (snapshot.getString("userId").equals(userId)) {
                        userExists = true;
                        String firstName = snapshot.getString("firstName");
                        String lastName = snapshot.getString("lastName");
                        String email = snapshot.getString("email");
                        String number = snapshot.getString("phone");
                        String profileUri = snapshot.getString("profileUri");

                        User currentUser = new User(userId, firstName, lastName, email, number, profileUri);
                        fetched.userLoaded(currentUser);
                    }
                }
            }
            if (!userExists) {
                fetched.userLoaded(null);
            }
        });
    }

    public interface UserFetched {
        void userLoaded(User user);
    }
}
