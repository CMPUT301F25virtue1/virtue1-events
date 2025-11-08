package com.example.linko;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.util.concurrent.atomic.AtomicReference;

/**
 *This is the class for handling the edit profile logic that interacts with the UI.
 * Allows user to edit any of their profile information. User accesses this functionality by selecting
 * the Edit Profile button on the ProfileActivity screen
 * <p>
 *     User can edit their first and last name, email, phone number, and profile photo here.
 * </p>
 * @see ProfileActivity
 */
public class EditProfileActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private ImageView userProfile;
    EditText inputFirstName;
    EditText inputLastName;
    EditText inputEmailAddress;
    EditText inputPhoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);

        ImageView backButton = findViewById(R.id.button_back_button);
        userProfile = findViewById(R.id.image_profile);
        inputFirstName = findViewById(R.id.input_firstname);
        inputLastName = findViewById(R.id.input_lastname);
        inputEmailAddress = findViewById(R.id.input_email);
        inputPhoneNumber = findViewById(R.id.input_phonenumber);
        Button saveChanges = findViewById(R.id.button_save_changes);
        Button removePhoneNumber = findViewById(R.id.button_remove_phonenumber);

        AtomicReference<User> userToSave = new AtomicReference<>();
        UserDatabaseHandler databaseHandler = new UserDatabaseHandler();
        databaseHandler.getCurrentUser(this, currentUser -> {
            userToSave.set(currentUser);
            String number = currentUser.getPhone();
            if (number == null) {
                number = "(Optional) Phone Number";
            }

            inputFirstName.setHint(currentUser.getFirstName());
            inputLastName.setHint(currentUser.getLastName());
            inputEmailAddress.setHint(currentUser.getEmail());
            inputPhoneNumber.setHint(number);
            Glide.with(EditProfileActivity.this).load(currentUser.getProfileUrl()).placeholder(R.drawable.outline_person_black_24).circleCrop().into(userProfile);
        });

        userProfile.setOnClickListener(v -> {
            openFileChooser();
        });

        saveChanges.setOnClickListener(v -> {
            saveUserChanges(userToSave.get());
        });
        removePhoneNumber.setOnClickListener(v -> {
            inputPhoneNumber.setHint("(Optional) Phone Number");
            userToSave.get().setPhone(null);
        });
        backButton.setOnClickListener(v -> {
            startActivity(new Intent(EditProfileActivity.this, ProfileActivity.class));
            finish();
        });
    }

    /**
     * Method for allowing user to select a file to be their profile pic
     */
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
                Glide.with(EditProfileActivity.this).load(imageUri).circleCrop().placeholder(R.drawable.outline_person_black_24).into(userProfile);
            }
        }
    }

    /**
     * Method for saving the changes the user made to their profile
     * @param savedUser The user were saving the changes for
     */
    private void saveUserChanges(User savedUser) {
        String firstName = inputFirstName.getText().toString().trim();
        String lastName = inputLastName.getText().toString().trim();
        String email = inputEmailAddress.getText().toString().trim();
        String phone = inputPhoneNumber.getText().toString().trim();

        if (!firstName.isEmpty()) {
            savedUser.setFirstName(firstName);
        }
        if (!lastName.isEmpty()) {
            savedUser.setLastName(lastName);
        }
        if (!email.isEmpty()) {
            savedUser.setEmail(email);
        }
        if (!phone.isEmpty()) {
            savedUser.setPhone(phone);
        }

        String userId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        if (imageUri != null) {
            ImageStorageHandler profilePictureUpload = new ImageStorageHandler();
            profilePictureUpload.uploadProfileImage(imageUri, userId, new ImageStorageHandler.imageUploaded() {
                @Override
                public void onUploadSuccess(String downloadUrl) {
                    Log.d("user", "uploading" + downloadUrl);
                    savedUser.setProfileUrl(downloadUrl);
                    Log.d("user", savedUser.getProfileUrl());
                    updateUser(savedUser);
                }

                @Override
                public void onUploadFailed(Exception e) {
                    Toast.makeText(EditProfileActivity.this, "Error uploading profile picture: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
        else {
            // add user with null pfp
            updateUser(savedUser);
        }

    }

    /**
     * Updates the database with the updated user info
     * @param user The user that was just edited and waiting to be updated
     */
    private void updateUser(User user) {
        new UserDatabaseHandler().addUser(user, new UserDatabaseHandler.UserAdded() {
            @Override
            public void userAdd() {
                Toast.makeText(EditProfileActivity.this, "Profile updated!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(EditProfileActivity.this, ProfileActivity.class));
                finish();
            }

            @Override
            public void userFailedToAdd(Exception e) {
                Log.e("Firestore", "Error saving profile", e);
                Toast.makeText(EditProfileActivity.this, "Error saving profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}