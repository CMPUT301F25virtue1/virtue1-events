package com.example.linko;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class NotificationsActivity extends AppCompatActivity {

    private EditText messageInput;
    private Button sendButton;
    private TextView titleText;

    private String eventId;
    private String eventName;
    private String listType;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        db = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        eventId = intent.getStringExtra("eventId");
        eventName = intent.getStringExtra("eventName");
        listType = intent.getStringExtra("listType");

        initializeViews();

        ImageButton backButton = findViewById(R.id.button_back);
        backButton.setOnClickListener(v -> finish());
    }

    private void initializeViews() {
        titleText = findViewById(R.id.text_notification_title);
        messageInput = findViewById(R.id.edit_message_input);
        sendButton = findViewById(R.id.button_send_notification);

        String title = "Send Notification to " + getListTypeDisplay(listType) + " Entrants";
        titleText.setText(title);

        sendButton.setOnClickListener(v -> sendNotifications());
    }

    private String getListTypeDisplay(String type) {
        switch (type) {
            case "waiting": return "Waiting List";
            case "cancelled": return "Cancelled";
            case "invited": return "Invited";
            case "signedUp": return "Signed Up";
            default: return "";
        }
    }

    private void sendNotifications() {
        String message = messageInput.getText().toString().trim();

        if (message.isEmpty()) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
            return;
        }

        // Load and send to all entrants of the selected type
        db.collection("events").document(eventId)
                .collection("entrants")
                .whereEqualTo("status", listType)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int recipientCount = queryDocumentSnapshots.size();

                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Entrant entrant = document.toObject(Entrant.class);
                        if (entrant != null) {
                            sendNotificationToEntrant(entrant, message);
                        }
                    }

                    // Log the notification action
                    logNotificationAction(message, recipientCount);

                    // Show success dialog
                    showSuccessDialog(recipientCount);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to send notifications", Toast.LENGTH_SHORT).show();
                    Log.e("NotificationActivity", "Error sending notifications", e);
                });
    }

    private void showSuccessDialog(int recipientCount) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Notification Sent Successfully");
        builder.setMessage("✓ Sent to " + recipientCount + " " + getListTypeDisplay(listType).toLowerCase() + " entrants\n" +
                "✓ Event: " + eventName + "\n" +
                "✓ Message recorded in logs with timestamp\n" +
                "✓ Color-coded as: " + getNotificationColor(listType));

        builder.setPositiveButton("OK", (dialog, which) -> {
            finish(); // Close activity after success
        });

        builder.show();
    }

    private String getNotificationColor(String type) {
        switch (type) {
            case "waiting": return "blue";
            case "cancelled": return "red";
            case "invited": return "green";
            case "signedUp": return "green";
            default: return "blue";
        }
    }

    private void sendNotificationToEntrant(Entrant entrant, String message) {
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("userId", entrant.getId());
        notificationData.put("userName", entrant.getName());
        notificationData.put("userEmail", entrant.getEmail());
        notificationData.put("eventId", eventId);
        notificationData.put("eventName", eventName);
        notificationData.put("message", message);
        notificationData.put("type", listType);
        notificationData.put("timestamp", System.currentTimeMillis());
        notificationData.put("read", false);

        db.collection("notifications")
                .add(notificationData)
                .addOnFailureListener(e -> {
                    Log.e("Notification", "Error sending notification to " + entrant.getEmail(), e);
                });
    }

    private void logNotificationAction(String message, int recipientCount) {
        Map<String, Object> logData = new HashMap<>();
        logData.put("eventId", eventId);
        logData.put("eventName", eventName);
        logData.put("message", message);
        logData.put("type", listType);
        logData.put("recipientCount", recipientCount);
        logData.put("timestamp", System.currentTimeMillis());
        logData.put("status", "success");
        logData.put("color", getNotificationColor(listType));

        db.collection("notificationLogs")
                .add(logData)
                .addOnFailureListener(e -> {
                    Log.e("NotificationLog", "Error logging notification", e);
                });
    }
}