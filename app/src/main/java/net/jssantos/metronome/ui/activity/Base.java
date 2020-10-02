package net.jssantos.metronome.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;

import net.jssantos.metronome.service.Metronome;
import net.jssantos.metronome.R;

public abstract class Base extends Activity
{
    protected Metronome metr;
    private Vibrator vibes=null;

    public void clickSettings(MenuItem item)
    {
        metr.stop();
        Intent intent=new Intent(this, Settings.class);
        startActivity(intent);
    }

    public void clickExit(MenuItem item)
    {
        Intent intent=new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        metr.stop();
        startActivity(intent);
        finish();
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        metr=new Metronome (this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    protected void hideKeyboard()
    {
        InputMethodManager inputManager=(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    protected void vibrate(long millis)
    {
        if(vibes==null) {
            vibes=(Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        }
        vibes.vibrate(millis);
    }
}
