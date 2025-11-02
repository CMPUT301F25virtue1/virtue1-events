package com.example.linko;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Event implements Serializable {
    private String ownerID;
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

    public Event() {}
    public Event(String name, Integer eventCapacity, Integer entrantLimit, Boolean geolocationRequired, String eventLocation, String eventTime, Date registrationStart, Date registrationEnd, String description, String eventPosterURL) {
        this.ownerID = null;
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
    }
    public Event(String ownerID, String name, Integer eventCapacity, Integer entrantLimit, Boolean geolocationRequired, String eventLocation, String eventTime, Date registrationStart, Date registrationEnd, String description, String eventPosterURL) {
        this.ownerID = ownerID;
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
    }

    public String getOwnerID() {
        return ownerID;
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

    public void setOwnerID(String ownerID) {
        this.ownerID = ownerID;
    }

    public String getEntrantCount() {
        return String.valueOf(entrants.size());
    }
}
