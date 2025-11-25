package com.example.linko;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.File;
import java.io.FileWriter;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * This is the class for handling the event details for the organizer logic that interacts with the UI.
 */
public class OrganizerEventDetailsActivity extends AppCompatActivity {

    private Uri imageUri;
    private List<User> totalEntrantsList;
    private List<User> invitedEntrantsList;
    private List<User> signedUpEntrantsList;
    private List<User> cancelledEntrantsList;

    private FirebaseFirestore db;
    private CollectionReference usersRef;

    private UserRecyclerAdapter entrantsUserRecyclerAdapter;
    private UserRecyclerAdapter invitedEntrantsUserRecyclerAdapter;
    private UserRecyclerAdapter signedUpEntrantsUserRecyclerAdapter;
    private UserRecyclerAdapter cancelledEntrantsUserRecyclerAdapter;

    private Event eventReceived;

    // to keep track of which list to send notifications to in the system tab (0 = invited, 1 = signedup, 2 = cancelled)
    private int currentClicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_organizer_event_details);

        currentClicked = 0;

        // top bar
        ImageView backButton = findViewById(R.id.button_back_button);
        Button eventDetails = findViewById(R.id.button_event);
        Button totalEntrants = findViewById(R.id.button_entrants);
        Button system = findViewById(R.id.button_system);

        // event tab ui
        ConstraintLayout eventDetailsContainer = findViewById(R.id.event_details_container);
        TextView eventName = findViewById(R.id.text_event_name);
        TextView eventCapacity = findViewById(R.id.text_event_capacity);
        TextView entrantCount = findViewById(R.id.text_entrant_count);
        CheckBox geolocationCheck = findViewById(R.id.checkBox);

        TextView eventTime = findViewById(R.id.text_event_start_time);
        TextView registrationStart = findViewById(R.id.text_event_registration_start);
        TextView registrationEnd = findViewById(R.id.text_event_registration_end);

        TextView eventDescription = findViewById(R.id.text_event_description);
        TextView eventGuidelines = findViewById(R.id.text_event_guidelines);
        TextView descriptionButton = findViewById(R.id.click_event_description);
        TextView posterButton = findViewById(R.id.click_event_poster);
        TextView guidelinesButton = findViewById(R.id.click_event_guidelines);

        ImageView eventPoster = findViewById(R.id.image_event_poster);
        Button editEvent = findViewById(R.id.button_edit_event);

        // entrants tab ui
        ConstraintLayout entrantsContainer = findViewById(R.id.event_entrants_container);
        TextView noEntrants = findViewById(R.id.text_no_entrants);
        Button sendNotificationAll = findViewById(R.id.button_send_notification);
        Button exportCsvButton = findViewById(R.id.button_export_csv);

        // system tab ui
        ConstraintLayout systemContainer = findViewById(R.id.system_container);
        ConstraintLayout notYetSampled = findViewById(R.id.container_not_sampled);
        Button sampleButton = findViewById(R.id.sample_button);
        Button invited = findViewById(R.id.button_invited);
        Button signedUp = findViewById(R.id.button_signed_up);
        Button cancelled = findViewById(R.id.button_cancelled);
        Button sendNotificationSystem = findViewById(R.id.button_send_notification_system);

        // total entrants recycler view
        RecyclerView entrantsRecyclerView = findViewById(R.id.recycler_event_entrants);
        totalEntrantsList = new ArrayList<>();
        entrantsUserRecyclerAdapter = new UserRecyclerAdapter(totalEntrantsList, false);
        entrantsRecyclerView.setAdapter(entrantsUserRecyclerAdapter);
        LinearLayoutManager entrantsLayoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        entrantsRecyclerView.setLayoutManager(entrantsLayoutManager);

        // system recycler views
        RecyclerView invitedEntrantsRecyclerView = findViewById(R.id.recycler_invited_entrants);
        RecyclerView signedUpEntrantsRecyclerView = findViewById(R.id.recycler_signed_up_entrants);
        RecyclerView cancelledEntrantsRecyclerView = findViewById(R.id.recycler_cancelled_entrants);

        invitedEntrantsList = new ArrayList<>();
        signedUpEntrantsList = new ArrayList<>();
        cancelledEntrantsList = new ArrayList<>();

        invitedEntrantsUserRecyclerAdapter = new UserRecyclerAdapter(invitedEntrantsList, true);
        signedUpEntrantsUserRecyclerAdapter = new UserRecyclerAdapter(signedUpEntrantsList, true);
        cancelledEntrantsUserRecyclerAdapter = new UserRecyclerAdapter(cancelledEntrantsList, true);

        invitedEntrantsRecyclerView.setAdapter(invitedEntrantsUserRecyclerAdapter);
        signedUpEntrantsRecyclerView.setAdapter(signedUpEntrantsUserRecyclerAdapter);
        cancelledEntrantsRecyclerView.setAdapter(cancelledEntrantsUserRecyclerAdapter);

        LinearLayoutManager invitedEntrantsLayoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        LinearLayoutManager signedUpEntrantsLayoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        LinearLayoutManager cancelledEntrantsLayoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        invitedEntrantsRecyclerView.setLayoutManager(invitedEntrantsLayoutManager);
        signedUpEntrantsRecyclerView.setLayoutManager(signedUpEntrantsLayoutManager);
        cancelledEntrantsRecyclerView.setLayoutManager(cancelledEntrantsLayoutManager);

        // Get event
        eventReceived = (Event) getIntent().getSerializableExtra("clickedEvent");
        if (eventReceived == null) {
            Log.e("Event", "The event clicked was null.");
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();
        usersRef = db.collection("users");

        // Listen for entrants
        usersRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", error.toString());
            }
            if (value != null && !value.isEmpty()) {
                Log.d("firebase", "checking documents");
                totalEntrantsList.clear();
                invitedEntrantsList.clear();
                signedUpEntrantsList.clear();
                cancelledEntrantsList.clear();

                for (QueryDocumentSnapshot snapshot : value) {
                    List<String> userRegisteredEvents =
                            (List<String>) snapshot.get("eventsRegistered");
                    User userToAdd = snapshot.toObject(User.class);

                    // if user is not registered in this event -> skip
                    if (userRegisteredEvents == null
                            || !userRegisteredEvents.contains(eventReceived.getEventId())) {
                        Log.d("system", "SKIPPED " + userToAdd.getUserId()
                                + eventReceived.getInvitedEntrants());
                        continue;
                    }

                    if (eventReceived.getInvitedEntrants().contains(userToAdd.getUserId())) {
                        invitedEntrantsList.add(userToAdd);
                    }
                    if (eventReceived.getCancelledEntrants().contains(userToAdd.getUserId())) {
                        cancelledEntrantsList.add(userToAdd);
                    }
                    if (eventReceived.getSignedUpEntrants().contains(userToAdd.getUserId())) {
                        signedUpEntrantsList.add(userToAdd);
                    }
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

                // update systems tab
                if (!invitedEntrantsList.isEmpty()
                        || !signedUpEntrantsList.isEmpty()
                        || !cancelledEntrantsList.isEmpty()) {
                    notYetSampled.setVisibility(View.GONE);
                } else {
                    notYetSampled.setVisibility(View.VISIBLE);
                }

                exportCsvButton.setEnabled(!totalEntrantsList.isEmpty());
                exportCsvButton.setAlpha(totalEntrantsList.isEmpty() ? 0.5f : 1f);

                entrantsUserRecyclerAdapter.notifyDataSetChanged();
                invitedEntrantsUserRecyclerAdapter.notifyDataSetChanged();
                signedUpEntrantsUserRecyclerAdapter.notifyDataSetChanged();
                cancelledEntrantsUserRecyclerAdapter.notifyDataSetChanged();
            }
        });

        // Fill event info
        eventName.setText(eventReceived.getName());
        Integer eventCapacityNumber = eventReceived.getEventCapacity();
        String eventCapacityString = eventCapacityNumber.toString();
        eventCapacity.setText(eventCapacityString);
        Glide.with(this)
                .load(eventReceived.getEventPosterURL())
                .placeholder(R.drawable.outline_photo_camera_24)
                .centerCrop()
                .into(eventPoster);

        if (eventReceived.getEntrantLimit() != null) {
            entrantCount.setText(
                    eventReceived.getEntrantCount() + "/" + eventReceived.getEntrantLimit());
        } else {
            entrantCount.setText(eventReceived.getEntrantCount());
        }

        geolocationCheck.setChecked(eventReceived.isGeolocationRequired());

        Date eventStart = eventReceived.getEventTime();
        Date start = eventReceived.getRegistrationStart();
        Date end = eventReceived.getRegistrationEnd();
        SimpleDateFormat sdf =
                new SimpleDateFormat("MMM-dd-yyyy | hh:mm a", Locale.getDefault());
        eventTime.setText(sdf.format(eventStart));
        registrationStart.setText(sdf.format(start));
        registrationEnd.setText(sdf.format(end));

        eventDescription.setText(eventReceived.getDescription());
        eventGuidelines.setText(eventReceived.getGuidelines());

        // Description/Poster/Guidelines toggles
        descriptionButton.setOnClickListener(v -> {
            eventDescription.setVisibility(View.VISIBLE);
            eventPoster.setVisibility(View.GONE);
            eventGuidelines.setVisibility(View.GONE);
            descriptionButton.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.blue)));
            posterButton.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerBlue)));
            guidelinesButton.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerBlue)));
        });

        posterButton.setOnClickListener(v -> {
            eventDescription.setVisibility(View.GONE);
            eventPoster.setVisibility(View.VISIBLE);
            eventGuidelines.setVisibility(View.GONE);
            descriptionButton.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerBlue)));
            posterButton.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.blue)));
            guidelinesButton.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerBlue)));
        });

        guidelinesButton.setOnClickListener(v -> {
            eventDescription.setVisibility(View.GONE);
            eventPoster.setVisibility(View.GONE);
            eventGuidelines.setVisibility(View.VISIBLE);
            descriptionButton.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerBlue)));
            posterButton.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerBlue)));
            guidelinesButton.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.blue)));
        });

        // Back button
        backButton.setOnClickListener(v -> {
            startActivity(new Intent(this, MyEventsActivity.class));
            finish();
        });

        // Edit poster
        editEvent.setOnClickListener(v -> {
            EditEventPosterDialog editDialog = EditEventPosterDialog.newInstance(eventReceived);
            editDialog.setOnPosterUpdatedListener(
                    newPosterUrl -> Glide.with(this)
                            .load(newPosterUrl)
                            .centerCrop()
                            .into(eventPoster));
            editDialog.show(getSupportFragmentManager(), "EditPosterDialog");
        });

        // system tab ui logic
        invited.setOnClickListener(v -> {
            invited.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.blue)));
            signedUp.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerBlue)));
            cancelled.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerBlue)));
            currentClicked = 0;
            invitedEntrantsRecyclerView.setVisibility(View.VISIBLE);
            signedUpEntrantsRecyclerView.setVisibility(View.GONE);
            cancelledEntrantsRecyclerView.setVisibility(View.GONE);
        });

        signedUp.setOnClickListener(v -> {
            invited.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerBlue)));
            signedUp.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.blue)));
            cancelled.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerBlue)));
            currentClicked = 1;
            invitedEntrantsRecyclerView.setVisibility(View.GONE);
            signedUpEntrantsRecyclerView.setVisibility(View.VISIBLE);
            cancelledEntrantsRecyclerView.setVisibility(View.GONE);
        });

        cancelled.setOnClickListener(v -> {
            invited.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerBlue)));
            signedUp.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkerBlue)));
            cancelled.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.blue)));
            currentClicked = 2;
            invitedEntrantsRecyclerView.setVisibility(View.GONE);
            signedUpEntrantsRecyclerView.setVisibility(View.GONE);
            cancelledEntrantsRecyclerView.setVisibility(View.VISIBLE);
        });

        // notification listeners
        sendNotificationAll.setOnClickListener(v -> {
            if (totalEntrantsList.isEmpty()) {
                return;
            }
            Intent intent =
                    new Intent(this, OrganizerNotificationsActivity.class);
            intent.putExtra("listToNotify", (Serializable) totalEntrantsList);
            intent.putExtra("event", eventReceived);
            startActivity(intent);
        });

        sendNotificationSystem.setOnClickListener(v -> {
            if (currentClicked == 0 && invitedEntrantsList.isEmpty()) {
                return;
            } else if (currentClicked == 1 && signedUpEntrantsList.isEmpty()) {
                return;
            } else if (currentClicked == 2 && cancelledEntrantsList.isEmpty()) {
                return;
            }

            Intent intent =
                    new Intent(this, OrganizerNotificationsActivity.class);
            intent.putExtra("event", eventReceived);

            if (currentClicked == 0) {
                intent.putExtra("listToNotify", (Serializable) invitedEntrantsList);
            } else if (currentClicked == 1) {
                intent.putExtra("listToNotify", (Serializable) signedUpEntrantsList);
            } else if (currentClicked == 2) {
                intent.putExtra("listToNotify", (Serializable) cancelledEntrantsList);
            }
            startActivity(intent);
        });

        // export csv button
        exportCsvButton.setOnClickListener(v -> {
            if (totalEntrantsList.isEmpty()) {
                Toast.makeText(this, "No entrants to export", Toast.LENGTH_SHORT).show();
                return;
            }
            exportEntrantsAsCsv(totalEntrantsList);
        });

        // Sampling
        sampleButton.setOnClickListener(v -> {
            SampleButtonHandler handler = new SampleButtonHandler();
            handler.sampling(eventReceived, new SampleButtonHandler.SampleCallback() {
                @Override
                public void onSuccess(int freeSpace,
                                      List<String> invited,
                                      List<String> signedUp) {
                    eventReceived.setInvitedEntrants(invited);
                    eventReceived.setSignedUpEntrants(signedUp);

                    Toast.makeText(OrganizerEventDetailsActivity.this,
                            "Sampling complete! " + invited.size() + " users invited!",
                            Toast.LENGTH_SHORT).show();

                    invitedEntrantsUserRecyclerAdapter.notifyDataSetChanged();
                    signedUpEntrantsUserRecyclerAdapter.notifyDataSetChanged();
                }

                @Override
                public void onFail(String error) {
                    Toast.makeText(OrganizerEventDetailsActivity.this,
                            "Sampling failed: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        });

        // top bar listeners
        eventDetails.setOnClickListener(v -> {
            eventDetailsContainer.setVisibility(View.VISIBLE);
            entrantsContainer.setVisibility(View.GONE);
            systemContainer.setVisibility(View.GONE);

            eventDetails.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white)));
            totalEntrants.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.lightBlue)));
            system.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.lightBlue)));
            eventDetails.setTextColor(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkestBlue)));
            totalEntrants.setTextColor(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkestBlueNotSelected)));
            system.setTextColor(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkestBlueNotSelected)));
        });

        totalEntrants.setOnClickListener(v -> {
            eventDetailsContainer.setVisibility(View.GONE);
            entrantsContainer.setVisibility(View.VISIBLE);
            systemContainer.setVisibility(View.GONE);

            eventDetails.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.lightBlue)));
            totalEntrants.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white)));
            system.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.lightBlue)));
            eventDetails.setTextColor(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkestBlueNotSelected)));
            totalEntrants.setTextColor(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkestBlue)));
            system.setTextColor(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkestBlueNotSelected)));
        });

        system.setOnClickListener(v -> {
            eventDetailsContainer.setVisibility(View.GONE);
            entrantsContainer.setVisibility(View.GONE);
            systemContainer.setVisibility(View.VISIBLE);

            eventDetails.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.lightBlue)));
            totalEntrants.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.lightBlue)));
            system.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white)));
            eventDetails.setTextColor(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkestBlueNotSelected)));
            totalEntrants.setTextColor(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkestBlueNotSelected)));
            system.setTextColor(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.darkestBlue)));
        });

        // QR code button
        findViewById(R.id.button_qr_code).setOnClickListener(v -> {
            String eventId = eventReceived.getEventId();
            QRCodeDialog dialog = QRCodeDialog.newInstance(eventId);
            dialog.show(getSupportFragmentManager(), "QRCodeDialog");
        });

        // Swipe to delete invited users
        ItemTouchHelper.SimpleCallback simpleCallback =
                new ItemTouchHelper.SimpleCallback(0,
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView,
                                          @NonNull RecyclerView.ViewHolder viewHolder,
                                          @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder,
                                         int direction) {
                        int pos = viewHolder.getAbsoluteAdapterPosition();
                        User user = invitedEntrantsList.get(pos);

                        invitedEntrantsList.remove(user);
                        invitedEntrantsUserRecyclerAdapter.notifyItemRemoved(pos);

                        eventReceived.getInvitedEntrants().remove(user.getUserId());
                        FirebaseFirestore.getInstance()
                                .collection("events")
                                .document(eventReceived.getEventId())
                                .update("invitedEntrants", eventReceived.getInvitedEntrants());

                        Toast.makeText(OrganizerEventDetailsActivity.this,
                                "Removed user " + user.getFirstName() + " " + user.getLastName()
                                        + " from invited entrants.",
                                Toast.LENGTH_SHORT).show();
                    }
                };
        new ItemTouchHelper(simpleCallback)
                .attachToRecyclerView(invitedEntrantsRecyclerView);
    }

    // csv export
    private void exportEntrantsAsCsv(List<User> entrants) {
        StringBuilder csv = new StringBuilder();
        csv.append("First Name,Last Name,Email,Phone Number\n");

        for (User u : entrants) {
            String first = u.getFirstName() != null ? u.getFirstName() : "";
            String last = u.getLastName() != null ? u.getLastName() : "";
            String email = u.getEmail() != null ? u.getEmail() : "";
            String phone = (u.getPhone() != null && !u.getPhone().isEmpty())
                    ? u.getPhone()
                    : "N/A";

            csv.append(first).append(",");
            csv.append(last).append(",");
            csv.append(email).append(",");
            csv.append(phone).append("\n");
        }

        try {
            File downloadsDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            File linkoFolder = new File(downloadsDir, "Linko");
            if (!linkoFolder.exists()) {
                linkoFolder.mkdirs();
            }

            String safeName = eventReceived.getName().replaceAll("[^a-zA-Z0-9_\\-]", "_");
            String fileName = "entrants_" + safeName + ".csv";
            File file = new File(linkoFolder, fileName);

            FileWriter writer = new FileWriter(file);
            writer.write(csv.toString());
            writer.close();

            Toast.makeText(this, "Exported to Downloads/Linko", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to export CSV", Toast.LENGTH_SHORT).show();
        }
    }
}
