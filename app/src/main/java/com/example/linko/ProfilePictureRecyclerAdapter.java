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
 * Custom recycler adapter for profile pictures, boosts performance by saving events in the cache for later
 */
public class ProfilePictureRecyclerAdapter extends RecyclerView.Adapter<ProfilePictureRecyclerAdapter.UserViewHolder> {

    private List<User> userList;
    private OnItemClickListener listener;

    public ProfilePictureRecyclerAdapter(List<User> userList) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_profile_picture, parent, false);
        return new UserViewHolder(view,listener);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        Glide.with(holder.itemView.getContext()).load(user.getProfileUrl()).circleCrop().placeholder(R.drawable.outline_image_24).into(holder.profilePicture);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * Used to hold all the ID's for an user view for later use
     */
    public class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView closeButton;
        ImageView profilePicture;
        public UserViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            closeButton = itemView.findViewById(R.id.button_delete);
            profilePicture = itemView.findViewById(R.id.image_user_profile);

            closeButton.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(pos);
                }
            });
        }
    }
}