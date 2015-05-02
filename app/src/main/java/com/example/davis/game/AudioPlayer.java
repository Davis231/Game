package com.example.davis.game;

import android.content.Context;
import android.media.MediaPlayer;

public class AudioPlayer {

    private MediaPlayer mPlayer;
    private boolean mCompleted = false;

    public void stop() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    public void play(Context c) {

        if(mPlayer == null){
            mPlayer = MediaPlayer.create(c, R.raw.multifaros_the_factory);
        }

        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                stop();
                mCompleted = true;
            }
        });

        mPlayer.start();
    }

    public void pause() {
        if(mPlayer != null){
            mPlayer.pause();
        }
    }
}