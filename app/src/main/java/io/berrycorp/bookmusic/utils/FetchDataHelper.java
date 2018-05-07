package io.berrycorp.bookmusic.utils;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
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

import io.berrycorp.bookmusic.adapter.SingerAdapter;
import io.berrycorp.bookmusic.adapter.SongAdapter;
import io.berrycorp.bookmusic.connect.RQSingleton;
import io.berrycorp.bookmusic.models.Singer;
import io.berrycorp.bookmusic.models.Song;

public class FetchDataHelper {
    public static void fetchSingersByAPI(String API, final Context context, final SingerAdapter adapter, final ArrayList<Singer> singers) {
        adapter.clear();
        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                API,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray JSONsongs = new JSONArray(response);
                            for (int i = 0; i < JSONsongs.length(); i++) {
                                JSONObject JSONsong = JSONsongs.getJSONObject(i);
                                String singerName = JSONsong.getString("name");
                                String singerId = JSONsong.getString("id");
                                singers.add(new Singer(Integer.valueOf(singerId), singerName));
                                adapter.notifyDataSetChanged();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
        RQSingleton.getInstance(context).addToRequestQueue(stringRequest);
    }
}
