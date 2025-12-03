package com.example.musica;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private ImageButton btnPlayPause, btnPrevious, btnNext, btnPlaylist, btnMasSec, btnMenosSec, btnPlayMode;
    private SeekBar progressBar;
    private TextView currentTime, totalTime, songTitle, artistName;
    private final Handler handler = new Handler();
    private MusicManager musicManager;

    // 0 = repetir una, 1 = repetir todas, 2 = aleatorio
    private int playMode = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        musicManager = MusicManager.getInstance();

        initViews();
        setupButtons();
        setupSeekBar();
        updatePlayModeIcon(); // Para asegurar que el ícono sea el correcto al iniciar.
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Sincroniza la UI cada vez que la actividad vuelve a estar en primer plano.
        // Esto es clave para mantener la consistencia.
        updateUI();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // Actualiza el intent de la actividad con el nuevo.
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent != null && intent.hasExtra("SONG_PATH")) {
            // Este bloque se ejecuta cuando se selecciona una canción desde la Playlist.
            String songPath = intent.getStringExtra("SONG_PATH");
            musicManager.setCurrentSongByPath(songPath); // Asegura que el manager sepa la canción actual.
            playCurrentSong();
        }
    }

    private void initViews() {
        btnPlayPause = findViewById(R.id.btn_play_pause);
        btnPrevious = findViewById(R.id.btn_previous);
        btnNext = findViewById(R.id.btn_next);
        btnPlaylist = findViewById(R.id.btn_playlist);
        progressBar = findViewById(R.id.progress_bar);
        currentTime = findViewById(R.id.current_time);
        totalTime = findViewById(R.id.total_time);
        songTitle = findViewById(R.id.song_title);
        artistName = findViewById(R.id.artist_name);
        btnMasSec = findViewById(R.id.MasSegundos);
        btnMenosSec = findViewById(R.id.MenosSegundos);
        btnPlayMode = findViewById(R.id.btn_play_mode);
    }

    /**
     * Actualiza toda la interfaz de usuario basándose en el estado actual
     * del MusicManager y el MediaPlayer.
     */
    private void updateUI() {
        Song currentSong = musicManager.getCurrentSong();
        if (currentSong != null) {
            updateSongInfo(currentSong.getTitle(), currentSong.getArtist());
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                btnPlayPause.setImageResource(R.drawable.ic_pause);
                startSeekBarUpdate();
            } else {
                btnPlayPause.setImageResource(R.drawable.ic_play);
                // Si no se está reproduciendo, detenemos la actualización del seekbar.
                handler.removeCallbacksAndMessages(null);
            }
        } else {
            updateSongInfo("Selecciona una canción", "Desde la lista de reproducción");
            btnPlayPause.setImageResource(R.drawable.ic_play);
            progressBar.setProgress(0);
            currentTime.setText("00:00");
            totalTime.setText("00:00");
        }
    }

    private void setupButtons() {
        btnPlayPause.setOnClickListener(v -> {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                pauseMusic();
            } else {
                playMusic();
            }
        });

        btnPrevious.setOnClickListener(v -> playPreviousSong());
        btnNext.setOnClickListener(v -> playNextSong());
        btnMasSec.setOnClickListener(v -> skipForward());
        btnMenosSec.setOnClickListener(v -> skipBackward());

        btnPlaylist.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PlaylistActivity.class);
            startActivity(intent);
        });

        btnPlayMode.setOnClickListener(v -> {
            playMode = (playMode + 1) % 3; // Manera más corta de ciclar entre 0, 1, 2
            updatePlayModeIcon();
        });
    }

    /**
     * Reproduce la canción actualmente seleccionada en el MusicManager.
     * Este método se encarga de preparar el MediaPlayer.
     */
    private void playCurrentSong() {
        Song songToPlay = musicManager.getCurrentSong();
        if (songToPlay == null) {
            Toast.makeText(this, "No hay canción seleccionada.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setOnCompletionListener(mp -> onSongCompletion());
            }
            mediaPlayer.reset();
            mediaPlayer.setDataSource(songToPlay.getPath());
            mediaPlayer.prepareAsync(); // Usar prepareAsync para no bloquear el hilo principal.

            mediaPlayer.setOnPreparedListener(mp -> {
                updateSongInfo(songToPlay.getTitle(), songToPlay.getArtist());
                playMusic();
            });

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al cargar la canción.", Toast.LENGTH_SHORT).show();
        }
    }

    private void playMusic() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            updateUI(); // Centralizamos la actualización de la UI aquí.
        }
    }

    private void pauseMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            updateUI();
        }
    }

    private void onSongCompletion() {
        switch (playMode) {
            case 0: // Repetir una
                mediaPlayer.seekTo(0);
                playMusic();
                break;
            case 1: // Repetir todas (siguiente)
                playNextSong();
                break;
            case 2: // Aleatorio
                musicManager.getRandomSong();
                playCurrentSong();
                break;
        }
    }

    private void playNextSong() {
        musicManager.getNextSong();
        playCurrentSong();
    }

    private void playPreviousSong() {
        musicManager.getPreviousSong();
        playCurrentSong();
    }

    // El resto de tus métodos (formatTime, skip, etc.) pueden permanecer igual.
    // ... (incluye aquí tus métodos skipForward, skipBackward, updateSongInfo, formatTime, setupSeekBar, etc.)

    private void updatePlayModeIcon() {
        switch (playMode) {
            case 0:
                btnPlayMode.setImageResource(R.drawable.ic_repeat_one);
                Toast.makeText(this, "Modo: Repetir una", Toast.LENGTH_SHORT).show();
                break;
            case 1:
                btnPlayMode.setImageResource(R.drawable.ic_repeat);
                Toast.makeText(this, "Modo: Repetir todas", Toast.LENGTH_SHORT).show();
                break;
            case 2:
                btnPlayMode.setImageResource(R.drawable.ic_shuffle);
                Toast.makeText(this, "Modo: Aleatorio", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacksAndMessages(null);
    }

    // ... Asegúrate de que los otros métodos como setupSeekBar, startSeekBarUpdate, etc. están aquí.
    private void setupSeekBar() {
        progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void startSeekBarUpdate() {
        if (mediaPlayer != null) {
            progressBar.setMax(mediaPlayer.getDuration());
            totalTime.setText(formatTime(mediaPlayer.getDuration()));

            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                        int currentPosition = mediaPlayer.getCurrentPosition();
                        progressBar.setProgress(currentPosition);
                        currentTime.setText(formatTime(currentPosition));
                        handler.postDelayed(this, 1000);
                    }
                }
            });
        }
    }

    private void updateSongInfo(String title, String artist) {
        songTitle.setText(title);
        artistName.setText(artist);
    }

    private String formatTime(int milliseconds) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(milliseconds),
                TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds))
        );
    }

    private void skipForward() {
        if (mediaPlayer != null) {
            int newPosition = mediaPlayer.getCurrentPosition() + 10000; // 10 segundos
            if (newPosition > mediaPlayer.getDuration()) {
                newPosition = mediaPlayer.getDuration();
            }
            mediaPlayer.seekTo(newPosition);
            progressBar.setProgress(newPosition);
        }
    }

    private void skipBackward() {
        if (mediaPlayer != null) {
            int newPosition = mediaPlayer.getCurrentPosition() - 10000; // -10 segundos
            if (newPosition < 0) {
                newPosition = 0;
            }
            mediaPlayer.seekTo(newPosition);
            progressBar.setProgress(newPosition);
        }
    }
}
