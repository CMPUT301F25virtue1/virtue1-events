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

        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

        Intent chooserIntent = Intent.createChooser(galleryIntent, "Select or capture a photo");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{cameraIntent});

        startActivityForResult(chooserIntent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                imageUri = data.getData();
                imageProfile.setImageURI(imageUri);
            } else if (data != null && data.getExtras() != null) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                imageProfile.setImageBitmap(photo);
                imageUri = null;
            }
        }
    }

    private void handleSignUp() {
        String firstName = inputFirstName.getText().toString().trim();
        String lastName = inputLastName.getText().toString().trim();
        String email = inputEmail.getText().toString().trim();
        String phone = inputPhone.getText().toString().trim();
        String profileUriString = (imageUri != null) ? imageUri.toString() : null;

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Please fill out all required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        String phoneNumber = phone.isEmpty() ? null : phone;
        User newUser = new User(userId, firstName, lastName, email, phoneNumber, profileUriString);

        UserDatabaseHandler db = new UserDatabaseHandler();
        db.addUser(newUser, new UserDatabaseHandler.UserAdded() {
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