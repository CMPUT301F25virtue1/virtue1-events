package com.example.linko;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

/**
 * This is the class for handling the delete profile logic that interacts with the UI.
 * Calls UserDatabaseHandler to remove users from database once profile has been deleted.
 * @see UserDatabaseHandler
 */
public class DeleteProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_profile);
        EdgeToEdge.enable(this);

        Button deleteProfile = findViewById(R.id.button_delete_profile);
        Button goBack = findViewById(R.id.button_go_back);

        goBack.setOnClickListener(v -> {
            startActivity(new Intent(DeleteProfileActivity.this, SettingsActivity.class));
            finish();
        });

        deleteProfile.setOnClickListener(v -> {
            String userId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

            // delete all events that have this user as their organizer first
            EventDatabaseHandler deleteEventsHelper = new EventDatabaseHandler();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            CollectionReference eventsRef = db.collection("events");
            eventsRef.addSnapshotListener((value, error) -> {
                if (error != null) {
                    Log.e("Firestore", error.toString());
                }
                if (value != null && !value.isEmpty()) {
                    Log.d("Firebase", "checking documents");
                    for (QueryDocumentSnapshot snapshot : value) {
                        Event eventToDelete = snapshot.toObject(Event.class);
                        if (eventToDelete.getOwnerId().equals(userId)) {
                            deleteEventsHelper.deleteEvent(eventToDelete, new EventDatabaseHandler.EventDeleted() {
                                @Override
                                public void eventDelete() {

                                }

                                @Override
                                public void eventDeleteFailed(Exception e) {
                                    Toast.makeText(DeleteProfileActivity.this, "Error deleting events.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }
            });
            // now delete their document and update event lists they are in
            UserDatabaseHandler deleteHelper = new UserDatabaseHandler();
            deleteHelper.deleteUserById(userId, new UserDatabaseHandler.UserDeletedFromId() {
                @Override
                public void userDelete() {
                    Toast.makeText(DeleteProfileActivity.this, "Profile successfully deleted!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(DeleteProfileActivity.this, MainActivity.class));
                    finish();
                }

                @Override
                public void userDeleteFailed(Exception e) {
                    Toast.makeText(DeleteProfileActivity.this, "Error deleting profile.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}