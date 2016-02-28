/* eslint strict: 0 */
'use strict';
require('electron-debug')();
const path = require('path');
const electron = require('electron');
const notifier = require('node-notifier');
const moment = require('moment');

const app = electron.app;
const BrowserWindow = electron.BrowserWindow;
const crashReporter = electron.crashReporter;
const ipcMain = electron.ipcMain;


let mainWindow = null;


function log(msg) {
    mainWindow.webContents.send('log', msg);
}


function handleQuitting() {
    mainWindow.on('close', e => {
        if (mainWindow.forceClose !== true) {
            e.preventDefault();
            // mainWindow.hide();
        }
    });

    app.on('window-all-closed', () => {
        if (process.platform !== 'darwin') app.quit();
    });

    app.on('before-quit', () => {
        if (process.platform === 'darwin') {
            mainWindow.forceClose = true;
        }
    });

}
function handleTitleChanges() {
    let originalTitle = "Music stream";

    ipcMain.on('song-changed', function (event, newSong) {
        if (newSong) {
            mainWindow.setTitle(`${newSong.name} - ${newSong.artist} - ${originalTitle}`);
        } else {
            mainWindow.setTitle(originalTitle);
        }
    });
}
function handleUseIdling() {
    var exec = require('child_process').exec;
    var cmd = path.join("lib", "binary", "win32-GetIdleTime.exe");

    setInterval(()=> {
        exec(cmd, function (error, stdout, stderr) {
            log(`Idle time: ${stdout}`);
            mainWindow.webContents.send('idle', stdout);
        });
    }, 60000);


}
function handleGlobalShortcuts() {
    const globalShortcut = electron.globalShortcut;
    let currentSong = null;

    ipcMain.on('song-changed', function (event, newSong) {
        currentSong = newSong;
        if (!currentSong) {
            return;
        }

        currentSong.artistImageBuffer = null;

        // TODO: There might be a race condition in which it would show a picture of a previous artist
        // once the song has changed
        var request = require('request').defaults({encoding: null});
        request.get(currentSong.artistImage, function (err, res, body) {
            currentSong.artistImageBuffer = body;
        });
    });


    globalShortcut.register('F8', function () {
        log('play/pause toggle key pressed');
        mainWindow.webContents.send('playPauseToggle');
    });

    globalShortcut.register('Ctrl+Cmd+Alt+Shift+F8', function () {
        log('show info pressed');
        const notifier = require('node-notifier');
        const iconPath = __dirname + '/app/images/icon.icns';
        log("icon: " + iconPath)

        if (currentSong) {
            let lastPlayed = moment.unix(currentSong.lastPlayed).fromNow();
            var rating = currentSong.rating / 20;
            var stars = "★".repeat(rating);
            var noStars = "☆".repeat(5 - rating);

            notifier.notify({
                title: `${currentSong.artist} - ${currentSong.name}`,
                message: `Rating: ${stars}${noStars}\n` +
                `Play count: ${currentSong.playCount}\n` +
                `Last played: ${lastPlayed}`,
                'appIcon': iconPath,
                'contentImage': __dirname + '/images/icon.icns'
            });
        }
    });


    app.on('will-quit', function () {
        // Unregister all shortcuts.
        globalShortcut.unregisterAll();
    });
}

app.on('ready', () => {
    crashReporter.start();
    mainWindow = new BrowserWindow({width: 1024, height: 728, icon: "app/images/icon.ico"});

    handleTitleChanges();
    handleUseIdling();
    handleGlobalShortcuts();
    handleQuitting();

    if (process.env.HOT) {
        mainWindow.loadURL(`file://${__dirname}/app/hot-dev-index.html`);
    } else {
        mainWindow.loadURL(`file://${__dirname}/dist/index.html`);
    }

});
