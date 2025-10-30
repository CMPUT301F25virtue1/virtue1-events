package com.example.linko;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.BounceInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private CollectionReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button signUpButton = findViewById(R.id.button_signup);
        Button loginButton = findViewById(R.id.button_login);

        UserDatabaseHandler databaseHandler = new UserDatabaseHandler();
        databaseHandler.getCurrentUser(this, currentUser -> {
            if (currentUser == null) {
                loginButton.setAlpha(0.5f);
                loginButton.setOnClickListener(v -> {
                    Toast.makeText(this, "You must sign up for an account.", Toast.LENGTH_SHORT).show();
                });
                signUpButton.setOnClickListener(v -> {
                    startActivity(new Intent(MainActivity.this, SignUpActivity.class));
                    finish();
                });
            }
            else {
                signUpButton.setAlpha(0.5f);
                signUpButton.setOnClickListener(v -> {
                    Toast.makeText(this, "You have already made an account for this device.", Toast.LENGTH_SHORT).show();
                });
                loginButton.setOnClickListener(v -> {
                    startActivity(new Intent(MainActivity.this, NotificationsActivity.class));
                    finish();
                });
            }
        });

        ImageView mascot = findViewById(R.id.img_linko_mascot);

        ObjectAnimator dropDown = ObjectAnimator.ofFloat(mascot, "translationY", -2000, 0);
        dropDown.setDuration(2000);
        dropDown.setInterpolator(new BounceInterpolator());
        dropDown.start();

    }
}