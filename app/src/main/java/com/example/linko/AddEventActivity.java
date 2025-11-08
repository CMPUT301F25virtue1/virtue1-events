package com.example.linko;

import android.content.Intent;
import android.content.res.ColorStateList;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Handles all of the UI logic for adding events. Calls the Database Handler when necessary to upload the
 * event to the Firebase Database
 *
 * @see EventDatabaseHandler
 */
public class AddEventActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private CollectionReference eventsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);
        EdgeToEdge.enable(this);

        Button postEvent = findViewById(R.id.button_post_event);
        ImageView editButton = findViewById(R.id.button_edit_event);
        ImageView backButton = findViewById(R.id.button_back_button);
        TextView descriptionButton = findViewById(R.id.click_event_description);
        TextView posterButton = findViewById(R.id.click_event_poster);
        TextView guidelinesButton = findViewById(R.id.click_event_guidelines);

        // event display stuff
        TextView eventName = findViewById(R.id.text_event_name);
        TextView eventCapacity = findViewById(R.id.text_event_capacity);
        TextView entrantLimit = findViewById(R.id.text_entrant_count);
        CheckBox geolocationCheck = findViewById(R.id.checkBox);
        TextView eventTime = findViewById(R.id.text_event_start_time);
        TextView registrationStart = findViewById(R.id.text_event_registration_start);
        TextView registrationEnd = findViewById(R.id.text_event_registration_end);
        TextView eventDescription = findViewById(R.id.text_event_description);
        TextView eventGuidelines = findViewById(R.id.text_event_guidelines);
        ImageView eventPoster = findViewById(R.id.image_event_poster);

        Event eventReceived = (Event) getIntent().getSerializableExtra("savedEvent");
        // https://stackoverflow.com/questions/8017374/how-to-pass-a-uri-to-an-intent
        Bundle extras = getIntent().getExtras();
        String uriString = extras != null ? extras.getString("imageUri") : null;
        Uri eventPosterUri = uriString != null ? Uri.parse(uriString) : null;

        if (eventReceived != null) {
            eventName.setText(eventReceived.getName());
            Integer eventCapacityNumber = eventReceived.getEventCapacity();
            String eventCapacityString = eventCapacityNumber.toString();
            eventCapacity.setText(eventCapacityString);

            Integer entrantLimitNumber = eventReceived.getEntrantLimit();
            if (eventReceived.getEntrantLimit() == null) {
                entrantLimit.setText("N/A");
            }
            else {
                String entrantLimitString = entrantLimitNumber.toString();
                entrantLimit.setText(entrantLimitString);
            }

            geolocationCheck.setChecked(eventReceived.isGeolocationRequired());

            Date eventStart = eventReceived.getEventTime();
            Date start = eventReceived.getRegistrationStart();
            Date end = eventReceived.getRegistrationEnd();
            SimpleDateFormat sdf = new SimpleDateFormat("MMM-dd-yyyy | hh:mm a", Locale.getDefault());
            eventTime.setText(sdf.format(eventStart));
            registrationStart.setText(sdf.format(start));
            registrationEnd.setText(sdf.format(end));


            Glide.with(AddEventActivity.this).load(eventPosterUri).placeholder(R.drawable.outline_photo_camera_24).centerCrop().into(eventPoster);

            eventDescription.setText(eventReceived.getDescription());
            eventGuidelines.setText(eventReceived.getGuidelines());

        }

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
            startActivity(new Intent(AddEventActivity.this, MyEventsActivity.class));
            finish();
        });

        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(AddEventActivity.this, EditEventActivity.class);
            intent.putExtra("savedEvent", eventReceived);
            intent.putExtra("imageUri", eventPosterUri != null ? eventPosterUri.toString() : null);
            if (eventPosterUri != null) {
                getContentResolver().takePersistableUriPermission(eventPosterUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            startActivity(intent);
            finish();
        });

        postEvent.setOnClickListener(v -> {
            Toast.makeText(this, "Posting event...", Toast.LENGTH_SHORT).show();
            if (eventReceived != null) {
                db = FirebaseFirestore.getInstance();
                eventsRef = db.collection("events");
                String userId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                eventReceived.setOwnerId(userId);
                DocumentReference docRef = eventsRef.document();
                String eventId = docRef.getId();
                if (eventPosterUri != null) {
                    ImageStorageHandler eventPictureUpload = new ImageStorageHandler();
                    eventPictureUpload.uploadEventImage(eventPosterUri, eventId, new ImageStorageHandler.imageUploaded() {
                        @Override
                        public void onUploadSuccess(String downloadUrl) {
                            eventReceived.setEventPosterURL(downloadUrl);
                            addEvent(eventReceived, docRef, eventId);
                        }

                        @Override
                        public void onUploadFailed(Exception e) {
                            Toast.makeText(AddEventActivity.this, "Error uploading event poster: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
                else {
                    // add event with null poster
                    addEvent(eventReceived, docRef, eventId);
                }
            }
            else {
                Toast.makeText(AddEventActivity.this, "You have not provided sufficient event details to post.", Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * "Posts" the event and calls the Database handler to add it to the Firebase database
     * @param event The event object itself
     * @param docRef Used to document where the event is in Firebase and allows for reading and writing of it to the database
     * @param eventId The Firebase Id for the event
     *
     * @see EventDatabaseHandler
     */
    public void addEvent(Event event, DocumentReference docRef, String eventId) {
        new EventDatabaseHandler().addEvent(event, new EventDatabaseHandler.EventAdded() {

            @Override
            public void eventAdd() {
                Toast.makeText(AddEventActivity.this, "Event has been posted!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(AddEventActivity.this, MyEventsActivity.class));
                finish();
            }

            @Override
            public void eventAddFailed(Exception e) {
                Log.e("Firestore", "Error saving user", e);
                Toast.makeText(AddEventActivity.this, "Error saving event: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, docRef, eventId);
    }
}