package io.berrycorp.bookmusic.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import io.berrycorp.bookmusic.R;
import io.berrycorp.bookmusic.StartActivity;
import io.berrycorp.bookmusic.models.Singer;
import io.berrycorp.bookmusic.models.Song;
import io.berrycorp.bookmusic.utils.Constant;

import static io.berrycorp.bookmusic.utils.Constant.API;

public class MusicService extends Service implements MediaPlayer.OnErrorListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnSeekCompleteListener {

    private IBinder mMusicBinder = new MusicBinder();

    private MediaPlayer mMediaPlayer;
    private Song mSongToPlay;
    private int mDuration = 0;
    private ArrayList<Song> mSongs;
    private int mPosition = 0;

    private OnEndListener onEndListener;

    private WifiManager.WifiLock wifiLock;
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Toast.makeText(this, "onbind", Toast.LENGTH_SHORT).show();
        return mMusicBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(this, "create", Toast.LENGTH_SHORT).show();
        wifiLock = ((WifiManager) Objects.requireNonNull(getApplicationContext().getSystemService(Context.WIFI_SERVICE)))
                .createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "music_wifi_lock");
        wifiLock.setReferenceCounted(true);

        if (wifiLock.isHeld()){
            wifiLock.release();
        } else {
            wifiLock.acquire();
        }
        powerManager = (PowerManager)getApplicationContext().getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager != null ? powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock") : null;
        if (wakeLock != null) {
            if (wakeLock.isHeld()) {
                wifiLock.release();
            } else {
                wakeLock.acquire(10*60*1000L /*10 minutes*/);
            }
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "startCommand", Toast.LENGTH_SHORT).show();
        if (intent != null) {
            mPosition = 0;
            mSongs = intent.getParcelableArrayListExtra("KEY_SONGS");
            try {
                startPlayer(mPosition);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            stopSelf();
        }
        return START_STICKY;
    }

    private void showNotification(Song song) {

        StringBuilder builder = new StringBuilder();
        ArrayList<Singer> mSingers = mSongToPlay.getSinger();
        for (int i = 0; i< mSingers.size(); i++) {
            if (i == mSingers.size() - 1) {
                builder.append(mSingers.get(i).getName());
            } else {
                builder.append(mSingers.get(i).getName()).append(", ");
            }
        }

        Intent notificationIntent = new Intent(this, StartActivity.class);
        notificationIntent.putExtra("KEY_ACTIVITY", Constant.NOTIFICATION_ACTIVITY);
        notificationIntent.setAction("io.berrycorp.bookmusic.action.main");
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle(song.getName())
                .setTicker(song.getKind())
                .setContentText(builder)
                .setSmallIcon(R.drawable.ic_play)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();

        startForeground(1337, notification);
    }

    private void startPlayer(int position) throws IOException {
        mSongToPlay = mSongs.get(position);
        mSongToPlay.setPlaying(true);
        if (mMediaPlayer != null) {
            releasePlayer();
        }
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setDataSource(API + mSongToPlay.getUrl());
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.prepareAsync();

        showNotification(mSongToPlay);
    }

    private void releasePlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
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
        releaseAll();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "destroy", Toast.LENGTH_SHORT).show();
        releaseAll();
    }

    private void releaseAll() {
        releasePlayer();
        if (wifiLock.isHeld())
            wifiLock.release();
        if (wakeLock.isHeld())
            wakeLock.release();
        stopSelf();
    }

    public void togglePlay() {
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
            } else {
                mMediaPlayer.start();
            }
        }
    }

    public void seekTo(int pos) {
        if (mMediaPlayer != null) {
            mMediaPlayer.pause();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mMediaPlayer.seekTo(pos, MediaPlayer.SEEK_NEXT_SYNC);
            } else {
                mMediaPlayer.seekTo(pos);
            }
        }
    }


    public int getCurrentPosition() {
        if (mMediaPlayer != null)
            return mMediaPlayer.getCurrentPosition();
        return 0;
    }

    public int getDuration() {
        if (mMediaPlayer != null)
            return mDuration;
        return 0;
    }

    public boolean isPlaying() {
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }

    public int getPosition() {
        return mPosition;
    }

    public void next() throws IOException {
        mSongToPlay.setPlaying(false);
        mPosition++;
        if (mPosition > mSongs.size() - 1) {
            mPosition = 0;
        }
        startPlayer(mPosition);
    }

    public void prev() throws IOException {
        mSongToPlay.setPlaying(false);
        mPosition--;
        if (mPosition < 0) {
            mPosition = mSongs.size() - 1;
        }
        startPlayer(mPosition);
    }

    public void playSelected(int position) throws IOException {
        mSongToPlay.setPlaying(false);
        mPosition = position;
        startPlayer(mPosition);
    }

    public ArrayList<Song> getSongs() {
        return mSongs;
    }

    public void setOnEndListener(OnEndListener onEndListener) {
        this.onEndListener = onEndListener;
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mDuration = mediaPlayer.getDuration();
        mediaPlayer.start();
    }


    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        onEndListener.onEnd();
        try {
            next();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
    }


    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    public interface OnEndListener {
        void onEnd();
    }

}
