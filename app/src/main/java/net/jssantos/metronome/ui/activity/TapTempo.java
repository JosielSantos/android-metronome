package net.jssantos.metronome.ui.activity;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.TextView;

import com.javonharper.tempo.BpmCalculator;
import net.jssantos.metronome.R;

public class TapTempo extends Base
{
    BpmCalculator bpmCalculator;
    Timer timer;
    int bpm=0, measure;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_tap_tempo);
        bpmCalculator = new BpmCalculator();
        Intent intent=getIntent();
        measure=intent.getIntExtra("net.jssantos.metronome.measure", 4);
        metr.setMeasure(measure);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        initialize();
    }

    @Override
    protected void onDestroy()
    {
        stopResetTimer();
        super.onDestroy();
    }

    @Override
    public void finish()
    {
        metr.stop();
        SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
        Intent data=new Intent();
        if(prefs.getBoolean("pass_taptempo_bpm_to_mainscreen", true)) {
            getBpmIfApplicable();
            data.putExtra("bpm", bpm);
        }
        bpmCalculator.clearTimes();
        setResult(RESULT_OK, data);
        super.finish();
    }

    private void initialize()
    {
        TextView bpmTextView = (TextView) findViewById(R.id.bpmTextView);
        bpmTextView.setText("0");
        setupTouchListener();
    }

    private void setupTouchListener()
    {
        View layout= (View) findViewById(R.id.actTempo);
        layout.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    handleTouch();
                    return true;
                }
                return false;
            }
        });
    }

    public void handleTouch()
    {
        metr.stop();
        vibrate(50);
        bpmCalculator.recordTime();
        restartResetTimer();
        updateView();
    }

    private void getBpmIfApplicable()
    {
        if (bpmCalculator.times.size() >= 2) {
            bpm=bpmCalculator.getBpm();
            bpm=((bpm<30)?30:(bpm>500)?500:bpm);
        }
    }

    private void updateView()
    {
        String displayValue;
        getBpmIfApplicable();
        if(bpm>0) {
            displayValue = Integer.valueOf(bpm).toString();
        } else {
            displayValue = "Toque novamente";
        }
        TextView bpmTextView = (TextView) findViewById(R.id.bpmTextView);
        bpmTextView.setText(displayValue);
    }

    private void restartResetTimer()
    {
        stopResetTimer();
        startResetTimer();
    }

    private void startResetTimer()
    {
        timer = new Timer("reset-bpm-calculator", true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                getBpmIfApplicable();
                if(bpm>0) {
                    metr.setBpm(bpm);
                    metr.stop();
                    metr.play();
                }
                bpmCalculator.clearTimes();
            }
        }, 2000);
    }

    private void stopResetTimer()
    {
        if (timer != null) {
            timer.cancel();
        }
    }
}
