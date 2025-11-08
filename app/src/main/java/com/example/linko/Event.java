package com.example.linko;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This is for Event objects. Stores all the necessary info for events as well as
 * lists for managing entrants. Has an empty constructor for Firebase.
 * <p>
 *     There are two separate constructors in this class. One for when the
 *     Firebase ownerId is known before creation and then the main constructor
 *     that's used more often where the ownerId isn't known ahead of time.
 * </p>
 * @see EditEventActivity
 * @see EventDatabaseHandler
 * @see AddEventActivity
 */
public class Event implements Serializable {
    private String ownerId;
    private String name;
    private Integer eventCapacity;
    private Integer entrantLimit;
    private boolean geolocationRequired;
    private Date eventTime;
    private Date registrationStart;
    private Date registrationEnd;
    private String description;
    private String guidelines;
    private String eventPosterURL;
    private List<String> entrants;
    private List<String> invitedEntrants;
    private List<String> signedUpEntrants;
    private List<String> cancelledEntrants;
    private String eventId;


    public Event() {
        this.entrants = new ArrayList<>();
        this.invitedEntrants = new ArrayList<>();
        this.signedUpEntrants = new ArrayList<>();
        this.cancelledEntrants = new ArrayList<>();
    }

    /**
     * Event info object. On constructor call, makes new entrants, invitedEntrants,
     * signedUpEntrants, and cancelledEntrants arrayLists.
     * <p>
     *     ownerId and eventId are added
     *     added later when info is pulled from Firebase database
     * </p>
     * @param ownerId Owner ID
     * @param name Event name
     * @param eventCapacity Entrant capacity for event
     * @param entrantLimit:
     * @param geolocationRequired Boolean used to check if geolocation is necessary for event
     * @param eventTime What time the event takes place
     * @param registrationStart When registration for the event starts
     * @param registrationEnd When event registration closes
     * @param description Event description
     * @param eventPosterURL Firebase URL for the events poster
     * @param eventId Event ID
     */
    public Event(String ownerId, String name, Integer eventCapacity, Integer entrantLimit, boolean geolocationRequired, Date eventTime, Date registrationStart, Date registrationEnd, String description, String guidelines, String eventPosterURL, String eventId) {
        this.ownerId = ownerId;
        this.name = name;
        this.eventCapacity = eventCapacity;
        this.entrantLimit = entrantLimit;
        this.geolocationRequired = geolocationRequired;
        this.eventTime = eventTime;
        this.registrationStart = registrationStart;
        this.registrationEnd = registrationEnd;
        this.description = description;
        this.guidelines = guidelines;
        this.eventPosterURL = eventPosterURL;
        this.entrants = new ArrayList<>();
        this.invitedEntrants = new ArrayList<>();
        this.signedUpEntrants = new ArrayList<>();
        this.cancelledEntrants = new ArrayList<>();
        this.eventId = eventId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getEventCapacity() {
        return eventCapacity;
    }

    public void setEventCapacity(Integer eventCapacity) {
        this.eventCapacity = eventCapacity;
    }

    public Integer getEntrantLimit() {
        return entrantLimit;
    }

    public void setEntrantLimit(Integer entrantLimit) {
        this.entrantLimit = entrantLimit;
    }

    public boolean isGeolocationRequired() {
        return geolocationRequired;
    }

    public void setGeolocationRequired(boolean geolocationRequired) {
        this.geolocationRequired = geolocationRequired;
    }

    public Date getEventTime() {
        return eventTime;
    }

    public void setEventTime(Date eventTime) {
        this.eventTime = eventTime;
    }

    public Date getRegistrationStart() {
        return registrationStart;
    }

    public void setRegistrationStart(Date registrationStart) {
        this.registrationStart = registrationStart;
    }

    public Date getRegistrationEnd() {
        return registrationEnd;
    }

    public void setRegistrationEnd(Date registrationEnd) {
        this.registrationEnd = registrationEnd;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGuidelines() {
        return guidelines;
    }

    public void setGuidelines(String guidelines) {
        this.guidelines = guidelines;
    }

    public String getEventPosterURL() {
        return eventPosterURL;
    }

    public void setEventPosterURL(String eventPosterURL) {
        this.eventPosterURL = eventPosterURL;
    }

    public List<String> getEntrants() {
        return entrants;
    }

    public void setEntrants(List<String> entrants) {
        this.entrants = entrants;
    }

    public List<String> getInvitedEntrants() {
        return invitedEntrants;
    }

    public void setInvitedEntrants(List<String> invitedEntrants) {
        this.invitedEntrants = invitedEntrants;
    }

    public List<String> getSignedUpEntrants() {
        return signedUpEntrants;
    }

    public void setSignedUpEntrants(List<String> signedUpEntrants) {
        this.signedUpEntrants = signedUpEntrants;
    }

    public List<String> getCancelledEntrants() {
        return cancelledEntrants;
    }

    public void setCancelledEntrants(List<String> cancelledEntrants) {
        this.cancelledEntrants = cancelledEntrants;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEntrantCount() {
        return String.valueOf(entrants.size());
    }
}
