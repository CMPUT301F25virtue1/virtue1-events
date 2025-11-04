package com.example.linko;

import static com.example.linko.NavigationBarHandler.navigationListener;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.service.autofill.UserData;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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
            Glide.with(ProfileActivity.this).load(currentUser.getProfileUrl()).circleCrop().placeholder(R.drawable.outline_person_24).into(userProfile);
        });

        editProfile.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));
            finish();
        });

        navigationListener(this);
    }
}