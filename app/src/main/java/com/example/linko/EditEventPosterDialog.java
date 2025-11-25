package com.example.linko;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;

/**
 * This class adds contains the logic for adding an event poster to an event
 */
public class EditEventPosterDialog extends DialogFragment {
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView editPoster;
    private Button saveButton;
    private ImageView closeButton;
    private Uri imageUri;
    private Event eventReceived;
    private OnPosterUpdatedListener onPosterUpdatedListener;

    public static EditEventPosterDialog newInstance(Event event) {
        EditEventPosterDialog dialog = new EditEventPosterDialog();
        Bundle args = new Bundle();
        args.putSerializable("event", event);
        dialog.setArguments(args);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_edit_event_poster, container, false);

        editPoster = view.findViewById(R.id.image_edit_poster);
        saveButton = view.findViewById(R.id.button_save_poster);
        closeButton = view.findViewById(R.id.button_close_dialog);

        eventReceived = (Event) getArguments().getSerializable("event");

        // load the current poster OR placeholder if event had none
        Glide.with(this)
                .load(eventReceived.getEventPosterURL())
                .placeholder(R.drawable.outline_photo_camera_24)
                .centerCrop()
                .into(editPoster);

        closeButton.setOnClickListener(v -> dismiss());

        editPoster.setOnClickListener(v -> openFileChooser());
        saveButton.setOnClickListener(v-> {
            if (imageUri == null) {
                dismiss();
                return;
            }

            ImageStorageHandler eventPictureUpdate = new ImageStorageHandler();

            eventPictureUpdate.uploadEventImage(imageUri, eventReceived.getEventId(), new ImageStorageHandler.imageUploaded() {
                @Override
                public void onUploadSuccess(String downloadUrl) {
                    eventReceived.setEventPosterURL(downloadUrl);
                    EventDatabaseHandler db = new EventDatabaseHandler();
                    db.update(eventReceived, new EventDatabaseHandler.EventUpdated() {
                        @Override
                        public void eventUpdate() {
                            Toast.makeText(getContext(), "Poster successfully updated!", Toast.LENGTH_LONG).show();
                            onPosterUpdatedListener.onPosterUpdated(eventReceived.getEventPosterURL());
                            Glide.with(EditEventPosterDialog.this).load(imageUri).centerCrop().into(editPoster);
                            dismiss();
                        }

                        @Override
                        public void eventUpdateFailed(Exception e) {
                            Toast.makeText(getContext(), "Error updating event poster: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }

                @Override
                public void onUploadFailed(Exception e) {
                    Toast.makeText(getContext(), "Error uploading event poster: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });

        return view;
    }

    public void setOnPosterUpdatedListener(OnPosterUpdatedListener listener) {
        this.onPosterUpdatedListener = listener;
    }

    public interface OnPosterUpdatedListener {
        void onPosterUpdated(String newPosterUrl);
    }

    /**
     * This method contains the logic that allows the user to select a file for the poster image
     */
    private void openFileChooser() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");

        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK) {
            if (data != null && data.getData() != null) {
                imageUri = data.getData();
                Glide.with(EditEventPosterDialog.this).load(imageUri).centerCrop().into(editPoster);
            }
        }
    }
}
