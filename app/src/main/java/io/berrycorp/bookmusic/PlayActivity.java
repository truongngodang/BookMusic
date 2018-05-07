package io.berrycorp.bookmusic;

import android.annotation.SuppressLint;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.github.ybq.android.spinkit.style.ChasingDots;
import com.github.ybq.android.spinkit.style.CubeGrid;
import com.github.ybq.android.spinkit.style.DoubleBounce;
import com.github.ybq.android.spinkit.style.ThreeBounce;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
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
import io.berrycorp.bookmusic.utils.FetchDataHelper;

import static io.berrycorp.bookmusic.utils.Constant.API;
import static io.berrycorp.bookmusic.utils.Constant.API_ALL_SONG;
import static io.berrycorp.bookmusic.utils.Constant.API_BOOK_SINGER;

public class PlayActivity extends AppCompatActivity {

    // Controls
    ListView lvSong;
    SongAdapter adapter;
    TextView tvName, tvTime, tvTimeCurrent, tvSinger;
    ImageButton btnPlay, btnPrev, btnNext;
    SeekBar sbSong;
    ProgressBar pbLoad;

    // Data
    Integer size;
    ArrayList<Singer> singers;
    String activityOld;
    ArrayList<Song> songs = new ArrayList<>();

    // Exo Player
    private SimpleExoPlayer exoPlayer;
    private BandwidthMeter bandwidthMeter;
    private ExtractorsFactory extractorsFactory;
    private TrackSelection.Factory trackSelectionFactory;
    private TrackSelector trackSelector;
    private DefaultBandwidthMeter defaultBandwidthMeter;
    private DataSource.Factory dataSourceFactory;
    private MediaSource mediaSource;

    // variable state
    int position = 0;
    int iDetectClick = 0;

    // Create handler for UI Player
    Handler handlerTime = new Handler();
    Runnable updateTimeRunnable = new Runnable() {
        @SuppressLint("SimpleDateFormat")
        @Override
        public void run() {
            tvTimeCurrent.setText(new SimpleDateFormat("mm:ss").format(exoPlayer.getCurrentPosition()));
            sbSong.setProgress((int) exoPlayer.getCurrentPosition());
            handlerTime.postDelayed(this, 1000);
        }
    };
    Handler handlerButtonPlay = new Handler();
    Runnable updateButtonPlay = new Runnable() {
        @Override
        public void run() {
            if (exoPlayer.getPlayWhenReady()) {
                btnPlay.setImageResource(R.drawable.ic_pause);
            } else  {
                btnPlay.setImageResource(R.drawable.ic_play);
            }
            handlerButtonPlay.postDelayed(this, 500);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        addControls();
        addEvents();

        // Init ExoPlayer
        initExoPlayer();
        // Init List Song to ListView and play first song
        initPlaylist();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        exoPlayer.setPlayWhenReady(false);
        exoPlayer.release();
    }

    @Override
    protected void onStart() {
        super.onStart();
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

        // Set Adapter
        adapter = new SongAdapter(PlayActivity.this, R.layout.row_item_song, songs);
        lvSong.setAdapter(adapter);
    }

    private void initExoPlayer() {
        bandwidthMeter = new DefaultBandwidthMeter();
        extractorsFactory = new DefaultExtractorsFactory();

        trackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);

        trackSelector = new DefaultTrackSelector(trackSelectionFactory);

        defaultBandwidthMeter = new DefaultBandwidthMeter();
        dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "BookMusic"), defaultBandwidthMeter);

        exoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
        updateStateButtonPlay();
        updateTimeSong();


        exoPlayer.addListener(new Player.DefaultEventListener() {

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                exoPlayer.setPlayWhenReady(false);
                Toast.makeText(PlayActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (!playWhenReady || playbackState == Player.STATE_BUFFERING) {
                    pbLoad.setVisibility(View.VISIBLE);
                }
                switch (playbackState) {
                    case Player.STATE_ENDED: {
                        position++;
                        if (position > songs.size() - 1) {
                            position = 0;
                        }
                        playSong(songs.get(position));
                        break;
                    }
                    case Player.STATE_BUFFERING: {
                    }
                    case Player.STATE_READY: {
                        sbSong.setMax((int) exoPlayer.getDuration());
                        setTimeTotal();
                        pbLoad.setVisibility(View.INVISIBLE);
                    }
                }
            }

            @Override
            public void onLoadingChanged(boolean isLoading) {
                super.onLoadingChanged(isLoading);
                if (isLoading) {
                    pbLoad.setVisibility(View.VISIBLE);
                } else {
                    pbLoad.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onSeekProcessed() {
                pbLoad.setVisibility(View.VISIBLE);
                super.onSeekProcessed();
            }
        });
    }

    private void addEvents() {
        lvSong.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                sbSong.setMax(0);
                position = i;
                playSong(songs.get(position));
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (exoPlayer.getPlayWhenReady()) {
                    exoPlayer.setPlayWhenReady(false);
                } else {
                    exoPlayer.setPlayWhenReady(true);
                }

            }
        });


        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sbSong.setMax(0);
                position++;
                if (position > songs.size() - 1) {
                    position = 0;
                }

                if (exoPlayer.getPlayWhenReady()) {
                    playSong(songs.get(position));
                } else {
                    tvName.setText(songs.get(position).getName());
                    playSong(songs.get(position));
                    exoPlayer.setPlayWhenReady(false);
                }
            }
        });

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iDetectClick++;
                Handler mHandler = new Handler();
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        iDetectClick = 0;
                    }
                };
                if (iDetectClick == 1) {
                    sbSong.setMax(0);
                    exoPlayer.seekTo(0);
                    mHandler.postDelayed(runnable, 400);
                } else if (iDetectClick == 2)  {
                    sbSong.setMax(0);
                    position--;
                    if (position < 0) {
                        position = songs.size() - 1;
                    }

                    if (exoPlayer.getPlayWhenReady()) {
                        playSong(songs.get(position));
                    } else {
                        tvName.setText(songs.get(position).getName());
                        playSong(songs.get(position));
                        exoPlayer.setPlayWhenReady(false);
                    }
                }
            }
        });

        sbSong.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");
                tvTimeCurrent.setText(dateFormat.format(i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                handlerTime.removeCallbacks(updateTimeRunnable);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                exoPlayer.seekTo(sbSong.getProgress());
                updateTimeSong();
            }
        });
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
            exoPlayer.setPlayWhenReady(false);
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
            exoPlayer.setPlayWhenReady(true);
        }
        return super.onKeyDown(keyCode, event);
    }

    // Play and Auto next song
    private void playSong(Song song) {
        setInfoSong(song);
        mediaSource = new ExtractorMediaSource(Uri.parse(API + song.getUrl()), dataSourceFactory, extractorsFactory, null, null);
        exoPlayer.prepare(mediaSource);
        exoPlayer.setPlayWhenReady(true);
    }

    private void setInfoSong(Song song) {
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
    }

    // setTime total for text view and seekBar
    private void setTimeTotal() {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");
        sbSong.refreshDrawableState();
        tvTime.setText(dateFormat.format(exoPlayer.getDuration()));
        sbSong.setMax((int) exoPlayer.getDuration());
    }

    // Update Time song countdown
    private void updateTimeSong() {
        handlerTime.removeCallbacks(updateTimeRunnable);
        handlerTime.postDelayed(updateTimeRunnable, 100);
    }

    // Update state Button Play Pause <-> Play
    private void updateStateButtonPlay() {
        handlerButtonPlay.removeCallbacks(updateButtonPlay);
        handlerButtonPlay.postDelayed(updateButtonPlay, 500);
    }

    // Init Playlist on ListView
    private void initPlaylist() {
        if (activityOld.equals("BookSingerActivity")) {
            fetchBookBySingers();
        } else {
            Toast.makeText(this, activityOld, Toast.LENGTH_SHORT).show();
        }
    }

    // FetchData
    private void fetchBookBySingers() {
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
                        try {
                            Song songFirst = songs.get(0);
                            playSong(songFirst);
                        } catch (Exception e) {
                            tvName.setText("No Song");
                            e.printStackTrace();
                        }

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
