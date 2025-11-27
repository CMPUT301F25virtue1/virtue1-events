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

        db = FirebaseFirestore.getInstance();

        deleteProfile.setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, DeleteProfileActivity.class));
            finish();
        });

        UserDatabaseHandler databaseHandler = new UserDatabaseHandler();
        databaseHandler.getCurrentUser(this, currentUser -> {
            if (currentUser != null) {
                userRef = db.collection("users").document(currentUser.getUserId());

                Log.d("Admin", "User loaded: " + currentUser.getUserId() + ", isAdmin: " + currentUser.isAdmin());
                if (currentUser.isAdmin()) {
                    adminButton.setVisibility(View.VISIBLE);
                } else {
                    adminButton.setVisibility(View.GONE);
                }

                if (currentUser.isNotificationsEnabled()) {
                    allNotificationsCard.setVisibility(View.VISIBLE);
                    silenceNotificationsCard.setVisibility(View.GONE);
                }
                else {
                    allNotificationsCard.setVisibility(View.GONE);
                    silenceNotificationsCard.setVisibility(View.VISIBLE);
                }
            }
        });

        adminButton.setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, AdminActivity.class));
            finish();
        });

        allNotificationsCard.setOnClickListener(v -> {
            allNotificationsCard.setVisibility(View.GONE);
            silenceNotificationsCard.setVisibility(View.VISIBLE);
            updateNotificationSettings(false);
        });

        silenceNotificationsCard.setOnClickListener(v -> {
            allNotificationsCard.setVisibility(View.VISIBLE);
            silenceNotificationsCard.setVisibility(View.GONE);
            updateNotificationSettings(true);
        });

        navigationListener(this);
    }

    private void updateNotificationSettings(boolean enabled) {
        if (userRef != null) {
            userRef.update("notificationsEnabled", enabled)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("SettingsActivity", "Notification settings saved: " + enabled);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("SettingsActivity", "Failed to update notification settings", e);
                    });
        }
    }

}