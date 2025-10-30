package com.example.linko;

import static com.example.linko.NavigationBarHandler.navigationListener;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.service.autofill.UserData;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        String userId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        Button deleteProfile = findViewById(R.id.button_delete_profile);

        UserDatabaseHandler db = new UserDatabaseHandler();
        deleteProfile.setOnClickListener(v -> {
            db.deleteCurrentUser(this, () -> startActivity(new Intent(SettingsActivity.this, MainActivity.class)));
        });

        navigationListener(this);

    }
}