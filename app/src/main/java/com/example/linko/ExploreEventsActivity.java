package com.example.linko;

import static com.example.linko.NavigationBarHandler.navigationListener;
import static com.example.linko.SearchBarHandler.eventSearchHandler;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExploreEventsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private CollectionReference eventsRef;

    private List<Event> availableEventsList;
    private List<Event> originalEventsList;
    private EventRecyclerAdapter eventRecyclerAdapter;

    private EditText searchBar;
    private Calendar userFilterStart = Calendar.getInstance();
    private Calendar userFilterEnd = Calendar.getInstance();
    private boolean userFilterStartPicked = false;
    private boolean userFilterEndPicked = false;
    private TextView noAvailableEvents;
    private TextView noEventsMatchFilter;
    private RecyclerView availableRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_explore_events);
        navigationListener(this);

        noAvailableEvents = findViewById(R.id.text_no_event_available);
        noEventsMatchFilter = findViewById(R.id.text_no_event_from_filter);
        searchBar = findViewById(R.id.input_search);

        // filter views
        ImageView filterButton = findViewById(R.id.button_filter_events);

        // recycler view setup
        availableRecyclerView = findViewById(R.id.recycler_available_events);
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

                    // if registration hasn't started yet, skip
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
                // keep copy of original events if user searches and clears
                originalEventsList = new ArrayList<>(availableEventsList);
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

        // search bar
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyCurrentFilters();
            }
        });

        // filter
        filterButton.setOnClickListener(v -> {
            EventFilterDialog filterDialog = EventFilterDialog.newInstance(userFilterStart, userFilterEnd, userFilterStartPicked, userFilterEndPicked);
            filterDialog.setOnFilterAppliedListener(((start, end, startPicked, endPicked) -> {
                // store vars so filter is saved when user opens it again
                userFilterStart = start;
                userFilterEnd = end;
                userFilterStartPicked = startPicked;
                userFilterEndPicked = endPicked;
                applyCurrentFilters();
            }));
            filterDialog.show(getSupportFragmentManager(), "eventFilterDialog");
        });
    }

    private void applyCurrentFilters() {
        List<Event> filteredList = new ArrayList<>(originalEventsList);

        // search filter
        String userInput = searchBar.getText().toString().trim();
        if (!userInput.isEmpty()) {
            filteredList = eventSearchHandler(filteredList, userInput);
        }

        // date filter (if user didn't pick anything, skip it)
        if (userFilterStartPicked && userFilterEndPicked) {
            Date startFilter = userFilterStart.getTime();
            Date endFilter = userFilterEnd.getTime();

            List<Event> dateFilteredList = new ArrayList<>();
            for (Event event : filteredList) {
                Date eventTime = event.getEventTime();
                Log.d("datecheck", eventTime.toString());
                Log.d("datecheck", startFilter.toString());
                Log.d("datecheck", endFilter.toString());

                // must be within range of user input
                if (eventTime.after(startFilter) && eventTime.before(endFilter)) {
                    dateFilteredList.add(event);
                }
            }
            filteredList = dateFilteredList;
        }
        Log.d("datecheck", filteredList.toString());

        availableEventsList.clear();
        availableEventsList.addAll(filteredList);
        eventRecyclerAdapter.notifyDataSetChanged();

        if (filteredList.isEmpty()) {
            Log.d("datecheck", filteredList.toString());

            noEventsMatchFilter.setVisibility(View.VISIBLE);
            noAvailableEvents.setVisibility(View.GONE);
            availableRecyclerView.setVisibility(View.GONE);
        } else {
            noEventsMatchFilter.setVisibility(View.GONE);
            noAvailableEvents.setVisibility(View.GONE);
            availableRecyclerView.setVisibility(View.VISIBLE);
        }
    }
}