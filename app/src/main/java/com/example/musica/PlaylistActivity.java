package com.example.musica;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlaylistActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 123;
    private static final String TAG = "PlaylistActivity";
    private ListView playlistView;

    // AQUÍ VA LA DECLARACIÓN - cambia ArrayList<HashMap> por List<Song>
    private List<Song> musicList;  // ← ESTA LÍNEA

    private MusicScanner musicScanner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        playlistView = findViewById(R.id.playlist_view);
        musicScanner = new MusicScanner();

        Log.d(TAG, "Activity creada, verificando permisos...");
        checkPermissionAndLoadMusic();
        Button btnRefresh = findViewById(R.id.btn_refresh);
        btnRefresh.setOnClickListener(v -> {
            Log.d(TAG, "Botón actualizar presionado");
            loadMusic();
        });
    }

    private void checkPermissionAndLoadMusic() {
        String permission;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = android.Manifest.permission.READ_MEDIA_AUDIO;
            Log.d(TAG, "Android 13+, usando READ_MEDIA_AUDIO");
        } else {
            permission = android.Manifest.permission.READ_EXTERNAL_STORAGE;
            Log.d(TAG, "Android 12 o inferior, usando READ_EXTERNAL_STORAGE");
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permiso ya concedido, cargando música...");
            loadMusic();
        } else {
            Log.d(TAG, "Solicitando permiso: " + permission);
            ActivityCompat.requestPermissions(this, new String[]{permission}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult - requestCode: " + requestCode);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permiso concedido, cargando música...");
                loadMusic();
            } else {
                Log.d(TAG, "Permiso denegado por el usuario");
                Toast.makeText(this, "Permiso denegado. No se pueden cargar canciones.", Toast.LENGTH_LONG).show();
                loadDefaultSongs();
            }
        }
    }

    private void loadMusic() {
        Log.d(TAG, "Iniciando carga de música...");

        ArrayList<HashMap<String, String>> rawMusicList = musicScanner.getMusicFiles(getContentResolver());
        musicList = new ArrayList<>();

        // Convertir HashMap a objetos Song
        for (HashMap<String, String> rawSong : rawMusicList) {
            String title = rawSong.get("title");
            String artist = rawSong.get("artist");
            String album = rawSong.get("album");
            String path = rawSong.get("path");
            String durationStr = rawSong.get("duration");
            long duration = 0;

            try {
                duration = Long.parseLong(durationStr);
            } catch (NumberFormatException e) {
                duration = 0;
            }

            Song song = new Song(title, artist, album, path, duration);
            musicList.add(song);
        }

        Log.d(TAG, "Canciones convertidas a objetos: " + musicList.size());

        if (musicList.isEmpty()) {
            Log.d(TAG, "No se encontraron canciones en el dispositivo");
            Toast.makeText(this, "No se encontraron canciones en tu dispositivo", Toast.LENGTH_LONG).show();
            loadDefaultSongs();
        } else {
            // Establecer la playlist en el MusicManager
            MusicManager.getInstance().setPlaylist(musicList);

            // Crear lista para mostrar en el ListView
            ArrayList<String> songNames = new ArrayList<>();
            for (Song song : musicList) {
                songNames.add(song.toString());
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, songNames);
            playlistView.setAdapter(adapter);

            playlistView.setOnItemClickListener((parent, view, position, id) -> {
                Song selectedSong = musicList.get(position);

                // Establecer como canción actual en el MusicManager
                MusicManager.getInstance().setCurrentSong(position);

                // Enviar a MainActivity para reproducir
                Intent intent = new Intent(PlaylistActivity.this, MainActivity.class);
                intent.putExtra("SONG_PATH", selectedSong.getPath());
                intent.putExtra("SONG_TITLE", selectedSong.getTitle());
                intent.putExtra("SONG_ARTIST", selectedSong.getArtist());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            });

            Toast.makeText(this, "Canciones cargadas: " + musicList.size(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadDefaultSongs() {
        Log.d(TAG, "Cargando canciones por defecto");
        ArrayList<String> defaultList = new ArrayList<>();
        defaultList.add("No se encontraron canciones");
        defaultList.add("Asegúrate de tener archivos de audio en tu dispositivo");
        defaultList.add("O concede los permisos necesarios");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, defaultList);
        playlistView.setAdapter(adapter);
    }
}