package io.berrycorp.bookmusic.viewmodels;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.berrycorp.bookmusic.connect.RQSingleton;
import io.berrycorp.bookmusic.models.Singer;
import io.berrycorp.bookmusic.models.Song;

import static io.berrycorp.bookmusic.utils.Constant.API_BOOK_SINGER;

public class SongViewModel {

    public interface VolleyCallback {
        public void onSuccess(ArrayList<Song> songs);
    }

    public static void requestSongFollowSingers(final Context context, final ArrayList<Singer> singers, final int size, final VolleyCallback callback ) {
        StringRequest stringRequest = new StringRequest (
                Request.Method.POST,
                API_BOOK_SINGER,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        ArrayList<Song> songs = new ArrayList<>();
                        try {
                            JSONArray JSONsongs = new JSONArray(response);
                            for (int i = 0; i < JSONsongs.length(); i++) {
                                JSONObject JSONsong = JSONsongs.getJSONObject(i);
                                songs.add(parseSong(JSONsong));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
                        }
                        callback.onSuccess(songs);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("size", String.valueOf(size));
                for (int i = 0; i < singers.size(); i++) {
                    params.put("singers[" + i +"]", String.valueOf(singers.get(i).getId()));
                }
                return params;
            }
        };

        RQSingleton.getInstance(context).addToRequestQueue(stringRequest);
    }

    private static Song parseSong(JSONObject jsonSong) {
        try {
            String songName = jsonSong.getString("name");
            String songKind = jsonSong.getString("kind");
            String songURL = jsonSong.getString("url");

            String JSONSingers = jsonSong.getString("singer");
            List<String> singerList = Arrays.asList(JSONSingers.substring(1, JSONSingers.length() - 1).replaceAll("\"", "").split(","));
            ArrayList<Singer> songSingers = new ArrayList<>();
            for (int j = 0; j < singerList.size(); j++) {
                songSingers.add(new Singer(singerList.get(j)));
            }
            return new Song(songName, songSingers, songKind, songURL);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
