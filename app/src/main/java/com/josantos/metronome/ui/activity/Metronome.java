package com.josantos.metronome.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.josantos.metronome.R;
import com.josantos.metronome.service.Metronome.MetronomeListener;

public class Metronome extends Base implements MetronomeListener
{
    private NumberPicker npbpm;
    private EditText etMeasure;
    private SeekBar volumeBar;
    private ToggleButton btnPlayStop;
    private Spinner noteFiguresList;
    private float[] noteFigures = {0.25F, 0.5F, 1.0F, 2.0F};

    private void setDefaultValues()
    {
        SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
        npbpm.setValue(prefs.getInt("default_bpm", 120));
        etMeasure.setText(Integer.toString(prefs.getInt("default_measure", 4)));
        volumeBar.setProgress(prefs.getInt("volume", 100));
    }

    private void setParameters()
    {
        metr.setBpm(npbpm.getValue());
        metr.setMeasure(Integer.parseInt(etMeasure.getText().toString()));
        metr.setVolume(volumeBar.getProgress());
    }

    public void clickAssumeDefaultValues(View btn)
    {
        setDefaultValues();
    }

    public void clickSetDefault(View btn)
    {
        SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor=prefs.edit();
        editor.putInt("default_bpm", npbpm.getValue());
        editor.putInt("default_measure", Integer.parseInt(etMeasure.getText().toString()));
        editor.commit();
    }

    public void clickTapTempo(View btn)
    {
        metr.stop();
        Intent tapTempoIntent=new Intent(this, TapTempo.class);
        tapTempoIntent.putExtra("com.josantos.metronome.actualBpm", npbpm.getValue());
        tapTempoIntent.putExtra("com.josantos.metronome.measure", Integer.parseInt(etMeasure.getText().toString()));
        startActivityForResult(tapTempoIntent, 0);
    }

    public void clickPlayStop(View btn)
    {
        if(!metr.isPlaying()) {
            setParameters();
            metr.play();
        } else {
            metr.stop();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        setContentView(R.layout.act_metronome);
        metr.setListener(this);
        setupNpbpm();
        setupEtMeasure();
        setupVolumeBar();
        setupNoteFiguresList();
        btnPlayStop=(ToggleButton)findViewById(R.id.btnPlayStop);
        setDefaultValues();
    }

    private void setupNpbpm()
    {
        npbpm=(NumberPicker)findViewById(R.id.npbpm);
        npbpm.setMinValue(30);
        npbpm.setMaxValue(500);
        npbpm.setWrapSelectorWheel(true);
        npbpm.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override public void onValueChange(NumberPicker npk, int oldVal, int newVal) {
                setParameters();
                metr.restartIfPlaying();
            }
        });
    }

    private void setupEtMeasure()
    {
        etMeasure=(EditText)findViewById(R.id.etMeasure);
        etMeasure.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override public boolean onEditorAction(TextView edt, int actionId, KeyEvent key) {
                boolean handled=false;
                if(actionId==EditorInfo.IME_ACTION_DONE) {
                    setParameters();
                    metr.restartIfPlaying();
                    edt.clearFocus();
                    hideKeyboard();
                    handled=true;
                }
                return handled;
            }
        });
    }

    private void setupVolumeBar()
    {
        volumeBar=(SeekBar)findViewById(R.id.volumeBar);
        volumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                metr.setVolume(progressValue);
                SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(Metronome.this);
                SharedPreferences.Editor editor=prefs.edit();
                editor.putInt("volume", progressValue);
                editor.commit();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });
    }

    private void setupNoteFiguresList()
    {
        noteFiguresList=(Spinner)findViewById(R.id.noteFiguresList);
        String[] figureNames=new String[] {"Semicolcheia", "Colcheia", "Semínima", "Mínima"};
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, figureNames);
        noteFiguresList.setAdapter(adapter);
        noteFiguresList.setSelection(2);
        noteFiguresList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                metr.setNoteLength(noteFigures[pos]);
                metr.restartIfPlaying();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch(requestCode) {
            case 0: {
                onTapTempoResult(resultCode, data);
                break;
            }
        }
    }

    private void onTapTempoResult(int resultCode, Intent data)
    {
        if(resultCode==RESULT_OK && data instanceof Intent) {
            int newBpm=data.getIntExtra("bpm", 0);
            if(newBpm>0) {
                npbpm.setValue(newBpm);
            }
        }
    }

    public void onMetrPlay()
    {
        btnPlayStop.setChecked(true);
    }

    public void onMetrStop()
    {
        btnPlayStop.setChecked(false);
    }
}
