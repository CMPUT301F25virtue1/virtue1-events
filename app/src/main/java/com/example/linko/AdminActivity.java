package com.example.linko;

import static com.example.linko.SearchBarHandler.eventSearchHandler;
import static com.example.linko.SearchBarHandler.userSearchHandler;

import android.content.Intent;
import android.content.res.ColorStateList;
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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class AdminActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private CollectionReference eventsRef;
    private CollectionReference usersRef;
    private List<Event> allEventsList;
    private List<Event> originalEventsList;
    private List<User> allOrganizerList;
    private List<User> allProfilesList;
    private List<User> originalProfilesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);

        ImageView backButton = findViewById(R.id.button_back_button);
        // views under event
        Button eventButton = findViewById(R.id.button_event);
        ConstraintLayout eventContainer = findViewById(R.id.event_container);
        TextView eventEventsButton = findViewById(R.id.click_event_events);
        TextView eventOrganizersButton = findViewById(R.id.click_event_organizers);
        RecyclerView eventsRecyclerView = findViewById(R.id.recycler_all_events);
        RecyclerView organizersRecyclerView = findViewById(R.id.recycler_all_organizers);
        EditText eventSearchBar = findViewById(R.id.input_event_search);

        // views under profiles
        Button profilesButton = findViewById(R.id.button_profiles);
        ConstraintLayout profilesContainer = findViewById(R.id.profiles_container);
        EditText profilesSearchBar = findViewById(R.id.input_profiles_search);
        RecyclerView profilesRecyclerView = findViewById(R.id.recycler_all_profiles);

        // views under button
        Button imagesButton = findViewById(R.id.button_images);

        // event tab recycler view setup
        allEventsList = new ArrayList<>();
        allOrganizerList = new ArrayList<>();
        EventRecyclerAdapter eventRecyclerAdapter = new EventRecyclerAdapter(allEventsList, true);
        eventsRecyclerView.setAdapter(eventRecyclerAdapter);
        LinearLayoutManager eventsLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        eventsRecyclerView.setLayoutManager(eventsLayoutManager);

        UserRecyclerAdapter organizerRecyclerAdapter = new UserRecyclerAdapter(allOrganizerList, true);
        organizersRecyclerView.setAdapter(organizerRecyclerAdapter);
        LinearLayoutManager organizerLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        organizersRecyclerView.setLayoutManager(organizerLayoutManager);

        // swipe to delete events
        ItemTouchHelper.SimpleCallback swipeToDeleteEvent = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();

                Event eventToDelete = allEventsList.get(position);
                EventDatabaseHandler deleteHelper = new EventDatabaseHandler();

                deleteHelper.deleteEvent(eventToDelete, new EventDatabaseHandler.EventDeleted() {
                    @Override
                    public void eventDelete() {
                        Toast.makeText(AdminActivity.this, "Events successfully deleted!", Toast.LENGTH_SHORT).show();
                        allEventsList.remove(eventToDelete);
                        eventRecyclerAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void eventDeleteFailed(Exception e) {
                        Toast.makeText(AdminActivity.this, "Error deleting events.", Toast.LENGTH_SHORT).show();
                        eventRecyclerAdapter.notifyDataSetChanged();
                    }
                });

            }
        };
        // attach swipe to delete to the all events recyclerview
        new ItemTouchHelper(swipeToDeleteEvent).attachToRecyclerView(eventsRecyclerView);

        // swipe to delete organizers
        ItemTouchHelper.SimpleCallback swipeToDeleteOrganizer = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();

                User organizerToDelete = allOrganizerList.get(position);
                String organizerId = organizerToDelete.getUserId();
                EventDatabaseHandler deleteHelper = new EventDatabaseHandler();

                // delete all events that have the organizer's id as their owner
                db = FirebaseFirestore.getInstance();
                eventsRef = db.collection("events");
                eventsRef.addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Firestore", error.toString());
                    }
                    if (value != null && !value.isEmpty()) {
                        Log.d("firebase", "checking documents");
                        for (QueryDocumentSnapshot snapshot : value) {
                            Event eventToDelete = snapshot.toObject(Event.class);
                            if (eventToDelete.getOwnerId().equals(organizerId)) {
                                deleteHelper.deleteEvent(eventToDelete, new EventDatabaseHandler.EventDeleted() {
                                    @Override
                                    public void eventDelete() {
                                        Toast.makeText(AdminActivity.this, "Event successfully deleted!", Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void eventDeleteFailed(Exception e) {
                                        Toast.makeText(AdminActivity.this, "Error deleting event.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    }
                });

                allOrganizerList.remove(organizerToDelete);
                organizerRecyclerAdapter.notifyDataSetChanged();
            }
        };
        // attach swipe to delete to the all organizers recyclerview
        new ItemTouchHelper(swipeToDeleteOrganizer).attachToRecyclerView(organizersRecyclerView);

        // add every event to the recycler view initially
        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("events");
        eventsRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", error.toString());
            }
            if (value != null && !value.isEmpty()) {
                Log.d("firebase", "checking documents");
                allEventsList.clear();
                allOrganizerList.clear();
                for (QueryDocumentSnapshot snapshot : value) {
                    Event eventToAdd = snapshot.toObject(Event.class);
                    allEventsList.add(eventToAdd);
                }
                // keep copy of original events if user searches and clears
                originalEventsList = new ArrayList<>(allEventsList);
                eventRecyclerAdapter.notifyDataSetChanged();

                // populate organizers list
                for (Event e : originalEventsList) {
                    UserDatabaseHandler organizerHelper = new UserDatabaseHandler();
                    organizerHelper.fetchUserById(e.getOwnerId(), new UserDatabaseHandler.UserFetchedFromId() {
                        @Override
                        public void userFetch(User user) {
                            // avoid dupes
                            if (!allOrganizerList.contains(user)) {
                                allOrganizerList.add(user);
                                organizerRecyclerAdapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void userFetchFailed(Exception ee) {
                            Log.e("AdminActivity", "Failed to fetch organizer: " + e.getOwnerId(), ee);
                        }
                    });
                }
            }
        });

        eventEventsButton.setOnClickListener(v -> {
            eventsRecyclerView.setVisibility(View.VISIBLE);
            organizersRecyclerView.setVisibility(View.GONE);
            eventSearchBar.setVisibility(View.VISIBLE);
            eventEventsButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.teal)));
            eventOrganizersButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerTeal)));
        });

        eventOrganizersButton.setOnClickListener(v -> {
            eventsRecyclerView.setVisibility(View.GONE);
            organizersRecyclerView.setVisibility(View.VISIBLE);
            eventSearchBar.setVisibility(View.GONE);
            eventEventsButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerTeal)));
            eventOrganizersButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.teal)));
        });

        eventSearchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                List<Event> filteredList = new ArrayList<>(originalEventsList);

                // search filter
                String userInput = eventSearchBar.getText().toString().trim();
                if (!userInput.isEmpty()) {
                    filteredList = eventSearchHandler(filteredList, userInput);
                }

                allEventsList.clear();
                allEventsList.addAll(filteredList);
                eventRecyclerAdapter.notifyDataSetChanged();
            }
        });

        // profiles tab recycler view setup
        allProfilesList = new ArrayList<>();
        UserRecyclerAdapter profilesRecyclerAdapter = new UserRecyclerAdapter(allProfilesList, true);
        profilesRecyclerView.setAdapter(profilesRecyclerAdapter);
        LinearLayoutManager profilesLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        profilesRecyclerView.setLayoutManager(profilesLayoutManager);
        // swipe to delete profiles
        ItemTouchHelper.SimpleCallback swipeToDeleteProfile = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();

                User userToDelete = allProfilesList.get(position);
                UserDatabaseHandler deleteHelper = new UserDatabaseHandler();
                String userId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                if (userId.equals(userToDelete.getUserId())) {
                    Toast.makeText(AdminActivity.this, "You cannot delete your own profile", Toast.LENGTH_SHORT).show();
                    profilesRecyclerAdapter.notifyDataSetChanged();
                    return;
                }

                String organizerId = userToDelete.getUserId();
                EventDatabaseHandler deleteEventsHelper = new EventDatabaseHandler();
                // delete all events that have this user as their organizer first
                db = FirebaseFirestore.getInstance();
                eventsRef = db.collection("events");
                eventsRef.addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Firestore", error.toString());
                    }
                    if (value != null && !value.isEmpty()) {
                        Log.d("firebase", "checking documents");
                        for (QueryDocumentSnapshot snapshot : value) {
                            Event eventToDelete = snapshot.toObject(Event.class);
                            if (eventToDelete.getOwnerId().equals(organizerId)) {
                                deleteEventsHelper.deleteEvent(eventToDelete, new EventDatabaseHandler.EventDeleted() {
                                    @Override
                                    public void eventDelete() {
                                    }

                                    @Override
                                    public void eventDeleteFailed(Exception e) {
                                        Toast.makeText(AdminActivity.this, "Error deleting events.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    }
                });

                // now delete their document and update event lists they are in
                deleteHelper.deleteUserById(userToDelete.getUserId(), new UserDatabaseHandler.UserDeletedFromId() {
                    @Override
                    public void userDelete() {
                        Toast.makeText(AdminActivity.this, "Profile successfully deleted!", Toast.LENGTH_SHORT).show();
                        allProfilesList.remove(userToDelete);
                        profilesRecyclerAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void userDeleteFailed(Exception e) {
                        Toast.makeText(AdminActivity.this, "Error deleting profile.", Toast.LENGTH_SHORT).show();
                        profilesRecyclerAdapter.notifyDataSetChanged();
                    }
                });
            }
        };
        // attach swipe to delete to the all events recyclerview
        new ItemTouchHelper(swipeToDeleteProfile).attachToRecyclerView(profilesRecyclerView);

        // add every profile to the recycler view initially
        usersRef = db.collection("users");
        usersRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", error.toString());
            }
            if (value != null && !value.isEmpty()) {
                Log.d("firebase", "checking documents");
                allProfilesList.clear();
                for (QueryDocumentSnapshot snapshot : value) {
                    User userToAdd = snapshot.toObject(User.class);
                    allProfilesList.add(userToAdd);
                }
                // keep copy of original events if user searches and clears
                originalProfilesList = new ArrayList<>(allProfilesList);
                profilesRecyclerAdapter.notifyDataSetChanged();
            }
        });

        profilesSearchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                List<User> filteredList = new ArrayList<>(originalProfilesList);

                // search filter
                String userInput = profilesSearchBar.getText().toString().trim();
                if (!userInput.isEmpty()) {
                    filteredList = userSearchHandler(filteredList, userInput);
                }
                allProfilesList.clear();
                allProfilesList.addAll(filteredList);
                profilesRecyclerAdapter.notifyDataSetChanged();
            }
        });

        // top bar
        eventButton.setOnClickListener(v -> {
            eventContainer.setVisibility(View.VISIBLE);
            profilesContainer.setVisibility(View.GONE);
            eventButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.teal)));
            profilesButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerTeal)));
        });

        profilesButton.setOnClickListener(v -> {
            eventContainer.setVisibility(View.GONE);
            profilesContainer.setVisibility(View.VISIBLE);
            eventButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerTeal)));
            profilesButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.teal)));
        });

        backButton.setOnClickListener(v -> {
            startActivity(new Intent(AdminActivity.this, SettingsActivity.class));
            finish();
        });
    }
}