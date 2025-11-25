package com.example.linko;

import static androidx.core.app.ActivityCompat.requestPermissions;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

// https://developer.android.com/develop/background-work/services/fgs/declare
public class NotificationListenerService extends Service {
    private   String CHANNEL_ID_FOREGROUND = "LINKOFOREGROUND";
    private String CHANNEL_ID_USER_NOTIFS = "LINKONOTIFICATIONS";
    private FirebaseFirestore db;

    @Override
    public void onCreate() {
        super.onCreate();

        db = FirebaseFirestore.getInstance();

        createNotificationChannels();
        startForeground(1, buildForegroundNotification());


        db = FirebaseFirestore.getInstance();
        CollectionReference notifsRef = db.collection("notifications");

        // retrieve all notifs that were made when the user wasn't on the app, and send those
        notifsRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            new UserDatabaseHandler().getCurrentUser(NotificationListenerService.this, new UserDatabaseHandler.UserFetched() {
                @Override
                public void userLoaded(User user) {
                    for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                        UserNotification notificationToAdd = snapshot.toObject(UserNotification.class);
                        // if notification isn't in the current user's local android list, skip
                        if (!user.getLocalAndroidNotificationlist().contains(notificationToAdd.getNotificationId())) {
                            continue;
                        }
                        // show the notif otherwise
                        new EventDatabaseHandler().fetchEventById(notificationToAdd.getEventId(), new EventDatabaseHandler.EventFetched() {
                            @Override
                            public void eventFetch(Event event) {
                                showUserNotification(event.getName(), notificationToAdd.getMessage(), notificationToAdd.getNotificationId().hashCode());
                                user.getLocalAndroidNotificationlist().remove(notificationToAdd.getNotificationId());
                                new UserDatabaseHandler().addUser(user, new UserDatabaseHandler.UserAdded() {
                                    @Override
                                    public void userAdd() {

                                    }

                                    @Override
                                    public void userFailedToAdd(Exception e) {

                                    }
                                });

                            }

                            @Override
                            public void eventFetchFailed(Exception e) {

                            }
                        });
                    }
                }
            });
        });

        // if the user is on the app, WHILE an organizer sends a notif, get notifs
        notifsRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", error.toString());
            }
            if (value != null && !value.isEmpty()) {
                Log.d("firebase", "checking documents");

                new UserDatabaseHandler().getCurrentUser(NotificationListenerService.this, new UserDatabaseHandler.UserFetched() {
                    @Override
                    public void userLoaded(User user) {
                        for (QueryDocumentSnapshot snapshot : value) {
                            UserNotification notificationToAdd = snapshot.toObject(UserNotification.class);
                            if (!user.getLocalAndroidNotificationlist().contains(notificationToAdd.getNotificationId())) {
                                continue;
                            }
                            new EventDatabaseHandler().fetchEventById(notificationToAdd.getEventId(), new EventDatabaseHandler.EventFetched() {
                                @Override
                                public void eventFetch(Event event) {
                                    showUserNotification(event.getName(), notificationToAdd.getMessage(), notificationToAdd.getNotificationId().hashCode());
                                    user.getLocalAndroidNotificationlist().remove(notificationToAdd.getNotificationId());

                                    new UserDatabaseHandler().addUser(user, new UserDatabaseHandler.UserAdded() {
                                        @Override
                                        public void userAdd() {

                                        }

                                        @Override
                                        public void userFailedToAdd(Exception e) {

                                        }
                                    });

                                }

                                @Override
                                public void eventFetchFailed(Exception e) {

                                }
                            });
                        }
                    }
                });
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    // https://developer.android.com/develop/ui/views/notifications/channels
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "linko";
            String description = "local android notifications for linko events";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel userNotifChannel = new NotificationChannel(CHANNEL_ID_USER_NOTIFS, name, importance);
            userNotifChannel.setDescription(description);

            NotificationChannel foregroundChannel = new NotificationChannel(CHANNEL_ID_FOREGROUND, "linko foreground", NotificationManager.IMPORTANCE_LOW);
            foregroundChannel.setDescription("foreground listening");
            foregroundChannel.setSound(null, null);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(userNotifChannel);
            notificationManager.createNotificationChannel(foregroundChannel);
        }
    }

    private Notification buildForegroundNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID_FOREGROUND)
                .setContentTitle("Linko")
                .setContentText("Listening for new event notifications..")
                .setSmallIcon(R.drawable.outline_notifications_24)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();
    }

    private void showUserNotification(String title, String message, int notifId) {
        // if the users notif permissions are off, don't send anything
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID_USER_NOTIFS)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.outline_notifications_24)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(notifId, notification);
    }
}

