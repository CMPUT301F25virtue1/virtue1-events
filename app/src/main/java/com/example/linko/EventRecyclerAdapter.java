package com.example.linko;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class EventRecyclerAdapter extends RecyclerView.Adapter<EventRecyclerAdapter.EventViewHolder> {

    private List<Event> eventList;

    public EventRecyclerAdapter(List<Event> eventList) {
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.eventName.setText(event.getName());
        holder.entrantCount.setText(event.getEntrantCount());
        Glide.with(holder.itemView.getContext())
                .load(event.getEventPosterURL())
                .centerCrop()
                .placeholder(R.drawable.outline_image_24)
                .into(holder.eventPosterPreview);
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }
    public class EventViewHolder extends RecyclerView.ViewHolder {
        ImageView eventPosterPreview;
        TextView eventName;
        TextView entrantCount;
        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventPosterPreview = itemView.findViewById(R.id.image_event);
            eventName = itemView.findViewById(R.id.text_event_name);
            entrantCount = itemView.findViewById(R.id.text_entrant_number);

        }
    }
}