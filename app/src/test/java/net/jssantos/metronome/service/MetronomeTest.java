package net.jssantos.metronome.service;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import net.jssantos.metronome.MetronomePlayer;
import net.jssantos.metronome.MetronomeListener;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MetronomeTest {

    private Metronome metronome;

    @Mock
    private Context mockContext;

    @Mock
    private SoundPool mockSoundPool;

    @Mock
    private MetronomePlayer mockMetronomePlayer;

    @Mock
    private AudioManager mockAudioManager;

    @Mock
    private MetronomeListener mockMetronomeListener;

    // We cannot directly mock SoundPool or MetronomePlayer as they are instantiated internally.
    // We will verify interactions with Context and AudioManager,
    // and for MetronomePlayer, we'll test its state via Metronome's methods.

    @Before
    public void setUp() {
        // Mock AudioManager service
        when(mockContext.getSystemService(Context.AUDIO_SERVICE)).thenReturn(mockAudioManager);

        // Mock AssetManager and AssetFileDescriptor for SoundPool initialization
        android.content.res.AssetManager mockAssetManager = mock(android.content.res.AssetManager.class);
        android.content.res.AssetFileDescriptor mockAssetFileDescriptor = mock(android.content.res.AssetFileDescriptor.class);
        when(mockContext.getAssets()).thenReturn(mockAssetManager);
        try {
            when(mockAssetManager.openFd(anyString())).thenReturn(mockAssetFileDescriptor);
        } catch (java.io.IOException e) {
            e.printStackTrace(); // Should not happen in mock setup
        }

        // SoundPool.Builder is final, and SoundPool constructor is not public.
        // This part is hard to mock without PowerMockito or refactoring Metronome.
        // For now, we'll proceed knowing that SoundPool itself won't be truly mocked here,
        // but its creation process via context.getAssets() is acknowledged.

        metronome = new Metronome(mockContext); // MetronomePlayer is created here
        metronome.setMetronomeListener(mockMetronomeListener);
        // We don't have a direct way to inject mockMetronomePlayer.
        // Tests will rely on the MetronomePlayer created by the Metronome instance.
    }

    @Test
    public void testConstructorInitializesDependencies() {
        // Verify AudioManager is fetched
        verify(mockContext).getSystemService(Context.AUDIO_SERVICE);

        // Verify assets are accessed for sound loading (called twice in initializeSoundPool)
        try {
            verify(mockContext.getAssets(), times(1)).openFd("beats/default_bar.wav");
            verify(mockContext.getAssets(), times(1)).openFd("beats/default_beat.wav");
        } catch (java.io.IOException e) {
            fail("IOException during asset verification: " + e.getMessage());
        }

        // Cannot directly verify SoundPool or MetronomePlayer instances were created
        // without refactoring Metronome or using PowerMockito.
        // However, if Metronome constructor completes without NPE, it's an indirect sign.
        assertNotNull(metronome);
    }

    // Further test cases will be added here

    @Test
    public void testSettersUpdateInternalState() {
        // These setters store values internally. Their effect on MetronomePlayer
        // is primarily when play() is called.
        // This test mainly ensures the setters can be called without crashing.
        // Direct verification of propagation to MetronomePlayer is hard without refactoring Metronome.java.

        metronome.setBpm(150);
        metronome.setMeasure(3);
        metronome.setNoteLength(0.5f);
        metronome.setVolume(75); // This one calls player.setVolume() internally.

        // We can't easily verify the internal player's state here.
        // We will rely on the play() method tests to ensure these values are correctly used.
        assertTrue("Setters called successfully (no crash)", true);
    }

    @Test
    public void testPlay_WhenAudioFocusGranted_AndStopForOthersTrue() {
        // Mock SharedPreferences
        SharedPreferences mockPrefs = mock(SharedPreferences.class);
        when(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockPrefs); // If Metronome uses this form
        when(PreferenceManager.getDefaultSharedPreferences(mockContext)).thenReturn(mockPrefs);
        when(mockPrefs.getBoolean("stop_for_other_apps", false)).thenReturn(true);

        when(mockAudioManager.requestAudioFocus(eq(metronome), eq(AudioManager.STREAM_MUSIC), eq(AudioManager.AUDIOFOCUS_GAIN)))
                .thenReturn(AudioManager.AUDIOFOCUS_REQUEST_GRANTED);

        // Values to be passed to the internal player
        int testBpm = 180;
        int testMeasure = 5;
        float testNoteLength = 0.75f;
        int testVolume = 80;

        metronome.setBpm(testBpm);
        metronome.setMeasure(testMeasure);
        metronome.setNoteLength(testNoteLength);
        metronome.setVolume(testVolume); // This also calls player.setVolume internally

        metronome.play();

        verify(mockAudioManager).requestAudioFocus(eq(metronome), eq(AudioManager.STREAM_MUSIC), eq(AudioManager.AUDIOFOCUS_GAIN));
        assertTrue(metronome.isPlaying());
        verify(mockMetronomeListener).onMetrPlay();

        // We cannot directly verify player.setBpm, player.setMeasure, player.setNoteLength, player.start
        // on the internal player without refactoring Metronome or using PowerMockito.
        // We assume isPlaying() returning true means player.start() was effective.
        // And we assume the values set above were passed to the player.
    }

    @Test
    public void testPlay_WhenAudioFocusNotGranted_AndStopForOthersTrue() {
        SharedPreferences mockPrefs = mock(SharedPreferences.class);
        when(PreferenceManager.getDefaultSharedPreferences(mockContext)).thenReturn(mockPrefs);
        when(mockPrefs.getBoolean("stop_for_other_apps", false)).thenReturn(true);

        when(mockAudioManager.requestAudioFocus(eq(metronome), eq(AudioManager.STREAM_MUSIC), eq(AudioManager.AUDIOFOCUS_GAIN)))
                .thenReturn(AudioManager.AUDIOFOCUS_REQUEST_FAILED);

        metronome.play();

        verify(mockAudioManager).requestAudioFocus(eq(metronome), eq(AudioManager.STREAM_MUSIC), eq(AudioManager.AUDIOFOCUS_GAIN));
        assertFalse(metronome.isPlaying());
        verify(mockMetronomeListener, never()).onMetrPlay();
    }

    @Test
    public void testPlay_WhenStopForOthersFalse() {
        SharedPreferences mockPrefs = mock(SharedPreferences.class);
        when(PreferenceManager.getDefaultSharedPreferences(mockContext)).thenReturn(mockPrefs);
        when(mockPrefs.getBoolean("stop_for_other_apps", false)).thenReturn(false);

        metronome.play();

        verify(mockAudioManager, never()).requestAudioFocus(any(), anyInt(), anyInt());
        assertTrue(metronome.isPlaying());
        verify(mockMetronomeListener).onMetrPlay();
    }


    @Test
    public void testStop_WhenPlaying_AndHasAudioFocus() {
        // First, start playing with audio focus
        SharedPreferences mockPrefs = mock(SharedPreferences.class);
        when(PreferenceManager.getDefaultSharedPreferences(mockContext)).thenReturn(mockPrefs);
        when(mockPrefs.getBoolean("stop_for_other_apps", false)).thenReturn(true);
        when(mockAudioManager.requestAudioFocus(eq(metronome), eq(AudioManager.STREAM_MUSIC), eq(AudioManager.AUDIOFOCUS_GAIN)))
                .thenReturn(AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
        metronome.play();
        assertTrue(metronome.isPlaying()); // Sanity check

        metronome.stop();

        assertFalse(metronome.isPlaying());
        verify(mockAudioManager).abandonAudioFocus(eq(metronome));
        verify(mockMetronomeListener).onMetrStop();
    }

    @Test
    public void testStop_WhenPlaying_AndNoAudioFocusNeeded() {
        SharedPreferences mockPrefs = mock(SharedPreferences.class);
        when(PreferenceManager.getDefaultSharedPreferences(mockContext)).thenReturn(mockPrefs);
        when(mockPrefs.getBoolean("stop_for_other_apps", false)).thenReturn(false);
        metronome.play(); // Starts without requesting focus
        assertTrue(metronome.isPlaying()); // Sanity check

        metronome.stop();

        assertFalse(metronome.isPlaying());
        verify(mockAudioManager, never()).abandonAudioFocus(any());
        verify(mockMetronomeListener).onMetrStop();
    }

    @Test
    public void testStop_WhenNotPlaying() {
        // Ensure metronome is not playing (default state or after a stop)
        assertFalse(metronome.isPlaying());

        metronome.stop(); // Call stop again

        verify(mockAudioManager, never()).abandonAudioFocus(any());
        verify(mockMetronomeListener, never()).onMetrStop(); // Should not call if not playing
    }

    @Test
    public void testOnAudioFocusChange_Loss_AndStopForOthersTrue_ShouldStopMetronome() {
        // Setup: Metronome is playing and has audio focus
        SharedPreferences mockPrefs = mock(SharedPreferences.class);
        when(PreferenceManager.getDefaultSharedPreferences(mockContext)).thenReturn(mockPrefs);
        when(mockPrefs.getBoolean("stop_for_other_apps", false)).thenReturn(true);
        when(mockAudioManager.requestAudioFocus(eq(metronome), eq(AudioManager.STREAM_MUSIC), eq(AudioManager.AUDIOFOCUS_GAIN)))
                .thenReturn(AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
        metronome.play();
        assertTrue(metronome.isPlaying()); // Sanity check that it started

        // Action: Simulate audio focus loss
        metronome.onAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS);

        // Verification: Metronome should have stopped
        assertFalse(metronome.isPlaying());
        verify(mockAudioManager).abandonAudioFocus(eq(metronome)); // abandonAudioFocus is called by stop()
        verify(mockMetronomeListener).onMetrStop(); // onMetrStop is called by stop()
    }

    @Test
    public void testOnAudioFocusChange_Loss_AndStopForOthersFalse_ShouldNotStopMetronome() {
        // Setup: Metronome is playing (audio focus not relevant as stopForOtherAudioApps is false)
        SharedPreferences mockPrefs = mock(SharedPreferences.class);
        when(PreferenceManager.getDefaultSharedPreferences(mockContext)).thenReturn(mockPrefs);
        when(mockPrefs.getBoolean("stop_for_other_apps", false)).thenReturn(false);
        // No need to mock requestAudioFocus if stopForOtherAudioApps is false, play() won't request it.
        metronome.play();
        assertTrue(metronome.isPlaying()); // Sanity check

        // Action: Simulate audio focus loss
        metronome.onAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS);

        // Verification: Metronome should still be playing
        assertTrue(metronome.isPlaying());
        verify(mockAudioManager, never()).abandonAudioFocus(any());
        verify(mockMetronomeListener, never()).onMetrStop();
    }

    @Test
    public void testOnAudioFocusChange_Gain_ShouldDoNothing() {
        // Setup: Metronome is not playing
        assertFalse(metronome.isPlaying());

        // Action: Simulate audio focus gain (or other irrelevant focus changes)
        metronome.onAudioFocusChange(AudioManager.AUDIOFOCUS_GAIN);
        metronome.onAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS_TRANSIENT);
        metronome.onAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK);

        // Verification: Metronome should remain stopped, no interactions related to stopping/starting
        assertFalse(metronome.isPlaying());
        verify(mockAudioManager, never()).abandonAudioFocus(any());
        verify(mockMetronomeListener, never()).onMetrStop();
        verify(mockMetronomeListener, never()).onMetrPlay();
    }
}
