package io.berrycorp.bookmusic;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;

import io.berrycorp.bookmusic.adapter.SongAdapter;
import io.berrycorp.bookmusic.models.Singer;
import io.berrycorp.bookmusic.models.Song;
import io.berrycorp.bookmusic.services.MusicService;
import io.berrycorp.bookmusic.utils.Constant;

public class PlayActivity extends AppCompatActivity {

    // Controls
    private LinearLayout layoutContainer;
    private ListView lvSong;
    private SongAdapter adapter;
    private TextView tvName, tvTime, tvTimeCurrent, tvSinger;
    private ImageButton btnPlay, btnPrev, btnNext;
    private SeekBar sbSong;

    // Data
    private ArrayList<Song> mSongs = new ArrayList<>();
    private int mContextBefore;

    boolean binded = false;
    private MusicService musicService;

    final SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");

    private int position = 0;

    // Handler Update UI
    private final Handler handler = new Handler();

    private final Runnable uiRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            int duration = musicService.getDuration();
            int current = musicService.getCurrentPosition();
            sbSong.setProgress(current);
            sbSong.setMax(duration);
            tvTime.setText(dateFormat.format(duration));

            if (musicService.isPlaying())
                btnPlay.setImageResource(R.drawable.ic_pause);
            else
                btnPlay.setImageResource(R.drawable.ic_play);

            if (mSongs.size() != 0 && mSongs.get(musicService.getPosition()) != null) {
                Song playing = mSongs.get(musicService.getPosition());
                StringBuilder builder = new StringBuilder();
                ArrayList<Singer> mSingers = playing.getSinger();
                for (int i = 0; i < mSingers.size(); i++) {
                    if (i == mSingers.size() - 1) {
                        builder.append(mSingers.get(i).getName());
                    } else {
                        builder.append(mSingers.get(i).getName()).append(", ");
                    }
                }
                tvName.setText(playing.getName());
                tvSinger.setText(builder.toString());
            }

            handler.postDelayed(this, 1000);
        }
    };


    // Connect Service
    ServiceConnection musicServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) iBinder;
            musicService = binder.getService();
            musicService.setOnEndListener(new MusicService.OnEndListener() {
                @Override
                public void onEnd() {
                    mSongs.get(position).setPlaying(false);
                    position++;
                    if (position > mSongs.size() - 1) {
                        position = 0;
                    }
                    mSongs.get(position).setPlaying(true);
                    adapter.notifyDataSetChanged();
                }
            });
            try {
                ArrayList<Song> songFromServices = musicService.getSongs();
                if (mSongs.size() == 0 && songFromServices != null) {
                    for (Song item: songFromServices) {
                        mSongs.add(item);
                        adapter.notifyDataSetChanged();
                    }
                }
                position = musicService.getPosition();
                mSongs.get(position).setPlaying(true);
            } catch (Exception e) {
                e.printStackTrace();
            }

            binded = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            binded = false;
        }
    };

    // Activity lifecycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        addControls();
        addEvents();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (binded) {
            this.unbindService(musicServiceConnection);
            binded = false;
            //Intent intent = new Intent(PlayActivity.this, MusicService.class);
            //stopService(intent);
            handler.removeCallbacks(uiRefreshRunnable);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(PlayActivity.this, MusicService.class);
        bindService(intent, musicServiceConnection, Context.BIND_AUTO_CREATE);
        handler.postDelayed(uiRefreshRunnable, 1000);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void addControls() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        // Mapping
        layoutContainer = findViewById(R.id.layout_container);
        lvSong = findViewById(R.id.lv_song);
        tvName = findViewById(R.id.tv_name);
        tvTime = findViewById(R.id.tv_time);
        tvTimeCurrent = findViewById(R.id.tv_time_current);
        tvSinger = findViewById(R.id.tv_singer);
        btnPlay = findViewById(R.id.btn_play);
        btnPrev = findViewById(R.id.btn_prev);
        btnNext = findViewById(R.id.btn_next);
        sbSong = findViewById(R.id.sb_song);
        btnPlay.setImageResource(R.drawable.ic_play);

        // Process variable Intent
        //mSongs = getIntent().getParcelableArrayListExtra("KEY_SONGS");
        mContextBefore = getIntent().getIntExtra("KEY_ACTIVITY", 0);
        if (mContextBefore == Constant.BOOK_LINE_ACTIVITY) {
            layoutContainer.setBackgroundResource(R.drawable.bg_friday2);
        }

        // Set Adapter List song
        adapter = new SongAdapter(PlayActivity.this, R.layout.row_item_song, mSongs);
        lvSong.setAdapter(adapter);
    }

    private void addEvents() {
        lvSong.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                sbSong.setMax(0);
                try {
                    mSongs.get(position).setPlaying(false);
                    musicService.playSelected(i);
                    position = i;
                    mSongs.get(position).setPlaying(true);
                    adapter.notifyDataSetChanged();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                musicService.togglePlay();
            }
        });


        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mSongs.get(position).setPlaying(false);
                    musicService.next();
                    position++;
                    if (position > mSongs.size() - 1) {
                        position = 0;
                    }
                    mSongs.get(position).setPlaying(true);
                    adapter.notifyDataSetChanged();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mSongs.get(position).setPlaying(false);
                    musicService.prev();
                    position--;
                    if (position < 0) {
                        position = mSongs.size() - 1;
                    }
                    mSongs.get(position).setPlaying(true);
                    adapter.notifyDataSetChanged();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        sbSong.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                tvTimeCurrent.setText(dateFormat.format(i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                handler.removeCallbacks(uiRefreshRunnable);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                musicService.seekTo(sbSong.getProgress());
                handler.postDelayed(uiRefreshRunnable, 1000);
            }
        });
    }
}
