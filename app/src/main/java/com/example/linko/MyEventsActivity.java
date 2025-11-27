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

/**
 * This is the class for handling the my events page logic that interacts with the UI.
 */
public class MyEventsActivity extends AppCompatActivity {
    private List<Event> organizedEventsList;
    private List<Event> registeredEventsList;

    private EventRecyclerAdapter organizedEventRecyclerAdapter;
    private EventRecyclerAdapter registeredEventRecyclerAdapter;

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
        RecyclerView registeredRecyclerView = findViewById(R.id.recycler_registered_events);

        // create event array
        organizedEventsList = new ArrayList<>();
        organizedEventRecyclerAdapter = new EventRecyclerAdapter(organizedEventsList, false);
        registeredEventsList = new ArrayList<>();
        registeredEventRecyclerAdapter = new EventRecyclerAdapter(registeredEventsList, false);

        organizedRecyclerView.setAdapter(organizedEventRecyclerAdapter);
        registeredRecyclerView.setAdapter(registeredEventRecyclerAdapter);

        LinearLayoutManager organizedLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        LinearLayoutManager registeredLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        registeredRecyclerView.setLayoutManager(registeredLayoutManager);
        organizedRecyclerView.setLayoutManager(organizedLayoutManager);

        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("events");

        // get registered events list from database
        eventsRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", error.toString());
            }
            if (value != null && !value.isEmpty()) {
                String currentUser = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                Log.d("firebase", "checking documents");
                registeredEventsList.clear();
                for (QueryDocumentSnapshot snapshot : value) {

                    List<String> entrants = (List<String>) snapshot.get("entrants");
                    Date eventStartTime = snapshot.get("eventTime", Date.class);
                    // only show the event in registered if it has not started yet
                    if (eventStartTime.before(new Date())) {
                        continue;
                    }

                    if (!entrants.contains(currentUser)) {
                        Log.d("registered", "user is not registered in the event");
                        continue;
                    }

                    Log.d("registered", "user is registered in the event");
                    Event eventToAdd = snapshot.toObject(Event.class);

                    registeredEventsList.add(eventToAdd);
                }
                // update the registered tab
                if (registeredEventsList.isEmpty()) {
                    noEventsRegistered.setVisibility(View.VISIBLE);
                    noEventsOrganized.setVisibility(View.GONE);
                    registeredRecyclerView.setVisibility(View.GONE);
                }
                else {
                    noEventsRegistered.setVisibility(View.GONE);
                    noEventsOrganized.setVisibility(View.GONE);
                    registeredRecyclerView.setVisibility(View.VISIBLE);
                }
                registeredEventRecyclerAdapter.notifyDataSetChanged();
            }
        });

        // get organized events list from database
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
                    Log.d("firebase", "checking owner id" + currentUser + ownerId);
                    if (!currentUser.equals(ownerId)) {
                        continue;
                    }
                    Log.d("firebase", "passed check");
                    Event eventToAdd = snapshot.toObject(Event.class);
                    organizedEventsList.add(eventToAdd);
                }
                organizedEventRecyclerAdapter.notifyDataSetChanged();
            }
        });


        registeredEvents.setOnClickListener( v -> {
            if (registeredEventsList.isEmpty()) {
                noEventsRegistered.setVisibility(View.VISIBLE);
                noEventsOrganized.setVisibility(View.GONE);
                registeredRecyclerView.setVisibility(View.GONE);
            }
            else {
                noEventsRegistered.setVisibility(View.GONE);
                noEventsOrganized.setVisibility(View.GONE);
                registeredRecyclerView.setVisibility(View.VISIBLE);
            }
            organizeAnEvent.setVisibility(View.GONE);
            organizedRecyclerView.setVisibility(View.GONE);
            registeredEvents.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white)));
            organizedEvents.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.lightBlue)));
            registeredEvents.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkestBlue)));
            organizedEvents.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkestBlueNotSelected)));
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
            registeredRecyclerView.setVisibility(View.GONE);
            registeredEvents.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.lightBlue)));
            organizedEvents.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white)));
            registeredEvents.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkestBlueNotSelected)));
            organizedEvents.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkestBlue)));
        });

        organizeAnEvent.setOnClickListener(v -> {
            startActivity(new Intent(MyEventsActivity.this, AddEventActivity.class));
            finish();
        });

        organizedEventRecyclerAdapter.setOnItemClickListener(position -> {
            Event clickedEvent = organizedEventsList.get(position);
            Intent intent = new Intent(this, OrganizerEventDetailsActivity.class);
            intent.putExtra("eventId", clickedEvent.getEventId());
            startActivity(intent);
            finish();
        });

        registeredEventRecyclerAdapter.setOnItemClickListener(position -> {
            Event clickedEvent = registeredEventsList.get(position);
            Intent intent = new Intent(this, EventDetailsActivity.class);
            intent.putExtra("eventId", clickedEvent.getEventId());
            intent.putExtra("activity", "myEvents");
            startActivity(intent);
            finish();
        });
        navigationListener(this);

    }
}