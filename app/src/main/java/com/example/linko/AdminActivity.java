package com.example.linko;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;


public class AdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);

        // views under event
        Button eventButton = findViewById(R.id.button_event);
        TextView eventEventsButton = findViewById(R.id.click_event_events);
        TextView eventOrganizersButton = findViewById(R.id.click_event_organizers);
        RecyclerView eventsRecyclerView = findViewById(R.id.recycler_all_events);
        RecyclerView organizersRecyclerView = findViewById(R.id.recycler_all_organizers);
        EditText eventSearchBar = findViewById(R.id.input_event_search);
        // views under profiles
        Button profilesButton = findViewById(R.id.button_profiles);

        // views under button
        Button imagesButton = findViewById(R.id.button_images);

        eventEventsButton.setOnClickListener(v -> {
            eventsRecyclerView.setVisibility(View.VISIBLE);
            organizersRecyclerView.setVisibility(View.GONE);
            eventSearchBar.setVisibility(View.VISIBLE);
            eventEventsButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.teal)));
            eventOrganizersButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerTeal)));
        });

        eventOrganizersButton.setOnClickListener(v -> {
            eventsRecyclerView.setVisibility(View.GONE);
            organizersRecyclerView.setVisibility(View.VISIBLE);
            eventSearchBar.setVisibility(View.GONE);
            eventEventsButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerTeal)));
            eventOrganizersButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.teal)));
        });
    }
}