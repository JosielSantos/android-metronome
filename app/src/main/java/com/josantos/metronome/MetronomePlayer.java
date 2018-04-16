package com.josantos.metronome;

import android.media.SoundPool;

public class MetronomePlayer implements Runnable
{
    private int bpm=120;
    private int measure=4;
    private float volume=1.0F;
    private volatile boolean playing=false;
    private SoundPool sp;
    private int id1, id2;

    public MetronomePlayer(SoundPool sp, int id1, int id2)
    {
        this.sp=sp;
        this.id1=id1;
        this.id2=id2;
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

    public void setPlaying(boolean playing)
    {
        this.playing=playing;
    }

    @Override
    public void run()
    {
        playing=true;
        for(long beat=0; playing; ++beat) {
            if(beat%measure==0) {
                sp.play(id1, volume, volume, 0, 0, 1.0F);
            } else {
                sp.play(id2, volume, volume, 0, 0, 1.0F);
            }
            try {
                Thread.sleep((long)60000/bpm);
            } catch(InterruptedException intExc) {
            }
        }
        return;
    }
}
