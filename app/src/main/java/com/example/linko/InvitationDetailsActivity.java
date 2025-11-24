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
 * Activity for viewing event invitation and accepting/declining it
 * Similar to EventDetailsActivity but with Accept/Decline buttons
 */
public class InvitationDetailsActivity extends AppCompatActivity {
    private Event eventReceived;
    private Button acceptButton;
    private Button declineButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_invitation_details);

        // UI elements
        ImageView backButton = findViewById(R.id.button_back_button);
        TextView eventName = findViewById(R.id.text_event_name);
        TextView eventCapacity = findViewById(R.id.text_event_capacity);
        TextView entrantCount = findViewById(R.id.text_entrant_count);
        CheckBox geolocationCheck = findViewById(R.id.checkBox);
        TextView eventTime = findViewById(R.id.text_event_start_time);
        TextView registrationStart = findViewById(R.id.text_event_registration_start);
        TextView registrationEnd = findViewById(R.id.text_event_registration_end);
        TextView eventDescription = findViewById(R.id.text_event_description);
        TextView eventGuidelines = findViewById(R.id.text_event_guidelines);
        TextView descriptionButton = findViewById(R.id.click_event_description);
        TextView posterButton = findViewById(R.id.click_event_poster);
        TextView guidelinesButton = findViewById(R.id.click_event_guidelines);
        ImageView eventPoster = findViewById(R.id.image_event_poster);
        acceptButton = findViewById(R.id.button_accept);
        declineButton = findViewById(R.id.button_decline);

        // Get event from intent
        eventReceived = (Event) getIntent().getSerializableExtra("event");
        if (eventReceived == null) {
            Log.e("Event", "The event was null.");
            finish();
            return;
        }

        // Populate UI with event data
        eventName.setText(eventReceived.getName());
        eventCapacity.setText(String.valueOf(eventReceived.getEventCapacity()));
        
        if (eventReceived.getEntrantLimit() != null) {
            entrantCount.setText(eventReceived.getEntrantCount() + "/" + eventReceived.getEntrantLimit());
        } else {
            entrantCount.setText(eventReceived.getEntrantCount());
        }

        geolocationCheck.setChecked(eventReceived.isGeolocationRequired());

        SimpleDateFormat sdf = new SimpleDateFormat("MMM-dd-yyyy | hh:mm a", Locale.getDefault());
        eventTime.setText(sdf.format(eventReceived.getEventTime()));
        registrationStart.setText(sdf.format(eventReceived.getRegistrationStart()));
        registrationEnd.setText(sdf.format(eventReceived.getRegistrationEnd()));
        eventDescription.setText(eventReceived.getDescription());
        eventGuidelines.setText(eventReceived.getGuidelines());
        
        Glide.with(this)
            .load(eventReceived.getEventPosterURL())
            .placeholder(R.drawable.outline_photo_camera_24)
            .centerCrop()
            .into(eventPoster);

        // Accept button click
        acceptButton.setOnClickListener(v -> acceptInvitation());

        // Decline button click
        declineButton.setOnClickListener(v -> declineInvitation());

        // Tab switching for Description/Poster/Guidelines
        descriptionButton.setOnClickListener(v -> {
            eventDescription.setVisibility(View.VISIBLE);
            eventPoster.setVisibility(View.GONE);
            eventGuidelines.setVisibility(View.GONE);
            descriptionButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.blue)));
            posterButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerBlue)));
            guidelinesButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerBlue)));
        });

        posterButton.setOnClickListener(v -> {
            eventDescription.setVisibility(View.GONE);
            eventPoster.setVisibility(View.VISIBLE);
            eventGuidelines.setVisibility(View.GONE);
            descriptionButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerBlue)));
            posterButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.blue)));
            guidelinesButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerBlue)));
        });

        guidelinesButton.setOnClickListener(v -> {
            eventDescription.setVisibility(View.GONE);
            eventPoster.setVisibility(View.GONE);
            eventGuidelines.setVisibility(View.VISIBLE);
            descriptionButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerBlue)));
            posterButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerBlue)));
            guidelinesButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.blue)));
        });

        backButton.setOnClickListener(v -> {
            finish();
        });
    }

    /**
     * Accept the invitation - move user from invited to signed up
     */
    private void acceptInvitation() {
        UserDatabaseHandler userHandler = new UserDatabaseHandler();
        userHandler.getCurrentUser(this, currentUser -> {
            String userId = currentUser.getUserId();
            
            // Move from invited to signed up
            List<String> invitedList = eventReceived.getInvitedEntrants();
            List<String> signedUpList = eventReceived.getSignedUpEntrants();
            
            if (invitedList.contains(userId)) {
                invitedList.remove(userId);
                signedUpList.add(userId);
                
                // Update event in Firebase
                EventDatabaseHandler eventHandler = new EventDatabaseHandler();
                eventHandler.update(eventReceived, new EventDatabaseHandler.EventUpdated() {
                    @Override
                    public void eventUpdate() {
                        Toast.makeText(InvitationDetailsActivity.this, 
                            "Invitation accepted! You're registered for " + eventReceived.getName(), 
                            Toast.LENGTH_LONG).show();
                        finish();
                    }

                    @Override
                    public void eventUpdateFailed(Exception e) {
                        Toast.makeText(InvitationDetailsActivity.this, 
                            "Error accepting invitation: " + e.getMessage(), 
                            Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    /**
     * Decline the invitation - remove from invited list and reopen spot
     */
    private void declineInvitation() {
        UserDatabaseHandler userHandler = new UserDatabaseHandler();
        userHandler.getCurrentUser(this, currentUser -> {
            String userId = currentUser.getUserId();
            
            // Remove from invited list
            List<String> invitedList = eventReceived.getInvitedEntrants();
            List<String> entrantsList = eventReceived.getEntrants();
            
            if (invitedList.contains(userId)) {
                invitedList.remove(userId);
                // Remove from waiting list too
                entrantsList.remove(userId);
                
                // Update event in Firebase
                EventDatabaseHandler eventHandler = new EventDatabaseHandler();
                eventHandler.update(eventReceived, new EventDatabaseHandler.EventUpdated() {
                    @Override
                    public void eventUpdate() {
                        Toast.makeText(InvitationDetailsActivity.this, 
                            "Invitation declined. Spot reopened for another entrant.", 
                            Toast.LENGTH_LONG).show();
                        finish();
                    }

                    @Override
                    public void eventUpdateFailed(Exception e) {
                        Toast.makeText(InvitationDetailsActivity.this, 
                            "Error declining invitation: " + e.getMessage(), 
                            Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}
