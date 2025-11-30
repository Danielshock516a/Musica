package com.example.musica;

import java.util.ArrayList;
import java.util.List;

public class MusicManager {
    private static MusicManager instance;
    private List<Song> playlist;
    private int currentSongIndex;
    private boolean isShuffled = false;
    private List<Song> originalPlaylist;

    private MusicManager() {
        playlist = new ArrayList<>();
        currentSongIndex = -1;
    }

    public static MusicManager getInstance() {
        if (instance == null) {
            instance = new MusicManager();
        }
        return instance;
    }

    public void setPlaylist(List<Song> songs) {
        this.playlist = new ArrayList<>(songs);
        this.originalPlaylist = new ArrayList<>(songs);
        this.currentSongIndex = -1;
    }

    public void addToPlaylist(Song song) {
        playlist.add(song);
        if (originalPlaylist != null) {
            originalPlaylist.add(song);
        }
    }

    public Song getCurrentSong() {
        if (currentSongIndex >= 0 && currentSongIndex < playlist.size()) {
            return playlist.get(currentSongIndex);
        }
        return null;
    }

    public Song getNextSong() {
        if (playlist.isEmpty()) return null;

        currentSongIndex++;
        if (currentSongIndex >= playlist.size()) {
            currentSongIndex = 0; // Volver al inicio (loop)
        }
        return getCurrentSong();
    }

    public Song getPreviousSong() {
        if (playlist.isEmpty()) return null;

        currentSongIndex--;
        if (currentSongIndex < 0) {
            currentSongIndex = playlist.size() - 1; // Ir al final (loop)
        }
        return getCurrentSong();
    }

    public void setCurrentSong(int index) {
        if (index >= 0 && index < playlist.size()) {
            currentSongIndex = index;
        }
    }

    public void setCurrentSongByPath(String path) {
        for (int i = 0; i < playlist.size(); i++) {
            if (playlist.get(i).getPath().equals(path)) {
                currentSongIndex = i;
                break;
            }
        }
    }

    public void shuffle() {
        if (isShuffled) {
            // Restaurar orden original
            playlist = new ArrayList<>(originalPlaylist);
            isShuffled = false;
        } else {
            // Mezclar playlist
            List<Song> shuffled = new ArrayList<>(playlist);
            java.util.Collections.shuffle(shuffled);
            playlist = shuffled;
            isShuffled = true;
        }

        // Reajustar el índice actual después de mezclar
        Song current = getCurrentSong();
        if (current != null) {
            setCurrentSongByPath(current.getPath());
        }
    }

    public boolean hasNext() {
        return !playlist.isEmpty();
    }

    public boolean hasPrevious() {
        return !playlist.isEmpty();
    }

    public int getCurrentIndex() {
        return currentSongIndex;
    }

    public int getPlaylistSize() {
        return playlist.size();
    }

    public List<Song> getPlaylist() {
        return new ArrayList<>(playlist);
    }

    public boolean isShuffled() {
        return isShuffled;
    }
}