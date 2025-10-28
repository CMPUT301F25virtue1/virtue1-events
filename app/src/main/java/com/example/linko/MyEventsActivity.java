package com.example.linko;

import static com.example.linko.NavigationBarHandler.navigationListener;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MyEventsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_events);

        Button registeredEvents = findViewById(R.id.button_registered);
        Button organizedEvents = findViewById(R.id.button_organized);
        TextView noEventsRegistered = findViewById(R.id.text_no_event_registered);
        TextView noEventsOrganized = findViewById(R.id.text_no_event_organized);

        // placeholder
        registeredEvents.setOnClickListener( v -> {
            noEventsRegistered.setVisibility(View.VISIBLE);
            noEventsOrganized.setVisibility(View.GONE);
            registeredEvents.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.teal)));
            organizedEvents.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerTeal)));
        });

        organizedEvents.setOnClickListener( v -> {
            noEventsRegistered.setVisibility(View.GONE);
            noEventsOrganized.setVisibility(View.VISIBLE);
            organizedEvents.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.teal)));
            registeredEvents.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerTeal)));
        });

        navigationListener(this);

    }
}