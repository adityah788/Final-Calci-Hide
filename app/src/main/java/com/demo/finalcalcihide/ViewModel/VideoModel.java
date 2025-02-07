package com.demo.finalcalcihide.ViewModel;

import android.net.Uri;

public class VideoModel {
    private Uri uri;
    private String name;
    private long duration;

    public VideoModel(Uri uri, String name, long duration) {
        this.uri = uri;
        this.name = name;
        this.duration = duration;
    }

    public Uri getUri() {
        return uri;
    }

    public String getName() {
        return name;
    }

    public long getDuration() {
        return duration;
    }
}
