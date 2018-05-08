package io.berrycorp.bookmusic.services;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
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
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;

import io.berrycorp.bookmusic.models.Song;

import static io.berrycorp.bookmusic.utils.Constant.API;

public class MusicService extends Service {

    private IBinder musicBinder = new MusicBinder();

    private SimpleExoPlayer exoPlayer;
    private BandwidthMeter bandwidthMeter;
    private ExtractorsFactory extractorsFactory;
    private TrackSelection.Factory trackSelectionFactory;
    private TrackSelector trackSelector;
    private DefaultBandwidthMeter defaultBandwidthMeter;
    private DataSource.Factory dataSourceFactory;
    private MediaSource mediaSource;

    private int position = 0;
    private int duration = 0;
    private int exoState = 0;

    public final static int STATE_SEEK_PROCESS = 100;
    public final static int STATE_IS_LOADING = 101;
    public final static int STATE_IS_UNLOADING = 102;

    private static final int BUFFER_SEGMENT_SIZE = 64 * 1024;
    private static final int BUFFER_SEGMENT_COUNT = 256;
    private static final int BUFFER_SIZE = BUFFER_SEGMENT_SIZE * BUFFER_SEGMENT_COUNT;

    public static final int NOTIFICATION_ID = 234;


    private ArrayList<Song> songs;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Toast.makeText(this, "onbind", Toast.LENGTH_SHORT).show();
        return musicBinder;
    }

    @Override
    public void onCreate() {
        Toast.makeText(this, "create", Toast.LENGTH_SHORT).show();
        initExoPlayer();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "startCommand", Toast.LENGTH_SHORT).show();
        if (intent != null) {
            startMusic(intent);
        } else stopMusic();

        return START_STICKY;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Toast.makeText(this, "unBind", Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        Toast.makeText(this, "Rebind", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Toast.makeText(this, "remove", Toast.LENGTH_SHORT).show();
        stopMusic();
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "destroy", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    private void startMusic(Intent intent) {
        songs = intent.getParcelableArrayListExtra("SONGS");
        position = 0;
        playSong(songs.get(position));
    }

    private void stopMusic() {
        exoPlayer.release();
        stopSelf();
    }

    private void playSong(Song song) {
        if (exoPlayer.getPlayWhenReady()) {
            exoPlayer.setPlayWhenReady(false);
        }
        Toast.makeText(this, song.getName(), Toast.LENGTH_SHORT).show();
        mediaSource = new ExtractorMediaSource(Uri.parse(API + song.getUrl()), dataSourceFactory, extractorsFactory, null, null);
        exoPlayer.prepare(mediaSource);
        exoPlayer.setPlayWhenReady(true);
    }

    public Song playSong(int pos) {
        position = pos;
        Song song = songs.get(pos);
        playSong(songs.get(pos));
        return song;
    }

    public Song nextSong() {
        position++;
        if (position > songs.size() - 1) {
            position = 0;
        }
        Song song = songs.get(position);
        playSong(song);
        song.setDuration(duration);
        return song;
    }

    public Song prevSong() {
        position--;
        if (position < 0) {
            position = songs.size() - 1;
        }
        Song song = songs.get(position);
        playSong(song);
        song.setDuration(duration);
        return song;
    }

    public boolean togglePlay() {
        if (exoPlayer.getPlayWhenReady()) {
            exoPlayer.setPlayWhenReady(false);
            return false;
        } else {
            exoPlayer.setPlayWhenReady(true);
            return true;
        }
    }

    public void seekTo(int pos) {
        exoPlayer.seekTo(pos);
    }


    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    public int getDuration() {
        return duration;
    }

    public int getCurrentPosition() {
        return (int) exoPlayer.getCurrentPosition();
    }

    public int getPlaybackState() {
        return exoState;
    }

    private void initExoPlayer() {
        bandwidthMeter = new DefaultBandwidthMeter();
        extractorsFactory = new DefaultExtractorsFactory();

        trackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);

        trackSelector = new DefaultTrackSelector(trackSelectionFactory);

        defaultBandwidthMeter = new DefaultBandwidthMeter();
        dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "BookMusic"));
        Cache cache = new SimpleCache(this.getCacheDir(),
                new LeastRecentlyUsedCacheEvictor(1024 * 1024 * 10));
        dataSourceFactory = new CacheDataSourceFactory(cache,
                dataSourceFactory);
        exoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector);

        exoPlayer.addListener(new Player.DefaultEventListener() {
            @Override
            public void onSeekProcessed() {
                exoState = STATE_SEEK_PROCESS;
            }

            @Override
            public void onLoadingChanged(boolean isLoading) {
                if (isLoading) {
                    exoState = STATE_IS_LOADING;
                } else {
                    exoState = STATE_IS_UNLOADING;
                }
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                switch (playbackState) {
                    case Player.STATE_READY : {
                        duration = (int) exoPlayer.getDuration();
                        exoState = Player.STATE_READY;
                        break;
                    }
                    case Player.STATE_BUFFERING : {
                        exoState = Player.STATE_BUFFERING;
                        break;
                    }
                    case Player.STATE_ENDED : {
                        exoState = Player.STATE_ENDED;
                        break;
                    }
                    case Player.STATE_IDLE : {
                        exoState = Player.STATE_IDLE;
                        break;
                    }
                }
            }
        });
    }
}
