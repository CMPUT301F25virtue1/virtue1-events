package com.example.linko;

import static com.example.linko.NavigationBarHandler.navigationListener;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.service.autofill.UserData;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

/**
 * Class that contains all the logic for UI interaction with the profile settings part of the app.
 * Also calls the DeleteProfileActivity when the delete profile button in the settings it clicked
 * @see DeleteProfileActivity
 */
public class SettingsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private DocumentReference userRef;

    private CheckBox allNotificationsCheckbox;
    private CheckBox silenceNotificationsCheckbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        Button deleteProfile = findViewById(R.id.button_delete_profile);
        Button adminButton = findViewById(R.id.button_enter_admin);

        CardView allNotificationsCard = findViewById(R.id.all_notifications_card);
        CardView silenceNotificationsCard = findViewById(R.id.silence_notifications_card);
        allNotificationsCheckbox = findViewById(R.id.all_notifications_checkbox);
        silenceNotificationsCheckbox = findViewById(R.id.silence_notifications_checkbox);

        db = FirebaseFirestore.getInstance();

        allNotificationsCheckbox.setClickable(false);
        allNotificationsCheckbox.setFocusable(false);
        silenceNotificationsCheckbox.setClickable(false);
        silenceNotificationsCheckbox.setFocusable(false);

        deleteProfile.setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, DeleteProfileActivity.class));
            finish();
        });

        UserDatabaseHandler databaseHandler = new UserDatabaseHandler();
        databaseHandler.getCurrentUser(this, currentUser -> {
            if (currentUser != null) {
                userRef = db.collection("users").document(currentUser.getUserId());
                loadNotificationSettings();

                Log.d("Admin", "User loaded: " + currentUser.getUserId() + ", isAdmin: " + currentUser.isAdmin());
                if (currentUser.isAdmin()) {
                    adminButton.setVisibility(View.VISIBLE);
                } else {
                    adminButton.setVisibility(View.GONE);
                }
            }
        });

        adminButton.setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, AdminActivity.class));
            finish();
        });

        allNotificationsCard.setOnClickListener(v -> {
            if (!allNotificationsCheckbox.isChecked()) {
                updateNotificationSettings(true);
            }
        });

        silenceNotificationsCard.setOnClickListener(v -> {
            if (!silenceNotificationsCheckbox.isChecked()) {
                updateNotificationSettings(false);
            }
        });

        navigationListener(this);
    }

    private void loadNotificationSettings() {
        if (userRef != null) {
            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists() && documentSnapshot.contains("notificationsEnabled")) {
                    Boolean notificationsEnabled = documentSnapshot.getBoolean("notificationsEnabled");
                    if (notificationsEnabled != null) {
                        updateCheckboxes(notificationsEnabled);
                    } else {
                        updateCheckboxes(true);
                    }
                } else {
                    updateNotificationSettings(true);
                }
            }).addOnFailureListener(e -> {
                Log.e("SettingsActivity", "Failed to load notification settings", e);
                updateCheckboxes(true);
            });
        }
    }

    private void updateNotificationSettings(boolean enabled) {
        updateCheckboxes(enabled);

        if (userRef != null) {
            userRef.update("notificationsEnabled", enabled)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("SettingsActivity", "Notification settings saved: " + enabled);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("SettingsActivity", "Failed to update notification settings", e);
                        updateCheckboxes(!enabled);
                    });
        }
    }

    private void updateCheckboxes(boolean allNotificationsEnabled) {
        allNotificationsCheckbox.setChecked(allNotificationsEnabled);
        silenceNotificationsCheckbox.setChecked(!allNotificationsEnabled);
    }
}