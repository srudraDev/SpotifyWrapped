package com.example.spotifywrapped;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.TrackViewHolder> {

    private final List<top10Tracks> trackList;

    public TrackAdapter(List<top10Tracks> trackList) {
        this.trackList = trackList;
    }

    static class TrackViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameTextView;
        private final TextView artistNameTextView;
        private final TextView albumNameTextView;
        private final ImageView trackImageView;
        private final TextView artist_rank_textview;

        public TrackViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.name_text_view);
            albumNameTextView = itemView.findViewById(R.id.album_name_text_view);
            artistNameTextView = itemView.findViewById(R.id.additional_details_text_view);
            trackImageView = itemView.findViewById(R.id.artist_image_view);
            artist_rank_textview = itemView.findViewById(R.id.artist_rank_textview);
        }
        public void bind(String trackName, String artistName, String albumName, int rank) {
            nameTextView.setText(trackName);
            artistNameTextView.setText(artistName);
            albumNameTextView.setText(albumName);
            artist_rank_textview.setText(String.valueOf(rank));
        }
    }
    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_artist, parent, false);
        return new TrackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder holder, int position) {
        String trackName = trackList.get(position).getName();
        String artistName = trackList.get(position).getArtistName();
        String albumName = trackList.get(position).getAlbumName();
        Log.d("HOLLAAAAAA", albumName);
        Glide.with(holder.trackImageView.getContext())
                .load(trackList.get(position).getSecondImageUrl())
                .into(holder.trackImageView);
        holder.bind(trackName, artistName, albumName, position + 1);
    }

    @Override
    public int getItemCount() {
        return trackList.size();
    }

}
