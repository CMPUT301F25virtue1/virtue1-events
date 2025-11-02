package com.example.linko;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EventDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_details);

        // ui
        ImageView backButton = findViewById(R.id.button_back_button);
        TextView eventName = findViewById(R.id.text_event_name);
        TextView eventCapacity = findViewById(R.id.text_event_capacity);
        TextView entrantCount = findViewById(R.id.text_entrant_count);
        CheckBox geolocationCheck = findViewById(R.id.checkBox);
        TextView eventLocation = findViewById(R.id.text_event_location);
        TextView eventTime = findViewById(R.id.text_event_time);
        TextView registrationPeriod = findViewById(R.id.text_event_registration_period);
        TextView eventDescription = findViewById(R.id.text_event_description);
        ImageView eventPoster = findViewById(R.id.image_event_poster);
        Button joinWaitlist = findViewById(R.id.button_join_waitlist);
        Button leaveWaitlist = findViewById(R.id.button_leave_waitlist);

        Event eventReceived = (Event) getIntent().getSerializableExtra("clickedEvent");
        if (eventReceived == null) {
            Log.e("Event", "The event clicked was null.");
            finish();
            return;
        }

        eventName.setText(eventReceived.getName());
        Integer eventCapacityNumber = eventReceived.getEventCapacity();
        String eventCapacityString = eventCapacityNumber.toString();
        eventCapacity.setText(eventCapacityString);

        entrantCount.setText(eventReceived.getEntrantCount());

        geolocationCheck.setChecked(eventReceived.isGeolocationRequired());
        eventLocation.setText(eventReceived.getEventLocation());
        eventTime.setText(eventReceived.getEventTime());

        Date start = eventReceived.getRegistrationStart();
        Date end = eventReceived.getRegistrationEnd();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.getDefault());String period = sdf.format(start) + " to " + sdf.format(end);
        registrationPeriod.setText(period);
        eventDescription.setText(eventReceived.getDescription());

        joinWaitlist.setOnClickListener(v -> {
            joinWaitlist.setVisibility(View.INVISIBLE);
            leaveWaitlist.setVisibility(View.VISIBLE);
        });

        leaveWaitlist.setOnClickListener(v -> {
            joinWaitlist.setVisibility(View.VISIBLE);
            leaveWaitlist.setVisibility(View.INVISIBLE);
        });

        backButton.setOnClickListener(v -> {
            startActivity(new Intent(EventDetailsActivity.this, ExploreEventsActivity.class));
            finish();
        });
    }
}