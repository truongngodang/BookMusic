package io.berrycorp.bookmusic.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikhaellopez.circularimageview.CircularImageView;
import com.plumillonforge.android.chipview.ChipViewAdapter;
import com.squareup.picasso.Picasso;

import io.berrycorp.bookmusic.R;
import io.berrycorp.bookmusic.models.Singer;

import static io.berrycorp.bookmusic.utils.Constant.API;

public class SingerChipAdapter extends ChipViewAdapter {

    private OnRemoveChipListener removeChipListener;

    public SingerChipAdapter(Context context) {
        super(context);
    }

    @Override
    public int getLayoutRes(int position) {
        return R.layout.chip_singer;
    }

    @Override
    public int getBackgroundRes(int position) {
        return 0;
    }

    @Override
    public int getBackgroundColor(int position) {
        return getColor(R.color.colorWhite);
    }

    @Override
    public int getBackgroundColorSelected(int position) {
        return 0;
    }

    public void setOnRemoveChipListener(OnRemoveChipListener removeChipListener) {
        this.removeChipListener = removeChipListener;
    }

    @Override
    public void onLayout(View view, final int position) {
        final Singer singer = (Singer) getChip(position);

        CircularImageView ivSinger = view.findViewById(R.id.iv_singer);
        TextView tvSinger = view.findViewById(R.id.tv_singer);
        ImageButton btnCancel = view.findViewById(R.id.btn_cancel);
        Picasso.get().load(API + singer.getImage()).into(ivSinger);
        tvSinger.setText(singer.getName());
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeChipListener.removed(singer);
                remove(getChip(position));
            }
        });
    }

    public interface OnRemoveChipListener {
        void removed(Singer singer);
    }
}
