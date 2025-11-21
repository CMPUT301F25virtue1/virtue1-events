package com.example.linko;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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
    private FirebaseFirestore db;
    private CollectionReference usersRef;
    private UserRecyclerAdapter entrantsUserRecyclerAdapter;

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
        ConstraintLayout systemContainer = findViewById(R.id.syste)
        // total entrants recycler view
        RecyclerView entrantsRecyclerView = findViewById(R.id.recycler_event_entrants);
        totalEntrantsList = new ArrayList<>();
        entrantsUserRecyclerAdapter = new UserRecyclerAdapter(totalEntrantsList, false);
        entrantsRecyclerView.setAdapter(entrantsUserRecyclerAdapter);

        LinearLayoutManager entrantsLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        entrantsRecyclerView.setLayoutManager(entrantsLayoutManager);

        Event eventReceived = (Event) getIntent().getSerializableExtra("clickedEvent");
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

                for (QueryDocumentSnapshot snapshot : value) {
                    List<String> userRegisteredEvents = (List<String>) snapshot.get("eventsRegistered");
                    // if user is not registered in this event ->>>> skip
                    if (!userRegisteredEvents.contains(eventReceived.getEventId())) {
                        continue;
                    }

                    User userToAdd = snapshot.toObject(User.class);
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
                entrantsUserRecyclerAdapter.notifyDataSetChanged();
            }
        });
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

        descriptionButton.setOnClickListener(v -> {
            eventDescription.setVisibility(View.VISIBLE);
            eventPoster.setVisibility(View.GONE);
            eventGuidelines.setVisibility(View.GONE);
            descriptionButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.teal)));
            posterButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerTeal)));
            guidelinesButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerTeal)));
        });

        posterButton.setOnClickListener(v -> {
            eventDescription.setVisibility(View.GONE);
            eventPoster.setVisibility(View.VISIBLE);
            eventGuidelines.setVisibility(View.GONE);
            descriptionButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerTeal)));
            posterButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.teal)));
            guidelinesButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerTeal)));
        });

        guidelinesButton.setOnClickListener(v -> {
            eventDescription.setVisibility(View.GONE);
            eventPoster.setVisibility(View.GONE);
            eventGuidelines.setVisibility(View.VISIBLE);
            descriptionButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerTeal)));
            posterButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerTeal)));
            guidelinesButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.teal)));
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

        // top bar listeners
        eventDetails.setOnClickListener(v -> {
            eventDetailsContainer.setVisibility(View.VISIBLE);
            entrantsContainer.setVisibility(View.GONE);

            eventDetails.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.teal)));
            totalEntrants.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerTeal)));

        });

        totalEntrants.setOnClickListener(v -> {
            eventDetailsContainer.setVisibility(View.GONE);
            entrantsContainer.setVisibility(View.VISIBLE);

            eventDetails.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerTeal)));
            totalEntrants.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.teal)));

        });
        findViewById(R.id.button_qr_code).setOnClickListener(v -> {
            String eventId = eventReceived.getEventId();
            QRCodeDialog dialog = QRCodeDialog.newInstance(eventId);
            dialog.show(getSupportFragmentManager(), "QRCodeDialog");
        });
    }
}