package com.example.linko;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.location.Location;
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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.google.firebase.firestore.GeoPoint;
import java.util.HashMap;


/**
 * Our class for managing the UI interactive logic for event details.
 * Calls EventDatabaseHandler to update our event in the database with any changes
 * @see EventDatabaseHandler
 */
public class EventDetailsActivity extends AppCompatActivity {
    private Button joinWaitlist;
    private Button leaveWaitlist;
    private Button acceptInvite;
    private Button declineInvite;
    private TextView acceptedInvite;
    private TextView eventClosed;
    private FirebaseFirestore db;
    private CollectionReference notifsRef;
    private CollectionReference eventsRef;
    private Event eventReceived;
    private TextView eventName;
    private TextView eventCapacity;
    private TextView entrantCount;
    private CheckBox geolocationCheck;
    private TextView eventTime;
    private TextView registrationStart;
    private TextView registrationEnd;
    private TextView eventDescription;
    private TextView eventGuidelines;
    private ImageView eventPoster;
    private Date eventStart;
    private Date start;
    private Date end;

    // https://developer.android.com/develop/sensors-and-location/location/retrieve-current
    private FusedLocationProviderClient fusedLocationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_details);

        // ui
        ImageView backButton = findViewById(R.id.button_back_button);
        eventName = findViewById(R.id.text_event_name);
        eventCapacity = findViewById(R.id.text_event_capacity);
        entrantCount = findViewById(R.id.text_entrant_count);
        geolocationCheck = findViewById(R.id.checkBox);
        eventTime = findViewById(R.id.text_event_start_time);
        registrationStart = findViewById(R.id.text_event_registration_start);
        registrationEnd = findViewById(R.id.text_event_registration_end);
        eventDescription = findViewById(R.id.text_event_description);
        eventGuidelines = findViewById(R.id.text_event_guidelines);
        eventPoster = findViewById(R.id.image_event_poster);

        TextView descriptionButton = findViewById(R.id.click_event_description);
        TextView posterButton = findViewById(R.id.click_event_poster);
        TextView guidelinesButton = findViewById(R.id.click_event_guidelines);
        joinWaitlist = findViewById(R.id.button_join_waitlist);
        leaveWaitlist = findViewById(R.id.button_leave_waitlist);
        acceptInvite = findViewById(R.id.button_accept);
        declineInvite = findViewById(R.id.button_decline);
        acceptedInvite = findViewById(R.id.text_accepted_invitation);
        eventClosed = findViewById(R.id.text_closed_event);

        String eventIdReceived = getIntent().getStringExtra("eventId");

        if (eventIdReceived == null) {
            Log.e("Event", "The event clicked was null.");
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();
        notifsRef = db.collection("notifications");
        eventsRef = db.collection("events");
        // update every event details in real time
        eventsRef.addSnapshotListener((value, error) -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }
            if (error != null) {
                Log.e("Firestore", error.toString());
            }
            if (value != null && !value.isEmpty()) {
                Log.d("firebase", "checking documents");
                // update the event received lists so the entrants are updated in real time
                for (QueryDocumentSnapshot doc : value) {
                    if (doc.getId().equals(eventIdReceived)) {
                        eventReceived = doc.toObject(Event.class);
                        updateEventDetails();
                        checkUserRegistered();
                    }
                }
            }
        });

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
                return;
            }

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
                    eventReceived.getEntrantLocations().remove(user.getUserId());

                    new EventDatabaseHandler().update(eventReceived, new EventDatabaseHandler.EventUpdated() {
                        @Override
                        public void eventUpdate() {
                            checkUserRegistered();

                            // sample a new entrant
                            SampleButtonHandler handler = new SampleButtonHandler();

                            handler.sampling(eventReceived, new SampleButtonHandler.SampleCallback() {
                                @Override
                                public void onSuccess(int freeSpace, List<String> newInvited, List<String> invited, List<String> signedUp) {
                                    // sampling handler handles the event sampling updates itself

                                    // now just send the invitation to the person that just got sampled
                                    DocumentReference invitedDocRef = notifsRef.document();
                                    String invitedNotifId = invitedDocRef.getId();
                                    UserNotification invitedNotificationToSend = new UserNotification(invitedNotifId, eventReceived.getEventId(), "You have received an invitation!", "invited");
                                    // add notif to db
                                    invitedDocRef.set(invitedNotificationToSend).addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            // add notif to the newly invited entrants
                                            for (String user : newInvited) {
                                                new UserDatabaseHandler().fetchUserById(user, new UserDatabaseHandler.UserFetchedFromId() {
                                                    @Override
                                                    public void userFetch(User user) {
                                                        if (!user.isNotificationsEnabled()) {
                                                            return;
                                                        }
                                                        user.getNotificationList().add(invitedNotifId);
                                                        user.getLocalAndroidNotificationlist().add(invitedNotifId);
                                                        new UserDatabaseHandler().addUser(user, new UserDatabaseHandler.UserAdded() {
                                                            @Override
                                                            public void userAdd() {
                                                                Log.d("notification", "successfully added to user list in database");
                                                            }

                                                            @Override
                                                            public void userFailedToAdd(Exception e) {
                                                                Log.e("notification", "error adding notif to user list in database");
                                                            }
                                                        });
                                                    }

                                                    @Override
                                                    public void userFetchFailed(Exception e) {

                                                    }
                                                });

                                            }
                                        }
                                        else {
                                            Log.e("notification", "Error adding notification to database", task.getException());
                                        }
                                    });
                                }

                                @Override
                                public void onFail(String error) {

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
            List<String> userEventHistory = currentUser.getEventHistory();

            Log.d("eventReceived", eventReceived.getEventId());
            Log.d("eventReceived", "event received" + eventReceived.getOwnerId());
            Log.d("eventReceived", "event received" + eventReceived.getEntrants().toString());

            if (eventReceived.getEntrants().contains(currentUser.getUserId())) {
                // user leaves waitlist
                Log.d("eventReceived", "removing entrant from waitlist");

                eventReceived.getEntrants().remove(currentUser.getUserId());

                EventDatabaseHandler eventDatabaseHandler = new EventDatabaseHandler();
                eventDatabaseHandler.update(eventReceived, new EventDatabaseHandler.EventUpdated() {
                    @Override
                    public void eventUpdate() {
                        Log.d("eventReceived", eventReceived.getEntrants().toString());
                        checkUserRegistered();
                    }

                    @Override
                    public void eventUpdateFailed(Exception e) {
                        Toast.makeText(EventDetailsActivity.this, "Error updating waitlist: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
            else {
                // if event has geolocation, then get the location and store it (ask for permission if it is the user's first time joining an event with geolocation on)
                if (eventReceived.isGeolocationRequired()) {
                    // https://developer.android.com/develop/sensors-and-location/location/retrieve-current
                    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
                    if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) || (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED)) {
                        fusedLocationClient.getLastLocation()
                                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                                    @Override
                                    public void onSuccess(Location location) {
                                        if (location != null) {
                                            eventReceived.getEntrantLocations().put(currentUser.getUserId(), new GeoPoint(location.getLatitude(), location.getLongitude()));
                                            // user joins waitlist
                                            eventReceived.getEntrants().add(currentUser.getUserId());

                                            if (!userEventHistory.contains(eventReceived.getEventId())) {
                                                userEventHistory.add(eventReceived.getEventId());
                                            }

                                            EventDatabaseHandler eventDatabaseHandler = new EventDatabaseHandler();
                                            eventDatabaseHandler.update(eventReceived, new EventDatabaseHandler.EventUpdated() {
                                                @Override
                                                public void eventUpdate() {
                                                    Log.d("eventReceived", eventReceived.getEntrants().toString());
                                                    checkUserRegistered();
                                                }

                                                @Override
                                                public void eventUpdateFailed(Exception e) {
                                                    Toast.makeText(EventDetailsActivity.this, "Error updating waitlist: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                                }
                                            });
                                        }
                                    }
                                });
                    } else {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 124);
                    }
                }
                // else geolocation not enabled, just add the user to the event list with no location permission requests
                else {
                    // user joins waitlist
                    eventReceived.getEntrants().add(currentUser.getUserId());

                    if (!userEventHistory.contains(eventReceived.getEventId())) {
                        userEventHistory.add(eventReceived.getEventId());
                    }

                    EventDatabaseHandler eventDatabaseHandler = new EventDatabaseHandler();
                    eventDatabaseHandler.update(eventReceived, new EventDatabaseHandler.EventUpdated() {
                        @Override
                        public void eventUpdate() {
                            Log.d("eventReceived", eventReceived.getEntrants().toString());
                            checkUserRegistered();
                        }

                        @Override
                        public void eventUpdateFailed(Exception e) {
                            Toast.makeText(EventDetailsActivity.this, "Error updating waitlist: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    /**
     * Method to check the database to see if a user is registered for an event
     */
    public void checkUserRegistered() {
        UserDatabaseHandler databaseHandler = new UserDatabaseHandler();
        databaseHandler.getCurrentUser(this, currentUser -> {
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

    private void updateEventDetails() {
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

        eventStart = eventReceived.getEventTime();
        start = eventReceived.getRegistrationStart();
        end = eventReceived.getRegistrationEnd();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM-dd-yyyy | hh:mm a", Locale.getDefault());
        eventTime.setText(sdf.format(eventStart));
        registrationStart.setText(sdf.format(start));
        registrationEnd.setText(sdf.format(end));
        eventDescription.setText(eventReceived.getDescription());
        eventGuidelines.setText(eventReceived.getGuidelines());
    }

    @SuppressLint("MissingPermission") // suppress because fusedLocationClient will only run if the permission is granted
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 124:
                if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                    // if user granted one of the permissions get their last location
                    new UserDatabaseHandler().getCurrentUser(EventDetailsActivity.this, currentUser ->  {
                        List<String> userEventHistory = currentUser.getEventHistory();

                        fusedLocationClient.getLastLocation()
                                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                                    @Override
                                    public void onSuccess(Location location) {
                                        if (location != null) {
                                            eventReceived.getEntrantLocations().put(currentUser.getUserId(), new GeoPoint(location.getLatitude(), location.getLongitude()));
                                            // user joins waitlist
                                            eventReceived.getEntrants().add(currentUser.getUserId());

                                            if (!userEventHistory.contains(eventReceived.getEventId())) {
                                                userEventHistory.add(eventReceived.getEventId());
                                            }

                                            EventDatabaseHandler eventDatabaseHandler = new EventDatabaseHandler();
                                            eventDatabaseHandler.update(eventReceived, new EventDatabaseHandler.EventUpdated() {
                                                @Override
                                                public void eventUpdate() {
                                                    Log.d("eventReceived", eventReceived.getEntrants().toString());
                                                    checkUserRegistered();
                                                }

                                                @Override
                                                public void eventUpdateFailed(Exception e) {
                                                    Toast.makeText(EventDetailsActivity.this, "Error updating waitlist: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                                }
                                            });
                                        }
                                    }
                                });
                    });

                }  else {
                    // user didn't grant a permission, do nothing
                    Toast.makeText(EventDetailsActivity.this, "Event has geolocation required. Location permission must be granted.", Toast.LENGTH_LONG);
                }

        }
    }
}