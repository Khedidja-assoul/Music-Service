package com.example.musicservice;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.MediaStore;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.util.ArrayList;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

public class MusicService extends Service {


        private  MyReceiver myReceiver;
        static ArrayList<Song> playlist ;

        static int current = 0;
        MediaPlayer myMediaPlayer ;
        static int currrentMusicIndex = 0 ;
        static String currentAction = "play";

        Notification notif ;
        NotificationManager maNotificationManager;
        String channelId = "musicChannelId";
        CharSequence channelName = "musicChannelName";

        PendingIntent pendingIntent;
        PendingIntent play_pause_PendingIntent;
        PendingIntent nextPendingIntent;
        PendingIntent prevPendingIntent;

    public MusicService() {}

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

        playlist = new ArrayList<>();
        ReadMusic();

        myReceiver = new MyReceiver();
        registerReceiver(myReceiver,new IntentFilter("PlayPause"));
        registerReceiver(myReceiver,new IntentFilter("Next"));
        registerReceiver(myReceiver,new IntentFilter("Previous"));

        myMediaPlayer = new MediaPlayer();
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Intent notificationIntent = new Intent(this, MainActivity.class);
        pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        play_pause_PendingIntent = PendingIntent.getBroadcast(this, 0, new
                Intent("PlayPause"), FLAG_UPDATE_CURRENT);

        nextPendingIntent = PendingIntent.getBroadcast(this, 0, new
                Intent("Next"), FLAG_UPDATE_CURRENT);

        prevPendingIntent = PendingIntent.getBroadcast(this, 0, new
                Intent("Previous"), FLAG_UPDATE_CURRENT);


        maNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int importanceLevel = NotificationManager.IMPORTANCE_HIGH;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new
                    NotificationChannel(channelId, channelName, importanceLevel);
            maNotificationManager.createNotificationChannel(notificationChannel);
        }

        notif =   new NotificationCompat.Builder(this, channelId)
                    .setContentTitle("Playing")
                    .setSmallIcon(R.drawable.music3)
                    .setContentText(playlist.get(currrentMusicIndex).getTitle())
                    .addAction(R.drawable.next, "Previous", nextPendingIntent)
                    .addAction(R.drawable.play, "Play/Pause", play_pause_PendingIntent)
                    .addAction(R.drawable.next, "Next", nextPendingIntent)
                    .setContentIntent(pendingIntent)
                    .build();
        startForeground(10, notif);

        switch (currentAction){
            case "Pause":
                PauseMusic();
                break;
            case "Play":
                PlayMusic();
                break;
            case "Next":
                PlayNextMusic();
                break;
            case "Previous":
                PlayPrevMusic();
                break;
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(myMediaPlayer.isPlaying()) myMediaPlayer.stop();
        unregisterReceiver(myReceiver);
    }


    public class MyReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            switch (action){
                case "PlayPause":
                    if(myMediaPlayer.isPlaying())
                        PauseMusic();
                    else
                        PlayMusic();
                break;
                case "Next":
                    PlayNextMusic();
                break;
                case "Previous":
                    PlayPrevMusic();
                break;
            }

        }
    }

    public void ReadMusic(){
        ContentResolver contentResolver = getContentResolver();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        Cursor cursor = contentResolver.query(songUri,null,null,null,null);

        if(cursor!=null && cursor.moveToFirst()){

            int titleCursor = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int artistCursor = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int pathCursor = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);

            do{
                String title = cursor.getString(titleCursor);
                String artist = cursor.getString(artistCursor);
                String path = cursor.getString(pathCursor);
                Song song = new Song(title,artist,path);
                playlist.add(song);

            }while (cursor.moveToNext());
        }
     }

    public void PlayMusic(){
        try {
            UpdateNotif();
            myMediaPlayer = new MediaPlayer();
            myMediaPlayer.setDataSource(playlist.get(currrentMusicIndex).getPath());
            myMediaPlayer.prepare();
            myMediaPlayer.seekTo(current);
            myMediaPlayer.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void PauseMusic(){
        myMediaPlayer.pause();
        current = myMediaPlayer.getCurrentPosition();
    }

    public  void PlayNextMusic(){
        currrentMusicIndex++;
        myMediaPlayer.stop();
        current = 0 ;
        PlayMusic();
    }

    public  void PlayPrevMusic(){
        currrentMusicIndex--;
        myMediaPlayer.stop();
        current = 0 ;
        PlayMusic();
    }

    public void UpdateNotif(){
        if(currrentMusicIndex == 0 ){
            notif =   new NotificationCompat.Builder(this, channelId)
                    .setContentTitle("Playing")
                    .setSmallIcon(R.drawable.music3)
                    .setContentText(playlist.get(currrentMusicIndex).getTitle())
                    .addAction(R.drawable.play, "Play/Pause", play_pause_PendingIntent)
                    .addAction(R.drawable.next, "Next", nextPendingIntent)
                    .setContentIntent(pendingIntent)
                    .build();
        }else{
            if(currrentMusicIndex == playlist.size()-1){
                notif =   new NotificationCompat.Builder(this, channelId)
                        .setContentTitle("Playing")
                        .setSmallIcon(R.drawable.music3)
                        .setContentText(playlist.get(currrentMusicIndex).getTitle())
                        .addAction(R.drawable.previous, "Previous", prevPendingIntent)
                        .addAction(R.drawable.play, "Play/Pause", play_pause_PendingIntent)
                        .setContentIntent(pendingIntent)
                        .build();
            }else{
                notif =   new NotificationCompat.Builder(this, channelId)
                        .setContentTitle("Playing")
                        .setSmallIcon(R.drawable.music3)
                        .setContentText(playlist.get(currrentMusicIndex).getTitle())
                        .addAction(R.drawable.previous, "Previous", prevPendingIntent)
                        .addAction(R.drawable.play, "Play/Pause", play_pause_PendingIntent)
                        .addAction(R.drawable.previous, "Next", nextPendingIntent)
                        .setContentIntent(pendingIntent)
                        .build();
            }
        }

        maNotificationManager.notify(27, notif);
     }

}
