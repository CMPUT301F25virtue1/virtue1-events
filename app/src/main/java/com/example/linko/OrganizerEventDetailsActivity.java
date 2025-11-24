package com.example.linko;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ListUpdateCallback;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
    private UserRecyclerAdapter entrantsUserRecyclerAdapter;
    private UserRecyclerAdapter invitedEntrantsUserRecyclerAdapter;
    private UserRecyclerAdapter signedUpEntrantsUserRecyclerAdapter;
    private UserRecyclerAdapter cancelledEntrantsUserRecyclerAdapter;
    private Event eventReceived;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_organizer_event_details);

        // top bar
        ImageView backButton = findViewById(R.id.button_back_button);
        Button eventDetails = findViewById(R.id.button_event);
        Button totalEntrants = findViewById(R.id.button_entrants);
        Button system = findViewById(R.id.button_system);

        // event tab ui
        ConstraintLayout eventDetailsContainer = findViewById(R.id.event_details_container);
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
        Button editEvent = findViewById(R.id.button_edit_event);

        // entrants tab ui
        ConstraintLayout entrantsContainer = findViewById(R.id.event_entrants_container);
        TextView noEntrants = findViewById(R.id.text_no_entrants);

        // system tab ui
        ConstraintLayout systemContainer = findViewById(R.id.system_container);
        ConstraintLayout notYetSampled = findViewById(R.id.container_not_sampled);
        Button invited = findViewById(R.id.button_invited);
        Button signedUp = findViewById(R.id.button_signed_up);
        Button cancelled = findViewById(R.id.button_cancelled);

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

        eventReceived = (Event) getIntent().getSerializableExtra("clickedEvent");
        if (eventReceived == null) {
            Log.e("Event", "The event clicked was null.");
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();
        usersRef = db.collection("users");

        usersRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", error.toString());
            }
            if (value != null && !value.isEmpty()) {
                Log.d("firebase", "checking documents");
                totalEntrantsList.clear();
                invitedEntrantsList.clear();
                signedUpEntrantsList.clear();
                cancelledEntrantsList.clear();
                for (QueryDocumentSnapshot snapshot : value) {
                    List<String> userRegisteredEvents = (List<String>) snapshot.get("eventsRegistered");
                    User userToAdd = snapshot.toObject(User.class);

                    // if user is not registered in this event ->>>> skip
                    if (userRegisteredEvents == null || !userRegisteredEvents.contains(eventReceived.getEventId())) {
                        Log.d("system", "SKIPPED" + userToAdd.getUserId() + eventReceived.getInvitedEntrants().toString());

                        continue;
                    }
                    Log.d("system", eventReceived.getInvitedEntrants().toString());
                    Log.d("system", userToAdd.getUserId());

                    if (eventReceived.getInvitedEntrants().contains(userToAdd.getUserId())) {
                        invitedEntrantsList.add(userToAdd);
                    }
                    if (eventReceived.getCancelledEntrants().contains(userToAdd.getUserId())) {
                        cancelledEntrantsList.add(userToAdd);
                    }
                    if (eventReceived.getSignedUpEntrants().contains(userToAdd.getUserId())) {
                        signedUpEntrantsList.add(userToAdd);
                    }
                    totalEntrantsList.add(userToAdd);
                }
                // update the entrants tab
                if (totalEntrantsList.isEmpty()) {
                    noEntrants.setVisibility(View.VISIBLE);
                    entrantsRecyclerView.setVisibility(View.GONE);
                } else {
                    noEntrants.setVisibility(View.GONE);
                    entrantsRecyclerView.setVisibility(View.VISIBLE);
                }

                // update systems tab
                if (!invitedEntrantsList.isEmpty() || !signedUpEntrantsList.isEmpty() || !cancelledEntrantsList.isEmpty()) {
                    notYetSampled.setVisibility(View.GONE);
                }
                else {
                    notYetSampled.setVisibility(View.VISIBLE);
                }
                Log.d("system", invitedEntrantsList.toString());
                entrantsUserRecyclerAdapter.notifyDataSetChanged();
                invitedEntrantsUserRecyclerAdapter.notifyDataSetChanged();
                signedUpEntrantsUserRecyclerAdapter.notifyDataSetChanged();
                cancelledEntrantsUserRecyclerAdapter.notifyDataSetChanged();

            }
        });

        eventName.setText(eventReceived.getName());
        Integer eventCapacityNumber = eventReceived.getEventCapacity();
        String eventCapacityString = eventCapacityNumber.toString();
        eventCapacity.setText(eventCapacityString);
        Glide.with(OrganizerEventDetailsActivity.this).load(eventReceived.getEventPosterURL()).placeholder(R.drawable.outline_photo_camera_24).centerCrop().into(eventPoster);

        if (eventReceived.getEntrantLimit() != null) {
            entrantCount.setText(eventReceived.getEntrantCount() + "/" + eventReceived.getEntrantLimit());
        } else {
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
            startActivity(new Intent(OrganizerEventDetailsActivity.this, MyEventsActivity.class));
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

            invitedEntrantsRecyclerView.setVisibility(View.VISIBLE);
            signedUpEntrantsRecyclerView.setVisibility(View.GONE);
            cancelledEntrantsRecyclerView.setVisibility(View.GONE);
        });

        signedUp.setOnClickListener(v -> {
            invited.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerBlue)));
            signedUp.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.blue)));
            cancelled.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerBlue)));

            invitedEntrantsRecyclerView.setVisibility(View.GONE);
            signedUpEntrantsRecyclerView.setVisibility(View.VISIBLE);
            cancelledEntrantsRecyclerView.setVisibility(View.GONE);
        });

        cancelled.setOnClickListener(v -> {
            invited.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerBlue)));
            signedUp.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerBlue)));
            cancelled.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.blue)));

            invitedEntrantsRecyclerView.setVisibility(View.GONE);
            signedUpEntrantsRecyclerView.setVisibility(View.GONE);
            cancelledEntrantsRecyclerView.setVisibility(View.VISIBLE);
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
    }
}