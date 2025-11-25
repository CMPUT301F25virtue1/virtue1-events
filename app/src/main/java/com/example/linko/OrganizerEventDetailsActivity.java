package com.example.linko;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
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
 * Handles organizer event details, entrants, CSV export, notifications, and system tools.
 */
public class OrganizerEventDetailsActivity extends AppCompatActivity {

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
    private int currentClicked; // 0 = invited, 1 = signed up, 2 = cancelled

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_organizer_event_details);

        currentClicked = 0;

        //---------------------------------------------
        // TOP BAR
        //---------------------------------------------
        ImageView backButton = findViewById(R.id.button_back_button);
        Button eventDetails = findViewById(R.id.button_event);
        Button totalEntrants = findViewById(R.id.button_entrants);
        Button system = findViewById(R.id.button_system);

        //---------------------------------------------
        // EVENT TAB UI
        //---------------------------------------------
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

        //---------------------------------------------
        // ENTRANTS TAB UI
        //---------------------------------------------
        ConstraintLayout entrantsContainer = findViewById(R.id.event_entrants_container);
        TextView noEntrants = findViewById(R.id.text_no_entrants);
        Button sendNotificationAll = findViewById(R.id.button_send_notification);

        Button exportCsvButton = findViewById(R.id.button_export_csv);

        //---------------------------------------------
        // SYSTEM TAB UI
        //---------------------------------------------
        ConstraintLayout systemContainer = findViewById(R.id.system_container);
        ConstraintLayout notYetSampled = findViewById(R.id.container_not_sampled);
        Button sampleButton = findViewById(R.id.sample_button);
        Button invited = findViewById(R.id.button_invited);
        Button signedUp = findViewById(R.id.button_signed_up);
        Button cancelled = findViewById(R.id.button_cancelled);
        Button sendNotificationSystem = findViewById(R.id.button_send_notification_system);

        //---------------------------------------------
        // RECYCLER VIEWS
        //---------------------------------------------
        RecyclerView entrantsRecyclerView = findViewById(R.id.recycler_event_entrants);
        totalEntrantsList = new ArrayList<>();
        entrantsUserRecyclerAdapter = new UserRecyclerAdapter(totalEntrantsList, false);
        entrantsRecyclerView.setAdapter(entrantsUserRecyclerAdapter);
        entrantsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

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

        invitedEntrantsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        signedUpEntrantsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cancelledEntrantsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //---------------------------------------------
        // GET EVENT PASSED IN
        //---------------------------------------------
        eventReceived = (Event) getIntent().getSerializableExtra("clickedEvent");
        if (eventReceived == null) {
            finish();
            return;
        }

        //---------------------------------------------
        // FIRESTORE LISTENER
        //---------------------------------------------
        db = FirebaseFirestore.getInstance();
        usersRef = db.collection("users");

        usersRef.addSnapshotListener((value, error) -> {
            if (value != null) {

                totalEntrantsList.clear();
                invitedEntrantsList.clear();
                signedUpEntrantsList.clear();
                cancelledEntrantsList.clear();

                for (QueryDocumentSnapshot snapshot : value) {
                    List<String> reg = (List<String>) snapshot.get("eventsRegistered");
                    User u = snapshot.toObject(User.class);

                    if (reg == null || !reg.contains(eventReceived.getEventId()))
                        continue;

                    if (eventReceived.getInvitedEntrants().contains(u.getUserId()))
                        invitedEntrantsList.add(u);

                    if (eventReceived.getCancelledEntrants().contains(u.getUserId()))
                        cancelledEntrantsList.add(u);

                    if (eventReceived.getSignedUpEntrants().contains(u.getUserId()))
                        signedUpEntrantsList.add(u);

                    totalEntrantsList.add(u);
                }

                noEntrants.setVisibility(totalEntrantsList.isEmpty() ? View.VISIBLE : View.GONE);
                entrantsRecyclerView.setVisibility(totalEntrantsList.isEmpty() ? View.GONE : View.VISIBLE);

                exportCsvButton.setEnabled(!totalEntrantsList.isEmpty());
                exportCsvButton.setAlpha(totalEntrantsList.isEmpty() ? 0.5f : 1f);

                notYetSampled.setVisibility(
                        invitedEntrantsList.isEmpty() &&
                                signedUpEntrantsList.isEmpty() &&
                                cancelledEntrantsList.isEmpty()
                                ? View.VISIBLE
                                : View.GONE
                );

                entrantsUserRecyclerAdapter.notifyDataSetChanged();
                invitedEntrantsUserRecyclerAdapter.notifyDataSetChanged();
                signedUpEntrantsUserRecyclerAdapter.notifyDataSetChanged();
                cancelledEntrantsUserRecyclerAdapter.notifyDataSetChanged();
            }
        });

        //---------------------------------------------
        // SET EVENT DETAILS
        //---------------------------------------------
        eventName.setText(eventReceived.getName());
        eventCapacity.setText(String.valueOf(eventReceived.getEventCapacity()));

        Glide.with(this)
                .load(eventReceived.getEventPosterURL())
                .centerCrop()
                .placeholder(R.drawable.outline_photo_camera_24)
                .into(eventPoster);

        if (eventReceived.getEntrantLimit() != null)
            entrantCount.setText(eventReceived.getEntrantCount() + "/" + eventReceived.getEntrantLimit());
        else
            entrantCount.setText(eventReceived.getEntrantCount());

        geolocationCheck.setChecked(eventReceived.isGeolocationRequired());

        SimpleDateFormat sdf = new SimpleDateFormat("MMM-dd-yyyy | hh:mm a", Locale.getDefault());
        eventTime.setText(sdf.format(eventReceived.getEventTime()));
        registrationStart.setText(sdf.format(eventReceived.getRegistrationStart()));
        registrationEnd.setText(sdf.format(eventReceived.getRegistrationEnd()));

        eventDescription.setText(eventReceived.getDescription());
        eventGuidelines.setText(eventReceived.getGuidelines());

        //---------------------------------------------
        // DESCRIPTION / POSTER / GUIDELINES SWITCHING
        //---------------------------------------------
        descriptionButton.setOnClickListener(v -> {
            eventDescription.setVisibility(View.VISIBLE);
            eventPoster.setVisibility(View.GONE);
            eventGuidelines.setVisibility(View.GONE);
        });

        posterButton.setOnClickListener(v -> {
            eventDescription.setVisibility(View.GONE);
            eventPoster.setVisibility(View.VISIBLE);
            eventGuidelines.setVisibility(View.GONE);
        });

        guidelinesButton.setOnClickListener(v -> {
            eventDescription.setVisibility(View.GONE);
            eventPoster.setVisibility(View.GONE);
            eventGuidelines.setVisibility(View.VISIBLE);
        });

        //---------------------------------------------
        // BACK BUTTON
        //---------------------------------------------
        backButton.setOnClickListener(v -> {
            startActivity(new Intent(this, MyEventsActivity.class));
            finish();
        });

        //---------------------------------------------
        // EDIT EVENT POSTER
        //---------------------------------------------
        editEvent.setOnClickListener(v -> {
            EditEventPosterDialog dialog = EditEventPosterDialog.newInstance(eventReceived);
            dialog.setOnPosterUpdatedListener(url ->
                    Glide.with(this).load(url).centerCrop().into(eventPoster)
            );
            dialog.show(getSupportFragmentManager(), "EditPosterDialog");
        });

        //---------------------------------------------
        // SYSTEM TAB — FILTER BUTTONS
        //---------------------------------------------


        signedUp.setOnClickListener(v -> {
            currentClicked = 1;
            invitedEntrantsRecyclerView.setVisibility(View.GONE);
            signedUpEntrantsRecyclerView.setVisibility(View.VISIBLE);
            cancelledEntrantsRecyclerView.setVisibility(View.GONE);
        });

        cancelled.setOnClickListener(v -> {
            currentClicked = 2;
            invitedEntrantsRecyclerView.setVisibility(View.GONE);
            signedUpEntrantsRecyclerView.setVisibility(View.GONE);
            cancelledEntrantsRecyclerView.setVisibility(View.VISIBLE);
        });

        //---------------------------------------------
        // SEND NOTIFICATION (ALL)
        //---------------------------------------------
        sendNotificationAll.setOnClickListener(v -> {
            if (totalEntrantsList.isEmpty()) return;

            Intent i = new Intent(this, OrganizerNotificationsActivity.class);
            i.putExtra("listToNotify", (Serializable) totalEntrantsList);
            i.putExtra("event", eventReceived);
            startActivity(i);
        });

        //---------------------------------------------
        // SEND NOTIFICATION (SYSTEM TAB)
        //---------------------------------------------
        sendNotificationSystem.setOnClickListener(v -> {
            List<User> target;
            if (currentClicked == 0) target = invitedEntrantsList;
            else if (currentClicked == 1) target = signedUpEntrantsList;
            else target = cancelledEntrantsList;

            if (target.isEmpty()) return;

            Intent i = new Intent(this, OrganizerNotificationsActivity.class);
            i.putExtra("event", eventReceived);
            i.putExtra("listToNotify", (Serializable) target);
            startActivity(i);
        });

        //---------------------------------------------
        // CSV EXPORT BUTTON
        //---------------------------------------------
        exportCsvButton.setOnClickListener(v -> {
            if (totalEntrantsList.isEmpty()) {
                Toast.makeText(this, "No entrants to export", Toast.LENGTH_SHORT).show();
                return;
            }
            exportEntrantsAsCsv(totalEntrantsList);
        });

        //---------------------------------------------
        // SAMPLE BUTTON (SYSTEM TAB)
        //---------------------------------------------
        sampleButton.setOnClickListener(v -> {
            SampleButtonHandler handler = new SampleButtonHandler();

            handler.sampling(eventReceived, new SampleButtonHandler.SampleCallback() {
                @Override
                public void onSuccess(int freeSpace, List<String> invited, List<String> signed) {
                    eventReceived.setInvitedEntrants(invited);
                    eventReceived.setSignedUpEntrants(signed);

                    Toast.makeText(
                            OrganizerEventDetailsActivity.this,
                            "Sampling complete! " + invited.size() + " users invited!",
                            Toast.LENGTH_SHORT).show();

                    invitedEntrantsUserRecyclerAdapter.notifyDataSetChanged();
                    signedUpEntrantsUserRecyclerAdapter.notifyDataSetChanged();
                }

                @Override
                public void onFail(String error) {
                    Toast.makeText(
                            OrganizerEventDetailsActivity.this,
                            "Sampling failed: " + error,
                            Toast.LENGTH_SHORT).show();
                }
            });
        });

        //---------------------------------------------
        // TAB SWITCHING
        //---------------------------------------------
        eventDetails.setOnClickListener(v -> {
            eventDetailsContainer.setVisibility(View.VISIBLE);
            entrantsContainer.setVisibility(View.GONE);
            systemContainer.setVisibility(View.GONE);
        });

        totalEntrants.setOnClickListener(v -> {
            eventDetailsContainer.setVisibility(View.GONE);
            entrantsContainer.setVisibility(View.VISIBLE);
            systemContainer.setVisibility(View.GONE);
        });

        system.setOnClickListener(v -> {
            eventDetailsContainer.setVisibility(View.GONE);
            entrantsContainer.setVisibility(View.GONE);
            systemContainer.setVisibility(View.VISIBLE);
        });

        //---------------------------------------------
        // QR DIALOG
        //---------------------------------------------
        findViewById(R.id.button_qr_code).setOnClickListener(v -> {
            QRCodeDialog dialog = QRCodeDialog.newInstance(eventReceived.getEventId());
            dialog.show(getSupportFragmentManager(), "QRCodeDialog");
        });
    }

    //----------------------------------------------------------
    // ⭐ NEW CSV EXPORT — SAVES TO DOWNLOADS/LINKO ⭐
    //----------------------------------------------------------
    private void exportEntrantsAsCsv(List<User> entrants) {
        StringBuilder csv = new StringBuilder();
        csv.append("Full Name,Email,UserID\n");

        for (User u : entrants) {
            String fullName = u.getFirstName() + " " + u.getLastName();
            csv.append(fullName).append(",");
            csv.append(u.getEmail()).append(",");
            csv.append(u.getUserId()).append("\n");
        }

        try {
            // Public downloads directory
            File downloadsDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            // "Linko" folder
            File linkoFolder = new File(downloadsDir, "Linko");
            if (!linkoFolder.exists()) {
                linkoFolder.mkdirs();
            }

            // File name
            String fileName = "entrants_" + eventReceived.getName() + ".csv";
            File file = new File(linkoFolder, fileName);

            // Write CSV
            FileWriter writer = new FileWriter(file);
            writer.write(csv.toString());
            writer.close();

            Toast.makeText(
                    this,
                    "Exported to Downloads/Linko",
                    Toast.LENGTH_LONG
            ).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to export CSV", Toast.LENGTH_SHORT).show();
        }
    }
}
