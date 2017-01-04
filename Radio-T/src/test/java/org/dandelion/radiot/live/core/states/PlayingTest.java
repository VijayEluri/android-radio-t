package org.dandelion.radiot.live.core.states;

import org.dandelion.radiot.live.core.LiveShowPlayer;
import org.dandelion.radiot.live.core.LiveShowState;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class PlayingTest {
    private LiveShowPlayer player = mock(LiveShowPlayer.class);
    private LiveShowState state = LiveShowState.Playing;

    @Test
    public void togglePlaybackGoesStopping() throws Exception {
        state.togglePlayback(player);
        verify(player).beStopping();
    }

    @Test
    public void onErrorSwitchesToConnecting() throws Exception {
        state.handleError(player);
        verify(player).beConnecting();
    }
}
