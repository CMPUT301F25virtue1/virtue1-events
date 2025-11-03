package com.example.linko;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditEventActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private Calendar startCalendar = Calendar.getInstance();
    private Calendar endCalendar = Calendar.getInstance();
    private ImageView eventPoster;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);
        EdgeToEdge.enable(this);

        Button saveEventChanges = findViewById(R.id.button_save_event_changes);
        EditText eventNameInput = findViewById(R.id.text_event_name);
        EditText eventCapacityInput = findViewById(R.id.text_event_capacity);
        EditText entrantLimitInput = findViewById(R.id.text_entrant_count);
        EditText eventLocationInput = findViewById(R.id.text_event_location);
        EditText eventDescriptionInput = findViewById(R.id.text_event_description);
        CheckBox geolocationBox = findViewById(R.id.checkBox);
        EditText eventTimeInput = findViewById(R.id.text_event_time);
        TextView registrationPeriod = findViewById(R.id.text_event_registration_period);
        eventPoster = findViewById(R.id.image_event_poster);
        ImageView backButton = findViewById(R.id.button_back_button);

        Event eventReceived = (Event) getIntent().getSerializableExtra("savedEvent");
        Bundle extras = getIntent().getExtras();
        String uriString = extras != null ? extras.getString("imageUri") : null;
        imageUri = uriString != null ? Uri.parse(uriString) : null;

        if (eventReceived != null) {
            eventNameInput.setText(eventReceived.getName());
            Integer eventCapacityNumber = eventReceived.getEventCapacity();
            String eventCapacityString = eventCapacityNumber.toString();
            eventCapacityInput.setText(eventCapacityString);

            Integer entrantLimitNumber = eventReceived.getEntrantLimit();
            if (eventReceived.getEntrantLimit() != null) {
                String entrantLimitString = entrantLimitNumber.toString();
                entrantLimitInput.setText(entrantLimitString);
            }

            geolocationBox .setChecked(eventReceived.isGeolocationRequired());
            eventLocationInput.setText(eventReceived.getEventLocation());
            eventTimeInput.setText(eventReceived.getEventTime());

            Date start = eventReceived.getRegistrationStart();
            Date end = eventReceived.getRegistrationEnd();
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.getDefault());
            String period = sdf.format(start) + " to " + sdf.format(end);
            registrationPeriod.setText(period);

            Glide.with(EditEventActivity.this).load(imageUri).centerCrop().placeholder(R.drawable.outline_photo_camera_24).into(eventPoster);

            eventDescriptionInput.setText(eventReceived.getDescription());
        }

        backButton.setOnClickListener(v -> {
            startActivity(new Intent(EditEventActivity.this, AddEventActivity.class));
            finish();
        });

        registrationPeriod.setOnClickListener(v ->
                pickDateTime(startCalendar, "Start", start ->
                        pickDateTime(endCalendar, "End", end -> {
                            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.getDefault());
                            String period = sdf.format(start.getTime()) + " to " + sdf.format(end.getTime());
                            registrationPeriod.setText(period);
                        })
                )
        );

        eventPoster.setOnClickListener(v -> {
            openFileChooser();
        });
        saveEventChanges.setOnClickListener(v -> {
            String eventName = eventNameInput.getText().toString();
            if (eventName.isEmpty()) {
                Toast.makeText(this, "Please fill out your event name.", Toast.LENGTH_SHORT).show();
                return;
            }

            String eventCapacity = eventCapacityInput.getText().toString();
            if (eventCapacity.isEmpty()) {
                Toast.makeText(this, "Please fill out your event capacity.", Toast.LENGTH_SHORT).show();
                return;
            }
            Integer eventCapacityInt = Integer.parseInt(eventCapacity);

            Integer entrantLimit = null;
            String capacityStr = entrantLimitInput.getText().toString();
            if (!capacityStr.isEmpty()) {
                entrantLimit = Integer.parseInt(capacityStr);
            }

            boolean geolocationRequirement = geolocationBox.isChecked();

            String eventDescription = eventDescriptionInput.getText().toString();
            if (eventDescription.isEmpty()) {
                Toast.makeText(this, "Please fill out your event description.", Toast.LENGTH_SHORT).show();
                return;
            }

            // needs firebase storage to implement
            String eventPhotoURL = null;

            String eventLocation = eventLocationInput.getText().toString();
            if (eventLocation.isEmpty()) {
                Toast.makeText(this, "Please fill out your event location.", Toast.LENGTH_SHORT).show();
                return;
            }

            String eventTime = eventTimeInput.getText().toString();
            Date registrationStart = startCalendar.getTime();
            Date registrationEnd = endCalendar.getTime();
            Event eventToSave = new Event(eventName,eventCapacityInt,entrantLimit,geolocationRequirement,eventLocation, eventTime, registrationStart,registrationEnd, eventDescription,eventPhotoURL);

            Intent intent = new Intent(EditEventActivity.this, AddEventActivity.class);
            intent.putExtra("savedEvent", eventToSave);
            intent.putExtra("imageUri", imageUri != null ? imageUri.toString() : null);
            if (imageUri != null) {
                getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            startActivity(intent);
            finish();
        });
    }
    private void openFileChooser() {
        Intent galleryIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        galleryIntent.setType("image/*");
        galleryIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        galleryIntent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                imageUri = data.getData();
                getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Glide.with(EditEventActivity.this).load(imageUri).centerCrop().into(eventPoster);
            }
        }
    }

    private void pickDateTime(Calendar calendar, String title, DateTimePickedCallback callback) {
        // Date Picker
        DatePickerDialog datePicker = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);

                    TimePickerDialog timePicker = new TimePickerDialog(this,
                            (timeView, hourOfDay, minute) -> {
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);
                                callback.onDateTimePicked(calendar);
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            false // 12hr formatting
                    );
                    timePicker.setTitle(title + " Time");
                    timePicker.show();

                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePicker.setTitle(title + " Date");
        datePicker.show();
    }

    interface DateTimePickedCallback {
        void onDateTimePicked(Calendar calendar);
    }
}