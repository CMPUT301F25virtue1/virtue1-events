package com.example.linko;

import static com.example.linko.NavigationBarHandler.navigationListener;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This is the class for handling the explore events logic that interacts with the UI.
 */
public class ExploreEventsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private CollectionReference eventsRef;

    private List<Event> availableEventsList;
    private EventRecyclerAdapter eventRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_explore_events);
        navigationListener(this);

        TextView noAvailableEvents = findViewById(R.id.text_no_event_available);
        RecyclerView availableRecyclerView = findViewById(R.id.recycler_available_events);
        // layout
        availableEventsList = new ArrayList<>();
        eventRecyclerAdapter = new EventRecyclerAdapter(availableEventsList);
        availableRecyclerView.setAdapter(eventRecyclerAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        availableRecyclerView.setLayoutManager(layoutManager);


        // get events from db
        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("events");
        eventsRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", error.toString());
            }
            if (value != null && !value.isEmpty()) {
                String currentUser = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                Log.d("firebase", "checking documents");
                availableEventsList.clear();
                for (QueryDocumentSnapshot snapshot : value) {
                    Date registrationStart = snapshot.get("registrationStart", Date.class);
                    Date registrationEnd = snapshot.get("registrationEnd", Date.class);

                    // if registration hasnt started yet, skip
                    if (registrationStart != null && registrationStart.after(new Date())) {
                        continue;
                    }

                    // if registration has ended, skip
                    if (registrationEnd != null && registrationEnd.before(new Date())) {
                        continue;
                    }
                    Event eventToAdd = snapshot.toObject(Event.class);
                    availableEventsList.add(eventToAdd);
                }
                eventRecyclerAdapter.notifyDataSetChanged();
                if (availableEventsList.isEmpty()) {
                    noAvailableEvents.setVisibility(View.VISIBLE);
                    availableRecyclerView.setVisibility(View.GONE);
                }
                else {
                    noAvailableEvents.setVisibility(View.GONE);
                    availableRecyclerView.setVisibility(View.VISIBLE);
                }
            }
        });

        // go to the event details on click of each recycler view  item
        eventRecyclerAdapter.setOnItemClickListener(position -> {
            Event clickedEvent = availableEventsList.get(position);
            String userId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            if (clickedEvent.getOwnerId().equals(userId)) {
                Toast.makeText(ExploreEventsActivity.this, "This is your event. Go to the organized events tab to view details", Toast.LENGTH_LONG).show();
                return;
            }
            Intent intent = new Intent(this, EventDetailsActivity.class);
            intent.putExtra("clickedEvent", clickedEvent);
            intent.putExtra("activity", "exploreEvents");
            startActivity(intent);
            finish();
        });


    }
}