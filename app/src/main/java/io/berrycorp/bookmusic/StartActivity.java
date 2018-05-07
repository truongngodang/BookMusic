package io.berrycorp.bookmusic;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import io.berrycorp.bookmusic.adapter.SongAdapter;
import io.berrycorp.bookmusic.models.Song;
import io.berrycorp.bookmusic.utils.FetchDataHelper;

import static io.berrycorp.bookmusic.utils.Constant.API_ALL_SONG;

public class StartActivity extends AppCompatActivity {


    // Controls
    LinearLayout cardBookSingle, cardMusicLine;
    SongAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        addControls();
        addEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void addControls() {
        cardBookSingle = (LinearLayout) findViewById(R.id.card_book_single);
        cardMusicLine = (LinearLayout) findViewById(R.id.card_music_line);
    }

    private void addEvents() {
        cardBookSingle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(StartActivity.this, "Book Single", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(StartActivity.this, BookSingerActivity.class);
                startActivity(intent);
            }
        });

        cardMusicLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(StartActivity.this, "Music Line", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(StartActivity.this, PlayActivity.class);
                intent.putExtra("ACTIVITY_NAME", "StartActivity");
                startActivity(intent);
            }
        });
    }
}
