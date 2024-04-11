package com.example.spotifywrapped;

import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the data for the user from firebase
 */
public class FireModel extends ViewModel {
    // User
    private String userName;
    private String userId;
    private String userImage;
    private boolean needReload = false;
    // Artists
    private List<top10Artists> artists10List = new ArrayList<>();
    private List<top10Artists> artistsLong10List = new ArrayList<>();
    private List<top10Artists> artistsMedium10List = new ArrayList<>();
    // Tracks
    private List<top10Tracks> tracks10List = new ArrayList<>();
    private List<top10Tracks> tracksLong10List = new ArrayList<>();
    private List<top10Tracks> tracksMedium10List = new ArrayList<>();

    // Getter and setter for needReload
    public boolean getNeedReload() {
        return needReload;
    }

    public void setNeedReload(boolean x) {
        this.needReload = x;
    }

    // Getter and setter for userName
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    // Getter and setter for userId
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    // Getter and setter for userImage
    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    // Getters and Setters for artists and tracks
    public List<top10Artists> getArtists10List() {
        return artists10List;
    }

    public List<top10Artists> getArtistsLong10List() {
        return artistsLong10List;
    }

    public List<top10Artists> getArtistsMedium10List() {
        return artistsMedium10List;
    }

    public void setArtists10List(List<top10Artists> artists10List) {
        this.artists10List = artists10List;
    }

    public void setArtistsLong10List(List<top10Artists> artistsLong10List) {
        this.artistsLong10List = artistsLong10List;
    }

    public void setArtistsMedium10List(List<top10Artists> artistsMedium10List) {
        this.artistsMedium10List = artistsMedium10List;
    }

    public List<top10Tracks> getTracks10List() {
        return tracks10List;
    }

    public List<top10Tracks> getTracksLong10List() {
        return tracksLong10List;
    }

    public List<top10Tracks> getTracksMedium10List() {
        return tracksMedium10List;
    }

    public void setTracks10List(List<top10Tracks> tracks10List) {
        this.tracks10List = tracks10List;
    }

    public void setTracksLong10List(List<top10Tracks> tracksLong10List) {
        this.tracksLong10List = tracksLong10List;
    }

    public void setTracksMedium10List(List<top10Tracks> tracksMedium10List) {
        this.tracksMedium10List = tracksMedium10List;
    }
}
