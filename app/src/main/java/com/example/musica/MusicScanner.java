package com.example.musica;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;

public class MusicScanner {

    private static final String TAG = "MusicScanner";

    public ArrayList<HashMap<String, String>> getMusicFiles(ContentResolver contentResolver) {
        ArrayList<HashMap<String, String>> musicList = new ArrayList<>();

        Log.d(TAG, "Iniciando escaneo de música...");

        Uri collection;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            collection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
            Log.d(TAG, "Usando MediaStore para Android 10+");
        } else {
            collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            Log.d(TAG, "Usando EXTERNAL_CONTENT_URI para Android 9 o inferior");
        }

        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.SIZE
        };

        // Filtro para archivos de música
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0 AND " +
                MediaStore.Audio.Media.SIZE + " > 0";

        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";

        try (Cursor cursor = contentResolver.query(
                collection,
                projection,
                selection,
                null,
                sortOrder
        )) {
            if (cursor == null) {
                Log.e(TAG, "Cursor es null - error en la consulta");
                return musicList;
            }

            Log.d(TAG, "Cursor obtenido, número de filas: " + cursor.getCount());

            if (cursor.moveToFirst()) {
                int titleIndex = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                int artistIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                int albumIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
                int durationIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
                int dataIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);

                Log.d(TAG, "Índices de columnas - Título: " + titleIndex + ", Artista: " + artistIndex);

                int count = 0;
                do {
                    HashMap<String, String> song = new HashMap<>();

                    String title = titleIndex != -1 ? cursor.getString(titleIndex) : null;
                    String artist = artistIndex != -1 ? cursor.getString(artistIndex) : null;
                    String path = dataIndex != -1 ? cursor.getString(dataIndex) : null;

                    // Solo agregar si tiene título y path válido
                    if (title != null && path != null && !path.isEmpty()) {
                        song.put("title", title);
                        song.put("artist", artist != null ? artist : "Artista Desconocido");
                        song.put("album", albumIndex != -1 ? cursor.getString(albumIndex) : "Álbum Desconocido");
                        song.put("duration", durationIndex != -1 ? cursor.getString(durationIndex) : "0");
                        song.put("path", path);

                        musicList.add(song);
                        count++;

                        // Log de las primeras 3 canciones
                        if (count <= 3) {
                            Log.d(TAG, "Canción " + count + ": " + title + " - " + artist + " | Path: " + path);
                        }
                    }
                } while (cursor.moveToNext());

                Log.d(TAG, "Total de canciones válidas encontradas: " + count);
            } else {
                Log.d(TAG, "Cursor vacío - no se encontraron archivos de música");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error al escanear música: " + e.getMessage());
            e.printStackTrace();
        }

        Log.d(TAG, "Escaneo completado. Canciones encontradas: " + musicList.size());
        return musicList;
    }
}