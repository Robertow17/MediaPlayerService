package com.example.student238033.mp3service;

import java.io.Serializable;

/**
 * Created by Student238033 on 19.05.2018.
 */

public class Audio implements Serializable {

    private String title;
    private String artist;
    private String time;
    private int cover;
    private int song;

    public Audio(String title, String artist, String time, int cover, int song) {
        this.title = title;
        this.artist = artist;
        this.time = time;
        this.cover = cover;
        this.song = song;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getCover() {
        return cover;
    }

    public void setCover(int cover) {
        this.cover = cover;
    }

    public int getSong() {
        return song;
    }

    public void setSong(int song) {
        this.song = song;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }
}

