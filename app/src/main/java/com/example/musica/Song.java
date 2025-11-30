package com.example.musica;

public class Song {
    private String title;
    private String artist;
    private String album;
    private String path;
    private long duration;

    public Song(String title, String artist, String album, String path, long duration) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.path = path;
        this.duration = duration;
    }

    // Getters
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getAlbum() { return album; }
    public String getPath() { return path; }
    public long getDuration() { return duration; }

    // Setters
    public void setTitle(String title) { this.title = title; }
    public void setArtist(String artist) { this.artist = artist; }
    public void setAlbum(String album) { this.album = album; }
    public void setPath(String path) { this.path = path; }
    public void setDuration(long duration) { this.duration = duration; }

    @Override
    public String toString() {
        return title + " - " + artist;
    }
}