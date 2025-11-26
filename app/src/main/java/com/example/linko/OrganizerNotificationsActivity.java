package com.example.linko;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

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
        Event eventReceived = (Event) getIntent().getSerializableExtra("event");
        Log.d("notificationactivity", listToNotify.toString());
        sendNotification.setOnClickListener(v -> {
            db = FirebaseFirestore.getInstance();
            notifsRef = db.collection("notifications");
            DocumentReference docRef = notifsRef.document();
            String notifId = docRef.getId();
            String message = customMessage.getText().toString().trim();
            UserNotification notificationToSend = new UserNotification(notifId, eventReceived.getEventId(), message, "custom");
            // add notif to db
            docRef.set(notificationToSend).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d("notification", "successfully added to database" + notifId + " " + notificationToSend.getNotificationId());
                    // add notif to the users of the list received
                    for (User user : listToNotify) {
                        Log.d("notification", "user list before:" + user.getNotificationList().toString());
                        user.getNotificationList().add(notifId);
                        user.getLocalAndroidNotificationlist().add(notifId);
                        Log.d("notification", "Adding notification" + notifId + "to user" + user.getUserId() + user.getNotificationList().toString());
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

            finish();
        });


        backButton.setOnClickListener(v -> {
            finish();
        });
    }
}