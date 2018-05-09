package io.berrycorp.bookmusic;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ybq.android.spinkit.style.ThreeBounce;
import com.google.android.exoplayer2.Player;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import io.berrycorp.bookmusic.adapter.SongAdapter;
import io.berrycorp.bookmusic.models.Singer;
import io.berrycorp.bookmusic.models.Song;
import io.berrycorp.bookmusic.services.MusicService;
import io.berrycorp.bookmusic.viewmodels.SongViewModel;

import static io.berrycorp.bookmusic.services.MusicService.STATE_IS_LOADING;
import static io.berrycorp.bookmusic.services.MusicService.STATE_IS_UNLOADING;
import static io.berrycorp.bookmusic.services.MusicService.STATE_SEEK_PROCESS;

public class PlayActivity extends AppCompatActivity {

    // Controls
    private ListView lvSong;
    private SongAdapter adapter;
    private TextView tvName, tvTime, tvTimeCurrent, tvSinger;
    private ImageButton btnPlay, btnPrev, btnNext;
    private SeekBar sbSong;
    private ProgressBar pbLoad;

    // Data
    private Integer size;
    private ArrayList<Singer> mSingers;
    private String activityOld;
    private ArrayList<Song> mSongs = new ArrayList<>();

    boolean binded = false;
    private MusicService musicService;

    final SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");

    private int position;

    // Handler Update UI
    private final Handler handler = new Handler();

    private final Runnable uiRefreshRunnable = new Runnable() {
        @Override
        public void run() {
        sbSong.setProgress(musicService.getCurrentPosition());
        sbSong.setMax(musicService.getDuration());
        tvTime.setText(dateFormat.format(musicService.getDuration()));
        if (musicService.isPlaying())
            btnPlay.setImageResource(R.drawable.ic_pause);
        else
            btnPlay.setImageResource(R.drawable.ic_play);
        if (musicService.getSongPlaying() != null) {
            StringBuilder builder = new StringBuilder();
            ArrayList<Singer> mSingers = musicService.getSongPlaying().getSinger();
            for (int i = 0; i< mSingers.size(); i++) {
                if (i == mSingers.size() - 1) {
                    builder.append(mSingers.get(i).getName());
                } else {
                    builder.append(mSingers.get(i).getName()).append(", ");
                }
            }
            tvName.setText(musicService.getSongPlaying().getName());
            tvSinger.setText(builder.toString());
        }

        if (musicService != null) {
            switch (musicService.getPlaybackState()) {
                case Player.STATE_ENDED : {
                    position++;
                    if (position > mSongs.size() - 1) {
                        position = 0;
                    }
                    bindSongToService(mSongs.get(position));
                    break;
                }
                case Player.STATE_BUFFERING : {
                    pbLoad.setVisibility(View.VISIBLE);
                    break;
                }
                case Player.STATE_READY : {
                    pbLoad.setVisibility(View.INVISIBLE);
                    break;
                }
                case STATE_SEEK_PROCESS : {
                    pbLoad.setVisibility(View.VISIBLE);
                    break;
                }
                case STATE_IS_LOADING : {
                    pbLoad.setVisibility(View.VISIBLE);
                    break;
                }
                case STATE_IS_UNLOADING : {
                    pbLoad.setVisibility(View.INVISIBLE);
                    break;
                }
            }
        } else {
            pbLoad.setVisibility(View.INVISIBLE);
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

        // Init List Song to ListView and play first song
        initializeSongs();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (binded) {
            this.unbindService(musicServiceConnection);
            binded = false;
            Intent intent = new Intent(PlayActivity.this, MusicService.class);
            stopService(intent);
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
    protected void onStop() {
        super.onStop();
    }

    private void addControls() {
        // Get Data Form Intent
        size = getIntent().getIntExtra("SIZE", 10);
        activityOld = getIntent().getStringExtra("ACTIVITY_NAME");
        mSingers = getIntent().getParcelableArrayListExtra("SINGERS_CHECKED");

        // Mapping
        lvSong = findViewById(R.id.lv_song);
        tvName = findViewById(R.id.tv_name);
        tvTime = findViewById(R.id.tv_time);
        tvTimeCurrent = findViewById(R.id.tv_time_current);
        tvSinger = findViewById(R.id.tv_singer);
        btnPlay = findViewById(R.id.btn_play);
        btnPrev = findViewById(R.id.btn_prev);
        btnNext = findViewById(R.id.btn_next);
        sbSong = findViewById(R.id.sb_song);
        pbLoad = findViewById(R.id.pb_load);

        ThreeBounce threeBounce = new ThreeBounce();
        pbLoad.setIndeterminateDrawable(threeBounce);
        pbLoad.setVisibility(View.VISIBLE);

        btnPlay.setImageResource(R.drawable.ic_play);

        // Set Adapter List song
        adapter = new SongAdapter(PlayActivity.this, R.layout.row_item_song, mSongs);
        lvSong.setAdapter(adapter);
    }

    private void addEvents() {
        lvSong.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                sbSong.setMax(0);
                position = i;
                bindSongToService(mSongs.get(position));
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
                position++;
                if (position > mSongs.size() - 1) {
                    position = 0;
                }
                bindSongToService(mSongs.get(position));
            }
        });

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

    private void bindSongToService(Song song) {
        // start service
        Intent intent = new Intent(this, MusicService.class);
        intent.putExtra("keySong", (Serializable) song);
        startService(intent);
    }


    // Init Playlist on ListView
    private void initializeSongs() {
        adapter.clear();
        if (activityOld.equals("BookSingerActivity")) {
            SongViewModel.requestSongFollowSingers(this, mSingers, size, new SongViewModel.VolleyCallback() {
                @Override
                public void onSuccess(ArrayList<Song> songs) {
                    for (Song song : songs) {
                        mSongs.add(song);
                        adapter.notifyDataSetChanged();
                    }
                    bindSongToService(songs.get(0));
                }
            });
        } else {
            Toast.makeText(this, activityOld, Toast.LENGTH_SHORT).show();
        }
    }
}
