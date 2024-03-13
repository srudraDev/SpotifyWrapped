package com.example.spotifywrapped;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

// TopArtistsAdapter.java
public class TopArtistsAdapter extends RecyclerView.Adapter<TopArtistsAdapter.ViewHolder> {
    private List<top10Artists> artistsList;

    public TopArtistsAdapter(List<top10Artists> artistsList) {
        this.artistsList = artistsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_artist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        top10Artists artist = artistsList.get(position);
        holder.artistNameTextView.setText(artist.getName());
        holder.artistGenresTextView.setText(TextUtils.join(", ", artist.getGenres()));

        // Load image using Glide
        Glide.with(holder.itemView.getContext())
                .load(artist.getSecondImageUrl())
                //.placeholder(R.drawable.placeholder_image) // Placeholder image while loading
                //.error(R.drawable.error_image) // Image to show if loading fails
                .into(holder.artistImageView);
    }

    @Override
    public int getItemCount() {
        return artistsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView artistNameTextView;
        TextView artistGenresTextView;
        ImageView artistImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            artistNameTextView = itemView.findViewById(R.id.artist_name_text_view);
            artistGenresTextView = itemView.findViewById(R.id.artist_genres_text_view);
            artistImageView = itemView.findViewById(R.id.artist_image_view);
        }
    }
}
