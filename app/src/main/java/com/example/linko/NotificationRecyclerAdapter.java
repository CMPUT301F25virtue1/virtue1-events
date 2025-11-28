package com.example.linko;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Custom notification recycler adapter to improve performance and cache any user info for later use
 */
public class NotificationRecyclerAdapter extends RecyclerView.Adapter<NotificationRecyclerAdapter.NotificationViewHolder> {

    private List<UserNotification> notificationsList;
    private OnItemClickListener listener;

    private boolean fromAdmin;
    public NotificationRecyclerAdapter(List<UserNotification> notificationsList) {
        this.notificationsList = notificationsList;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notifications, parent, false);
        return new NotificationViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        UserNotification notification = notificationsList.get(position);
        new EventDatabaseHandler().fetchEventById(notification.getEventId(), new EventDatabaseHandler.EventFetched() {
            @Override
            public void eventFetch(Event event) {
                holder.eventName.setText(event.getName());

            }

            @Override
            public void eventFetchFailed(Exception e) {
                holder.eventName.setText("Event Name");

            }
        });
        holder.customMessage.setText(notification.getMessage());

        if (notification.getType().equals("custom")) {
            holder.background.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.blue));
            holder.shadow.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.darkerBlue));
            holder.chevron.setVisibility(View.VISIBLE);
        }
        else if (notification.getType().equals("invited")){
            holder.background.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.green));
            holder.shadow.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.darkGreen));
            holder.chevron.setVisibility(View.VISIBLE);
        }
        else if (notification.getType().equals("cancelled")){
            holder.background.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.red));
            holder.shadow.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.darkRed));
            holder.chevron.setVisibility(View.GONE);
        }
        else {
            holder.background.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.darkRed));
            holder.shadow.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.darkerRed));
            holder.chevron.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return notificationsList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * Stores all the ID's for the user view for later use
     */
    public class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView eventName;
        TextView customMessage;
        ConstraintLayout background;
        View shadow;
        ImageView chevron;
        public NotificationViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            eventName = itemView.findViewById(R.id.text_event_name);
            customMessage = itemView.findViewById(R.id.text_notification_message);
            background = itemView.findViewById(R.id.main_background);
            shadow = itemView.findViewById(R.id.shadow);
            chevron = itemView.findViewById(R.id.chevron);

            itemView.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    UserNotification notif = notificationsList.get(pos);

                    // cant click cancelled type events
                    if (notif.getType().equals("cancelled")) {
                        return;
                    }

                    listener.onItemClick(pos);
                }
            });
        }
    }
}