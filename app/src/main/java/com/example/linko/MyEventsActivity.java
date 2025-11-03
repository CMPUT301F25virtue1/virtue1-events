package com.example.linko;

import static com.example.linko.NavigationBarHandler.navigationListener;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MyEventsActivity extends AppCompatActivity {
    private List<Event> organizedEventsList;
    private EventRecyclerAdapter eventRecyclerAdapter;
    private FirebaseFirestore db;
    private CollectionReference eventsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_events);

        Button registeredEvents = findViewById(R.id.button_registered);
        Button organizedEvents = findViewById(R.id.button_organized);
        Button organizeAnEvent = findViewById(R.id.button_organize_event);
        TextView noEventsRegistered = findViewById(R.id.text_no_event_registered);
        TextView noEventsOrganized = findViewById(R.id.text_no_event_organized);
        RecyclerView organizedRecyclerView = findViewById(R.id.recycler_organized_events);

        // create event array
        organizedEventsList = new ArrayList<>();
        eventRecyclerAdapter = new EventRecyclerAdapter(organizedEventsList);
        organizedRecyclerView.setAdapter(eventRecyclerAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        organizedRecyclerView.setLayoutManager(layoutManager);

        // get organized events list from database
        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("events");
        eventsRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", error.toString());
            }
            if (value != null && !value.isEmpty()) {
                String currentUser = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                Log.d("firebase", "checking documents");
                organizedEventsList.clear();
                for (QueryDocumentSnapshot snapshot : value) {
                    String ownerId = snapshot.getString("ownerId");
                    Log.d("firebase", "checking owner id");
                    if (!ownerId.equals(currentUser)) {
                        continue;
                    }
                    Log.d("firebase", "passed check");

                    String eventName = snapshot.getString("name");
                    Integer eventCapacityInt = snapshot.get("eventCapacity", Integer.class);
                    Integer entrantLimit = snapshot.get("entrantLimit", Integer.class);
                    boolean geolocationRequirement = snapshot.getBoolean("geolocationRequired");
                    String eventLocation = snapshot.getString("eventLocation");
                    String eventTime = snapshot.getString("eventTime");
                    Date registrationStart = snapshot.get("registrationStart", Date.class);
                    Date registrationEnd = snapshot.get("registrationEnd", Date.class);
                    String eventDescription = snapshot.getString("description");
                    String eventPhotoURL = snapshot.getString("eventPosterURL");

                    organizedEventsList.add(new Event(ownerId,eventName,eventCapacityInt,entrantLimit,geolocationRequirement,eventLocation, eventTime, registrationStart,registrationEnd, eventDescription,eventPhotoURL));
                }
                eventRecyclerAdapter.notifyDataSetChanged();
            }
        });

        // placeholder
        registeredEvents.setOnClickListener( v -> {
            noEventsRegistered.setVisibility(View.VISIBLE);
            organizeAnEvent.setVisibility(View.GONE);
            noEventsOrganized.setVisibility(View.GONE);
            organizedRecyclerView.setVisibility(View.GONE);
            registeredEvents.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.teal)));
            organizedEvents.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerTeal)));
        });

        organizedEvents.setOnClickListener( v -> {
            if (organizedEventsList.isEmpty()) {
                noEventsRegistered.setVisibility(View.GONE);
                noEventsOrganized.setVisibility(View.VISIBLE);
                organizedRecyclerView.setVisibility(View.GONE);
            }
            else {
                noEventsRegistered.setVisibility(View.GONE);
                noEventsOrganized.setVisibility(View.GONE);
                organizedRecyclerView.setVisibility(View.VISIBLE);
            }
            organizeAnEvent.setVisibility(View.VISIBLE);
            organizedEvents.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.teal)));
            registeredEvents.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerTeal)));
        });

        organizeAnEvent.setOnClickListener(v -> {
            startActivity(new Intent(MyEventsActivity.this, AddEventActivity.class));
            finish();
        });

        eventRecyclerAdapter.setOnItemClickListener(position -> {
            Event clickedEvent = organizedEventsList.get(position);
            Intent intent = new Intent(this, OrganizerEventDetailsActivity.class);
            intent.putExtra("clickedEvent", clickedEvent);
            startActivity(intent);
            finish();
        });

        navigationListener(this);

    }
}