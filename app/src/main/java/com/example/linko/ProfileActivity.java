package com.example.linko;

import static com.example.linko.NavigationBarHandler.navigationListener;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.service.autofill.UserData;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Class that contains the logic for interactions with the UI for user profiles.
 * Gets the current user from the Firebase database using the UserDatabaseHandler.
 * Also calls the EditProfileActivity class to edit the profile.
 * @see UserDatabaseHandler
 * @see EditProfileActivity
 */
public class ProfileActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        EdgeToEdge.enable(this);

        TextView userName = findViewById(R.id.text_user_name);
        TextView userEmail = findViewById(R.id.text_user_email);
        TextView userPhoneNumber = findViewById(R.id.text_user_number);
        ImageView userProfile = findViewById(R.id.image_profile);
        Button editProfile = findViewById(R.id.button_edit_profile);

        // event history setup
        RecyclerView eventHistoryRecyclerView = findViewById(R.id.recycler_event_history);
        List<Event> eventHistoryList = new ArrayList<>();
        EventHistoryRecyclerAdapter eventHistoryRecyclerAdapter = new EventHistoryRecyclerAdapter(eventHistoryList);
        eventHistoryRecyclerView.setAdapter(eventHistoryRecyclerAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        eventHistoryRecyclerView.setLayoutManager(layoutManager);

        UserDatabaseHandler databaseHandler = new UserDatabaseHandler();
        databaseHandler.getCurrentUser(this, currentUser -> {
            String name = currentUser.getFirstName() + " " + currentUser.getLastName();
            String email = currentUser.getEmail();
            String number = currentUser.getPhone();
            if (number == null) {
                number = "N/A";
            }
            userName.setText(name);
            userEmail.setText(email);
            userPhoneNumber.setText(number);
            Glide.with(ProfileActivity.this).load(currentUser.getProfileUrl()).circleCrop().placeholder(R.drawable.outline_person_black_24).into(userProfile);

            // get events from db
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            CollectionReference eventsRef = db.collection("events");
            eventsRef.get().addOnSuccessListener(value -> {
                if (value != null && !value.isEmpty()) {
                    Log.d("firebase", "checking documents");
                    eventHistoryList.clear();
                    for (QueryDocumentSnapshot snapshot : value) {
                        Event eventToAdd = snapshot.toObject(Event.class);
                        if (currentUser.getEventHistory().contains(eventToAdd.getEventId())) {
                            eventHistoryList.add(eventToAdd);
                        }
                    }
                    eventHistoryRecyclerAdapter.notifyDataSetChanged();
                }
            }).addOnFailureListener(error -> {
                Log.e("firebase", error.toString());
            });
        });

        editProfile.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));
            finish();
        });

        navigationListener(this);
    }
}