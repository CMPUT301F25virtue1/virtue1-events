package com.example.linko;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EventFilterDialog extends DialogFragment {

    private Calendar filterStart;
    private Calendar filterEnd;
    private boolean startPicked;
    private boolean endPicked;

    private TextView userStartFilter;
    private TextView userEndFilter;
    private Button clearButton;
    private OnFilterAppliedListener listener;

    public static EventFilterDialog newInstance(Calendar start, Calendar end, boolean startPicked, boolean endPicked) {
        EventFilterDialog dialog = new EventFilterDialog();
        Bundle args = new Bundle();
        args.putLong("startMillis", start.getTimeInMillis());
        args.putLong("endMillis", end.getTimeInMillis());
        args.putBoolean("startPicked", startPicked);
        args.putBoolean("endPicked", endPicked);
        dialog.setArguments(args);
        return dialog;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_event_filter, container, false);
        userStartFilter = view.findViewById(R.id.filter_user_start);
        userEndFilter = view.findViewById(R.id.filter_user_end);
        clearButton = view.findViewById(R.id.button_clear_filter);
        ImageView closeButton = view.findViewById(R.id.button_close_dialog);

        Bundle args = getArguments();
        filterStart = Calendar.getInstance();
        filterStart.setTimeInMillis(args.getLong("startMillis", System.currentTimeMillis()));

        filterEnd = Calendar.getInstance();
        filterEnd.setTimeInMillis(args.getLong("endMillis", System.currentTimeMillis()));

        startPicked = args.getBoolean("startPicked", false);
        endPicked = args.getBoolean("endPicked", false);

        // if previous filter, then show it
        SimpleDateFormat sdf = new SimpleDateFormat("MMM-dd-yyyy | hh:mm a", Locale.getDefault());
        if (startPicked && endPicked) {
            userStartFilter.setText(sdf.format(filterStart.getTime()));
            userEndFilter.setText(sdf.format(filterEnd.getTime()));
        }
        else {
            userStartFilter.setText("-");
            userEndFilter.setText("-");
        }


        userStartFilter.setOnClickListener(v -> {
            pickDateTime(filterStart, "Start", start -> {
                String period = sdf.format(start.getTime());
                userStartFilter.setText(period);
                startPicked = true;
            });
        });

        userEndFilter.setOnClickListener(v -> {
            if (!startPicked) {
                Toast.makeText(getContext(), "Please pick your filter start time first.", Toast.LENGTH_SHORT).show();
            }
            pickDateTime(filterEnd, "End", end -> {
                if (end.before(filterStart)) {
                    Toast.makeText(getContext(), "Filter end must be after filter start.", Toast.LENGTH_SHORT).show();
                    return;
                }
                String period = sdf.format(end.getTime());
                userEndFilter.setText(period);
                endPicked = true;
            });
        });

        clearButton.setOnClickListener(v -> {
            userStartFilter.setText("-");
            userEndFilter.setText("-");
            startPicked = false;
            endPicked = false;
        });

        closeButton.setOnClickListener(v -> {
            // if user picked both of the filters then safely go back
            if (startPicked && endPicked) {
                listener.onFilterApplied(filterStart, filterEnd, startPicked, endPicked);
                dismiss();
            } else {
                // if user cleared or just clicked accidentally and wants to go out without setting filter
                if (userStartFilter.getText().toString().equals("-") && userEndFilter.getText().toString().equals("-")) {

                    // reset the bools
                    startPicked = false;
                    endPicked = false;
                    listener.onFilterApplied(filterStart, filterEnd, startPicked, endPicked);
                    dismiss();
                }
                // if user picked one but not the other
                else {
                    Toast.makeText(getContext(), "Please pick both filter dates or clear them.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    // if the user taps outside the fragment, make sure args are given back to the exploreevents
    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        // if user picked both of the filters then safely go back
        if (startPicked && endPicked) {
            listener.onFilterApplied(filterStart, filterEnd, startPicked, endPicked);
        }
        // if user cleared or just clicked accidentally and wants to go out without setting filter
        else if (userStartFilter.getText().toString().equals("-") && userEndFilter.getText().toString().equals("-")) {
            // reset the bools
            startPicked = false;
            endPicked = false;
            listener.onFilterApplied(filterStart, filterEnd, startPicked, endPicked);
        }
    }

    public interface OnFilterAppliedListener {
        void onFilterApplied(Calendar start, Calendar end, boolean startPicked, boolean endPicked);
    }

    public void setOnFilterAppliedListener(OnFilterAppliedListener listener) {
        this.listener = listener;
    }
    private void pickDateTime(Calendar calendar, String title, EventFilterDialog.DateTimePickedCallback callback) {
        Calendar now = Calendar.getInstance();

        DatePickerDialog datePicker = new DatePickerDialog(getContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);

                    TimePickerDialog timePicker = new TimePickerDialog(getContext(),
                            (timeView, hourOfDay, minute) -> {
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);

                                if (calendar.getTimeInMillis() <= now.getTimeInMillis()) {
                                    Toast.makeText(getContext(), "Please select a time in the future.", Toast.LENGTH_SHORT).show();
                                } else {
                                    callback.onDateTimePicked(calendar);
                                }
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            false // 12hr formatting
                    );
                    timePicker.setTitle(title + " Time");
                    timePicker.show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        // buffer just in case
        datePicker.getDatePicker().setMinDate(System.currentTimeMillis()-1000);

        datePicker.setTitle(title + " Date");
        datePicker.show();
    }

    interface DateTimePickedCallback {
        void onDateTimePicked(Calendar calendar);
    }
}
