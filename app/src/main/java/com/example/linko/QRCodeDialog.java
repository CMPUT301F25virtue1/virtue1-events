package com.example.linko;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.OutputStream;

/**
 * This class handles the logic for the QR code functionality
 */
public class QRCodeDialog extends DialogFragment {

    private static final String ARG_EVENT_ID = "event_id";
    private Bitmap qrBitmap;

    public static QRCodeDialog newInstance(String eventId) {
        QRCodeDialog dialog = new QRCodeDialog();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        dialog.setArguments(args);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_qr_code, container, false);

        ImageView qrImage = view.findViewById(R.id.image_qr_code);
        Button downloadButton = view.findViewById(R.id.button_download_qr);
        ImageView closeButton = view.findViewById(R.id.button_close_dialog);

        String eventId = getArguments() != null ? getArguments().getString(ARG_EVENT_ID) : null;

        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(getContext(), "Error: No event ID provided", Toast.LENGTH_SHORT).show();
            dismiss();
            return view;
        }
        // https://github.com/journeyapps/zxing-android-embedded
        try {
            BarcodeEncoder encoder = new BarcodeEncoder();
            qrBitmap = encoder.encodeBitmap(eventId, BarcodeFormat.QR_CODE, 800, 800);
            qrImage.setImageBitmap(qrBitmap);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Failed to generate QR Code", Toast.LENGTH_SHORT).show();
        }

        downloadButton.setOnClickListener(v -> saveImageInGallery());

        closeButton.setOnClickListener(v -> dismiss());

        return view;
    }

    /**
     * Contains the logic for saving a generated QR code in the users image gallery
     */
    private void saveImageInGallery() {
        String fileName = "Linko_QR_" + System.currentTimeMillis() + ".png";
        OutputStream outputStream;
        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            // makes linko directory in the gallery if it doesnt exist already
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Linko");

            Uri imageUri = requireContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            if (imageUri != null) {
                outputStream = requireContext().getContentResolver().openOutputStream(imageUri);
                qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                if (outputStream != null) outputStream.close();

                Toast.makeText(getContext(), "QR code saved to gallery!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to put QR code inside gallery.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Failed to save image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
