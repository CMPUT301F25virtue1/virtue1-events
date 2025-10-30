package com.example.linko;

import static com.example.linko.NavigationBarHandler.navigationListener;

import android.os.Bundle;
import android.provider.Settings;
import android.service.autofill.UserData;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class ProfileActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        String userId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        TextView userName = findViewById(R.id.text_user_name);
        TextView userEmail = findViewById(R.id.text_user_email);
        TextView userPhoneNumber = findViewById(R.id.text_user_number);

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
        });

        navigationListener(this);

    }
}