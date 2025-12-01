package com.example.linko;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

/**
 * This class contains the logic for sending entrants notification from the organizer of the event
 * when the organizer clicks the send notifications button in the OrganizerEventDetails activity.
 *
 */
public class OrganizerNotificationsActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private CollectionReference notifsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_organizer_notifications);

        Button sendNotification = findViewById(R.id.button_send_notification);
        ImageView backButton = findViewById(R.id.button_back_button);
        EditText customMessage = findViewById(R.id.input_custom_message);

        List<User> listToNotify = (List<User>) getIntent().getSerializableExtra("listToNotify");
        String eventIdReceived = getIntent().getStringExtra("eventId");
        sendNotification.setOnClickListener(v -> {
            db = FirebaseFirestore.getInstance();
            notifsRef = db.collection("notifications");
            DocumentReference docRef = notifsRef.document();
            String notifId = docRef.getId();
            String message = customMessage.getText().toString().trim();
            UserNotification notificationToSend = new UserNotification(notifId, eventIdReceived, message, "custom");
            // add notif to db
            docRef.set(notificationToSend).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // add notif to the users of the list received
                    for (User user : listToNotify) {
                        if (!user.isNotificationsEnabled()) {
                            return;
                        }
                        user.getNotificationList().add(notifId);
                        user.getLocalAndroidNotificationlist().add(notifId);
                        new UserDatabaseHandler().addUser(user, new UserDatabaseHandler.UserAdded() {
                            @Override
                            public void userAdd() {
                                Log.d("notification", "successfully added to user list in database");
                            }

                            @Override
                            public void userFailedToAdd(Exception e) {
                                Log.e("notification", "error adding notif to user list in database");
                            }
                        });
                    }
                }
                else {
                    Log.e("notification", "Error adding notification to database", task.getException());
                }
            });
            Toast.makeText(OrganizerNotificationsActivity.this, "Successfully sent notifications!", Toast.LENGTH_SHORT).show();
            finish();
        });


        backButton.setOnClickListener(v -> {
            finish();
        });
    }
}