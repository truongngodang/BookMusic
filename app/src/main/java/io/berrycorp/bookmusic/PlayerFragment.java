package io.berrycorp.bookmusic;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import io.berrycorp.bookmusic.adapter.SongAdapter;
import io.berrycorp.bookmusic.models.Singer;
import io.berrycorp.bookmusic.models.Song;
import io.berrycorp.bookmusic.services.MusicService;
import io.berrycorp.bookmusic.utils.Constant;


public class PlayerFragment extends Fragment {

    public static final String EXTRA_DATA = "DATA_CONTENT";

    private String content;

    private Activity mContext;

    // Controls
    private FrameLayout layoutContainer;
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
            if (musicService != null) {
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


    public static PlayerFragment newInstance(String data) {
        PlayerFragment fragment = new PlayerFragment();
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_DATA, data);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            content = getArguments().getString(EXTRA_DATA);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (binded) {
            mContext.unbindService(musicServiceConnection);
            binded = false;
        }
        handler.removeCallbacks(uiRefreshRunnable);
    }

    @Override
    public void onStart() {
        super.onStart();
        handler.post(uiRefreshRunnable);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = (Activity) context;
        Intent intent = new Intent(context, MusicService.class);
        mContext.bindService(intent, musicServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);
        // Mapping
        layoutContainer = view.findViewById(R.id.layout_container);
        lvSong = view.findViewById(R.id.lv_song);
        tvName = view.findViewById(R.id.tv_name);
        tvTime = view.findViewById(R.id.tv_time);
        tvTimeCurrent = view.findViewById(R.id.tv_time_current);
        tvSinger = view.findViewById(R.id.tv_singer);
        btnPlay = view.findViewById(R.id.btn_play);
        btnPrev = view.findViewById(R.id.btn_prev);
        btnNext = view.findViewById(R.id.btn_next);
        sbSong = view.findViewById(R.id.sb_song);
        btnPlay.setImageResource(R.drawable.ic_play);

        // Process variable Intent
        mContextBefore = mContext.getIntent().getIntExtra("KEY_ACTIVITY", 0);
        if (mContextBefore == Constant.BOOK_LINE_ACTIVITY) {
            layoutContainer.setBackgroundResource(R.drawable.bg_friday2);
        }
        // Set Adapter List song
        adapter = new SongAdapter(mContext, R.layout.row_item_song, mSongs);
        lvSong.setAdapter(adapter);
        addEvents();
        return view;
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
