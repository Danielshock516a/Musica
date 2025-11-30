// QueueActivity.java
package com.example.musica;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class QueueActivity extends AppCompatActivity {

    private ListView queueView;
    private ImageButton btnBack;
    private ArrayList<String> queue;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queue);

        queueView = findViewById(R.id.queue_view);
        btnBack = findViewById(R.id.btn_back);

        // Cola de reproducción de ejemplo
        queue = new ArrayList<>();
        queue.add("Canción Actual - Artista Actual");
        queue.add("Siguiente Canción - Siguiente Artista");
        queue.add("Tercera Canción - Tercer Artista");
        queue.add("Cuarta Canción - Cuarto Artista");

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, queue);
        queueView.setAdapter(adapter);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}