package io.berrycorp.bookmusic;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import net.steamcrafted.loadtoast.LoadToast;

import java.util.ArrayList;

import io.berrycorp.bookmusic.adapter.SingerAdapter;
import io.berrycorp.bookmusic.models.Singer;
import io.berrycorp.bookmusic.models.Song;
import io.berrycorp.bookmusic.services.MusicService;
import io.berrycorp.bookmusic.utils.Constant;
import io.berrycorp.bookmusic.utils.NiceLoadToast;

public class BookSingerActivity extends AppCompatActivity implements Singer.SingerCallback {

    // Controls
    private ListView lvSinger;
    private Button btnPlay;
    private EditText etSize;
    private SingerAdapter adapter;

    // Data
    private ArrayList<Singer> mSingers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_singer);

        addControls();
        addEvents();

    }

    private void addControls() {

        lvSinger = (ListView) findViewById(R.id.lv_singer);
        btnPlay = (Button) findViewById(R.id.btn_play);
        etSize = (EditText) findViewById(R.id.et_size);

        adapter = new SingerAdapter(BookSingerActivity.this, R.layout.row_item_singer, mSingers);
        lvSinger.setAdapter(adapter);
        Singer.all(BookSingerActivity.this, this);
    }

    private void addEvents() {
        lvSinger.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Singer singer = mSingers.get(i);
                if (singer.getChecked()) {
                    singer.setChecked(false);
                } else {
                    singer.setChecked(true);
                }
                adapter.updateState(mSingers);
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<Singer> singersChecked = new ArrayList<>();
                Integer size = Integer.valueOf(etSize.getText().toString());
                for (Singer singer : mSingers) {
                    if (singer.getChecked()) {
                        singersChecked.add(singer);
                    }
                }

                if (singersChecked.size() == 0) {
                    Toast.makeText(BookSingerActivity.this, "Nhà ngươi hãy chọn một số ca sỹ", Toast.LENGTH_SHORT).show();
                } else if (etSize.getText().toString().trim().matches("")) {
                    Toast.makeText(BookSingerActivity.this, "Nhà ngươi hãy nhập số bài hát", Toast.LENGTH_SHORT).show();
                } else if (singersChecked.size() > size){
                    Toast.makeText(BookSingerActivity.this, "Hãy chọn số bài hát lớn hơn số ca sỹ", Toast.LENGTH_SHORT).show();
                } else {
                    final NiceLoadToast loadToast = new NiceLoadToast(BookSingerActivity.this);
                    loadToast.setText("Đang tạo...");
                    loadToast.setBackgroundColor(Color.rgb(51, 51, 51));
                    loadToast.setTextColor(Color.rgb(242, 242, 242));
                    loadToast.setProgressColor(Color.rgb(255, 102, 102));
                    loadToast.setTranslationY(200);
                    loadToast.show();

                    Song.shuffleSongOfSingers(BookSingerActivity.this, singersChecked, size, new Song.OnFetchSongListener() {
                        @Override
                        public void onSuccess(final ArrayList<Song> songs) {
                            if (songs.size() != 0) {
                                loadToast.success();
                                new java.util.Timer().schedule(
                                    new java.util.TimerTask() {
                                        @Override
                                        public void run() {
                                            Intent intent = new Intent(BookSingerActivity.this, PlayActivity.class);
                                            intent.putExtra("KEY_SONGS", songs);
                                            intent.putExtra("KEY_ACTIVITY", Constant.BOOK_SINGER_ACTIVITY);
                                            startActivity(intent);

                                            Intent intentService = new Intent(BookSingerActivity.this, MusicService.class);
                                            intentService.putExtra("KEY_SONGS", songs);
                                            startService(intentService);
                                        }
                                    },
                                    1000
                                );

                            } else {
                                loadToast.error();
                                Toast.makeText(BookSingerActivity.this, "Không có bài hát nào", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
            }
        });


    }

    @Override
    public void onSuccess(ArrayList<Singer> singers) {
        for (Singer singer : singers) {
            mSingers.add(singer);
            adapter.notifyDataSetChanged();
        }
    }
}
