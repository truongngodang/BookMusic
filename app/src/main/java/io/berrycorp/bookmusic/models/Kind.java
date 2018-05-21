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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

import io.berrycorp.bookmusic.connect.RQSingleton;

import static io.berrycorp.bookmusic.utils.Constant.API;
import static io.berrycorp.bookmusic.utils.Constant.API_ALL_KIND;

public class Kind implements Parcelable, Serializable {
    private int id;
    private String name;
    private Boolean isChecked = false;


    public Kind() {
    }

    public Kind(int id, String name) {
        this.id = id;
        this.name = name;
    }

    protected Kind(Parcel in) {
        id = in.readInt();
        name = in.readString();
        byte tmpIsChecked = in.readByte();
        isChecked = tmpIsChecked == 0 ? null : tmpIsChecked == 1;
    }

    public static final Creator<Kind> CREATOR = new Creator<Kind>() {
        @Override
        public Kind createFromParcel(Parcel in) {
            return new Kind(in);
        }

        @Override
        public Kind[] newArray(int size) {
            return new Kind[size];
        }
    };

    public Boolean getChecked() {
        return isChecked;
    }

    public void setChecked(Boolean checked) {
        isChecked = checked;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(name);
        parcel.writeByte((byte) (isChecked == null ? 0 : isChecked ? 1 : 2));
    }

    public interface KindCallback {
        public void onSuccess(ArrayList<Kind> kinds);
    }

    public static void all(final Context context, final KindCallback callback) {
        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                API_ALL_KIND,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            ArrayList<Kind> kinds = new ArrayList<>();
                            JSONArray JSONsongs = new JSONArray(response);
                            for (int i = 0; i < JSONsongs.length(); i++) {
                                JSONObject JSONsong = JSONsongs.getJSONObject(i);
                                String name = JSONsong.getString("name");
                                String id = JSONsong.getString("id");
                                kinds.add(new Kind(Integer.valueOf(id), name));
                            }
                            callback.onSuccess(kinds);
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
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(50000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RQSingleton.getInstance(context).addToRequestQueue(stringRequest);
    }
}
