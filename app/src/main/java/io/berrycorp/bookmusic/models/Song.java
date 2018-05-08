package io.berrycorp.bookmusic.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

public class Song implements Serializable, Parcelable {
    private String name;
    private ArrayList<Singer> singer;
    private String kind;
    private String url;
    private Integer id;

    private int duration;

    public Song() {
    }

    public Song(String name, ArrayList<Singer> singer, String kind, String url) {
        this.name = name;
        this.singer = singer;
        this.kind = kind;
        this.url = url;
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
}
