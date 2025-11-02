package com.example.linko;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.service.autofill.UserData;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SignUpActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;

    private ImageView imageProfile;
    private EditText inputFirstName, inputLastName, inputEmail, inputPhone;
    private Button signUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        imageProfile = findViewById(R.id.image_profile);
        inputFirstName = findViewById(R.id.input_firstname);
        inputLastName = findViewById(R.id.input_lastname);
        inputEmail = findViewById(R.id.input_email);
        inputPhone = findViewById(R.id.input_phonenumber);
        signUpButton = findViewById(R.id.button_signup);

        imageProfile.setOnClickListener(v -> openFileChooser());

        signUpButton.setOnClickListener(v -> handleSignUp());
    }

    private void openFileChooser() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");

        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                imageUri = data.getData();
                Glide.with(SignUpActivity.this).load(imageUri).circleCrop().into(imageProfile);
            }
        }
    }

    private void handleSignUp() {
        String firstName = inputFirstName.getText().toString().trim();
        String lastName = inputLastName.getText().toString().trim();
        String email = inputEmail.getText().toString().trim();
        String phone = inputPhone.getText().toString().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Please fill out all required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        String phoneNumber = phone.isEmpty() ? null : phone;

        User newUser = new User(userId, firstName, lastName, email, phoneNumber, null);
        if (imageUri != null) {
            ImageStorageHandler profilePictureUpload = new ImageStorageHandler();
            profilePictureUpload.uploadProfileImage(imageUri, userId, new ImageStorageHandler.imageUploaded() {
                @Override
                public void onUploadSuccess(String downloadUrl) {
                    Log.d("user", "uploading" + downloadUrl);
                    newUser.setProfileUrl(downloadUrl);
                    Log.d("user", newUser.getProfileUrl());
                    addUserToDatabase(newUser);
                }

                @Override
                public void onUploadFailed(Exception e) {
                    Toast.makeText(SignUpActivity.this, "Error uploading profile picture: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
        else {
            UserDatabaseHandler db = new UserDatabaseHandler();
        }

    }

    private void addUserToDatabase(User user) {
        new UserDatabaseHandler().addUser(user, new UserDatabaseHandler.UserAdded() {
            @Override
            public void userAdd() {
                Toast.makeText(SignUpActivity.this, "Sign-up successful!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(SignUpActivity.this, NotificationsActivity.class));
                finish();
            }

            @Override
            public void userFailedToAdd(Exception e) {
                Log.e("Firestore", "Error saving user", e);
                Toast.makeText(SignUpActivity.this, "Error saving user: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}