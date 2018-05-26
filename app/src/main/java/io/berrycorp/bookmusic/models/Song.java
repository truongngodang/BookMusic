package io.berrycorp.bookmusic.models;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.berrycorp.bookmusic.connect.RQSingleton;

import static io.berrycorp.bookmusic.utils.Constant.API_BOOK_KIND;
import static io.berrycorp.bookmusic.utils.Constant.API_BOOK_SINGER;

public class Song implements Serializable, Parcelable {
    private String name;
    private ArrayList<Singer> singer;
    private String kind;
    private String url;
    private Integer id;

    private int duration;
    private boolean playing;

    public Song() {
        this.playing = false;
    }

    public Song(String name, ArrayList<Singer> singer, String kind, String url) {
        this.name = name;
        this.singer = singer;
        this.kind = kind;
        this.url = url;
        this.playing = false;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Singer> getSinger() {
        return singer;
    }

    public void setSinger(ArrayList<Singer> singer) {
        this.singer = singer;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeTypedList(singer);
        parcel.writeString(kind);
        parcel.writeString(url);
        if (id == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(id);
        }
    }

    protected Song(Parcel in) {
        name = in.readString();
        singer = in.createTypedArrayList(Singer.CREATOR);
        kind = in.readString();
        url = in.readString();
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readInt();
        }
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    public interface OnFetchSongListener {
        public void onSuccess(ArrayList<Song> songs);
    }

    public static void shuffleSongOfSingers(final Context context, final ArrayList<Singer> singers, final int size, final OnFetchSongListener onFetchSongListener ) {
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
                        onFetchSongListener.onSuccess(songs);
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
                    params.put("singer-ids[" + i +"]", String.valueOf(singers.get(i).getId()));
                }
                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(50000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RQSingleton.getInstance(context).addToRequestQueue(stringRequest);
    }

    public static void shuffleSongOfKinds(final Context context, final ArrayList<Kind> kinds, final int size, final OnFetchSongListener onFetchSongListener ) {
        StringRequest stringRequest = new StringRequest (
                Request.Method.POST,
                API_BOOK_KIND,
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
                        onFetchSongListener.onSuccess(songs);
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
                for (int i = 0; i < kinds.size(); i++) {
                    params.put("kind-ids[" + i +"]", String.valueOf(kinds.get(i).getId()));
                }
                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(50000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RQSingleton.getInstance(context).addToRequestQueue(stringRequest);
    }

    private static Song parseSong(JSONObject jsonSong) {
        try {
            String songName = jsonSong.getString("name");
            String songKind = jsonSong.getString("kind_append");
            String songURL = jsonSong.getString("url");

            JSONArray JSONSingers = jsonSong.getJSONArray("singers_append");
            Type listType = new TypeToken<List<Singer>>() {}.getType();
            ArrayList<Singer> songSingers = new Gson().fromJson(String.valueOf(JSONSingers), listType);
            return new Song(songName, songSingers, songKind, songURL);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
