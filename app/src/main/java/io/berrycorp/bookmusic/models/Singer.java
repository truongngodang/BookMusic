package io.berrycorp.bookmusic.models;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.plumillonforge.android.chipview.Chip;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

import io.berrycorp.bookmusic.connect.RQSingleton;

import static io.berrycorp.bookmusic.utils.Constant.API_ALL_SINGER;

public class Singer implements Parcelable, Serializable, Chip {
    private Integer id;
    private String name;
    private String image;

    private Boolean isChecked = false;

    public Singer() {
    }

    public Singer(String name) {
        this.name = name;
    }

    public Singer(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Singer(Integer id, String name, String image) {
        this.id = id;
        this.name = name;
        this.image = image;
    }

    protected Singer(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readInt();
        }
        name = in.readString();
        image = in.readString();
        byte tmpIsChecked = in.readByte();
        isChecked = tmpIsChecked == 0 ? null : tmpIsChecked == 1;
    }

    public static final Creator<Singer> CREATOR = new Creator<Singer>() {
        @Override
        public Singer createFromParcel(Parcel in) {
            return new Singer(in);
        }

        @Override
        public Singer[] newArray(int size) {
            return new Singer[size];
        }
    };

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getChecked() {
        return isChecked;
    }

    public void setChecked(Boolean checked) {
        isChecked = checked;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        if (id == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(id);
        }
        parcel.writeString(name);
        parcel.writeString(image);
        parcel.writeByte((byte) (isChecked == null ? 0 : isChecked ? 1 : 2));
    }

    @Override
    public String getText() {
        return this.name;
    }


    public interface SingerCallback {
        public void onSuccess(ArrayList<Singer> singers);
    }

    public static void all(final Context context, final SingerCallback callback) {
        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                API_ALL_SINGER,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            ArrayList<Singer> singers = new ArrayList<>();
                            JSONArray JSONsongs = new JSONArray(response);
                            for (int i = 0; i < JSONsongs.length(); i++) {
                                JSONObject JSONsong = JSONsongs.getJSONObject(i);
                                String name = JSONsong.getString("name");
                                String id = JSONsong.getString("id");
                                String image = JSONsong.getString("image_path");
                                singers.add(new Singer(Integer.valueOf(id), name, image));
                            }
                            callback.onSuccess(singers);
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
