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
import androidx.appcompat.app.AppCompatActivity;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private ImageButton btnPlayPause, btnPrevious, btnNext, btnPlaylist;
    private SeekBar progressBar;
    private TextView currentTime, totalTime, songTitle, artistName;
    private Handler handler = new Handler();
    private boolean isPlaying = false;
    private MusicManager musicManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar MusicManager
        musicManager = MusicManager.getInstance();

        // Inicializar vistas
        initViews();

        // Configurar botones
        setupButtons();

        // Manejar intent (tanto inicial como nuevos)
        handleIntent(getIntent());
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
    }

    private void setupButtons() {
        btnPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying) {
                    pauseMusic();
                } else {
                    playMusic();
                }
            }
        });

        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPreviousSong();
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNextSong();
            }
        });

        btnPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PlaylistActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent != null && intent.hasExtra("SONG_PATH")) {
            String songPath = intent.getStringExtra("SONG_PATH");
            String songTitle = intent.getStringExtra("SONG_TITLE");
            String songArtist = intent.getStringExtra("SONG_ARTIST");

            if (songPath != null && !songPath.isEmpty()) {
                playSong(songPath, songTitle, songArtist);
            }
        } else {
            // Si no hay canción específica, verificar si hay una canción actual
            Song currentSong = musicManager.getCurrentSong();
            if (currentSong != null && isPlaying) {
                updateSongInfo(currentSong.getTitle(), currentSong.getArtist());
            } else {
                updateSongInfo("Selecciona una canción", "De la lista de reproducción");
            }
        }
    }

    public void playSong(String songPath, String title, String artist) {
        try {
            // Si hay una canción reproduciéndose, liberarla primero
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }

            // Crear nuevo MediaPlayer con la ruta del archivo
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(songPath);
            mediaPlayer.prepare(); // Preparar de forma síncrona

            // Configurar listeners
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    setupSeekBar();
                    updateSongInfo(title, artist);
                    playMusic();
                    Toast.makeText(MainActivity.this, "Reproduciendo: " + title, Toast.LENGTH_SHORT).show();
                }
            });

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    // Reproducir siguiente canción automáticamente
                    playNextSong();
                }
            });

            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Toast.makeText(MainActivity.this, "Error al reproducir la canción", Toast.LENGTH_SHORT).show();
                    return false;
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al reproducir la canción: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupSeekBar() {
        if (mediaPlayer == null) return;

        progressBar.setMax(mediaPlayer.getDuration());
        progressBar.setProgress(0);

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

        // Actualizar seekbar y tiempo
        startSeekBarUpdate();
    }

    private void startSeekBarUpdate() {
        handler.removeCallbacksAndMessages(null);

        Runnable updateSeekBar = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && isPlaying) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    progressBar.setProgress(currentPosition);
                    currentTime.setText(formatTime(currentPosition));
                    handler.postDelayed(this, 1000);
                }
            }
        };
        handler.post(updateSeekBar);
    }

    private void playMusic() {
        if (mediaPlayer != null && !isPlaying) {
            mediaPlayer.start();
            isPlaying = true;
            btnPlayPause.setImageResource(R.drawable.ic_pause);

            if (mediaPlayer.getDuration() > 0) {
                totalTime.setText(formatTime(mediaPlayer.getDuration()));
            }

            startSeekBarUpdate();
        }
    }

    private void pauseMusic() {
        if (mediaPlayer != null && isPlaying) {
            mediaPlayer.pause();
            isPlaying = false;
            btnPlayPause.setImageResource(R.drawable.ic_play);
        }
    }

    private void playPreviousSong() {
        Song previousSong = musicManager.getPreviousSong();
        if (previousSong != null) {
            playSong(previousSong.getPath(), previousSong.getTitle(), previousSong.getArtist());
        } else {
            Toast.makeText(this, "No hay canciones anteriores", Toast.LENGTH_SHORT).show();
        }
    }

    private void playNextSong() {
        Song nextSong = musicManager.getNextSong();
        if (nextSong != null) {
            playSong(nextSong.getPath(), nextSong.getTitle(), nextSong.getArtist());
        } else {
            Toast.makeText(this, "No hay más canciones", Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacksAndMessages(null);
    }
}