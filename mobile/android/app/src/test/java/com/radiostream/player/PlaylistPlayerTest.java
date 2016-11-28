 package com.radiostream.player;

import com.facebook.react.bridge.Arguments;
import com.radiostream.javascript.bridge.PlayerEventsEmitter;
import com.radiostream.javascript.bridge.PlaylistBridge;
import com.radiostream.javascript.bridge.PlaylistPlayerBridge;
import com.radiostream.javascript.bridge.SongBridge;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.List;

import static com.radiostream.player.Utils.resolvedPromise;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Arguments.class, android.util.Log.class})
public class PlaylistPlayerTest {

    @Mock
    Song mockFirstSong;

    @Mock
    Song mockSecondSong;

    @Mock
    Playlist mockPlaylist;

    @Mock
    PlaylistFactory mockPlaylistFactory;

    @Mock
    PlayerEventsEmitter mockPlayerEventsEmitter;

    @Before
    public void setUp() throws Exception {
        Utils.initTestLogging();
        Utils.mockAndroidStatics();

        final SongBridge dummyFirstSongBridge = new SongBridge();
        dummyFirstSongBridge.setTitle("mockFirstSong");
        when(mockFirstSong.toBridgeObject()).thenReturn(dummyFirstSongBridge);

        final SongBridge dummySecondSongBridge = new SongBridge();
        dummyFirstSongBridge.setTitle("mockFirstSong");
        when(mockSecondSong.toBridgeObject()).thenReturn(dummySecondSongBridge);

        when(mockFirstSong.preload()).thenReturn(resolvedPromise(mockFirstSong));
        when(mockSecondSong.preload()).thenReturn(resolvedPromise(mockSecondSong));

        when(mockPlaylist.nextSong())
            .thenReturn(resolvedPromise(mockFirstSong))
            .thenReturn(resolvedPromise(mockSecondSong));

        when(mockPlaylist.peekNextSong())
            .thenReturn(resolvedPromise(mockSecondSong));

        final PlaylistBridge dummyPlaylistBridge = new PlaylistBridge();
        dummyPlaylistBridge.setName("X");
        when(mockPlaylist.toBridgeObject()).thenReturn(dummyPlaylistBridge);
    }

    @Test
    public void play_playsNextSongIfNotSongAvailable() throws Exception {
        PlaylistPlayer playlistPlayer = new PlaylistPlayer(mockPlaylist, mockPlayerEventsEmitter);
        playlistPlayer.play();

        verify(mockFirstSong, times(1)).play();

        ArgumentCaptor<PlaylistPlayerBridge> captor = ArgumentCaptor.forClass(PlaylistPlayerBridge.class);
        verify(mockPlayerEventsEmitter, times(4)).sendPlayerStatus(captor.capture());

        final List<PlaylistPlayerBridge> bridges = captor.getAllValues();

        final PlaylistPlayerBridge loadingStartedState = bridges.get(0);
        assertEquals(true, loadingStartedState.getIsLoading());
        assertNull(loadingStartedState.getSong());

        final PlaylistPlayerBridge songPlayingState = bridges.get(2);
        assertEquals(false, songPlayingState.getIsLoading());
        assertNotNull(songPlayingState.getSong());

    }

    @Test
    public void play_playSongIfSongAvailable() throws Exception {
        PlaylistPlayer playlistPlayer = new PlaylistPlayer(mockPlaylist, mockPlayerEventsEmitter);
        playlistPlayer.play();

        verify(mockPlaylist, times(1)).nextSong();
        verify(mockFirstSong, times(1)).play();

        playlistPlayer.play();
        verify(mockPlaylist, times(1)).nextSong();
        verify(mockFirstSong, times(2)).play();
    }

    @Test
    public void playNext_playingSecondSong() throws Exception {
        PlaylistPlayer playlistPlayer = new PlaylistPlayer(mockPlaylist, mockPlayerEventsEmitter);
        playlistPlayer.playNext();
        playlistPlayer.playNext();

        verify(mockFirstSong, times(1)).play();
        verify(mockFirstSong, times(1)).close();
        verify(mockSecondSong, times(1)).play();
    }

    @Test
    public void playNext_playFirstSong() throws Exception {
        PlaylistPlayer playlistPlayer = new PlaylistPlayer(mockPlaylist, mockPlayerEventsEmitter);
        playlistPlayer.playNext();

        verify(mockFirstSong, times(1)).play();
    }

    @Test(expected=IllegalStateException.class)
    public void pause_throwsExceptionIfNoSong() throws Exception {
        PlaylistPlayer playlistPlayer = new PlaylistPlayer(mockPlaylist, mockPlayerEventsEmitter);
        playlistPlayer.pause();
    }

    @Test
    public void close_closesPlaylist() throws Exception {
        PlaylistPlayer playlistPlayer = new PlaylistPlayer(mockPlaylist, mockPlayerEventsEmitter);
        playlistPlayer.close();
    }

    @Test
    public void close_closesSongIfExists() throws Exception {
        PlaylistPlayer playlistPlayer = new PlaylistPlayer(mockPlaylist, mockPlayerEventsEmitter);
        playlistPlayer.play();
        playlistPlayer.close();

        verify(mockFirstSong, times(1)).close();
    }

    @Test
    public void playNext_retriesOnFailure() throws Exception {
        when(mockFirstSong.preload()).thenReturn(Utils.<Song>rejectedPromise(new Exception()));

        PlaylistPlayer playlistPlayer = new PlaylistPlayer(mockPlaylist, mockPlayerEventsEmitter);
        playlistPlayer.playNext();

        // second song will be loaded if preloading the first one failed
        verify(mockSecondSong, times(1)).play();
    }
}