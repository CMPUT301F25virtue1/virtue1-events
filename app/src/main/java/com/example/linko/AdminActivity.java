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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

/**
 *This class contains the UI interaction logic for the Admin mode.
 *<p>
 *Implements a swipe to delete feature for admin for Events, Organizers, Users, and profiles.
 *</p>
 */
public class AdminActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private CollectionReference eventsRef;
    private CollectionReference usersRef;
    private CollectionReference notifsRef;
    private List<Event> allEventsList;
    private List<Event> originalEventsList;
    private List<User> allOrganizerList;
    private List<User> allProfilesList;
    private List<User> originalProfilesList;
    private List<Event> allEventPostersList;
    private List<Event> originalEventPostersList;
    private List<User> allProfilePicturesList;
    private List<User> originalProfilePicturesList;
    private List<UserNotification> notificationsList;

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

        // views under images
        Button imagesButton = findViewById(R.id.button_images);
        ConstraintLayout imagesContainer = findViewById(R.id.images_container);
        TextView imagesPostersButton = findViewById(R.id.click_event_posters);
        TextView imagesProfilePicturesButton = findViewById(R.id.click_profile_pictures);
        EditText imageEventSearchBar = findViewById(R.id.input_event_image_search);
        EditText imageProfileSearchBar = findViewById(R.id.input_profile_image_search);
        RecyclerView eventPostersRecyclerView = findViewById(R.id.recycler_event_posters);
        RecyclerView profilePicturesRecyclerView = findViewById(R.id.recycler_profile_pictures);

        // views under logs
        Button logsButton = findViewById(R.id.button_logs);
        ConstraintLayout logsContainer = findViewById(R.id.logs_container);
        RecyclerView notificationLogsRecyclerView = findViewById(R.id.recycler_notification_logs);

        // lists to populate
        allEventsList = new ArrayList<>();
        allOrganizerList = new ArrayList<>();
        allEventPostersList = new ArrayList<>();
        allProfilePicturesList = new ArrayList<>();
        notificationsList = new ArrayList<>();

        // events recycler view setup
        EventRecyclerAdapter eventRecyclerAdapter = new EventRecyclerAdapter(allEventsList, true);
        eventsRecyclerView.setAdapter(eventRecyclerAdapter);
        LinearLayoutManager eventsLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        eventsRecyclerView.setLayoutManager(eventsLayoutManager);

        EventPosterRecyclerAdapter eventPostersRecyclerAdapter = new EventPosterRecyclerAdapter(allEventPostersList);
        eventPostersRecyclerView.setAdapter(eventPostersRecyclerAdapter);
        GridLayoutManager eventPostersLayoutManager = new GridLayoutManager(this, 3);
        eventPostersRecyclerView.setLayoutManager(eventPostersLayoutManager);

        // organizer recycler view setup
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
                        originalEventsList.remove(eventToDelete);
                        allEventPostersList.remove(eventToDelete);
                        originalEventPostersList.remove(eventToDelete);
                        eventPostersRecyclerAdapter.notifyDataSetChanged();
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
                eventsRef.get().addOnSuccessListener(query -> {
                    for (QueryDocumentSnapshot snapshot : query) {
                        Event eventToDelete = snapshot.toObject(Event.class);
                        if (eventToDelete.getOwnerId().equals(organizerId)) {
                            deleteHelper.deleteEvent(eventToDelete, new EventDatabaseHandler.EventDeleted() {
                                @Override
                                public void eventDelete() {
                                    // delete from lists since not using snapshot listener (update manually)
                                    allEventsList.remove(eventToDelete);
                                    originalEventsList.remove(eventToDelete);
                                    allEventPostersList.remove(eventToDelete);
                                    originalEventPostersList.remove(eventToDelete);
                                    eventPostersRecyclerAdapter.notifyDataSetChanged();
                                    eventRecyclerAdapter.notifyDataSetChanged();
                                }
                                @Override
                                public void eventDeleteFailed(Exception e) {
                                    Toast.makeText(AdminActivity.this, "Error deleting event.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                    // remove organizer
                    allOrganizerList.remove(organizerToDelete);
                    organizerRecyclerAdapter.notifyDataSetChanged();
                    Toast.makeText(AdminActivity.this, "Organizer deleted!", Toast.LENGTH_SHORT).show();

                }).addOnFailureListener(e -> {
                    Log.e("Firestore", "Error fetching events", e);
                    organizerRecyclerAdapter.notifyDataSetChanged();
                });
            }
        };
        // attach swipe to delete to the all organizers recyclerview
        new ItemTouchHelper(swipeToDeleteOrganizer).attachToRecyclerView(organizersRecyclerView);

        // add every event to the recycler view initially (also used for event posters tab)
        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("events");
        eventsRef.get().addOnSuccessListener(query -> {
            allEventsList.clear();
            allOrganizerList.clear();
            allEventPostersList.clear();
            for (QueryDocumentSnapshot snapshot : query) {
                Event eventToAdd = snapshot.toObject(Event.class);
                allEventsList.add(eventToAdd);
                if (eventToAdd.getEventPosterURL() != null) {
                    allEventPostersList.add(eventToAdd);
                }
            }
            // keep copy of original events if user searches and clears
            originalEventsList = new ArrayList<>(allEventsList);
            originalEventPostersList = new ArrayList<>(allEventPostersList);
            eventRecyclerAdapter.notifyDataSetChanged();
            eventPostersRecyclerAdapter.notifyDataSetChanged();
            // populate  organizers list
            for (Event e : allEventsList) {
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
        }).addOnFailureListener(e -> Log.e("Firestore", "Error fetching events", e));

        eventEventsButton.setOnClickListener(v -> {
            eventsRecyclerView.setVisibility(View.VISIBLE);
            organizersRecyclerView.setVisibility(View.GONE);
            eventSearchBar.setVisibility(View.VISIBLE);
            eventEventsButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.blue)));
            eventOrganizersButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerBlue)));
        });

        eventOrganizersButton.setOnClickListener(v -> {
            eventsRecyclerView.setVisibility(View.GONE);
            organizersRecyclerView.setVisibility(View.VISIBLE);
            eventSearchBar.setVisibility(View.GONE);
            eventEventsButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerBlue)));
            eventOrganizersButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.blue)));

            allOrganizerList.clear();
            // repopulate so if events from organizer is deleted manually one by one, organizers reflects that change
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

        // users (profiles/profile pictures) recycler view setup
        allProfilesList = new ArrayList<>();
        UserRecyclerAdapter profilesRecyclerAdapter = new UserRecyclerAdapter(allProfilesList, true);
        profilesRecyclerView.setAdapter(profilesRecyclerAdapter);
        LinearLayoutManager profilesLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        profilesRecyclerView.setLayoutManager(profilesLayoutManager);

        ProfilePictureRecyclerAdapter profilePicturesRecyclerAdapter = new ProfilePictureRecyclerAdapter(allProfilePicturesList);
        profilePicturesRecyclerView.setAdapter(profilePicturesRecyclerAdapter);
        GridLayoutManager profilePicturesLayoutManager = new GridLayoutManager(this, 3);
        profilePicturesRecyclerView.setLayoutManager(profilePicturesLayoutManager);
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
                eventsRef.get().addOnSuccessListener(query -> {
                    for (QueryDocumentSnapshot snapshot : query) {
                        Event eventToDelete = snapshot.toObject(Event.class);
                        if (eventToDelete.getOwnerId().equals(organizerId)) {
                            deleteEventsHelper.deleteEvent(eventToDelete, new EventDatabaseHandler.EventDeleted() {
                                @Override
                                public void eventDelete() {
                                    // manually update lists
                                    allEventsList.remove(eventToDelete);
                                    originalEventsList.remove(eventToDelete);
                                    allEventPostersList.remove(eventToDelete);
                                    originalEventPostersList.remove(eventToDelete);
                                    eventPostersRecyclerAdapter.notifyDataSetChanged();
                                    eventRecyclerAdapter.notifyDataSetChanged();
                                }

                                @Override
                                public void eventDeleteFailed(Exception e) {
                                    Toast.makeText(AdminActivity.this, "Error deleting events.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                    // now delete their document and update event lists they are in
                    deleteHelper.deleteUserById(userToDelete.getUserId(), new UserDatabaseHandler.UserDeletedFromId() {
                        @Override
                        public void userDelete() {
                            // update
                            allProfilesList.remove(userToDelete);
                            allOrganizerList.remove(userToDelete);
                            originalProfilesList.remove(userToDelete);
                            allProfilePicturesList.remove(userToDelete);
                            originalProfilePicturesList.remove(userToDelete);

                            profilePicturesRecyclerAdapter.notifyDataSetChanged();
                            profilesRecyclerAdapter.notifyDataSetChanged();
                            organizerRecyclerAdapter.notifyDataSetChanged();
                            Toast.makeText(AdminActivity.this, "Profile successfully deleted!", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void userDeleteFailed(Exception e) {
                            Toast.makeText(AdminActivity.this, "Error deleting profile.", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            }
        };
        // attach swipe to delete to the all events recyclerview
        new ItemTouchHelper(swipeToDeleteProfile).attachToRecyclerView(profilesRecyclerView);

        // add every profile to the recycler view initially (also used for profile pictures tab)
        usersRef = db.collection("users");
        usersRef.get().addOnSuccessListener(value -> {
            if (value != null && !value.isEmpty()) {
                Log.d("firebase", "checking documents");
                allProfilesList.clear();
                allProfilePicturesList.clear();
                for (QueryDocumentSnapshot snapshot : value) {
                    User userToAdd = snapshot.toObject(User.class);
                    allProfilesList.add(userToAdd);
                    if (userToAdd.getProfileUrl() != null) {
                        allProfilePicturesList.add(userToAdd);
                    }
                }
                // keep copy of original events if user searches and clears
                originalProfilesList = new ArrayList<>(allProfilesList);
                originalProfilePicturesList = new ArrayList<>(allProfilePicturesList);
                profilePicturesRecyclerAdapter.notifyDataSetChanged();
                profilesRecyclerAdapter.notifyDataSetChanged();
            }
        }).addOnFailureListener(error -> {
            Log.e("firebase", error.toString());
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

        // images tab recycler view setup
        eventPostersRecyclerAdapter.setOnItemClickListener(position -> {
            Event clickedEvent = allEventPostersList.get(position);
            if (clickedEvent.getEventPosterURL() != null && !clickedEvent.getEventPosterURL().isEmpty()) {
                FirebaseStorage.getInstance().getReferenceFromUrl(clickedEvent.getEventPosterURL())
                        .delete()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.d("Storage", "Event poster deleted");
                            } else {
                                Log.e("Storage", "Error deleting poster", task.getException());
                            }
                        });
            }
            clickedEvent.setEventPosterURL(null);
            EventDatabaseHandler eventDb = new EventDatabaseHandler();
            eventDb.update(clickedEvent, new EventDatabaseHandler.EventUpdated() {
                @Override
                public void eventUpdate() {

                }

                @Override
                public void eventUpdateFailed(Exception e) {
                    Log.e("Admin", "event update failed");
                }
            });
            Toast.makeText(AdminActivity.this, "Event poster deleted!", Toast.LENGTH_SHORT).show();

            allEventsList.set(allEventsList.indexOf(clickedEvent), clickedEvent);
            originalEventsList.set(originalEventsList.indexOf(clickedEvent), clickedEvent);

            allEventPostersList.remove(clickedEvent);
            originalEventPostersList.remove(clickedEvent);

            eventRecyclerAdapter.notifyDataSetChanged();
            eventPostersRecyclerAdapter.notifyDataSetChanged();
        });

        profilePicturesRecyclerAdapter.setOnItemClickListener(position -> {
            User clickedUser = allProfilePicturesList.get(position);
            if (clickedUser.getProfileUrl() != null && !clickedUser.getProfileUrl().isEmpty()) {
                FirebaseStorage.getInstance().getReferenceFromUrl(clickedUser.getProfileUrl())
                        .delete()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.d("Storage", "User profile picture deleted");
                            } else {
                                Log.e("Storage", "Error deleting profile picture", task.getException());
                            }
                        });
            }

            clickedUser.setProfileUrl(null);
            UserDatabaseHandler userDb = new UserDatabaseHandler();
            // logic for adding a user is the same as updating the user for userdatabasehandler
            userDb.addUser(clickedUser, new UserDatabaseHandler.UserAdded() {
                @Override
                public void userAdd() {

                }

                @Override
                public void userFailedToAdd(Exception e) {
                    Log.e("Admin", "user update failed");

                }
            });
            Toast.makeText(AdminActivity.this, "User profile picture deleted!", Toast.LENGTH_SHORT).show();

            allProfilesList.set(allProfilesList.indexOf(clickedUser), clickedUser);
            originalProfilesList.set(originalProfilesList.indexOf(clickedUser), clickedUser);

            allProfilePicturesList.remove(clickedUser);
            originalProfilePicturesList.remove(clickedUser);

            profilesRecyclerAdapter.notifyDataSetChanged();
            profilePicturesRecyclerAdapter.notifyDataSetChanged();
        });

        imagesPostersButton.setOnClickListener(view -> {
            eventPostersRecyclerView.setVisibility(View.VISIBLE);
            profilePicturesRecyclerView.setVisibility(View.GONE);
            imageEventSearchBar.setVisibility(View.VISIBLE);
            imageProfileSearchBar.setVisibility(View.GONE);
            imagesPostersButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.blue)));
            imagesProfilePicturesButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerBlue)));
        });

        imagesProfilePicturesButton.setOnClickListener(view -> {
            eventPostersRecyclerView.setVisibility(View.GONE);
            profilePicturesRecyclerView.setVisibility(View.VISIBLE);
            imageEventSearchBar.setVisibility(View.GONE);
            imageProfileSearchBar.setVisibility(View.VISIBLE);
            imagesPostersButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerBlue)));
            imagesProfilePicturesButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.blue)));
        });

        imageEventSearchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                List<Event> filteredList = new ArrayList<>(originalEventPostersList);

                // search filter
                String userInput = imageEventSearchBar.getText().toString().trim();
                if (!userInput.isEmpty()) {
                    filteredList = eventSearchHandler(filteredList, userInput);
                }

                allEventPostersList.clear();
                allEventPostersList.addAll(filteredList);
                eventPostersRecyclerAdapter.notifyDataSetChanged();
            }
        });

        imageProfileSearchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                List<User> filteredList = new ArrayList<>(originalProfilePicturesList);

                // search filter
                String userInput = imageProfileSearchBar.getText().toString().trim();
                if (!userInput.isEmpty()) {
                    filteredList = userSearchHandler(filteredList, userInput);
                }

                allProfilePicturesList.clear();
                allProfilePicturesList.addAll(filteredList);
                profilePicturesRecyclerAdapter.notifyDataSetChanged();
            }
        });

        // notification logs setup
        NotificationRecyclerAdapter notificationsRecyclerAdapter = new NotificationRecyclerAdapter(notificationsList);
        notificationLogsRecyclerView.setAdapter(notificationsRecyclerAdapter);

        LinearLayoutManager notificationsLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        notificationLogsRecyclerView.setLayoutManager(notificationsLayoutManager);

        notifsRef = db.collection("notifications");

        notifsRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", error.toString());
            }
            if (value != null && !value.isEmpty()) {
                Log.d("firebase", "checking documents");

                notificationsList.clear();

                for (QueryDocumentSnapshot snapshot : value) {
                    UserNotification notificationToAdd = snapshot.toObject(UserNotification.class);
                    notificationsList.add(notificationToAdd);
                    notificationsRecyclerAdapter.notifyDataSetChanged();
                }
            }
        });

        // top bar
        eventButton.setOnClickListener(v -> {
            eventContainer.setVisibility(View.VISIBLE);
            profilesContainer.setVisibility(View.GONE);
            imagesContainer.setVisibility(View.GONE);
            logsContainer.setVisibility(View.GONE);
            eventButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.blue)));
            profilesButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerBlue)));
            imagesButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerBlue)));
            logsButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerBlue)));
        });

        profilesButton.setOnClickListener(v -> {
            eventContainer.setVisibility(View.GONE);
            profilesContainer.setVisibility(View.VISIBLE);
            imagesContainer.setVisibility(View.GONE);
            logsContainer.setVisibility(View.GONE);
            eventButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerBlue)));
            profilesButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.blue)));
            imagesButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerBlue)));
            logsButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerBlue)));
        });

        imagesButton.setOnClickListener(v -> {
            eventContainer.setVisibility(View.GONE);
            profilesContainer.setVisibility(View.GONE);
            imagesContainer.setVisibility(View.VISIBLE);
            logsContainer.setVisibility(View.GONE);
            eventButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerBlue)));
            profilesButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerBlue)));
            imagesButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.blue)));
            logsButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerBlue)));
        });

        logsButton.setOnClickListener(v -> {
            eventContainer.setVisibility(View.GONE);
            profilesContainer.setVisibility(View.GONE);
            imagesContainer.setVisibility(View.GONE);
            logsContainer.setVisibility(View.VISIBLE);
            eventButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerBlue)));
            profilesButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerBlue)));
            imagesButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerBlue)));
            logsButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.blue)));
        });

        backButton.setOnClickListener(v -> {
            startActivity(new Intent(AdminActivity.this, SettingsActivity.class));
            finish();
        });
    }
}