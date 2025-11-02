package com.example.linko;

import android.net.Uri;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ImageStorageHandler {
    private FirebaseStorage storage;

    public ImageStorageHandler() {
        this.storage = FirebaseStorage.getInstance();
    }

    public void uploadProfileImage(Uri imageUri, String userId, imageUploaded uploaded) {
        StorageReference profileRef = storage.getReference().child("profile_pictures/" + userId + ".jpg");

        profileRef.putFile(imageUri).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                profileRef.getDownloadUrl().addOnCompleteListener(urlTask -> {
                    if (urlTask.isSuccessful()) {
                        String downloadUrl = urlTask.getResult().toString();
                        uploaded.onUploadSuccess(downloadUrl);
                    } else {
                        Log.e("ImageStorageHandler", "Failed to get download URL", urlTask.getException());
                        uploaded.onUploadFailed(urlTask.getException());
                    }
                });
            } else {
                Log.e("ImageStorageHandler", "Upload failed", task.getException());
                uploaded.onUploadFailed(task.getException());
            }
        });
    }

    public interface imageUploaded {
        void onUploadSuccess(String downloadUrl);
        void onUploadFailed(Exception e);
    }
}
