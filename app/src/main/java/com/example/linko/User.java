package com.example.linko;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This is for our User objects. Comes with getters and setters for all variables.
 * @see SignUpActivity
 * @see UserDatabaseHandler
 */
public class User implements Serializable {
    private String userId ;
    private String firstName ;
    private String lastName;
    private String email;
    private String phone;
    private String profileUrl;
    private List<String> eventsRegistered;
    private List<String> eventHistory;
    private List<String> notificationList;
    private boolean isAdmin;

    // 🔹 NEW: optional location for this user
    private Double latitude;
    private Double longitude;

    public User() {
        // empty for firebase
    }

    /**
     * User object
     * @param userId Device id. Stored in Firebase
     * @param firstName Users first name
     * @param lastName Users last name
     * @param email Users Email
     * @param phone Users phone number
     * @param profileUrl Link to the pfp stored in Firebase
     */
    public User(String userId, String firstName, String lastName, String email, String phone, String profileUrl) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.profileUrl = profileUrl;
        this.eventsRegistered = new ArrayList<>();
        this.eventHistory = new ArrayList<>();
        this.notificationList = new ArrayList<>();
        this.isAdmin = false;
        this.latitude = null;
        this.longitude = null;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public List<String> getEventsRegistered() {
        return eventsRegistered;
    }

    public void setEventsRegistered(List<String> eventsRegistered) {
        this.eventsRegistered = eventsRegistered;
    }

    public List<String> getEventHistory() {
        return eventHistory;
    }

    public void setEventHistory(List<String> eventHistory) {
        this.eventHistory = eventHistory;
    }

    public List<String> getNotificationList() {
        return notificationList;
    }

    public void setNotificationList(List<String> notificationList) {
        this.notificationList = notificationList;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        this.isAdmin = admin;
    }

    // 🔹 NEW getters/setters for geolocation
    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(userId);
    }
}
