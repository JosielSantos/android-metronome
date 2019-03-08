package com.josantos.metronome.service;

import java.io.IOException;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.preference.PreferenceManager;

import com.josantos.metronome.MetronomePlayer;

public class Metronome implements AudioManager.OnAudioFocusChangeListener
{
    private Context context;
    private AudioManager audioManager;
    private MetronomePlayer player;
    private Thread playerThread=null;
    private SoundPool sp=null;
    private int soundId1, soundId2;
    private int bpm=120, measure=4, volume=100;
    private float noteLength=1.0F;
    private MetronomeListener listener=null;
    private boolean hasAudioFocus=false;

    public Metronome(Context context)
    {
        this.context=context;
        audioManager=(AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        initializeSoundPool();
        player=new MetronomePlayer(sp, soundId1, soundId2);
    }

    private void initializeSoundPool()
    {
        if(sp!=null) {
            sp.release();
            sp=null;
        }
        try {
            sp=new SoundPool.Builder()
                .setMaxStreams(1)
                .build();
            soundId1=sp.load(context.getAssets().openFd("beats/default_bar.wav"), 1);
            soundId2=sp.load(context.getAssets().openFd("beats/default_beat.wav"), 1);
        } catch(IOException ioExc) {
            if(sp!=null) {
                sp.release();
                sp=null;
            }
        }
    }

    public Metronome setListener(MetronomeListener listener)
    {
        this.listener=listener;
        return this;
    }

    public Metronome setBpm(int bpm)
    {
        this.bpm=bpm;
        return this;
    }

    public Metronome setMeasure(int measure)
    {
        this.measure=measure;
        return this;
    }

    public Metronome setNoteLength(float noteLength)
    {
        this.noteLength=noteLength;
        return this;
    }

    public Metronome setVolume(int volume)
    {
        this.volume=volume;
        player.setVolume(volume);
        return this;
    }

    public boolean isPlaying()
    {
        return player.isPlaying() && playerThread!=null;
    }

    public boolean stopForOtherAudioApps()
    {
        SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean("stop_for_other_apps", false);
    }

    public void play()
    {
        boolean canPlay=true;
        if(stopForOtherAudioApps()) {
            int result=audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            if(result!=AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                canPlay=false;
            } else {
                canPlay=true;
                hasAudioFocus=true;
            }
        }
        if(canPlay) {
            playerThread=new Thread(player, "MetronomePlayer");
            player.setBpm(bpm);
            player.setMeasure(measure);
        player.setNoteLength(noteLength);
            player.setVolume(volume);
            playerThread.start();
            if(listener!=null) {
                listener.onMetrPlay();
            }
        }
    }

    public void stop()
    {
        if(isPlaying()) {
            player.setPlaying(false);
            playerThread.interrupt();
            playerThread=null;
            if(hasAudioFocus) {
                audioManager.abandonAudioFocus(this);
                hasAudioFocus=false;
            }
            if(listener!=null) {
                listener.onMetrStop();
            }
        }
    }

    public void restartIfPlaying()
    {
        if(isPlaying()) {
            stop();
            play();
        }
    }

    @Override
    public void onAudioFocusChange(int focusChange)
    {
        switch(focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS: {
                if(stopForOtherAudioApps()) {
                    stop();
                }
                break;
            }
        }
    }

    public interface MetronomeListener
    {
        public void onMetrPlay();
        public void onMetrStop();
    }
}
