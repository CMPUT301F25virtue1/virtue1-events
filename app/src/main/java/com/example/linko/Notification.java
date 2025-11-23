package com.example.linko;

import java.io.Serializable;

/**
 * Model class for storing notification logs
 */
public class Notification implements Serializable {
    private String notificationId;
    private String eventId;
    private String eventName;
    private String message;
    private String type; // "cancelled", "invited", "custom"
    private long timestamp;
    private int recipientCount;
    private String status; // "success", "error"

    public Notification() {
        // Empty constructor for Firebase
    }

    public Notification(String eventId, String eventName, String message, String type, int recipientCount, String status) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.message = message;
        this.type = type;
        this.recipientCount = recipientCount;
        this.status = status;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getRecipientCount() {
        return recipientCount;
    }

    public void setRecipientCount(int recipientCount) {
        this.recipientCount = recipientCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
