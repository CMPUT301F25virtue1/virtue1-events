package com.example.linko;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
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
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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

public class OrganizerEventDetailsActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private ImageView editPoster;
    private List<User> totalEntrantsList;
    private FirebaseFirestore db;
    private CollectionReference usersRef;

    private EventRecyclerAdapter entrantsUserRecyclerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_organizer_event_details);

        // ui
        ImageView backButton = findViewById(R.id.button_back_button);
        ImageView backButtonForEditPoster = findViewById(R.id.button_edit_poster_back);
        TextView eventName = findViewById(R.id.text_event_name);
        TextView eventCapacity = findViewById(R.id.text_event_capacity);
        TextView entrantCount = findViewById(R.id.text_entrant_count);
        CheckBox geolocationCheck = findViewById(R.id.checkBox);
        TextView eventLocation = findViewById(R.id.text_event_location);
        TextView eventTime = findViewById(R.id.text_event_time);
        TextView registrationPeriod = findViewById(R.id.text_event_registration_period);
        TextView eventDescription = findViewById(R.id.text_event_description);
        TextView descriptionButton = findViewById(R.id.click_event_description);
        TextView posterButton = findViewById(R.id.click_event_poster);
        ImageView eventPoster = findViewById(R.id.image_event_poster);
        Button editEvent = findViewById(R.id.button_edit_event);
        View backgroundDim = findViewById(R.id.background_dim);
        ConstraintLayout editPosterContainer = findViewById(R.id.edit_poster_container);
        Button savePoster = findViewById(R.id.button_save_poster);
        editPoster = findViewById(R.id.image_edit_poster);
        TextView noEntrants = findViewById(R.id.text_no_entrants);

        // total entrants recycler view
        RecyclerView entrantsRecyclerView = findViewById(R.id.recycler_event_entrants);
        totalEntrantsList = new ArrayList<>();
        entrantsUserRecyclerAdapter = new UserRecyclerAdapter(totalEntrantsList);
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

        entrantCount.setText(eventReceived.getEntrantCount() + "/" + eventReceived.getEntrantLimit());

        geolocationCheck.setChecked(eventReceived.isGeolocationRequired());
        eventLocation.setText(eventReceived.getEventLocation());
        eventTime.setText(eventReceived.getEventTime());

        Date start = eventReceived.getRegistrationStart();
        Date end = eventReceived.getRegistrationEnd();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.getDefault());
        String period = sdf.format(start) + " to " + sdf.format(end);
        registrationPeriod.setText(period);
        eventDescription.setText(eventReceived.getDescription());

        descriptionButton.setOnClickListener(v -> {
            eventDescription.setVisibility(View.VISIBLE);
            eventPoster.setVisibility(View.GONE);
            descriptionButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.teal)));
            posterButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerTeal)));
        });
        posterButton.setOnClickListener(v -> {
            eventDescription.setVisibility(View.GONE);
            eventPoster.setVisibility(View.VISIBLE);
            descriptionButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerTeal)));
            posterButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.teal)));
        });

        backButton.setOnClickListener(v -> {
            startActivity(new Intent(OrganizerEventDetailsActivity.this, MyEventsActivity.class));
            finish();
        });

        backButtonForEditPoster.setOnClickListener(v -> {
            backgroundDim.setVisibility(View.GONE);
            editPosterContainer.setVisibility(View.GONE);
        });

        editEvent.setOnClickListener(v -> {
            backgroundDim.setVisibility(View.VISIBLE);
            editPosterContainer.setVisibility(View.VISIBLE);
        });

        editPoster.setOnClickListener(v-> {
            openFileChooser();
        });

        savePoster.setOnClickListener(v-> {
            if (imageUri == null) {
                backgroundDim.setVisibility(View.GONE);
                editPosterContainer.setVisibility(View.GONE);
                return;
            }

            ImageStorageHandler eventPictureUpdate = new ImageStorageHandler();

            eventPictureUpdate.uploadEventImage(imageUri, eventReceived.getEventId(), new ImageStorageHandler.imageUploaded() {
                @Override
                public void onUploadSuccess(String downloadUrl) {
                    eventReceived.setEventPosterURL(downloadUrl);
                    EventDatabaseHandler db = new EventDatabaseHandler();
                    db.update(eventReceived, new EventDatabaseHandler.EventUpdated() {
                        @Override
                        public void eventUpdate() {
                            Toast.makeText(OrganizerEventDetailsActivity.this, "Poster successfully updated!", Toast.LENGTH_LONG).show();
                            Glide.with(OrganizerEventDetailsActivity.this).load(imageUri).centerCrop().into(eventPoster);
                            backgroundDim.setVisibility(View.GONE);
                            editPosterContainer.setVisibility(View.GONE);
                        }

                        @Override
                        public void eventFailedToUpdate(Exception e) {
                            Toast.makeText(OrganizerEventDetailsActivity.this, "Error updating event poster: " + e.getMessage(), Toast.LENGTH_LONG).show();

                        }
                    });
                }

                @Override
                public void onUploadFailed(Exception e) {
                    Toast.makeText(OrganizerEventDetailsActivity.this, "Error uploading event poster: " + e.getMessage(), Toast.LENGTH_LONG).show();

                }
            });

            backgroundDim.setVisibility(View.VISIBLE);
            editPosterContainer.setVisibility(View.VISIBLE);
        });


    }

    private void openFileChooser() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");

        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                imageUri = data.getData();
                Glide.with(OrganizerEventDetailsActivity.this).load(imageUri).centerCrop().into(editPoster);
            }
        }
    }
}