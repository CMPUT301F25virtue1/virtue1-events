package com.example.linko;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class DeleteProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_profile);
        Button deleteProfile = findViewById(R.id.button_delete_profile);
        Button goBack = findViewById(R.id.button_go_back);

        goBack.setOnClickListener(v -> {
            startActivity(new Intent(DeleteProfileActivity.this, SettingsActivity.class));
            finish();
        });

        UserDatabaseHandler db = new UserDatabaseHandler();
        deleteProfile.setOnClickListener(v -> {
            db.deleteCurrentUser(this, () -> {
                startActivity(new Intent(DeleteProfileActivity.this, MainActivity.class));
                finish();
            });
        });
    }
}