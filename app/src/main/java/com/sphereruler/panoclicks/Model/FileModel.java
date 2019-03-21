package com.sphereruler.panoclicks.Model;

public class FileModel {

    String title;
    long date;

    public FileModel(String title, long date) {
        this.title = title;
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public long getDate() {
        return date;
    }
}
