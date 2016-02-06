import loggerCreator from '../utils/logger'
//noinspection JSUnresolvedVariable
var logger = loggerCreator(__filename);

import * as actionTypes from '../actions/action_types'
import * as musicActions from '../actions/music_actions';
import storeContainer, { observeStore } from '../utils/store_container';

export function connect() {
// For desktop mode only
    if (!__WEB__) {

        function playPauseToggle() {
            var state = storeContainer.store.getState();

            let currentPlaylist = state.currentPlaylist;
            let currentSong = state.currentSongAsync.data;

            let action = musicActions.playTogglePlaylistAction(currentPlaylist.name, currentSong);
            storeContainer.store.dispatch(action);
        }

        require('ipc').on('log', function (msg) {
            logger.debug(msg);
        });

        require('ipc').on('playPauseToggle', function () {
            logger.debug("received message: playPauseToggle");
            playPauseToggle();
        });

        require('ipc').on('idle', function (idleOutput) {
            logger.debug("received idle: " + idleOutput);
            const idleSeconds = parseInt(idleOutput);

            var state = storeContainer.store.getState();
            if (idleSeconds > 60 && state.isPlaying) {
                logger.debug("idle too long - pausing song");
                playPauseToggle();
            }

        });

        const ipcRenderer = require('electron').ipcRenderer;
        observeStore(state => state.currentSongAsync.data, currentSong => {
            ipcRenderer.send('song-changed', currentSong);
        });

    }
}