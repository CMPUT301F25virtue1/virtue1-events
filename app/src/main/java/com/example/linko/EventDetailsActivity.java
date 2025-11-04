package com.example.linko;

import android.content.Intent;
import android.content.res.ColorStateList;
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
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Our class for managing the UI interactive logic for event details.
 * Calls EventDatabaseHandler to update our event in the database with any changes
 * @see EventDatabaseHandler
 */
public class EventDetailsActivity extends AppCompatActivity {
    private Event eventReceived;
    private Button joinWaitlist;
    private Button leaveWaitlist;
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
        TextView descriptionButton = findViewById(R.id.click_event_description);
        TextView posterButton = findViewById(R.id.click_event_poster);
        ImageView eventPoster = findViewById(R.id.image_event_poster);
        joinWaitlist = findViewById(R.id.button_join_waitlist);
        leaveWaitlist = findViewById(R.id.button_leave_waitlist);

        eventReceived = (Event) getIntent().getSerializableExtra("clickedEvent");
        if (eventReceived == null) {
            Log.e("Event", "The event clicked was null.");
            finish();
            return;
        }

        eventName.setText(eventReceived.getName());
        Integer eventCapacityNumber = eventReceived.getEventCapacity();
        String eventCapacityString = eventCapacityNumber.toString();
        eventCapacity.setText(eventCapacityString);
        Glide.with(EventDetailsActivity.this).load(eventReceived.getEventPosterURL()).placeholder(R.drawable.outline_photo_camera_24).centerCrop().into(eventPoster);

        entrantCount.setText(eventReceived.getEntrantCount() + "/" + eventReceived.getEntrantLimit());

        geolocationCheck.setChecked(eventReceived.isGeolocationRequired());
        eventLocation.setText(eventReceived.getEventLocation());
        eventTime.setText(eventReceived.getEventTime());

        Date start = eventReceived.getRegistrationStart();
        Date end = eventReceived.getRegistrationEnd();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.getDefault());
        String period = sdf.format(start) + " to " + sdf.format(end);
        registrationPeriod.setText(period);

        // check if it should be join or leave waitlist
        checkUserRegistered();
        joinWaitlist.setOnClickListener(v -> {
            joinWaitlist.setVisibility(View.INVISIBLE);
            leaveWaitlist.setVisibility(View.VISIBLE);
            changeUserWaitlist();

        });

        leaveWaitlist.setOnClickListener(v -> {
            joinWaitlist.setVisibility(View.VISIBLE);
            leaveWaitlist.setVisibility(View.INVISIBLE);
            changeUserWaitlist();
        });

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
            startActivity(new Intent(EventDetailsActivity.this, ExploreEventsActivity.class));
            finish();
        });
    }

    /**
     * Method for editing the User Waitlist in the Firebase database
     */
    public void changeUserWaitlist() {
        UserDatabaseHandler databaseHandler = new UserDatabaseHandler();
        databaseHandler.getCurrentUser(this, currentUser -> {
            List<String> userEventsRegistered = currentUser.getEventsRegistered();
            List<String> userEventHistory = currentUser.getEventHistory();

            Log.d("eventReceived", eventReceived.getEventId());
            Log.d("eventReceived", "event received" + eventReceived.getOwnerId());

            if (userEventsRegistered.contains(eventReceived.getEventId())) {
                userEventsRegistered.remove(eventReceived.getEventId());
                eventReceived.getEntrants().remove(currentUser.getUserId());
            }
            else {
                userEventsRegistered.add(eventReceived.getEventId());
                eventReceived.getEntrants().add(currentUser.getUserId());
                if (!userEventHistory.contains(eventReceived.getEventId())) {
                    userEventHistory.add(eventReceived.getEventId());
                }
            }

            EventDatabaseHandler eventDatabaseHandler = new EventDatabaseHandler();
            eventDatabaseHandler.update(eventReceived, new EventDatabaseHandler.EventUpdated() {
                @Override
                public void eventUpdate() {
                }

                @Override
                public void eventFailedToUpdate(Exception e) {
                    Toast.makeText(EventDetailsActivity.this, "Error updating waitlist: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

            // adding user is the same as updating
            databaseHandler.addUser(currentUser, new UserDatabaseHandler.UserAdded() {
                @Override
                public void userAdd() {
                    Toast.makeText(EventDetailsActivity.this, "Success!", Toast.LENGTH_LONG).show();
                }

                @Override
                public void userFailedToAdd(Exception e) {
                    Toast.makeText(EventDetailsActivity.this, "Error updating waitlist: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    /**
     * Method to check the database to see if a user is registered for an event
     */
    public void checkUserRegistered() {
        UserDatabaseHandler databaseHandler = new UserDatabaseHandler();
        databaseHandler.getCurrentUser(this, currentUser -> {
            List<String> userEventsRegistered = currentUser.getEventsRegistered();

            if (userEventsRegistered.contains(eventReceived.getEventId())) {
                joinWaitlist.setVisibility(View.INVISIBLE);
                leaveWaitlist.setVisibility(View.VISIBLE);
            }
            else {
                joinWaitlist.setVisibility(View.VISIBLE);
                leaveWaitlist.setVisibility(View.INVISIBLE);
            }
        });
    }
}