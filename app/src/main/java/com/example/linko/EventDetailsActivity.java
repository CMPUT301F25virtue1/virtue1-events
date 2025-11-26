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
    private Button acceptInvite;
    private Button declineInvite;
    private TextView entrantCount;
    private TextView acceptedInvite;
    private TextView eventClosed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_details);

        // ui
        ImageView backButton = findViewById(R.id.button_back_button);
        TextView eventName = findViewById(R.id.text_event_name);
        TextView eventCapacity = findViewById(R.id.text_event_capacity);
        entrantCount = findViewById(R.id.text_entrant_count);
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
        joinWaitlist = findViewById(R.id.button_join_waitlist);
        leaveWaitlist = findViewById(R.id.button_leave_waitlist);
        acceptInvite = findViewById(R.id.button_accept);
        declineInvite = findViewById(R.id.button_decline);
        acceptedInvite = findViewById(R.id.text_accepted_invitation);
        eventClosed = findViewById(R.id.text_closed_event);

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

        if (eventReceived.getEntrantLimit() != null) {
            entrantCount.setText(eventReceived.getEntrantCount() + "/" + eventReceived.getEntrantLimit());
        }
        else {
            entrantCount.setText(eventReceived.getEntrantCount());
        }


        geolocationCheck.setChecked(eventReceived.isGeolocationRequired());

        Date eventStart = eventReceived.getEventTime();
        Date start = eventReceived.getRegistrationStart();
        Date end = eventReceived.getRegistrationEnd();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM-dd-yyyy | hh:mm a", Locale.getDefault());
        eventTime.setText(sdf.format(eventStart));
        registrationStart.setText(sdf.format(start));
        registrationEnd.setText(sdf.format(end));
        eventDescription.setText(eventReceived.getDescription());
        eventGuidelines.setText(eventReceived.getGuidelines());
        checkUserRegistered();
        joinWaitlist.setOnClickListener(v -> {
            Date now = new Date();

            if (now.before(start)) {
                Toast.makeText(EventDetailsActivity.this, "Can't join waitlist — registration hasn't started yet.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (now.after(end)) {
                Toast.makeText(EventDetailsActivity.this, "This event's registration period has ended.", Toast.LENGTH_SHORT).show();
                return;
            }

            Integer eventEntrantLimit = eventReceived.getEntrantLimit();
            if (eventEntrantLimit != null) {
                if (eventReceived.getEntrants().size() >= eventEntrantLimit) {
                    Toast.makeText(EventDetailsActivity.this, "This event's entrant limit has been reached.", Toast.LENGTH_LONG).show();
                    return;
                }
            }

            joinWaitlist.setVisibility(View.INVISIBLE);
            leaveWaitlist.setVisibility(View.VISIBLE);
            changeUserWaitlist();
        });

        leaveWaitlist.setOnClickListener(v -> {
            Date now = new Date();

            if (now.after(eventStart)) {
                Toast.makeText(EventDetailsActivity.this, "This event has started already.", Toast.LENGTH_SHORT).show();
                return;
            }

            // if registration has ended, but event start time has not, still let the user leave waitlist
            if (eventReceived.getRegistrationEnd().before(now) && eventReceived.getEventTime().after(now)) {
                changeUserWaitlist();
                startActivity(new Intent(EventDetailsActivity.this, MyEventsActivity.class));
                finish();
                Toast.makeText(EventDetailsActivity.this, "You have left the waitlist after the registration deadline. You cannot rejoin.", Toast.LENGTH_LONG).show();
            }

            joinWaitlist.setVisibility(View.VISIBLE);
            leaveWaitlist.setVisibility(View.INVISIBLE);
            changeUserWaitlist();
        });

        acceptInvite.setOnClickListener(v -> {
            new UserDatabaseHandler().getCurrentUser(EventDetailsActivity.this, new UserDatabaseHandler.UserFetched() {
                @Override
                public void userLoaded(User user) {
                    // shift the user to the signed up entrant list
                    eventReceived.getInvitedEntrants().remove(user.getUserId());
                    eventReceived.getSignedUpEntrants().add(user.getUserId());
                    new EventDatabaseHandler().update(eventReceived, new EventDatabaseHandler.EventUpdated() {
                        @Override
                        public void eventUpdate() {
                            Toast.makeText(EventDetailsActivity.this, "Successfully accepted the invitation!", Toast.LENGTH_LONG).show();
                            checkUserRegistered();
                        }

                        @Override
                        public void eventUpdateFailed(Exception e) {

                        }
                    });
                }
            });
        });

        declineInvite.setOnClickListener(v -> {
            new UserDatabaseHandler().getCurrentUser(EventDetailsActivity.this, new UserDatabaseHandler.UserFetched() {
                @Override
                public void userLoaded(User user) {
                    // remove any mention of the user in the event lists
                    eventReceived.getEntrants().remove(user.getUserId());
                    eventReceived.getInvitedEntrants().remove(user.getUserId());
                    new EventDatabaseHandler().update(eventReceived, new EventDatabaseHandler.EventUpdated() {
                        @Override
                        public void eventUpdate() {
                            checkUserRegistered();
                            // now remove the event from the users registered events list
                            user.getEventsRegistered().remove(eventReceived.getEventId());
                            new UserDatabaseHandler().addUser(user, new UserDatabaseHandler.UserAdded() {
                                @Override
                                public void userAdd() {
                                    Toast.makeText(EventDetailsActivity.this, "Successfully declined the invitation!", Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void userFailedToAdd(Exception e) {

                                }
                            });

                            SampleButtonHandler handler = new SampleButtonHandler();

                            handler.sampling(eventReceived, new SampleButtonHandler.SampleCallback() {
                                @Override
                                public void onSuccess(int freeSpace, List<String> invited, List<String> signedUp) {
                                    eventReceived.setInvitedEntrants(invited);
                                    eventReceived.setSignedUpEntrants(signedUp);

                                    new EventDatabaseHandler().update(eventReceived, new EventDatabaseHandler.EventUpdated() {
                                        @Override
                                        public void eventUpdate() {

                                        }

                                        @Override
                                        public void eventUpdateFailed(Exception e) {

                                        }
                                    });

                                }

                                @Override
                                public void onFail(String error) {
                                    Toast.makeText(EventDetailsActivity.this, "Sampling failed: " + error, Toast.LENGTH_SHORT).show();
                                }
                            });

                        }

                        @Override
                        public void eventUpdateFailed(Exception e) {

                        }
                    });

                }
            });
        });

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
            String activityFrom = getIntent().getStringExtra("activity");
            if (activityFrom.equals("exploreEvents")) {
                startActivity(new Intent(EventDetailsActivity.this, ExploreEventsActivity.class));
            }
            else if (activityFrom.equals("myEvents")) {
                startActivity(new Intent(EventDetailsActivity.this, MyEventsActivity.class));
            }
            else if (activityFrom.equals("notifications")) {
                startActivity(new Intent(EventDetailsActivity.this, NotificationsActivity.class));
            }
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
                public void eventUpdateFailed(Exception e) {
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
            if (eventReceived.getEntrantLimit() != null) {
                entrantCount.setText(eventReceived.getEntrantCount() + "/" + eventReceived.getEntrantLimit());
            }
            else {
                entrantCount.setText(eventReceived.getEntrantCount());
            }
        });
    }

    /**
     * Method to check the database to see if a user is registered for an event
     */
    public void checkUserRegistered() {
        UserDatabaseHandler databaseHandler = new UserDatabaseHandler();
        databaseHandler.getCurrentUser(this, currentUser -> {
            List<String> userEventsRegistered = currentUser.getEventsRegistered();
            Date now = new Date();
            // if user is in the waitlist already, allow them to leave
            if (eventReceived.getEntrants().contains(currentUser.getUserId())) {
                joinWaitlist.setVisibility(View.INVISIBLE);
                leaveWaitlist.setVisibility(View.VISIBLE);
                acceptInvite.setVisibility(View.INVISIBLE);
                declineInvite.setVisibility(View.INVISIBLE);
                acceptedInvite.setVisibility(View.INVISIBLE);
                eventClosed.setVisibility(View.INVISIBLE);
            }
            // if user ISNT in the waitlist, and registration has ended (includes case where user declined)
            else if (!eventReceived.getEntrants().contains(currentUser.getUserId()) && eventReceived.getRegistrationEnd().before(now)) {
                joinWaitlist.setVisibility(View.INVISIBLE);
                leaveWaitlist.setVisibility(View.INVISIBLE);
                acceptInvite.setVisibility(View.INVISIBLE);
                declineInvite.setVisibility(View.INVISIBLE);
                acceptedInvite.setVisibility(View.INVISIBLE);
                eventClosed.setVisibility(View.VISIBLE);
            }
            // user isnt in the waitlist but can still register
            else {
                joinWaitlist.setVisibility(View.VISIBLE);
                leaveWaitlist.setVisibility(View.INVISIBLE);
                acceptInvite.setVisibility(View.INVISIBLE);
                declineInvite.setVisibility(View.INVISIBLE);
                acceptedInvite.setVisibility(View.INVISIBLE);
                eventClosed.setVisibility(View.INVISIBLE);
            }

            // checks if they have been invited or signed up already
            if (eventReceived.getInvitedEntrants().contains(currentUser.getUserId())) {
                joinWaitlist.setVisibility(View.INVISIBLE);
                leaveWaitlist.setVisibility(View.INVISIBLE);
                acceptInvite.setVisibility(View.VISIBLE);
                declineInvite.setVisibility(View.VISIBLE);
                acceptedInvite.setVisibility(View.INVISIBLE);
                eventClosed.setVisibility(View.INVISIBLE);
            }
            else if (eventReceived.getSignedUpEntrants().contains(currentUser.getUserId())) {
                joinWaitlist.setVisibility(View.INVISIBLE);
                leaveWaitlist.setVisibility(View.INVISIBLE);
                acceptInvite.setVisibility(View.INVISIBLE);
                declineInvite.setVisibility(View.INVISIBLE);
                acceptedInvite.setVisibility(View.VISIBLE);
                eventClosed.setVisibility(View.INVISIBLE);
            }
        });
    }
}