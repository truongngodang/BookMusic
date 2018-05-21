package io.berrycorp.bookmusic;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import io.berrycorp.bookmusic.adapter.SongAdapter;

public class StartActivity extends AppCompatActivity {


    // Controls
    private LinearLayout cardBookSingle, cardMusicLine, cardPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        addControls();
        addEvents();
    }


    private void addControls() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        cardBookSingle = (LinearLayout) findViewById(R.id.card_book_single);
        cardMusicLine = (LinearLayout) findViewById(R.id.card_music_line);
        cardPlayer = (LinearLayout) findViewById(R.id.card_player);
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
                Intent intent = new Intent(StartActivity.this, BookLineActivity.class);
                startActivity(intent);
            }
        });

        cardPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(StartActivity.this, "Player", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(StartActivity.this, PlayActivity.class);
                startActivity(intent);
            }
        });
    }
}
