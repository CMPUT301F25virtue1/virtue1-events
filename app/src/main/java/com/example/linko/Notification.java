package com.example.linko;

import java.io.Serializable;

/**
 * Model class for storing notification logs
 */
public class Notification implements Serializable {
    private String notificationId;
    private String eventId;
    private String message;
    private String type; // "cancelled", "invited", "custom"

    public Notification() {
        // Empty constructor for Firebase
    }

    public Notification(String notificationId, String eventId, String message, String type) {
        this.notificationId = notificationId;
        this.eventId = eventId;
        this.message = message;
        this.type = type;
    }

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
}