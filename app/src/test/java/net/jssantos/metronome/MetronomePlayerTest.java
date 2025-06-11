package net.jssantos.metronome;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MetronomePlayerTest {

    private MetronomePlayer metronomePlayer;

    @Mock
    private SoundPoolPlayer soundPoolPlayer;

    @Before
    public void setUp() {
        metronomePlayer = new MetronomePlayer(soundPoolPlayer);
    }

    // Test cases will be added here

    @Test
    public void testSetBpm() {
        metronomePlayer.setBpm(120);
        assertEquals(120, metronomePlayer.getBpm());

        metronomePlayer.setBpm(20); // Below minimum
        assertEquals(30, metronomePlayer.getBpm());

        metronomePlayer.setBpm(600); // Above maximum
        assertEquals(500, metronomePlayer.getBpm());
    }

    @Test
    public void testSetMeasure() {
        metronomePlayer.setMeasure(4);
        assertEquals(4, metronomePlayer.getMeasure());

        metronomePlayer.setMeasure(0); // Below minimum
        assertEquals(4, metronomePlayer.getMeasure()); // Defaults to 4

        metronomePlayer.setMeasure(-5); // Below minimum
        assertEquals(4, metronomePlayer.getMeasure()); // Defaults to 4
    }

    @Test
    public void testSetVolume() {
        metronomePlayer.setVolume(50);
        assertEquals(50, metronomePlayer.getVolume());
        // Assuming volume is stored as a float percentage internally for SoundPoolPlayer
        // verify(soundPoolPlayer).setVolume(0.5f); // This cannot be verified without actual SoundPoolPlayer logic

        metronomePlayer.setVolume(-10); // Below minimum
        assertEquals(0, metronomePlayer.getVolume());
        // verify(soundPoolPlayer).setVolume(0.0f);


        metronomePlayer.setVolume(150); // Above maximum
        assertEquals(100, metronomePlayer.getVolume());
        // verify(soundPoolPlayer).setVolume(1.0f);
    }

    @Test
    public void testStartStop() {
        assertFalse(metronomePlayer.isPlaying());

        metronomePlayer.start();
        assertTrue(metronomePlayer.isPlaying());

        metronomePlayer.stop();
        assertFalse(metronomePlayer.isPlaying());
    }

    @Test
    public void testAdvance() {
        metronomePlayer.setMeasure(4);
        metronomePlayer.setBeat(0); // Start at the beginning of the measure

        // First beat (bar sound)
        metronomePlayer.advance();
        verify(soundPoolPlayer).playSound(MetronomePlayer.SOUND_BAR);
        assertEquals(1, metronomePlayer.getBeat());

        // Second beat (beat sound)
        metronomePlayer.advance();
        verify(soundPoolPlayer).playSound(MetronomePlayer.SOUND_BEAT);
        assertEquals(2, metronomePlayer.getBeat());

        // Third beat (beat sound)
        metronomePlayer.advance();
        verify(soundPoolPlayer, times(2)).playSound(MetronomePlayer.SOUND_BEAT); // Called twice now
        assertEquals(3, metronomePlayer.getBeat());

        // Fourth beat (beat sound)
        metronomePlayer.advance();
        verify(soundPoolPlayer, times(3)).playSound(MetronomePlayer.SOUND_BEAT); // Called thrice now
        assertEquals(0, metronomePlayer.getBeat()); // Beat should reset to 0

        // Next measure, first beat (bar sound)
        metronomePlayer.advance();
        verify(soundPoolPlayer, times(2)).playSound(MetronomePlayer.SOUND_BAR); // Called twice now
        assertEquals(1, metronomePlayer.getBeat());
    }
}
