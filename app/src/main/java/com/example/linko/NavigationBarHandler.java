package com.example.linko;

import android.app.Activity;
import android.content.Intent;
import android.widget.ImageView;

public abstract class NavigationBarHandler {
    public static void navigationListener(Activity activity) {
        // init all the navigation icons
        ImageView notifications = activity.findViewById(R.id.img_notification_icon);
        ImageView myEvents = activity.findViewById(R.id.img_my_events_icon);
        ImageView exploreEvents = activity.findViewById(R.id.img_explore_events_icon);
        ImageView profile = activity.findViewById(R.id.img_profile_icon);
        ImageView settings = activity.findViewById(R.id.img_settings_icon);

        notifications.setOnClickListener(v -> {
            activity.startActivity(new Intent(activity, NotificationsActivity.class));
            activity.finish();
        });

        myEvents.setOnClickListener(v -> {
            activity.startActivity(new Intent(activity, MyEventsActivity.class));
            activity.finish();

        });

        exploreEvents.setOnClickListener(v -> {
            activity.startActivity(new Intent(activity, ExploreEventsActivity.class));
            activity.finish();

        });

        profile.setOnClickListener(v -> {
            activity.startActivity(new Intent(activity, ProfileActivity.class));
            activity.finish();

        });

        settings.setOnClickListener(v -> {
            activity.startActivity(new Intent(activity, SettingsActivity.class));
            activity.finish();

        });

        ImageView clickedIcon = null;
        Class<?> currentActivity = activity.getClass();
        if (currentActivity == NotificationsActivity.class) {
            clickedIcon = notifications;
        }
        else if (currentActivity == MyEventsActivity.class) {
            clickedIcon = myEvents;
        }
        else if (currentActivity == ExploreEventsActivity.class) {
            clickedIcon = exploreEvents;
        }
        else if (currentActivity == ProfileActivity.class) {
            clickedIcon = profile;
        }
        else if (currentActivity == SettingsActivity.class) {
            clickedIcon = settings;
        }

        ImageView[] allIcons = new ImageView[] {notifications, myEvents, exploreEvents, profile, settings};
        setAlpha(clickedIcon, allIcons);
    }

    public static void setAlpha(ImageView highlight, ImageView[] icons) {
        // to "highlight" the selected icon
        for (ImageView icon : icons) {
            if (icon == highlight) {
                icon.setAlpha(1f);
                continue;
            }
            icon.setAlpha(0.5f);
        }
    }

}
