package com.example.musica;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
    public Song getRandomSong() {
        if (playlist.isEmpty()) return null;

        Random random = new Random();
        int newIndex = random.nextInt(playlist.size());

        // Asegurar que cambie la canción (opcional)
        if (playlist.size() > 1 && newIndex == currentSongIndex) {
            newIndex = (newIndex + 1) % playlist.size();
        }

        currentSongIndex = newIndex;
        return playlist.get(currentSongIndex);
    }


}