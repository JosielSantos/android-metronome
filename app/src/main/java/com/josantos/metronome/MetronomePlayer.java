package com.josantos.metronome;

import java.util.Timer;
import java.util.TimerTask;

import android.media.SoundPool;

public class MetronomePlayer
{
    private int bpm=120;
    private int measure=4;
    private float noteLength=1.0F;
    private float volume=1.0F;

    private volatile boolean playing=false;
    private SoundPool sp;
    private int id1, id2;
    private Timer timer;
    private TimerTask task=null;
    private int beatsPerMeasure=4;
    private int beat=1;

    public MetronomePlayer(SoundPool sp, int id1, int id2)
    {
        this.sp=sp;
        this.id1=id1;
        this.id2=id2;
        this.timer=new Timer("metronomePlayer");
    }

    public int getBpm()
    {
        return bpm;
    }

    public MetronomePlayer setBpm(int bpm)
    {
        bpm=(bpm<30)?30:bpm;
        bpm=(bpm>500)?500:bpm;
        this.bpm=bpm;
        return this;
    }

    public int getMeasure()
    {
        return measure;
    }

    public MetronomePlayer setMeasure(int measure)
    {
        measure=(measure<1)?4:measure;
        this.measure=measure;
        return this;
    }

    public float getNoteLength()
    {
        return noteLength;
    }

    public MetronomePlayer setNoteLength(float noteLength)
    {
        this.noteLength=noteLength;
        return this;
    }

    public float getVolume()
    {
        return volume;
    }

    public MetronomePlayer setVolume(int volume)
    {
        volume=(volume<0 || volume>100)?100:volume;
        this.volume=(float)volume/100.0F;
        return this;
    }

    public boolean isPlaying()
    {
        return playing;
    }

    public void start()
    {
        playing=true;
        beatsPerMeasure=Math.round(measure/noteLength);
        beat=1;
        long interval=Math.round(60000/bpm*noteLength);
        task=new TimerTask() {
            public void run()
            {
                advance();
            }
        };
        timer.scheduleAtFixedRate(task, 0l, interval);
    }

    public void stop()
    {
        task.cancel();
        task=null;
        playing=false;
    }

    private void advance()
    {
        if(playing) {
            if(beat==1) {
                sp.play(id1, volume, volume, 0, 0, 1.0F);
            } else {
                sp.play(id2, volume, volume, 0, 0, 1.0F);
            }
            if(beat==beatsPerMeasure) {
                beat=1;
            } else {
                beat++;
            }
        }
        return;
    }
}
