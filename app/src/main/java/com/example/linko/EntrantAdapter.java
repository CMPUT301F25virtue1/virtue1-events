package com.example.linko;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EntrantAdapter extends RecyclerView.Adapter<EntrantAdapter.EntrantViewHolder> {

    private List<Entrant> entrants;
    private OnEntrantClickListener listener;

    public interface OnEntrantClickListener {
        void onEntrantSelected(Entrant entrant, boolean isSelected);
    }

    public EntrantAdapter(List<Entrant> entrants, OnEntrantClickListener listener) {
        this.entrants = entrants;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EntrantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_entrant, parent, false);
        return new EntrantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EntrantViewHolder holder, int position) {
        Entrant entrant = entrants.get(position);
        holder.nameText.setText(entrant.getName());
        holder.emailText.setText(entrant.getEmail());

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(false);

        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (listener != null) {
                    listener.onEntrantSelected(entrant, isChecked);
                }
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.checkBox.setChecked(!holder.checkBox.isChecked());
            }
        });
    }

    @Override
    public int getItemCount() {
        return entrants.size();
    }

    public static class EntrantViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        TextView emailText;
        CheckBox checkBox;

        public EntrantViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.text_entrant_name);
            emailText = itemView.findViewById(R.id.text_entrant_email);
            checkBox = itemView.findViewById(R.id.checkbox_entrant);
        }
    }
}