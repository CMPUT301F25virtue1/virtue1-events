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
public class EventRecyclerAdapter extends RecyclerView.Adapter<EventRecyclerAdapter.EventViewHolder> {

    private List<Event> eventList;
    private OnItemClickListener listener;
    private boolean fromAdmin;

    public EventRecyclerAdapter(List<Event> eventList, boolean fromAdmin) {
        this.eventList = eventList;
        this.fromAdmin = fromAdmin;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view,listener);
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

        if (fromAdmin) {
            holder.chevron.setVisibility(View.GONE);
            holder.shadow.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.darkerTeal)));
            holder.card.setCardBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.teal)));
        }
        else {
            holder.chevron.setVisibility(View.VISIBLE);
            holder.shadow.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.darkRed)));
            holder.card.setCardBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.red)));
        }
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

        CardView card;
        ImageView eventPosterPreview;
        TextView eventName;
        TextView entrantCount;
        ImageView chevron;
        View shadow;
        public EventViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            card = itemView.findViewById(R.id.card);
            eventPosterPreview = itemView.findViewById(R.id.image_event_poster);
            eventName = itemView.findViewById(R.id.text_event_name);
            entrantCount = itemView.findViewById(R.id.text_entrant_number);
            chevron = itemView.findViewById(R.id.chevron);
            shadow = itemView.findViewById(R.id.shadow);

            itemView.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(pos);
                }
            });
        }
    }
}