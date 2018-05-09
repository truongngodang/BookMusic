package io.berrycorp.bookmusic.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;

import java.util.Objects;

import io.berrycorp.bookmusic.models.Song;

import static io.berrycorp.bookmusic.utils.Constant.API;

public class MusicService extends Service {

    private IBinder musicBinder = new MusicBinder();

    private SimpleExoPlayer exoPlayer;
    private final DefaultBandwidthMeter DEFAULT_BANDWIDTH_METER = new DefaultBandwidthMeter();
    private final AdaptiveTrackSelection.Factory ADAPTIVE_TRACK_SELECTION_FACTORY = new AdaptiveTrackSelection.Factory(DEFAULT_BANDWIDTH_METER);
    private DataSource.Factory dataSourceFactory;
    private MediaSource mediaSource;

    private int exoState = 0;

    public final static int STATE_SEEK_PROCESS = 100;
    public final static int STATE_IS_LOADING = 101;
    public final static int STATE_IS_UNLOADING = 102;

    private WakeLock mWakeLock;
    private WifiManager.WifiLock wifiLock;
    private int duration;
    private Song songToPlay;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Toast.makeText(this, "onbind", Toast.LENGTH_SHORT).show();
        return musicBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(this, "create", Toast.LENGTH_SHORT).show();

        // Initialize the wake lock
        final PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        assert powerManager != null;
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        mWakeLock.setReferenceCounted(false);

        wifiLock = ((WifiManager) Objects.requireNonNull(getApplicationContext().getSystemService(Context.WIFI_SERVICE)))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");

        wifiLock.acquire();

        initializeExoPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "startCommand", Toast.LENGTH_SHORT).show();
        if (intent != null) {
            songToPlay = (Song) intent.getExtras().getSerializable("keySong");
            playSong(songToPlay);
        } else stopService();

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
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "destroy", Toast.LENGTH_SHORT).show();
        stopService();
        super.onDestroy();
    }

    private void stopService() {
        mWakeLock.release();
        wifiLock.release();
        exoPlayer.release();
        stopSelf();
    }

    private void playSong(Song song) {
        if (exoPlayer.getPlayWhenReady()) {
            exoPlayer.setPlayWhenReady(false);
        }
        Toast.makeText(this, song.getName(), Toast.LENGTH_SHORT).show();
        prepareExoPlayer(Uri.parse(API + song.getUrl()));
        exoPlayer.setPlayWhenReady(true);

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


    public int getCurrentPosition() {
        return (int) exoPlayer.getCurrentPosition();
    }

    public int getDuration() {
        return duration;
    }

    public int getPlaybackState() {
        return exoState;
    }

    public boolean isPlaying() {
        return exoPlayer.getPlayWhenReady();
    }

    public Song getSongPlaying() {
        return songToPlay;
    }


    private void prepareExoPlayer(Uri uri) {
        mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
        exoPlayer.prepare(mediaSource);
    }


    private void initializeExoPlayer() {
        dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "BookMusic"), DEFAULT_BANDWIDTH_METER);
        Cache cache = new SimpleCache(this.getCacheDir(),
                new LeastRecentlyUsedCacheEvictor(1024 * 1024 * 10));
        dataSourceFactory = new CacheDataSourceFactory(cache, dataSourceFactory);
        DefaultLoadControl loadControl = new DefaultLoadControl(new DefaultAllocator(true, 64 * 1024));
        exoPlayer = ExoPlayerFactory.newSimpleInstance(this,
                new DefaultTrackSelector(ADAPTIVE_TRACK_SELECTION_FACTORY), loadControl);


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
                        exoState = Player.STATE_READY;
                        duration = (int) exoPlayer.getDuration();
                        break;
                    }
                    case Player.STATE_BUFFERING : {
                        exoState = Player.STATE_BUFFERING;
                        break;
                    }
                    case Player.STATE_ENDED : {
                        exoState = Player.STATE_ENDED;
                        mWakeLock.acquire(30000);
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

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }
}
