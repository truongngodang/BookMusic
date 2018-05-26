package io.berrycorp.bookmusic;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;


public class HomeFragment extends Fragment {

    public static final String EXTRA_DATA = "DATA_CONTENT";

    private String content;

    private LinearLayout cardBookSingle, cardMusicLine;


    public static HomeFragment newInstance(String data) {
        HomeFragment fragment = new HomeFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        cardBookSingle =  view.findViewById(R.id.card_book_single);
        cardMusicLine =  view.findViewById(R.id.card_music_line);
        addEvents();
        return view;
    }

    private void addEvents() {
        cardBookSingle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Book Single", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), BookSingerActivity.class);
                startActivity(intent);
            }
        });

        cardMusicLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Music Line", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), BookLineActivity.class);
                startActivity(intent);
            }
        });
    }
}
