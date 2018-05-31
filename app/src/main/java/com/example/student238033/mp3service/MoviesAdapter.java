package com.example.student238033.mp3service;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MyViewHolder> {

    private List<Audio> audioList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, genre, time;
        public ImageView photo;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            genre = (TextView) view.findViewById(R.id.genre);
            photo = (ImageView) view.findViewById(R.id.photo);
            time = (TextView) view.findViewById(R.id.time);
        }
    }


    public MoviesAdapter(List<Audio> audioList) {
        this.audioList = audioList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.audio_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Audio audio = audioList.get(position);
        holder.title.setText(audio.getTitle());
        holder.genre.setText(audio.getArtist());
        holder.time.setText(audio.getTime());
        holder.photo.setImageResource(audio.getCover());
    }

    @Override
    public int getItemCount() {
        return audioList.size();
    }
}