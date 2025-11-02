package com.example.linko;

import static com.example.linko.NavigationBarHandler.navigationListener;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

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

public class ExploreEventsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private CollectionReference eventsRef;

    private List<Event> availableEventsList;
    private RecyclerView.Adapter eventRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                    Date registrationEnd = snapshot.get("registrationEnd", Date.class);
                    // event not active anymore
                    if (registrationEnd != null && !registrationEnd.after(new Date())) {
                        continue;
                    }
                    String ownerId = snapshot.getString("ownerID");
                    String eventName = snapshot.getString("name");
                    Integer eventCapacityInt = snapshot.get("eventCapacity", Integer.class);
                    Integer entrantLimit = snapshot.get("entrantLimit", Integer.class);
                    boolean geolocationRequirement = snapshot.getBoolean("geolocationRequired");
                    String eventLocation = snapshot.getString("eventLocation");
                    String eventTime = snapshot.getString("eventTime");
                    Date registrationStart = snapshot.get("registrationStart", Date.class);
                    String eventDescription = snapshot.getString("description");
                    String eventPhotoURL = snapshot.getString("eventPosterURL");

                    availableEventsList.add(new Event(ownerId,eventName,eventCapacityInt,entrantLimit,geolocationRequirement,eventLocation, eventTime, registrationStart,registrationEnd, eventDescription,eventPhotoURL));
                }
                eventRecyclerAdapter.notifyDataSetChanged();
            }
        });

        if (availableEventsList.isEmpty()) {
            noAvailableEvents.setVisibility(View.VISIBLE);
            availableRecyclerView.setVisibility(View.GONE);
        }
        else {
            noAvailableEvents.setVisibility(View.GONE);
            availableRecyclerView.setVisibility(View.VISIBLE);
        }
    }
}