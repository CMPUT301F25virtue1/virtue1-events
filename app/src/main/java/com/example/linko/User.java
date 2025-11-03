package com.example.linko;

/**
 * This is for our User objects. Comes with getters and setters for all variables.
 * @see SignUpActivity Where new User objects are created
 * @see UserDatabaseHandler Manages User objects in the Firebase database
 */
public class User {
    private String userId ;
    private String firstName ;
    private String lastName;
    private String email;
    private String phone;
    private String profileUrl;

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
}
