package com.example.linko;

import static com.example.linko.NavigationBarHandler.navigationListener;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that contains all the logic for UI interaction with the notifications tab of the app
 */
public class NotificationsActivity extends AppCompatActivity {

    private List<UserNotification> notificationsList;
    private NotificationRecyclerAdapter notificationsRecyclerAdapter;
    private FirebaseFirestore db;
    private CollectionReference notifsRef;
    private CollectionReference usersRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        EdgeToEdge.enable(this);

        RecyclerView notificationsRecyclerView = findViewById(R.id.recycler_notifications);
        TextView noNotifications = findViewById(R.id.text_no_notifications);

        notificationsList = new ArrayList<>();
        notificationsRecyclerAdapter = new NotificationRecyclerAdapter(notificationsList);
        notificationsRecyclerView.setAdapter(notificationsRecyclerAdapter);

        LinearLayoutManager notificationsLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        notificationsRecyclerView.setLayoutManager(notificationsLayoutManager);

        db = FirebaseFirestore.getInstance();
        notifsRef = db.collection("notifications");
        usersRef = db.collection("users");

        usersRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", error.toString());
            }
            if (value != null && !value.isEmpty()) {
                new UserDatabaseHandler().getCurrentUser(NotificationsActivity.this, new UserDatabaseHandler.UserFetched() {
                    @Override
                    public void userLoaded(User user) {
                        notificationsList.clear();

                        notifsRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
                            for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                                UserNotification notificationToAdd = snapshot.toObject(UserNotification.class);

                                if (user.getNotificationList().contains(notificationToAdd.getNotificationId())) {
                                    notificationsList.add(notificationToAdd);
                                }
                            }

                            notificationsRecyclerAdapter.notifyDataSetChanged();
                            if (notificationsList.isEmpty()) {
                                noNotifications.setVisibility(View.VISIBLE);
                                notificationsRecyclerView.setVisibility(View.GONE);
                            } else {
                                noNotifications.setVisibility(View.GONE);
                                notificationsRecyclerView.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                });
            }
        });

        // go to the event details on click of each recycler view  item
        notificationsRecyclerAdapter.setOnItemClickListener(position -> {
            UserNotification notificationFromEvent = notificationsList.get(position);
            new EventDatabaseHandler().fetchEventById(notificationFromEvent.getEventId(), new EventDatabaseHandler.EventFetched() {
                @Override
                public void eventFetch(Event event) {
                    Intent intent = new Intent(NotificationsActivity.this, EventDetailsActivity.class);
                    intent.putExtra("clickedEvent", event);
                    intent.putExtra("activity", "notifications");
                    startActivity(intent);
                    finish();
                }

                @Override
                public void eventFetchFailed(Exception e) {

                }
            });
        });


        // check app notification permissions
        // https://developer.android.com/training/permissions/requesting
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            // when the user is in the app, start sending out notifs if they have any (this also listens if a notif is sent out while the user is already in the app)
            Intent serviceIntent = new Intent(this, NotificationListenerService.class);
            ContextCompat.startForegroundService(this, serviceIntent);

            // update the notification boolean in the database if the user enabled it OUT of the app
            new UserDatabaseHandler().getCurrentUser(NotificationsActivity.this, new UserDatabaseHandler.UserFetched() {
                @Override
                public void userLoaded(User user) {
                    user.setNotificationsEnabled(true);
                    new UserDatabaseHandler().addUser(user, new UserDatabaseHandler.UserAdded() {
                        @Override
                        public void userAdd() {
                            // start the notification listener, which sends the local notifications
                            Intent serviceIntent = new Intent(NotificationsActivity.this, NotificationListenerService.class);
                            ContextCompat.startForegroundService(NotificationsActivity.this, serviceIntent);
                        }

                        @Override
                        public void userFailedToAdd(Exception e) {

                        }
                    });
                }
            });
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)) {
            // if user already denied before, don't do anything
        } else {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 527);
        }
        navigationListener(this);
    }

    // https://developer.android.com/training/permissions/requesting
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 527:
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // update the notification boolean in the database
                    new UserDatabaseHandler().getCurrentUser(NotificationsActivity.this, new UserDatabaseHandler.UserFetched() {
                        @Override
                        public void userLoaded(User user) {
                            user.setNotificationsEnabled(true);
                            new UserDatabaseHandler().addUser(user, new UserDatabaseHandler.UserAdded() {
                                @Override
                                public void userAdd() {
                                    // start the notification listener, which sends the local notifications
                                    Intent serviceIntent = new Intent(NotificationsActivity.this, NotificationListenerService.class);
                                    ContextCompat.startForegroundService(NotificationsActivity.this, serviceIntent);
                                }

                                @Override
                                public void userFailedToAdd(Exception e) {

                                }
                            });
                        }
                    });
                }  else {
                    Toast.makeText(NotificationsActivity.this, "You will not receive any notifications.", Toast.LENGTH_LONG).show();

                    // update the notification boolean in the database
                    new UserDatabaseHandler().getCurrentUser(NotificationsActivity.this, new UserDatabaseHandler.UserFetched() {
                        @Override
                        public void userLoaded(User user) {
                            user.setNotificationsEnabled(false);
                            new UserDatabaseHandler().addUser(user, new UserDatabaseHandler.UserAdded() {
                                @Override
                                public void userAdd() {
                                    // start the notification listener, which sends the local notifications
                                    Intent serviceIntent = new Intent(NotificationsActivity.this, NotificationListenerService.class);
                                    ContextCompat.startForegroundService(NotificationsActivity.this, serviceIntent);
                                }

                                @Override
                                public void userFailedToAdd(Exception e) {

                                }
                            });
                        }
                    });
                }
        }
    }
}