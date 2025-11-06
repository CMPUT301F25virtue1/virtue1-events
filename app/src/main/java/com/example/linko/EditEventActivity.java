package com.example.linko;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * This is the class for handling the edit event logic that interacts with the UI.
 * Allows user to edit an event they plan on organizing before they post it. To access this,
 * User clicks on the little pencil icon in the bottom right corner to enter edit mode.
 * <p>
 *     Once in edit mode, user can set the event name, capacity, entrant limit, if geolocation is required
 *     Event/registration times, event descriptions and set a photo for the event before they post.
 * </p>
 */
public class EditEventActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private Calendar startCalendar = Calendar.getInstance();
    private Calendar endCalendar = Calendar.getInstance();
    private Calendar eventTimeCalendar = Calendar.getInstance();

    private ImageView eventPoster;
    private boolean registrationStartPicked = false;
    private boolean registrationEndPicked = false;
    private boolean eventTimeStartPicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);
        EdgeToEdge.enable(this);

        Button saveEventChanges = findViewById(R.id.button_save_event_changes);
        EditText eventNameInput = findViewById(R.id.text_event_name);
        EditText eventCapacityInput = findViewById(R.id.text_event_capacity);
        EditText entrantLimitInput = findViewById(R.id.text_entrant_count);
        EditText eventDescriptionInput = findViewById(R.id.text_event_description);
        CheckBox geolocationBox = findViewById(R.id.checkBox);
        TextView eventTime = findViewById(R.id.text_event_start_time);
        TextView registrationStart = findViewById(R.id.text_event_registration_start);
        TextView registrationEnd = findViewById(R.id.text_event_registration_end);
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
            Date eventStart = eventReceived.getEventTime();
            Date start = eventReceived.getRegistrationStart();
            Date end = eventReceived.getRegistrationEnd();
            SimpleDateFormat sdf = new SimpleDateFormat("MMM-dd-yyyy | hh:mm a", Locale.getDefault());
            eventTime.setText(sdf.format(eventStart));
            registrationStart.setText(sdf.format(start));
            registrationEnd.setText(sdf.format(end));


            Glide.with(EditEventActivity.this).load(imageUri).centerCrop().placeholder(R.drawable.outline_photo_camera_24).into(eventPoster);

            eventDescriptionInput.setText(eventReceived.getDescription());
        }

        backButton.setOnClickListener(v -> {
            startActivity(new Intent(EditEventActivity.this, AddEventActivity.class));
            finish();
        });


        eventTime.setOnClickListener(v -> {
            if (!registrationStartPicked || !registrationEndPicked) {
                Toast.makeText(this, "Please pick registration start and end times first.", Toast.LENGTH_SHORT).show();
                return;
            }
            pickDateTime(eventTimeCalendar, "Start", start -> {
                // must be AFTER registration end
                if (start.before(endCalendar)) {
                    Toast.makeText(this, "Event start time must be after registration end.", Toast.LENGTH_SHORT).show();
                    return;
                }
                SimpleDateFormat sdf = new SimpleDateFormat("MMM-dd-yyyy | hh:mm a", Locale.getDefault());
                String period = sdf.format(start.getTime());
                eventTime.setText(period);
                eventTimeStartPicked = true;
            });
        });

        registrationStart.setOnClickListener(v ->
                pickDateTime(startCalendar, "Start", start -> {
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM-dd-yyyy | hh:mm a", Locale.getDefault());
                    String period = sdf.format(start.getTime());
                    registrationStart.setText(period);
                    registrationStartPicked = true;
                })
        );

        registrationEnd.setOnClickListener(v -> {
            if (!registrationStartPicked) {
                Toast.makeText(this, "Please pick the registration start time first.", Toast.LENGTH_SHORT).show();
                return;
            }
            pickDateTime(endCalendar, "End", end -> {
                if (end.before(startCalendar)) {
                    Toast.makeText(this, "Registration end must be after registration start.", Toast.LENGTH_SHORT).show();
                    return;
                }
                SimpleDateFormat sdf = new SimpleDateFormat("MMM-dd-yyyy | hh:mm a", Locale.getDefault());
                String period = sdf.format(end.getTime());
                registrationEnd.setText(period);
                registrationEndPicked = true;
            });
        });

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

            if (!eventTimeStartPicked || !registrationStartPicked || !registrationEndPicked) {
                Toast.makeText(this, "Please fill out your event detail times", Toast.LENGTH_SHORT).show();
                return;
            }

            // needs firebase storage to implement
            String eventPhotoURL = null;

            Date eventTimeSave = eventTimeCalendar.getTime();
            Date registrationStartSave = startCalendar.getTime();
            Date registrationEndSave = endCalendar.getTime();


            Event eventToSave = new Event(eventName,eventCapacityInt,entrantLimit,geolocationRequirement, eventTimeSave, registrationStartSave,registrationEndSave, eventDescription,eventPhotoURL);

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

    /**
     * Method for allowing user to select a file from their device to set as an image for the event
     */
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

    /**
     * Handles the logic for the date and time select pop ups for the event start, registration start,
     * and registration ends times.
     * @param calendar A calender object from the Java Calender class
     * @param title Says whether its a start or end time
     * @param callback Runs the lambda code from when we called pickDateTime earlier when callback is called
     */
    private void pickDateTime(Calendar calendar, String title, DateTimePickedCallback callback) {
        Calendar now = Calendar.getInstance();

        DatePickerDialog datePicker = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);

                    TimePickerDialog timePicker = new TimePickerDialog(this,
                            (timeView, hourOfDay, minute) -> {
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);

                                if (calendar.getTimeInMillis() <= now.getTimeInMillis()) {
                                    Toast.makeText(this, "Please select a time in the future.", Toast.LENGTH_SHORT).show();
                                } else {
                                    callback.onDateTimePicked(calendar);
                                }
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
        // buffer just in case
        datePicker.getDatePicker().setMinDate(System.currentTimeMillis()-1000);

        datePicker.setTitle(title + " Date");
        datePicker.show();
    }

    interface DateTimePickedCallback {
        void onDateTimePicked(Calendar calendar);
    }
}