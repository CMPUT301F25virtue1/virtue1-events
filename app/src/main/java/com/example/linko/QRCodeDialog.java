package com.example.linko;

import android.graphics.Bitmap;
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

import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class QRCodeDialog extends DialogFragment {

    private static final String ARG_EVENT_ID = "event_id";

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

        // ✅ match the XML IDs exactly
        ImageView qrImage = view.findViewById(R.id.image_qr_code);
        Button downloadButton = view.findViewById(R.id.button_download_qr);
        ImageView closeButton = view.findViewById(R.id.button_close_dialog);

        String eventId = getArguments() != null ? getArguments().getString(ARG_EVENT_ID) : null;

        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(getContext(), "Error: No event ID provided", Toast.LENGTH_SHORT).show();
            dismiss();
            return view;
        }

        try {
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.encodeBitmap(eventId, BarcodeFormat.QR_CODE, 800, 800);
            qrImage.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Failed to generate QR Code", Toast.LENGTH_SHORT).show();
        }

        downloadButton.setOnClickListener(v ->
                Toast.makeText(getContext(), "Download feature coming soon!", Toast.LENGTH_SHORT).show()
        );

        closeButton.setOnClickListener(v -> dismiss());

        return view;
    }
}
