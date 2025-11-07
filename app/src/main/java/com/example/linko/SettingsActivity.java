package com.example.linko;

import static com.example.linko.NavigationBarHandler.navigationListener;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.service.autofill.UserData;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

/**
 * Class that contains all the logic for UI interaction with the profile settings part of the app.
 * Also calls the DeleteProfileActivity when the delete profile button in the settings it clicked
 * @see DeleteProfileActivity
 */
public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        EdgeToEdge.enable(this);

        Button deleteProfile = findViewById(R.id.button_delete_profile);
        Button adminButton = findViewById(R.id.button_enter_admin);

        deleteProfile.setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, DeleteProfileActivity.class));
            finish();
        });

        UserDatabaseHandler databaseHandler = new UserDatabaseHandler();
        databaseHandler.getCurrentUser(this, currentUser -> {
            Log.d("Admin", "User loaded: " + currentUser.getUserId() + ", isAdmin: " + currentUser.isAdmin());
            if (currentUser.isAdmin()) {
                adminButton.setVisibility(View.VISIBLE);
            }
            else {
                adminButton.setVisibility(View.GONE);
            }
        });

        adminButton.setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, AdminActivity.class));
            finish();
        });

        navigationListener(this);
    }
}