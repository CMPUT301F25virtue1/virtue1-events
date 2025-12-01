package com.example.linko;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.File;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * This is the class for handling the event details for the organizer logic that interacts with the UI.
 */
public class OrganizerEventDetailsActivity extends AppCompatActivity {

    private Uri imageUri;
    private List<User> totalEntrantsList;
    private List<User> invitedEntrantsList;
    private List<User> signedUpEntrantsList;
    private List<User> cancelledEntrantsList;

    private FirebaseFirestore db;
    private CollectionReference usersRef;
    private CollectionReference eventsRef;
    private CollectionReference notifsRef;

    private UserRecyclerAdapter entrantsUserRecyclerAdapter;
    private UserRecyclerAdapter invitedEntrantsUserRecyclerAdapter;
    private UserRecyclerAdapter signedUpEntrantsUserRecyclerAdapter;
    private UserRecyclerAdapter cancelledEntrantsUserRecyclerAdapter;

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

    // to keep track of which list to send notifications to in the system tab (0 = invited, 1 = signedup, 2 = cancelled)
    private int currentClicked;

    // https://stackoverflow.com/questions/51158346/removing-firestore-snapshot-listener-inside-of-livedata-returning-function
    private ListenerRegistration usersListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_organizer_event_details);
        currentClicked = 0;

        // top bar
        ImageView backButton = findViewById(R.id.button_back_button);
        Button eventDetails = findViewById(R.id.button_event);
        Button totalEntrants = findViewById(R.id.button_entrants);
        Button system = findViewById(R.id.button_system);

        // event tab ui
        ConstraintLayout eventDetailsContainer = findViewById(R.id.event_details_container);
        // event details
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
        Button editEvent = findViewById(R.id.button_edit_event);

        // entrants tab ui
        ConstraintLayout entrantsContainer = findViewById(R.id.event_entrants_container);
        TextView noEntrants = findViewById(R.id.text_no_entrants);
        Button sendNotificationAll = findViewById(R.id.button_send_notification);
        Button exportCsvButton = findViewById(R.id.button_export_csv);
        ImageView entrantLocationButton = findViewById(R.id.button_entrant_location);

        // system tab ui
        ConstraintLayout systemContainer = findViewById(R.id.system_container);
        ConstraintLayout notYetSampled = findViewById(R.id.container_not_sampled);
        Button sampleButton = findViewById(R.id.sample_button);
        Button invited = findViewById(R.id.button_invited);
        Button signedUp = findViewById(R.id.button_signed_up);
        Button cancelled = findViewById(R.id.button_cancelled);
        Button sendNotificationSystem = findViewById(R.id.button_send_notification_system);

        // total entrants recycler view
        RecyclerView entrantsRecyclerView = findViewById(R.id.recycler_event_entrants);
        totalEntrantsList = new ArrayList<>();
        entrantsUserRecyclerAdapter = new UserRecyclerAdapter(totalEntrantsList, false);
        entrantsRecyclerView.setAdapter(entrantsUserRecyclerAdapter);

        LinearLayoutManager entrantsLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        entrantsRecyclerView.setLayoutManager(entrantsLayoutManager);

        // system recycler views
        RecyclerView invitedEntrantsRecyclerView = findViewById(R.id.recycler_invited_entrants);
        RecyclerView signedUpEntrantsRecyclerView = findViewById(R.id.recycler_signed_up_entrants);
        RecyclerView cancelledEntrantsRecyclerView = findViewById(R.id.recycler_cancelled_entrants);

        invitedEntrantsList = new ArrayList<>();
        signedUpEntrantsList = new ArrayList<>();
        cancelledEntrantsList = new ArrayList<>();

        invitedEntrantsUserRecyclerAdapter = new UserRecyclerAdapter(invitedEntrantsList, true);
        signedUpEntrantsUserRecyclerAdapter = new UserRecyclerAdapter(signedUpEntrantsList, true);
        cancelledEntrantsUserRecyclerAdapter = new UserRecyclerAdapter(cancelledEntrantsList, true);

        invitedEntrantsRecyclerView.setAdapter(invitedEntrantsUserRecyclerAdapter);
        signedUpEntrantsRecyclerView.setAdapter(signedUpEntrantsUserRecyclerAdapter);
        cancelledEntrantsRecyclerView.setAdapter(cancelledEntrantsUserRecyclerAdapter);

        LinearLayoutManager invitedEntrantsLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        LinearLayoutManager signedUpEntrantsLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        LinearLayoutManager cancelledEntrantsLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        invitedEntrantsRecyclerView.setLayoutManager(invitedEntrantsLayoutManager);
        signedUpEntrantsRecyclerView.setLayoutManager(signedUpEntrantsLayoutManager);
        cancelledEntrantsRecyclerView.setLayoutManager(cancelledEntrantsLayoutManager);

        // get event
        String eventIdReceived = getIntent().getStringExtra("eventId");
        if (eventIdReceived == null) {
            Log.e("Event", "The event clicked was null.");
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();
        usersRef = db.collection("users");
        eventsRef = db.collection("events");
        notifsRef = db.collection("notifications");

        // update every event list in real time
        eventsRef.addSnapshotListener((value, error) -> {
            // if db updates while this activity is destroyed, glide will crash
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
                        updateOrganizerEventDetails();
                    }
                }

                // only have one listener at a time (so we don't have 1000 listeners if eventsRef gets updated a lot)
                if (usersListener != null) {
                    usersListener.remove();
                }

                usersListener = usersRef.addSnapshotListener((queryDocumentSnapshots, error2) -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }
                    if (error2 != null) {
                        Log.e("Firestore", error.toString());
                    }
                    if (value != null && !value.isEmpty()) {
                        totalEntrantsList.clear();
                        invitedEntrantsList.clear();
                        signedUpEntrantsList.clear();
                        cancelledEntrantsList.clear();

                        for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                            User userToAdd = snapshot.toObject(User.class);

                            if (eventReceived.getInvitedEntrants().contains(userToAdd.getUserId())) {
                                invitedEntrantsList.add(userToAdd);
                            }
                            if (eventReceived.getCancelledEntrants().contains(userToAdd.getUserId())) {
                                cancelledEntrantsList.add(userToAdd);
                            }
                            if (eventReceived.getSignedUpEntrants().contains(userToAdd.getUserId())) {
                                signedUpEntrantsList.add(userToAdd);
                            }
                            if (eventReceived.getEntrants().contains(userToAdd.getUserId())) {
                                totalEntrantsList.add(userToAdd);
                            }
                        }

                        if (eventReceived.isGeolocationRequired()) {
                            entrantLocationButton.setAlpha(1f);
                        }
                        else {
                            entrantLocationButton.setAlpha(0.5f);
                        }

                        // update entrants tab
                        if (totalEntrantsList.isEmpty()) {
                            noEntrants.setVisibility(View.VISIBLE);
                            entrantsRecyclerView.setVisibility(View.GONE);
                            sendNotificationAll.setAlpha(0.5f);
                            entrantLocationButton.setAlpha(0.5f);

                        } else {
                            noEntrants.setVisibility(View.GONE);
                            entrantsRecyclerView.setVisibility(View.VISIBLE);
                            exportCsvButton.setAlpha(1f);
                            sendNotificationAll.setAlpha(1f);
                            entrantLocationButton.setAlpha(1f);

                        }

                        // update systems tab
                        // if event sample related lists have at least one person inside
                        if (!invitedEntrantsList.isEmpty() || !signedUpEntrantsList.isEmpty() || !cancelledEntrantsList.isEmpty()) {
                            notYetSampled.setVisibility(View.GONE);
                            sampleButton.setAlpha(0.5f);
                        } else if (eventReceived.getRegistrationEnd().before(new Date())) {
                            notYetSampled.setVisibility(View.VISIBLE);
                            sampleButton.setAlpha(1f);
                        } else {
                            notYetSampled.setVisibility(View.VISIBLE);
                            sampleButton.setAlpha(0.5f);
                        }

                        Log.d("system", invitedEntrantsList.toString());

                        // if the user is on the tab with no entrants, grey out the button
                        if ((currentClicked == 0 && invitedEntrantsList.isEmpty()) || (currentClicked == 1 && signedUpEntrantsList.isEmpty()) || (currentClicked == 2 && cancelledEntrantsList.isEmpty())) {
                            sendNotificationSystem.setAlpha(0.5f);
                        } else {
                            sendNotificationSystem.setAlpha(1f);
                        }

                        if (signedUpEntrantsList.isEmpty()) {
                            exportCsvButton.setAlpha(0.5f);
                        }
                        else {
                            exportCsvButton.setAlpha(1f);
                        }

                        entrantsUserRecyclerAdapter.notifyDataSetChanged();
                        invitedEntrantsUserRecyclerAdapter.notifyDataSetChanged();
                        signedUpEntrantsUserRecyclerAdapter.notifyDataSetChanged();
                        cancelledEntrantsUserRecyclerAdapter.notifyDataSetChanged();
                    }
                });
            }
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
            finish();
        });

        editEvent.setOnClickListener(v -> {
            EditEventPosterDialog editDialog = EditEventPosterDialog.newInstance(eventReceived);
            editDialog.setOnPosterUpdatedListener(newPosterUrl -> Glide.with(OrganizerEventDetailsActivity.this).load(newPosterUrl).centerCrop().into(eventPoster));
            editDialog.show(getSupportFragmentManager(), "EditPosterDialog");
        });

        // system tab ui logic
        invited.setOnClickListener(v -> {
            invited.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.blue)));
            signedUp.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerBlue)));
            cancelled.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerBlue)));
            currentClicked = 0;
            invitedEntrantsRecyclerView.setVisibility(View.VISIBLE);
            signedUpEntrantsRecyclerView.setVisibility(View.GONE);
            cancelledEntrantsRecyclerView.setVisibility(View.GONE);
            exportCsvButton.setVisibility(View.GONE);
            if ((currentClicked == 0 && invitedEntrantsList.isEmpty()) || (currentClicked == 1 && signedUpEntrantsList.isEmpty()) || (currentClicked == 2 && cancelledEntrantsList.isEmpty())) {
                sendNotificationSystem.setAlpha(0.5f);
            }
            else {
                sendNotificationSystem.setAlpha(1f);
            }
        });

        signedUp.setOnClickListener(v -> {
            invited.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerBlue)));
            signedUp.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.blue)));
            cancelled.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerBlue)));
            currentClicked = 1;
            invitedEntrantsRecyclerView.setVisibility(View.GONE);
            signedUpEntrantsRecyclerView.setVisibility(View.VISIBLE);
            cancelledEntrantsRecyclerView.setVisibility(View.GONE);
            exportCsvButton.setVisibility(View.VISIBLE);

            if ((currentClicked == 0 && invitedEntrantsList.isEmpty()) || (currentClicked == 1 && signedUpEntrantsList.isEmpty()) || (currentClicked == 2 && cancelledEntrantsList.isEmpty())) {
                sendNotificationSystem.setAlpha(0.5f);
            }
            else {
                sendNotificationSystem.setAlpha(1f);
            }

            if (signedUpEntrantsList.isEmpty()) {
                exportCsvButton.setAlpha(0.5f);
            }
            else {
                exportCsvButton.setAlpha(1f);
            }
        });

        cancelled.setOnClickListener(v -> {
            invited.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerBlue)));
            signedUp.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerBlue)));
            cancelled.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.blue)));
            currentClicked = 2;
            invitedEntrantsRecyclerView.setVisibility(View.GONE);
            signedUpEntrantsRecyclerView.setVisibility(View.GONE);
            cancelledEntrantsRecyclerView.setVisibility(View.VISIBLE);
            exportCsvButton.setVisibility(View.GONE);

            if ((currentClicked == 0 && invitedEntrantsList.isEmpty()) || (currentClicked == 1 && signedUpEntrantsList.isEmpty()) || (currentClicked == 2 && cancelledEntrantsList.isEmpty())) {
                sendNotificationSystem.setAlpha(0.5f);
            }
            else {
                sendNotificationSystem.setAlpha(1f);
            }
        });

        // notification listeners
        sendNotificationAll.setOnClickListener(v -> {
            if (totalEntrantsList.isEmpty()) {
                Toast.makeText(this, "No entrants to send notifications to", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(OrganizerEventDetailsActivity.this, OrganizerNotificationsActivity.class);
            intent.putExtra("listToNotify", (Serializable) totalEntrantsList);
            intent.putExtra("eventId", eventIdReceived);
            startActivity(intent);
        });

        sendNotificationSystem.setOnClickListener(v -> {
            if ((currentClicked == 0 && invitedEntrantsList.isEmpty()) || (currentClicked == 1 && signedUpEntrantsList.isEmpty()) || (currentClicked == 2 && cancelledEntrantsList.isEmpty())) {
                Toast.makeText(this, "No entrants to send notifications to", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(OrganizerEventDetailsActivity.this, OrganizerNotificationsActivity.class);
            intent.putExtra("eventId", eventIdReceived);

            if (currentClicked == 0) {
                intent.putExtra("listToNotify", (Serializable) invitedEntrantsList);
            }
            else if (currentClicked == 1) {
                intent.putExtra("listToNotify", (Serializable) signedUpEntrantsList);
            }
            else if (currentClicked == 2) {
                intent.putExtra("listToNotify", (Serializable) cancelledEntrantsList);
            }
            else {
                return;
            }
            startActivity(intent);
        });

        // export csv button
        exportCsvButton.setOnClickListener(v -> {
            if (signedUpEntrantsList.isEmpty()) {
                Toast.makeText(this, "No entrants to export", Toast.LENGTH_SHORT).show();
                return;
            }
            openFileChooser();
        });

        // map
        entrantLocationButton.setOnClickListener(v -> {
            if (!eventReceived.isGeolocationRequired()) {
                Toast.makeText(this, "Your event does not have geolocation required.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (eventReceived.getEntrants().isEmpty()) {
                Toast.makeText(this, "No entrants to show", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(OrganizerEventDetailsActivity.this, EntrantsMapActivity.class);
            intent.putExtra("eventId", eventReceived.getEventId());
            startActivity(intent);
        });
        sampleButton.setOnClickListener(v -> {

            // if sampling related lists are filled with at least one entrant, that means sampling is done.
            if (!invitedEntrantsList.isEmpty() || !signedUpEntrantsList.isEmpty() || !cancelledEntrantsList.isEmpty()) {
                Toast.makeText(OrganizerEventDetailsActivity.this, "Sampling already done.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!eventReceived.getRegistrationEnd().before(new Date())) {
                Toast.makeText(OrganizerEventDetailsActivity.this, "Your event registration period has not ended.", Toast.LENGTH_SHORT).show();
                return;
            }

            SampleButtonHandler handler = new SampleButtonHandler();

            handler.sampling(eventReceived, new SampleButtonHandler.SampleCallback() {
                @Override
                public void onSuccess(int freeSpace, List<String> newInvited, List<String> invited, List<String> signedUp) {
                    eventReceived.setInvitedEntrants(invited);

                    Toast.makeText(OrganizerEventDetailsActivity.this, "Sampling complete! " + invited.size() + " entrants invited!", Toast.LENGTH_SHORT).show();

                    invitedEntrantsUserRecyclerAdapter.notifyDataSetChanged();

                    usersRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
                        List<User> invitedUsersToNotify = new ArrayList<>();
                        List<User> usersNotInvited = new ArrayList<>();
                        for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                            User user = snapshot.toObject(User.class);
                            if (eventReceived.getInvitedEntrants().contains(user.getUserId())) {
                                invitedUsersToNotify.add(user);
                            }
                            else if (eventReceived.getEntrants().contains(user.getUserId())) {
                                usersNotInvited.add(user);
                            }
                        }

                        // NOTIFICATIONS FOR THOSE INVITED
                        DocumentReference invitedDocRef = notifsRef.document();
                        String invitedNotifId = invitedDocRef.getId();
                        UserNotification invitedNotificationToSend = new UserNotification(invitedNotifId, eventReceived.getEventId(), "You have received an invitation!", "invited");
                        // add notif to db
                        invitedDocRef.set(invitedNotificationToSend).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // add notif to the users of the list received
                                for (User user : invitedUsersToNotify) {
                                    if (!user.isNotificationsEnabled()) {
                                        continue;
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
                            }
                            else {
                                Log.e("notification", "Error adding notification to database", task.getException());
                            }
                        });

                        // NOTIFICATIONS FOR THOSE NOT INVITED
                        if(usersNotInvited.isEmpty()) {
                            return;
                        }
                        DocumentReference notInvitedDocRef = notifsRef.document();
                        String notInvitedNotifId = notInvitedDocRef.getId();
                        UserNotification notInvitedNotificationToSend = new UserNotification(notInvitedNotifId, eventReceived.getEventId(), "You have not received an invitation.", "not_invited");
                        // add notif to db
                        notInvitedDocRef.set(notInvitedNotificationToSend).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // add notif to the users of the list received
                                for (User user : usersNotInvited) {
                                    if (!user.isNotificationsEnabled()) {
                                        continue;
                                    }
                                    user.getNotificationList().add(notInvitedNotifId);
                                    user.getLocalAndroidNotificationlist().add(notInvitedNotifId);
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
                            }
                            else {
                                Log.e("notification", "Error adding notification to database", task.getException());
                            }
                        });
                    });

                }

                @Override
                public void onFail(String error) {
                    Toast.makeText(OrganizerEventDetailsActivity.this, "Sampling failed: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        });

        // top bar listeners
        eventDetails.setOnClickListener(v -> {
            eventDetailsContainer.setVisibility(View.VISIBLE);
            entrantsContainer.setVisibility(View.GONE);
            systemContainer.setVisibility(View.GONE);

            eventDetails.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white)));
            totalEntrants.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.lightBlue)));
            system.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.lightBlue)));
            eventDetails.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkestBlue)));
            totalEntrants.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkestBlueNotSelected)));
            system.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkestBlueNotSelected)));
        });

        totalEntrants.setOnClickListener(v -> {
            eventDetailsContainer.setVisibility(View.GONE);
            entrantsContainer.setVisibility(View.VISIBLE);
            systemContainer.setVisibility(View.GONE);

            eventDetails.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.lightBlue)));
            totalEntrants.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white)));
            system.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.lightBlue)));
            eventDetails.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkestBlueNotSelected)));
            totalEntrants.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkestBlue)));
            system.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkestBlueNotSelected)));
        });

        system.setOnClickListener(v -> {
            eventDetailsContainer.setVisibility(View.GONE);
            entrantsContainer.setVisibility(View.GONE);
            systemContainer.setVisibility(View.VISIBLE);

            eventDetails.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.lightBlue)));
            totalEntrants.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.lightBlue)));
            system.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white)));
            eventDetails.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkestBlueNotSelected)));
            totalEntrants.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkestBlueNotSelected)));
            system.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkestBlue)));
        });

        findViewById(R.id.button_qr_code).setOnClickListener(v -> {
            String eventId = eventReceived.getEventId();
            QRCodeDialog dialog = QRCodeDialog.newInstance(eventId);
            dialog.show(getSupportFragmentManager(), "QRCodeDialog");
        });

        //Swipe to delete invited users
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getBindingAdapterPosition();

                ConfirmationDialog confirmationDialog = ConfirmationDialog.newInstance("cancelEntrant");
                confirmationDialog.setOnConfirmedListener(deleteConfirmed -> {
                    if (!deleteConfirmed) {
                        invitedEntrantsUserRecyclerAdapter.notifyDataSetChanged();
                        return;
                    }
                    User user = invitedEntrantsList.get(pos);

                    invitedEntrantsList.remove(user);
                    invitedEntrantsUserRecyclerAdapter.notifyItemRemoved(pos);

                    eventReceived.getInvitedEntrants().remove(user.getUserId());
                    eventReceived.getEntrants().remove(user.getUserId());
                    eventReceived.getCancelledEntrants().add(user.getUserId());
                    eventReceived.getEntrantLocations().remove(user.getUserId());

                    FirebaseFirestore.getInstance().collection("events").document(eventReceived.getEventId()).update("invitedEntrants", eventReceived.getInvitedEntrants(), "entrants", eventReceived.getEntrants(), "cancelledEntrants", eventReceived.getCancelledEntrants(), "entrantLocations", eventReceived.getEntrantLocations());

                    Toast.makeText(OrganizerEventDetailsActivity.this, "Removed user " + user.getFirstName() + " " + user.getLastName() + " from invited entrants.", Toast.LENGTH_SHORT).show();

                    if (!user.isNotificationsEnabled()) {
                        return;
                    }

                    DocumentReference docRef = notifsRef.document();
                    String notifId = docRef.getId();
                    UserNotification notificationToSend = new UserNotification(notifId, eventReceived.getEventId(), "Your invitation has been cancelled.", "cancelled");
                    // add cancelled notif to db
                    docRef.set(notificationToSend).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // add notif to the users of the list received

                            user.getNotificationList().add(notifId);
                            user.getLocalAndroidNotificationlist().add(notifId);
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
                        } else {
                            Log.e("notification", "Error adding notification to database", task.getException());
                        }
                    });

                    // now sample a new entrant and send the new invite
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
                                    // add notif to the users of the list received
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
                                } else {
                                    Log.e("notification", "Error adding notification to database", task.getException());
                                }
                            });
                        }

                        @Override
                        public void onFail(String error) {

                        }
                    });
                });
                confirmationDialog.show(getSupportFragmentManager(), "confirmationDialog");
            }
        };
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(invitedEntrantsRecyclerView);
    }

    // csv export

    /**
     * Builds the csv of entrants file
     * @param entrants  List of all the entrants in the event
     * @return  returns the csv in string format
     */
    private String csvBuilder(List<User> entrants) {
        StringBuilder csv = new StringBuilder();
        csv.append("First Name,Last Name,Email,Phone Number\n");

        for (User u : entrants) {
            String first = u.getFirstName() != null ? u.getFirstName() : "";
            String last = u.getLastName() != null ? u.getLastName() : "";
            String email = u.getEmail() != null ? u.getEmail() : "";
            String phone = (u.getPhone() != null && !u.getPhone().isEmpty())
                    ? u.getPhone()
                    : "N/A";

            csv.append(first).append(",");
            csv.append(last).append(",");
            csv.append(email).append(",");
            csv.append(phone).append("\n");
        }
        return csv.toString();
    }

    /**
     *  Method that contains the logic for building the initial csv file
     */
    private void openFileChooser() {
        Intent documentSaveIntent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        documentSaveIntent.setType("text/csv");
        documentSaveIntent.putExtra(Intent.EXTRA_TITLE, "entrants_" + eventReceived.getName() + ".csv");
        startActivityForResult(documentSaveIntent, 653);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 653 && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                // https://stackoverflow.com/questions/74006079/android-create-file-using-action-create-document-then-write-to-file?utm_source=chatgpt.com
                Uri csvUri = data.getData();
                String csv = csvBuilder(signedUpEntrantsList);
                try {
                    OutputStream os = getContentResolver().openOutputStream(csvUri);
                    Writer writer = new OutputStreamWriter(os);

                    writer.write(csv);
                    writer.flush();
                    writer.close();

                    Toast.makeText(this, "Exported successfully!", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to export CSV", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     *  Allows us to update the OrganizerEventDetails activity whenever we call this
     */
    private void updateOrganizerEventDetails() {
        eventName.setText(eventReceived.getName());
        Integer eventCapacityNumber = eventReceived.getEventCapacity();
        String eventCapacityString = eventCapacityNumber.toString();
        eventCapacity.setText(eventCapacityString);
        Glide.with(OrganizerEventDetailsActivity.this).load(eventReceived.getEventPosterURL()).placeholder(R.drawable.outline_photo_camera_24).centerCrop().into(eventPoster);

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
    }
}