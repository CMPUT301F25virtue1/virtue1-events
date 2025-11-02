package com.example.linko;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddEventActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        Button postEvent = findViewById(R.id.button_post_event);
        ImageView editButton = findViewById(R.id.button_edit_event);
        ImageView backButton = findViewById(R.id.button_back_button);
        TextView descriptionButton = findViewById(R.id.click_event_description);
        TextView posterButton = findViewById(R.id.click_event_poster);

        // event display stuff
        TextView eventName = findViewById(R.id.text_event_name);
        TextView eventCapacity = findViewById(R.id.text_event_capacity);
        TextView entrantLimit = findViewById(R.id.text_entrant_limit);
        CheckBox geolocationCheck = findViewById(R.id.checkBox);
        TextView eventLocation = findViewById(R.id.text_event_location);
        TextView eventTime = findViewById(R.id.text_event_time);
        TextView registrationPeriod = findViewById(R.id.text_event_registration_period);
        TextView eventDescription = findViewById(R.id.text_event_description);
        ImageView eventPoster = findViewById(R.id.image_event_poster);

        Event eventReceived = (Event) getIntent().getSerializableExtra("savedEvent");
        if (eventReceived != null) {
            eventName.setText(eventReceived.getName());
            Integer eventCapacityNumber = eventReceived.getEventCapacity();
            String eventCapacityString = eventCapacityNumber.toString();
            eventCapacity.setText(eventCapacityString);

            Integer entrantLimitNumber = eventReceived.getEntrantLimit();
            if (eventReceived.getEntrantLimit() == null) {
                eventCapacity.setText("N/A");
            }
            else {
                String entrantLimitString = entrantLimitNumber.toString();
                entrantLimit.setText(entrantLimitString);
            }

            geolocationCheck.setChecked(eventReceived.isGeolocationRequired());
            eventLocation.setText(eventReceived.getEventLocation());
            eventTime.setText(eventReceived.getEventTime());

            Date start = eventReceived.getRegistrationStart();
            Date end = eventReceived.getRegistrationEnd();
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.getDefault());
            String period = sdf.format(start) + " to " + sdf.format(end);
            registrationPeriod.setText(period);

            eventDescription.setText(eventReceived.getDescription());
        }

        descriptionButton.setOnClickListener(v -> {
            eventDescription.setVisibility(View.VISIBLE);
            eventPoster.setVisibility(View.GONE);
            descriptionButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.teal)));
            posterButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerTeal)));
        });

        posterButton.setOnClickListener(v -> {
            eventDescription.setVisibility(View.GONE);
            eventPoster.setVisibility(View.VISIBLE);
            descriptionButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerTeal)));
            posterButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.teal)));
        });

        backButton.setOnClickListener(v -> {
            startActivity(new Intent(AddEventActivity.this, MyEventsActivity.class));
            finish();
        });

        editButton.setOnClickListener(v -> {
            startActivity(new Intent(AddEventActivity.this, EditEventActivity.class));
            finish();
        });

        postEvent.setOnClickListener(v -> {
            if (eventReceived != null) {
                EventDatabaseHandler db = new EventDatabaseHandler();
                String userId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                eventReceived.setOwnerID(userId);
                Log.d("eventreceivedcheck", eventReceived.getOwnerID());
                db.addEvent(eventReceived, new EventDatabaseHandler.EventAdded() {
                    @Override
                    public void eventAdd() {
                        Toast.makeText(AddEventActivity.this, "Event has been posted!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(AddEventActivity.this, MyEventsActivity.class));
                        finish();
                    }

                    @Override
                    public void eventFailedToAdd(Exception e) {
                        Log.e("Firestore", "Error saving user", e);
                        Toast.makeText(AddEventActivity.this, "Error saving event: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
            else {
                Toast.makeText(AddEventActivity.this, "You have not provided sufficient event details to post.", Toast.LENGTH_LONG).show();
            }
        });
    }
}