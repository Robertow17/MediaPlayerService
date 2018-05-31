package com.example.student238033.mp3service;

/**
 * Created by Student238033 on 19.05.2018.
 */


import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.lang.reflect.Type;

/**
 * Created by Valdio Veliu on 16-07-30.
 */
public class StorageUtil {

    private final String STORAGE = " com.valdioveliu.valdio.audioplayer.STORAGE";
    private SharedPreferences preferences;
    private Context context;

    public StorageUtil(Context context) {
        this.context = context;
    }

    public void storeAudio(ArrayList<Audio> arrayList) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(arrayList);
        editor.putString("audioArrayList", json);
        editor.apply();
    }

    public ArrayList<Audio> loadAudio() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = preferences.getString("audioArrayList", null);
        Type type = new TypeToken<ArrayList<Audio>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    public void storeAudioIndex(int index) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("audioIndex", index);
        editor.apply();
    }

    public int loadAudioIndex() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return preferences.getInt("audioIndex", -1);//return -1 if no data found
    }

    public void clearCachedAudioPlaylist() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }

    public boolean isRepeat()
    {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        int isRep = preferences.getInt("isRepeat", 0);//return -1 if no data found
        if(isRep==0) {return false;}
        else {return true;}
    }

    public void setIsRepeat(int repeat)
    {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("isRepeat", repeat);
        editor.apply();
    }

    public boolean isShuffle()
    {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        int isShu = preferences.getInt("isShuffle", 0);//return -1 if no data found
        if(isShu==0) {return false;}
        else {return true;}
    }

    public void setIsShuffle(int shuffle)
    {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("isShuffle", shuffle);
        editor.apply();
    }

    public void storeDuration(int repeat)
    {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("duration", repeat);
        editor.apply();
    }

    public int loadDuration() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return preferences.getInt("duration", 0);//return -1 if no data found
    }

}