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

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.github.ybq.android.spinkit.style.ThreeBounce;
import com.google.android.exoplayer2.Player;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.berrycorp.bookmusic.adapter.SongAdapter;
import io.berrycorp.bookmusic.connect.RQSingleton;
import io.berrycorp.bookmusic.models.Singer;
import io.berrycorp.bookmusic.models.Song;
import io.berrycorp.bookmusic.services.MusicService;

import static io.berrycorp.bookmusic.services.MusicService.STATE_IS_LOADING;
import static io.berrycorp.bookmusic.services.MusicService.STATE_IS_UNLOADING;
import static io.berrycorp.bookmusic.services.MusicService.STATE_SEEK_PROCESS;
import static io.berrycorp.bookmusic.utils.Constant.API_BOOK_SINGER;

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
    private ArrayList<Singer> singers;
    private String activityOld;
    private ArrayList<Song> songs = new ArrayList<>();

    boolean binded = false;
    private MusicService musicService;

    final SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");


    // Handler Update UI
    private final Handler handler = new Handler();

    private final Runnable uiCurrentPositionRunnable = new Runnable() {
        @Override
        public void run() {
            tvTimeCurrent.setText(dateFormat.format(musicService.getCurrentPosition()));
            sbSong.setProgress(musicService.getCurrentPosition());
            handler.postDelayed(this, 1000);
        }
    };
    private final Runnable uiDurationRunnable = new Runnable() {
        @Override
        public void run() {
            tvTime.setText(dateFormat.format(musicService.getDuration()));
            handler.postDelayed(this, 1000);
            sbSong.setMax(musicService.getDuration());
        }
    };



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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        addControls();
        addEvents();

        // Init List Song to ListView and play first song
        initPlaylist();
        updateByState();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (binded) {
            this.unbindService(musicServiceConnection);
            binded = false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(PlayActivity.this, MusicService.class);
        bindService(intent, musicServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void addControls() {
        // Get Data Form Intent
        size = getIntent().getIntExtra("SIZE", 10);
        activityOld = getIntent().getStringExtra("ACTIVITY_NAME");
        singers = getIntent().getParcelableArrayListExtra("SINGERS_CHECKED");

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

        // Set Adapter List song
        adapter = new SongAdapter(PlayActivity.this, R.layout.row_item_song, songs);
        lvSong.setAdapter(adapter);
    }

    private void addEvents() {
        lvSong.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                sbSong.setMax(0);
                updateSongUI(musicService.playSong(i));
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (musicService.togglePlay()) {
                    btnPlay.setImageResource(R.drawable.ic_pause);
                } else {
                    btnPlay.setImageResource(R.drawable.ic_play);
                }
            }
        });


        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSongUI(musicService.nextSong());
            }
        });

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSongUI(musicService.prevSong());
            }
        });

        sbSong.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                tvTimeCurrent.setText(dateFormat.format(i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                handler.removeCallbacks(uiCurrentPositionRunnable);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                musicService.seekTo(sbSong.getProgress());
                updateCurrentPosition();
            }
        });
    }

    private void updateSongUI(Song song) {
        StringBuilder builder = new StringBuilder();
        ArrayList<Singer> mSingers = song.getSinger();
        for (int i = 0; i< mSingers.size(); i++) {
            if (i == mSingers.size() - 1) {
                builder.append(mSingers.get(i).getName());
            } else {
                builder.append(mSingers.get(i).getName()).append(", ");
            }
        }
        tvName.setText(song.getName());
        tvSinger.setText(builder.toString());
        updateDuration();
        updateCurrentPosition();
    }

    private void updateDuration() {
        handler.postDelayed(uiDurationRunnable, 1000);
    }

    private void updateCurrentPosition() {
        handler.postDelayed(uiCurrentPositionRunnable, 1000);
    }

    private void updateByState() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (musicService != null) {
                    switch (musicService.getPlaybackState()) {
                        case Player.STATE_ENDED : {
                            updateSongUI(musicService.nextSong());
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
                            handler.removeCallbacks(uiDurationRunnable);
                            break;
                        }
                    }
                } else {
                    pbLoad.setVisibility(View.INVISIBLE);
                }
                handler.post(this);
            }
        });
    }


    // Init Playlist on ListView
    private void initPlaylist() {
        if (activityOld.equals("BookSingerActivity")) {
            fetchSongs();
        } else {
            Toast.makeText(this, activityOld, Toast.LENGTH_SHORT).show();
        }
    }

    // FetchData
    private void fetchSongs() {
        adapter.clear();
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                API_BOOK_SINGER,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray JSONsongs = new JSONArray(response);
                            for (int i = 0; i < JSONsongs.length(); i++) {
                                JSONObject JSONsong = JSONsongs.getJSONObject(i);
                                songs.add(createSongByJson(JSONsong));
                                adapter.notifyDataSetChanged();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(PlayActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                        }
                        Intent intent = new Intent(PlayActivity.this, MusicService.class);
                        intent.putExtra("SONGS", songs);
                        startService(intent);
                        updateSongUI(songs.get(0));
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Toast.makeText(PlayActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("size", String.valueOf(size));
                for (int i = 0; i < singers.size(); i++) {
                    params.put("singers[" + i +"]", String.valueOf(singers.get(i).getId()));
                }
                return params;
            }
        };

        RQSingleton.getInstance(PlayActivity.this).addToRequestQueue(stringRequest);
    }

    private static Song createSongByJson(JSONObject jsonSong) {
        try {
            String songName = jsonSong.getString("name");
            String songKind = jsonSong.getString("kind");
            String songURL = jsonSong.getString("url");

            String JSONSingers = jsonSong.getString("singer");
            List<String> singerList = Arrays.asList(JSONSingers.substring(1, JSONSingers.length() - 1).replaceAll("\"", "").split(","));
            ArrayList<Singer> songSingers = new ArrayList<>();
            for (int j = 0; j < singerList.size(); j++) {
                songSingers.add(new Singer(singerList.get(j)));
            }
            return new Song(songName, songSingers, songKind, songURL);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
