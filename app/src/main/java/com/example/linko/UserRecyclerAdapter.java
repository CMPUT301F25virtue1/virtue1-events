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
 * Custom user recycler adapter to improve performance and cache any user info for later use
 */
public class UserRecyclerAdapter extends RecyclerView.Adapter<UserRecyclerAdapter.UserViewHolder> {

    private List<User> userList;
    private boolean fromAdmin;
    public UserRecyclerAdapter(List<User> usersList, boolean fromAdmin) {
        this.userList = usersList;
        this.fromAdmin = fromAdmin;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.userName.setText(user.getFirstName() + " " + user.getLastName());
        holder.userEmail.setText(user.getEmail());
        String number = user.getPhone();
        if (number == null) {
            number = "N/A";
        }
        holder.userPhoneNumber.setText(number);

        if (fromAdmin) {
            Glide.with(holder.itemView.getContext()).load(user.getProfileUrl()).circleCrop().placeholder(R.drawable.outline_person_black_24).into(holder.userProfilePicture);
            holder.userProfilePicture.setBackgroundResource(R.drawable.circular_profile_white);
            holder.userName.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.white)));
            holder.userPhoneNumber.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.white)));
            holder.userEmail.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.white)));
            holder.card.setCardBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.teal)));
        }
        else {
            Glide.with(holder.itemView.getContext()).load(user.getProfileUrl()).circleCrop().placeholder(R.drawable.outline_person_24).into(holder.userProfilePicture);
            holder.userProfilePicture.setBackgroundResource(R.drawable.circular_profile);
            holder.userName.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.darkerTeal)));
            holder.userPhoneNumber.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.darkerTeal)));
            holder.userEmail.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.darkerTeal)));
            holder.card.setCardBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.white)));
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    /**
     * Stores all the ID's for the user view for later use
     */
    public class UserViewHolder extends RecyclerView.ViewHolder {
        CardView card;
        ImageView userProfilePicture;
        TextView userName;
        TextView userEmail;
        TextView userPhoneNumber;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card);
            userProfilePicture = itemView.findViewById(R.id.image_user_profile);
            userName = itemView.findViewById(R.id.text_user_name);
            userEmail = itemView.findViewById(R.id.text_user_email);
            userPhoneNumber = itemView.findViewById(R.id.text_user_phone);
        }
    }
}