package com.example.linko;

import java.util.ArrayList;
import java.util.List;

public class SearchBarHandler {

    // Search events
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
