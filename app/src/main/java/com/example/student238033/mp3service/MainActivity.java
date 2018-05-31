package com.example.student238033.mp3service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ActionProvider;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener, SeekBar.OnSeekBarChangeListener{

    public static final String Broadcast_PLAY_NEW_AUDIO = "com.example.student238033.mp3service.PlayNewAudio";
    private static final int SAMPLE_DELAY = 1000;
    private MediaPlayerService player;
    boolean serviceBound = false;
    ArrayList<Audio> audioList = new ArrayList<>();
    private RecyclerView recyclerView;
    private MoviesAdapter mAdapter;
    private int currentSongNumber =-1;
    private ImageButton play;
    private ImageButton next;
    private ImageButton previous;
    private Audio currentSong;
    private TextView title;
    private SeekBar songProgressBar;
    private TextView songCurrentDurationLabel;
    private TextView total;
    private Handler mHandler = new Handler();
    private Utilities utils;
    private Thread thread;
    private MediaPlayerService.LocalBinder binder;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            binder = (MediaPlayerService.LocalBinder) service;
            player = binder.getService();
            //setMyListener(player);
            serviceBound = true;

          // Toast.makeText(MainActivity.this, "Service Bound", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    private void setMyListener(MediaPlayerService player)
    {
        if(player!=null)
        player.mediaPlayer.setOnCompletionListener(this);
    }


    /**
     * Update timer on seekbar
     * */
    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    public void updateInfo()
    {
        mHandler.postDelayed(updateMusicInfo,1000);
    }

    @Override
    public void onCompletion(MediaPlayer arg0) {
        StorageUtil storage = new StorageUtil(getApplicationContext());
        // check for repeat is ON or OFF
        if(storage.isRepeat()){
            // repeat is on play same song again
           playAudio(currentSongNumber);
        } else if(storage.isShuffle()){
            // shuffle is on - play a random song
            Random rand = new Random();
            currentSongNumber = rand.nextInt((audioList.size() - 1) - 0 + 1) + 0;
            currentSong = audioList.get(currentSongNumber);
            playAudio(currentSongNumber);
            title.setText(currentSong.getTitle());
            total.setText(currentSong.getTime());
        } else{
            // no repeat or shuffle ON - play next song
            if(currentSongNumber < (audioList.size() - 1)){
                currentSongNumber = currentSongNumber + 1;
                currentSong = audioList.get(currentSongNumber);
                playAudio(currentSongNumber);
                title.setText(currentSong.getTitle());
                total.setText(currentSong.getTime());
            }else{
                // play first song
                currentSongNumber = 0;
                currentSong = audioList.get(currentSongNumber);
                playAudio(currentSongNumber);
                title.setText(currentSong.getTitle());
                total.setText(currentSong.getTime());
            }
        }
    }

    /**
     * Background Runnable thread
     * */
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long totalDuration = 0;
            long currentDuration = 0;
            if(player!=null) {
                totalDuration = player.mediaPlayer.getDuration();
                currentDuration = player.mediaPlayer.getCurrentPosition();
            }

            // Displaying time completed playing
            songCurrentDurationLabel.setText(""+utils.milliSecondsToTimer(currentDuration));

            // Updating progress bar
            int progress = (int)(utils.getProgressPercentage(currentDuration, totalDuration));
            //Log.d("Progress", ""+progress);
            songProgressBar.setProgress(progress);

            // Running this thread after 100 milliseconds
            mHandler.postDelayed(this, 100);
        }
    };

    private Runnable updateMusicInfo = new Runnable() {
        public void run() {
                StorageUtil storage = new StorageUtil(getApplicationContext());
                if(currentSongNumber!=storage.loadAudioIndex()){
                    currentSongNumber=storage.loadAudioIndex();
                    currentSong=audioList.get(currentSongNumber);
                    title.setText(currentSong.getTitle());
                    total.setText(currentSong.getTime());
                }
            // Running this thread after 100 milliseconds
            mHandler.postDelayed(this, 1000);
        }
    };

    /**
     *
     * */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {

    }

    /**
     * When user starts moving the progress handler
     * */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // remove message Handler from updating progress bar
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    /**
     * When user stops moving the progress hanlder
     * */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        int totalDuration = player.mediaPlayer.getDuration();
        int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);

        // forward or backward to certain seconds
        player.mediaPlayer.seekTo(currentPosition);

        // update timer progress again
        updateProgressBar();
        updateInfo();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater myInflater = getMenuInflater();
        myInflater.inflate(R.menu.menu, menu);
        return true;
    }

    private boolean isRepeat = false;
    private boolean isShuffle = false;

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem repeat = menu.findItem(R.id.action_repeat);
        repeat.setChecked(isRepeat);
        MenuItem shuffle = menu.findItem(R.id.action_shuffle);
        shuffle.setChecked(isShuffle);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        StorageUtil storage = new StorageUtil(getApplicationContext());
        switch (item.getItemId()) {
            case R.id.action_repeat:
                isRepeat = !item.isChecked();
                item.setChecked(isRepeat);
                if(isRepeat){
                    storage.setIsRepeat(1);
                    storage.setIsShuffle(0);
                    if(isShuffle){
                        isShuffle=false;
                    }
                }
                else
                {
                    storage.setIsRepeat(0);
                }

                break;
            case R.id.action_shuffle:
                isShuffle = !item.isChecked();
                item.setChecked(isShuffle);
                if(isShuffle){
                    storage.setIsShuffle(1);
                    storage.setIsRepeat(0);
                    if(isRepeat) {
                        isRepeat = false;
                    }
                }
                else
                {
                    storage.setIsShuffle(0);
                }
                break;
            case R.id.action_about_me:
                Intent i = new Intent(MainActivity.this, AboutMe.class);
                startActivity(i);
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        android.support.v7.widget.Toolbar myToolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        recyclerView = (RecyclerView) findViewById(R.id.audio_view);
        title = (TextView) findViewById(R.id.tytul);
        mAdapter = new MoviesAdapter(audioList);
        songProgressBar = (SeekBar) findViewById(R.id.progress);
        songCurrentDurationLabel = (TextView) findViewById(R.id.czas);
        total = (TextView) findViewById(R.id.total);
        utils = new Utilities();

        // Listeners
        //player.mediaPlayer.setOnCompletionListener(this); // Important
        songProgressBar.setOnSeekBarChangeListener(this); // Important

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                playAudio(position);
                currentSongNumber=position;
                currentSong=audioList.get(currentSongNumber);
                title.setText(currentSong.getTitle());
                total.setText(currentSong.getTime());
                play.setImageResource(R.drawable.baseline_pause_black_36);
                songProgressBar.setProgress(0);
                songProgressBar.setMax(100);
                updateProgressBar();
                updateInfo();

            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        play = (ImageButton) findViewById(R.id.play);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(player.isPlaying()) {
                    if (player != null) {
                        player.pauseMedia();
                        play.setImageResource(R.drawable.baseline_play_arrow_black_36);
                    }
                }
                else {
                    if(player!=null){
                        player.playMedia();
                        play.setImageResource(R.drawable.baseline_pause_black_24);
                    }
                }

            }
        });
        next = (ImageButton) findViewById(R.id.doPrzodu);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               player.plus10Media();
            }
        });

        previous = (ImageButton) findViewById(R.id.doTylu);
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.minus10Media();
            }
        });

        prepareAudioData();

        StorageUtil storage = new StorageUtil(getApplicationContext());
        isShuffle=storage.isShuffle();
        isRepeat = storage.isRepeat();
        currentSongNumber=storage.loadAudioIndex();
        currentSong=audioList.get(currentSongNumber);
        title.setText(currentSong.getTitle());
        total.setText(currentSong.getTime());
        //songProgressBar.setProgress(storage.loadDuration());

        playAudio(currentSongNumber);
        updateProgressBar();

        play.setImageResource(R.drawable.baseline_pause_black_36);
        //storage.storeDuration(player.mediaPlayer.getCurrentPosition());
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("ServiceState", serviceBound);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("ServiceState");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        StorageUtil storage = new StorageUtil(getApplicationContext());
        if(isShuffle)
            storage.setIsShuffle(1);
        if(isRepeat)
            storage.setIsRepeat(1);
        storage.storeAudioIndex(currentSongNumber);
        storage.storeDuration(player.mediaPlayer.getCurrentPosition());
        //storage.clearCachedAudioPlaylist();
      //  if (serviceBound) {
          //  unbindService(serviceConnection);
            //service is active
          //  player.stopSelf();
       // }
    }

    private void playAudio(int audioIndex) {
        //Check is service is active
        if (!serviceBound) {

            StorageUtil storage = new StorageUtil(getApplicationContext());
            storage.storeAudio(audioList);
            storage.storeAudioIndex(audioIndex);

            Intent playerIntent = new Intent(this, MediaPlayerService.class);
            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        } else {
            //Store the new audioIndex to SharedPreferences

            StorageUtil storage = new StorageUtil(getApplicationContext());
            storage.storeAudioIndex(audioIndex);
            //Service is active
            //Send a broadcast to the service -> PLAY_NEW_AUDIO
            Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
            sendBroadcast(broadcastIntent);
        }
        songProgressBar.setProgress(0);
        songProgressBar.setMax(100);

        if(player!=null)
        {
            updateProgressBar();
            updateInfo();

        }

    }

    private void prepareAudioData() {
        audioList.add(new Audio("Handler","Muse", "04:35",R.drawable.handler, R.raw.handler));
        audioList.add(new Audio("I Wanna Be Yours","Arctic Monkeys", "03:04",R.drawable.arcticmonkeys, R.raw.iwannabeyours));
        audioList.add(new Audio("Reapers","Muse","05:59", R.drawable.reapers, R.raw.reapers));
        audioList.add(new Audio("Californication","Red Hot Chili Peppers", "05:22",R.drawable.californication, R.raw.californication));
        audioList.add(new Audio("R U Mine","Arctic Monkeys", "03:21",R.drawable.arcticmonkeys, R.raw.rumine));
        audioList.add(new Audio("Dead Inside","Muse","04:24", R.drawable.deadinside, R.raw.deadinside));
        audioList.add(new Audio("Resistance","Muse", "05:47",R.drawable.resistance, R.raw.resistance));
        audioList.add(new Audio("Do I Wanna Know","Arctic Monkeys", "04:32",R.drawable.arcticmonkeys, R.raw.doiwannaknow));
        audioList.add(new Audio("Uprising","Muse","05:02", R.drawable.uprising, R.raw.uprising));
        audioList.add(new Audio("Psycho","Muse","05:18", R.drawable.psycho, R.raw.psycho));
        audioList.add(new Audio("Globalist","Muse","10:09", R.drawable.globalist, R.raw.globalist));

        //mAdapter.notifyDataSetChanged();
    }

    public interface ClickListener
    {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private MainActivity.ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final MainActivity.ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

}
