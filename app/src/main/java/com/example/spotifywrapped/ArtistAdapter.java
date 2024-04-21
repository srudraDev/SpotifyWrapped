package com.example.spotifywrapped;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ArtistViewHolder> {

    private final List<top10Artists> artistList;

    public ArtistAdapter(List<top10Artists> artistList) {
        this.artistList = artistList;
    }

    @NonNull
    @Override
    public ArtistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_artist, parent, false);
        return new ArtistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtistViewHolder holder, int position) {
        String artistName = artistList.get(position).getName();
        String artistGenres = "Genres: " + String.join(", ", artistList.get(position).getGenres());
        Glide.with(holder.artistImageView.getContext())
                .load(artistList.get(position).getSecondImageUrl())
                .into(holder.artistImageView);
        holder.bind(artistName, artistGenres, position + 1);
    }

    @Override
    public int getItemCount() {
        return artistList.size();
    }

    static class ArtistViewHolder extends RecyclerView.ViewHolder {
        private final TextView artistNameTextView;
        private final TextView artistGenresTextView;
        private final ImageView artistImageView;
        private final TextView artist_rank_textview;

        public ArtistViewHolder(@NonNull View itemView) {
            super(itemView);
            artistNameTextView = itemView.findViewById(R.id.name_text_view);
            artistGenresTextView = itemView.findViewById(R.id.additional_details_text_view);
            artistImageView = itemView.findViewById(R.id.artist_image_view);
            artist_rank_textview = itemView.findViewById(R.id.artist_rank_textview);
        }

        public void bind(String artistName, String artistGenres, int rank) {
            artistNameTextView.setText(artistName);
            artistGenresTextView.setText(artistGenres);
            artist_rank_textview.setText(String.valueOf(rank));
            if (rank == 1) {
                String gold = "#C5B147";
                rankingColors(gold);
            }
            else if (rank == 2) {
                String silver = "#858584";
                rankingColors(silver);
            }
            else if (rank == 3) {
                String bronze = "#A15822";
                rankingColors(bronze);
            }
        }
        public void rankingColors(String color) {
            artistNameTextView.setTextColor(Color.parseColor(color));
            artistGenresTextView.setTextColor(Color.parseColor(color));
            artist_rank_textview.setTextColor(Color.parseColor(color));
        }
    }
}

