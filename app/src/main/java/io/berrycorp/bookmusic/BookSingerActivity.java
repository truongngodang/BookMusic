package io.berrycorp.bookmusic;

import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import io.berrycorp.bookmusic.adapter.SingerAdapter;
import io.berrycorp.bookmusic.models.Singer;
import io.berrycorp.bookmusic.utils.FetchDataHelper;

import static io.berrycorp.bookmusic.utils.Constant.API_ALL_SINGER;

public class BookSingerActivity extends AppCompatActivity {

    // Controls
    ListView lvSinger;
    Button btnPlay;
    EditText etSize;
    SingerAdapter adapter;

    // Data
    ArrayList<Singer> singers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_singer);

        addControls();
        addEvents();

    }

    private void addControls() {
        lvSinger = (ListView) findViewById(R.id.lv_singer);
        btnPlay = (Button) findViewById(R.id.btn_play);
        etSize = (EditText) findViewById(R.id.et_size);

        adapter = new SingerAdapter(BookSingerActivity.this, R.layout.row_item_singer, singers);
        lvSinger.setAdapter(adapter);
        FetchDataHelper.fetchSingersByAPI(API_ALL_SINGER, BookSingerActivity.this, adapter, singers);
    }

    private void addEvents() {
        lvSinger.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Singer singer = singers.get(i);
                if (singer.getChecked()) {
                    singer.setChecked(false);
                } else {
                    singer.setChecked(true);
                }
                adapter.updateState(singers);
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<Singer> singersChecked = new ArrayList<>();
                for (Singer singer : singers) {
                    if (singer.getChecked()) {
                        singersChecked.add(singer);
                    }
                }

                if (singersChecked.size() == 0) {
                    Toast.makeText(BookSingerActivity.this, "Nhà ngươi hãy chọn một số ca sỹ", Toast.LENGTH_SHORT).show();
                } else if (etSize.getText().toString().trim().matches("")) {
                    Toast.makeText(BookSingerActivity.this, "Nhà ngươi hãy nhập số bài hát", Toast.LENGTH_SHORT).show();
                } else {
                    Integer size = Integer.valueOf(etSize.getText().toString());
                    Intent intent = new Intent(BookSingerActivity.this, PlayActivity.class);
                    intent.putExtra("SIZE", size);
                    intent.putExtra("SINGERS_CHECKED", singersChecked);
                    intent.putExtra("ACTIVITY_NAME", "BookSingerActivity");
                    startActivity(intent);
                }
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
