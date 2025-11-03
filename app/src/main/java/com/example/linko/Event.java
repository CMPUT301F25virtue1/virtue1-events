package com.example.linko;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Event implements Serializable {
    private String ownerId;
    private String name;
    private Integer eventCapacity;
    private Integer entrantLimit;
    private boolean geolocationRequired;
    private String eventLocation;
    private String eventTime;
    private Date registrationStart;
    private Date registrationEnd;
    private String description;
    private String eventPosterURL;
    private List<String> entrants;
    private List<String> invitedEntrants;
    private List<String> signedUpEntrants;
    private List<String> cancelledEntrants;
    private String eventId;


    public Event() {}
    public Event(String name, Integer eventCapacity, Integer entrantLimit, Boolean geolocationRequired, String eventLocation, String eventTime, Date registrationStart, Date registrationEnd, String description, String eventPosterURL) {
        this.ownerId = null;
        this.name = name;
        this.eventCapacity = eventCapacity;
        this.entrantLimit = entrantLimit;
        this.geolocationRequired = geolocationRequired;
        this.eventLocation = eventLocation;
        this.eventTime = eventTime;
        this.registrationStart = registrationStart;
        this.registrationEnd = registrationEnd;
        this.description = description;
        this.eventPosterURL = eventPosterURL;
        this.entrants = new ArrayList<>();
        this.invitedEntrants = new ArrayList<>();
        this.signedUpEntrants = new ArrayList<>();
        this.cancelledEntrants = new ArrayList<>();
        this.eventId = null;
    }
    public Event(String ownerId, String name, Integer eventCapacity, Integer entrantLimit, Boolean geolocationRequired, String eventLocation, String eventTime, Date registrationStart, Date registrationEnd, String description, String eventPosterURL) {
        this.ownerId = ownerId;
        this.name = name;
        this.eventCapacity = eventCapacity;
        this.entrantLimit = entrantLimit;
        this.geolocationRequired = geolocationRequired;
        this.eventLocation = eventLocation;
        this.eventTime = eventTime;
        this.registrationStart = registrationStart;
        this.registrationEnd = registrationEnd;
        this.description = description;
        this.eventPosterURL = eventPosterURL;
        this.entrants = new ArrayList<>();
        this.invitedEntrants = new ArrayList<>();
        this.signedUpEntrants = new ArrayList<>();
        this.cancelledEntrants = new ArrayList<>();
        this.eventId = null;
    }

    public Event(String ownerId, String name, Integer eventCapacity, Integer entrantLimit, boolean geolocationRequired, String eventLocation, String eventTime, Date registrationStart, Date registrationEnd, String description, String eventPosterURL, String eventId) {
        this.ownerId = ownerId;
        this.name = name;
        this.eventCapacity = eventCapacity;
        this.entrantLimit = entrantLimit;
        this.geolocationRequired = geolocationRequired;
        this.eventLocation = eventLocation;
        this.eventTime = eventTime;
        this.registrationStart = registrationStart;
        this.registrationEnd = registrationEnd;
        this.description = description;
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

    public String getName() {
        return name;
    }

    public Integer getEventCapacity() {
        return eventCapacity;
    }

    public Integer getEntrantLimit() {
        return entrantLimit;
    }

    public boolean isGeolocationRequired() {
        return geolocationRequired;
    }

    public String getEventLocation() {
        return eventLocation;
    }

    public String getEventTime() {
        return eventTime;
    }

    public Date getRegistrationStart() {
        return registrationStart;
    }

    public Date getRegistrationEnd() {
        return registrationEnd;
    }

    public String getDescription() {
        return description;
    }

    public String getEventPosterURL() {
        return eventPosterURL;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setEventPosterURL(String eventPosterURL) {
        this.eventPosterURL = eventPosterURL;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public List<String> getEntrants() {
        return entrants;
    }

    public String getEntrantCount() {
        return String.valueOf(entrants.size());
    }
}
