package com.example.linko;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class ConfirmationDialog extends DialogFragment {
    private OnConfirmedListener listener;
    private String toDo;

    public static ConfirmationDialog newInstance(String toDo) {
        ConfirmationDialog dialog = new ConfirmationDialog();
        Bundle args = new Bundle();
        args.putString("toDo", toDo);
        dialog.setArguments(args);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_confirmation, container, false);
        Button goBack = view.findViewById(R.id.button_go_back);
        Button deletePermanently = view.findViewById(R.id.button_yes_delete);
        TextView dialogTitle = view.findViewById(R.id.dialog_title);

        Bundle args = getArguments();
        toDo = args.getString("toDo", "Delete?");

        if (toDo.equals("event")) {
            dialogTitle.setText("Delete event?");
        }
        else if (toDo.equals("organizer")) {
            dialogTitle.setText("Delete organizer's events?");
        }
        else if (toDo.equals("profile")) {
            dialogTitle.setText("Delete profile?");
        }
        else if (toDo.equals("poster")) {
            dialogTitle.setText("Delete event poster?");
        }
        else if (toDo.equals("profilePicture")) {
            dialogTitle.setText("Delete profile picture?");
        }
        else if (toDo.equals("cancelEntrant")) {
            dialogTitle.setText("Cancel this entrant?");
            deletePermanently.setText("Yes, Cancel Entrant");
        }
        
        goBack.setOnClickListener(v -> {
            listener.onConfirmation(false);
            dismiss();
        });

        deletePermanently.setOnClickListener(v -> {
            listener.onConfirmation(true);
            dismiss();
        });

        return view;
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        listener.onConfirmation(false);
    }

    public interface OnConfirmedListener {
        void onConfirmation(boolean deleteConfirmed);
    }

    public void setOnConfirmedListener(OnConfirmedListener listener) {
        this.listener = listener;
    }
}
