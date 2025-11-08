package com.example.linko;

import static com.example.linko.NavigationBarHandler.navigationListener;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Class that contains all the logic for UI interaction with the notifications tab of the app
 */
public class NotificationsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        EdgeToEdge.enable(this);

        navigationListener(this);
    }
}