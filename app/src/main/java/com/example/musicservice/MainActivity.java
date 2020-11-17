package com.example.musicservice;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    ImageView music;
    ImageView previous;
    ImageView start;
    ImageView pause;
    ImageView next;
    final static int REQUEST_PERMISSION = 999 ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        music = findViewById(R.id.music);
        music.setImageResource(R.drawable.music3);


        previous  = findViewById(R.id.previous);
        start = findViewById(R.id.start);
        pause = findViewById(R.id.pause);
        next  = findViewById(R.id.next);

        start.setImageResource(R.drawable.play);
        pause.setImageResource(R.drawable.pause);
        next.setImageResource(R.drawable.next);
        previous.setImageResource(R.drawable.previous);

        pause.setVisibility(View.GONE);
        next.setVisibility(View.GONE);
        previous.setVisibility(View.GONE);


        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},REQUEST_PERMISSION);
        }else{
            start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playSong();
                }
            });
        }

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pauseSong();
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNextSong();
            }
        });
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPreviousSong();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                start.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playSong();
                    }
                });
            }
        }
    }

    public void playSong(){
            start.setVisibility(View.GONE);
            pause.setVisibility(View.VISIBLE);
            next.setVisibility(View.VISIBLE);
            previous.setVisibility(View.VISIBLE);

            MusicService.currentAction = "Play";
            startService(new Intent(getApplicationContext(), MusicService.class));
    }

    public void pauseSong(){

        previous.setVisibility(View.VISIBLE);
        start.setVisibility(View.VISIBLE);
        pause.setVisibility(View.GONE);
        next.setVisibility(View.VISIBLE);

        stopService(new Intent(getApplicationContext(), MusicService.class));
        MusicService.currentAction = "Pause";
        startService(new Intent(getApplicationContext(), MusicService.class));

    }

    public  void playNextSong(){
            int cursor = MusicService.currrentMusicIndex++;
            stopService(new Intent(getApplicationContext(), MusicService.class));
            MusicService.currentAction = "Next" ;
            MusicService.currrentMusicIndex = cursor;
            startService(new Intent(getApplicationContext(), MusicService.class));
    }

    public  void playPreviousSong(){
            int cursor = MusicService.currrentMusicIndex--;
            stopService(new Intent(getApplicationContext(), MusicService.class));
            MusicService.currentAction = "Previous";
            MusicService.currrentMusicIndex = cursor ;
            startService(new Intent(getApplicationContext(), MusicService.class));
    }


}