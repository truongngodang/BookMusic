package io.berrycorp.bookmusic.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import io.berrycorp.bookmusic.R;
import io.berrycorp.bookmusic.models.Kind;
import io.berrycorp.bookmusic.models.Singer;

public class KindAdapter extends ArrayAdapter<Kind> {
    private Context mContext;
    private int mResource;
    private ArrayList<Kind> mKinds;

    public KindAdapter(Context context, int resource, ArrayList<Kind> kinds) {
        super(context, resource, kinds);
        this.mContext = context;
        this.mResource = resource;
        this.mKinds = kinds;
    }

    @SuppressLint("ViewHolder")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;
        Kind kind = mKinds.get(position);

        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(mResource, parent, false);
            viewHolder.tvName = convertView.findViewById(R.id.tv_name);
            viewHolder.ivChecked = convertView.findViewById(R.id.iv_checked);
            convertView.setTag(viewHolder);
        } else  {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.tvName.setText(kind.getName());

        if (!kind.getChecked()) {
            viewHolder.ivChecked.setImageResource(R.drawable.ic_icon_plus_alt2);
        } else {
            viewHolder.ivChecked.setImageResource(R.drawable.ic_icon_check_alt);
        }

        return convertView;
    }

    public void updateState(ArrayList<Kind> kind) {
        this.mKinds = kind;
        notifyDataSetChanged();
    }

    class ViewHolder {
        TextView tvName;
        ImageView ivChecked;
        ViewHolder() {

        }
    }
}
