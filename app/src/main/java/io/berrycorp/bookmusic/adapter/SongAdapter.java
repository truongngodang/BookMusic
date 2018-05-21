package io.berrycorp.bookmusic.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import io.berrycorp.bookmusic.R;
import io.berrycorp.bookmusic.models.Singer;
import io.berrycorp.bookmusic.models.Song;

public class SongAdapter extends ArrayAdapter<Song> {

    private Context mContext;
    private int mResource;
    private ArrayList<Song> mSongs;

    public SongAdapter(Context context, int resource, ArrayList<Song> songs) {
        super(context, resource, songs);
        this.mContext = context;
        this.mResource = resource;
        this.mSongs = songs;
    }

    @SuppressLint("ViewHolder")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;
        Song song = mSongs.get(position);

        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(mResource, parent, false);
            viewHolder.tvName = convertView.findViewById(R.id.tv_name);
            viewHolder.tvSinger = convertView.findViewById(R.id.tv_singer);
            viewHolder.tvKind = convertView.findViewById(R.id.tv_kind);
            viewHolder.iv_disc = convertView.findViewById(R.id.iv_disc);
            convertView.setTag(viewHolder);
        } else  {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        StringBuilder singer = new StringBuilder();
        ArrayList<Singer> singers = song.getSinger();
        for (int i = 0; i < singers.size(); i++) {
            if (i == singers.size() - 1) {
                singer.append(singers.get(i).getName());
            } else  {
                singer.append(singers.get(i).getName()).append(", ");
            }
        }

        RotateAnimation rotateAnimation = new RotateAnimation(0, 360f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);

        rotateAnimation.setInterpolator(new LinearInterpolator());
        rotateAnimation.setDuration(1800);
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        if (song.isPlaying()) {
            viewHolder.iv_disc.setVisibility(View.VISIBLE);
            viewHolder.iv_disc.setAnimation(rotateAnimation);
        } else {
            viewHolder.iv_disc.setAnimation(null);
            viewHolder.iv_disc.setVisibility(View.INVISIBLE);
        }

        viewHolder.tvName.setText(song.getName());
        viewHolder.tvSinger.setText(singer.toString());
        viewHolder.tvName.setText(song.getName());
        viewHolder.tvKind.setText(song.getKind());

        return convertView;
    }

    class ViewHolder {
        TextView tvName;
        TextView tvSinger;
        TextView tvKind;
        ImageView iv_disc;
        ViewHolder() {

        }
    }
}
