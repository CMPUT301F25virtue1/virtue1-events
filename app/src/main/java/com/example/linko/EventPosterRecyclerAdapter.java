package com.example.linko;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Custom recycler adapter for events, boosts performance by saving events in the cache for later
 */
public class EventPosterRecyclerAdapter extends RecyclerView.Adapter<EventPosterRecyclerAdapter.EventViewHolder> {

    private List<Event> eventList;
    private OnItemClickListener listener;

    public EventPosterRecyclerAdapter(List<Event> eventList) {
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_event_poster, parent, false);
        return new EventViewHolder(view,listener);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        Glide.with(holder.itemView.getContext()).load(event.getEventPosterURL()).centerCrop().placeholder(R.drawable.outline_image_24).into(holder.eventPoster);
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * Used to hold all the ID's for an event view for later use
     */
    public class EventViewHolder extends RecyclerView.ViewHolder {
        ImageView closeButton;
        ImageView eventPoster;
        public EventViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            closeButton = itemView.findViewById(R.id.button_delete);
            eventPoster = itemView.findViewById(R.id.image_event_poster);

            closeButton.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(pos);
                }
            });
        }
    }
}