// AddSongsActivity.java
package com.example.musica;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class AddSongsActivity extends AppCompatActivity {

    private ListView availableSongsView;
    private ImageButton btnBack;
    private ArrayList<String> availableSongs;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addsongs);

        availableSongsView = findViewById(R.id.available_songs_view);
        btnBack = findViewById(R.id.btn_back);

        // Lista de canciones disponibles de ejemplo
        availableSongs = new ArrayList<>();
        availableSongs.add("Nueva Canción 1 - Nuevo Artista 1");
        availableSongs.add("Nueva Canción 2 - Nuevo Artista 2");
        availableSongs.add("Nueva Canción 3 - Nuevo Artista 3");
        availableSongs.add("Nueva Canción 4 - Nuevo Artista 4");
        availableSongs.add("Nueva Canción 5 - Nuevo Artista 5");

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, availableSongs);
        availableSongsView.setAdapter(adapter);

        availableSongsView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedSong = availableSongs.get(position);
            Toast.makeText(AddSongsActivity.this, "Canción agregada: " + selectedSong, Toast.LENGTH_SHORT).show();
            // Aquí iría la lógica para agregar la canción a la lista de reproducción
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}