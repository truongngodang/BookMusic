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

import io.berrycorp.bookmusic.adapter.KindAdapter;
import io.berrycorp.bookmusic.models.Kind;
import io.berrycorp.bookmusic.models.Song;
import io.berrycorp.bookmusic.services.MusicService;
import io.berrycorp.bookmusic.utils.Constant;
import io.berrycorp.bookmusic.utils.NiceLoadToast;

public class BookLineActivity extends AppCompatActivity implements Kind.KindCallback {

    // Controls
    private ListView lvKind;
    private Button btnPlay;
    private EditText etSize;
    private KindAdapter adapter;

    // Data
    ArrayList<Kind> mKinds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_line);

        addControls();
        addEvents();
    }

    private void addControls() {

        lvKind =  findViewById(R.id.lv_kind);
        btnPlay = findViewById(R.id.btn_play);
        etSize =  findViewById(R.id.et_size);

        adapter = new KindAdapter(BookLineActivity.this, R.layout.row_item_kind, mKinds);
        lvKind.setAdapter(adapter);
        Kind.all(BookLineActivity.this, this);
    }

    private void addEvents() {
        lvKind.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Kind kind = mKinds.get(i);
                if (kind.getChecked()) {
                    kind.setChecked(false);
                } else {
                    kind.setChecked(true);
                }
                adapter.updateState(mKinds);
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<Kind> kindsChecked = new ArrayList<>();
                Integer size = Integer.valueOf(etSize.getText().toString());
                for (Kind singer : mKinds) {
                    if (singer.getChecked()) {
                        kindsChecked.add(singer);
                    }
                }

                if (kindsChecked.size() == 0) {
                    Toast.makeText(BookLineActivity.this, "Nhà ngươi hãy chọn một số thể loại", Toast.LENGTH_SHORT).show();
                } else if (etSize.getText().toString().trim().matches("")) {
                    Toast.makeText(BookLineActivity.this, "Nhà ngươi hãy nhập số bài hát", Toast.LENGTH_SHORT).show();
                } else if (kindsChecked.size() > size){
                    Toast.makeText(BookLineActivity.this, "Hãy chọn số bài hát lớn hơn số thể loại", Toast.LENGTH_SHORT).show();
                } else {
                    final NiceLoadToast loadToast = new NiceLoadToast(BookLineActivity.this);
                    loadToast.setText("Đang tạo...");
                    loadToast.setBackgroundColor(Color.rgb(51, 51, 51));
                    loadToast.setTextColor(Color.rgb(242,242,242));
                    loadToast.setProgressColor(Color.rgb(255,102,102));
                    loadToast.setTranslationY(200);
                    loadToast.show();
                    Song.shuffleSongOfKinds(BookLineActivity.this, kindsChecked, size, new Song.OnFetchSongListener() {
                        @Override
                        public void onSuccess(final ArrayList<Song> songs) {
                            if (songs.size() != 0) {
                                loadToast.success();
                                new java.util.Timer().schedule(
                                    new java.util.TimerTask() {
                                        @Override
                                        public void run() {
                                            Intent intent = new Intent(BookLineActivity.this, PlayActivity.class);
                                            intent.putExtra("KEY_SONGS", songs);
                                            intent.putExtra("KEY_ACTIVITY", Constant.BOOK_LINE_ACTIVITY);
                                            startActivity(intent);

                                            Intent intentService = new Intent(BookLineActivity.this, MusicService.class);
                                            intentService.putExtra("KEY_SONGS", songs);
                                            startService(intentService);
                                        }
                                    },
                                    1000
                                );

                            } else {
                                loadToast.error();
                                Toast.makeText(BookLineActivity.this, "Không có bài hát nào", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });


                }
            }
        });


    }

    @Override
    public void onSuccess(ArrayList<Kind> kinds) {
        for (Kind kind : kinds) {
            mKinds.add(kind);
            adapter.notifyDataSetChanged();
        }
    }
}
