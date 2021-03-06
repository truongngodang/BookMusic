package io.berrycorp.bookmusic.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import io.berrycorp.bookmusic.R;
import io.berrycorp.bookmusic.models.Singer;
import io.berrycorp.bookmusic.models.Song;

import static io.berrycorp.bookmusic.utils.Constant.API;

public class SingerAdapter extends ArrayAdapter<Singer> {
    private Context mContext;
    private int mResource;
    private ArrayList<Singer> mSingers;

    public SingerAdapter(Context context, int resource, ArrayList<Singer> singers) {
        super(context, resource, singers);
        this.mContext = context;
        this.mResource = resource;
        this.mSingers = singers;
    }

    @SuppressLint("ViewHolder")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;
        Singer singer = mSingers.get(position);

        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(mResource, parent, false);
            viewHolder.tvName = convertView.findViewById(R.id.tv_name);
            viewHolder.ivChecked = convertView.findViewById(R.id.iv_checked);
            viewHolder.ivSinger = convertView.findViewById(R.id.iv_singer);
            convertView.setTag(viewHolder);
        } else  {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.tvName.setText(singer.getName());
        Picasso.get().load(API + singer.getImage()).into(viewHolder.ivSinger);
        if (!singer.getChecked()) {
            viewHolder.ivChecked.setImageResource(R.drawable.ic_icon_plus_alt2);
        } else {
            viewHolder.ivChecked.setImageResource(R.drawable.ic_icon_check_alt);
        }

        return convertView;
    }

    public void updateState(ArrayList<Singer> singers) {
        this.mSingers = singers;
        notifyDataSetChanged();
    }

    class ViewHolder {
        TextView tvName;
        ImageView ivChecked;
        CircularImageView ivSinger;
        ViewHolder() {

        }
    }
}
