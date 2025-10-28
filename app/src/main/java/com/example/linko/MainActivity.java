package com.example.linko;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.BounceInterpolator;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button signUpButton = findViewById(R.id.button_signup);
        signUpButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SignUpActivity.class));
        });

        Button loginButton = findViewById(R.id.button_login);
        loginButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, NotificationsActivity.class));
        });

        ImageView mascot = findViewById(R.id.img_linko_mascot);

        ObjectAnimator dropDown = ObjectAnimator.ofFloat(mascot, "translationY", -2000, 0);
        dropDown.setDuration(2000);
        dropDown.setInterpolator(new BounceInterpolator());
        dropDown.start();

    }
}