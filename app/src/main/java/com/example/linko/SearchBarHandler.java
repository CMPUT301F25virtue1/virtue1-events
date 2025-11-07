package com.example.linko;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains the logic for the search bar functionality. Lets the user search for certain events
 * and users.
 */
public class SearchBarHandler {

    // Search events

    /**
     * Searches for Events
     * @param eventsToSearchThrough List of all possible events
     * @param userSearchInput The users input for the search
     * @return Returns a list of events that fit the criteria
     */
    public static List<Event> eventSearchHandler(List<Event> eventsToSearchThrough, String userSearchInput) {
        List<Event> results = new ArrayList<>();
        if (userSearchInput == null || userSearchInput.isEmpty()) {
            results.addAll(eventsToSearchThrough);
            return results;
        }

        String query = userSearchInput.toLowerCase();
        for (Event e : eventsToSearchThrough) {
            if (e.getName().toLowerCase().contains(query) || e.getDescription().toLowerCase().contains(query)) {
                results.add(e);
            }
        }
        return results;
    }

    // Search users

    /**
     * Searches for users
     * @param usersToSearchThrough List of all possible users
     * @param userSearchInput The users input for the search
     * @return Returns a list of users that fit the criteria
     */
    public static List<User> userSearchHandler(List<User> usersToSearchThrough, String userSearchInput) {
        List<User> results = new ArrayList<>();
        if (userSearchInput == null || userSearchInput.isEmpty()) {
            results.addAll(usersToSearchThrough);
            return results;
        }

        String query = userSearchInput.toLowerCase();
        for (User u : usersToSearchThrough) {
            String fullName = (u.getFirstName() + " " + u.getLastName()).toLowerCase();
            if (fullName.contains(query) || u.getEmail().toLowerCase().contains(query)) {
                results.add(u);
            }
        }
        return results;
    }
}
