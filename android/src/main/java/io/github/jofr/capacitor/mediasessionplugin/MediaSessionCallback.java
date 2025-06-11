package io.github.jofr.capacitor.mediasessionplugin;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.KeyEvent;

import com.getcapacitor.JSObject;

public class MediaSessionCallback extends MediaSessionCompat.Callback {
    private static final String TAG = "MediaSessionCallback";

    private final MediaSessionPlugin plugin;

    MediaSessionCallback(MediaSessionPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * This callback handles media button events for all Android versions.
     * The onMediaButtonEvent method provides comprehensive support for Bluetooth headsets
     * and other media button sources by handling both specific key codes (like KEYCODE_MEDIA_PAUSE)
     * and the generic KEYCODE_MEDIA_PLAY_PAUSE toggle button commonly used by Bluetooth devices.
     */

    @Override
    public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
        KeyEvent keyEvent = mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        if (keyEvent == null) {
            Log.w(TAG, "Received media button event with no KeyEvent");
            return false;
        }

        // Only handle key down events to avoid duplicate calls
        if (keyEvent.getAction() != KeyEvent.ACTION_DOWN) {
            return false;
        }

        Log.d(TAG, "Media button event: " + keyEvent.getKeyCode());

        switch (keyEvent.getKeyCode()) {
            case KeyEvent.KEYCODE_MEDIA_PLAY:
                // Some Bluetooth devices send KEYCODE_MEDIA_PLAY (126) and KEYCODE_MEDIA_PAUSE (127) 
                // alternately instead of KEYCODE_MEDIA_PLAY_PAUSE (85). Handle as toggle for consistency.
                handlePlayPauseToggle();
                return true;
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
                // Some Bluetooth devices send KEYCODE_MEDIA_PAUSE (127) instead of KEYCODE_MEDIA_PLAY_PAUSE (85)
                // Handle as toggle for consistency with KEYCODE_MEDIA_PLAY behavior
                handlePlayPauseToggle();
                return true;
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                // Handle play/pause toggle button (common on Bluetooth headsets)
                handlePlayPauseToggle();
                return true;
            case KeyEvent.KEYCODE_MEDIA_STOP:
                plugin.actionCallback("stop");
                return true;
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                plugin.actionCallback("nexttrack");
                return true;
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                plugin.actionCallback("previoustrack");
                return true;
            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                plugin.actionCallback("seekforward");
                return true;
            case KeyEvent.KEYCODE_MEDIA_REWIND:
                plugin.actionCallback("seekbackward");
                return true;
            default:
                Log.w(TAG, "Unhandled media button: " + keyEvent.getKeyCode());
                // Let the system handle it via the default implementation
                return super.onMediaButtonEvent(mediaButtonEvent);
        }
    }

    private void handlePlayPauseToggle() {
        // For Bluetooth headsets that send KEYCODE_MEDIA_PLAY_PAUSE,
        // we need to determine whether to play or pause based on current state
        // 
        // IMPORTANT: This method depends on the app calling MediaSession.setPlaybackState()
        // after handling each action. If the app doesn't update the state, this toggle
        // logic will make incorrect decisions.
        Log.d(TAG, "Handling play/pause toggle");
        
        // Get current playback state from plugin to determine correct action
        String currentState = plugin.getPlaybackState();
        Log.d(TAG, "Current playback state: " + currentState);
        
        if ("playing".equals(currentState)) {
            // Currently playing, so trigger pause
            if (plugin.hasActionHandler("pause")) {
                Log.d(TAG, "Toggle: triggering pause (was playing)");
                plugin.actionCallback("pause");
            }
        } else {
            // Currently paused/stopped/none, so trigger play
            if (plugin.hasActionHandler("play")) {
                Log.d(TAG, "Toggle: triggering play (was not playing)");
                plugin.actionCallback("play");
            } else if (plugin.hasActionHandler("pause")) {
                // Fallback for compatibility
                Log.d(TAG, "Toggle: fallback to pause (no play handler)");
                plugin.actionCallback("pause");
            }
        }
    }

    @Override
    public void onPlay() {
        Log.d(TAG, "onPlay() called");
        plugin.actionCallback("play");
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause() called");
        plugin.actionCallback("pause");
    }

    @Override
    public void onSeekTo(long pos) {
        Log.d(TAG, "onSeekTo() called with position: " + pos);
        JSObject data = new JSObject();
        data.put("seekTime", (double) pos/1000.0);
        plugin.actionCallback("seekto", data);
    }

    @Override
    public void onRewind() {
        Log.d(TAG, "onRewind() called");
        plugin.actionCallback("seekbackward");
    }

    @Override
    public void onFastForward() {
        Log.d(TAG, "onFastForward() called");
        plugin.actionCallback("seekforward");
    }

    @Override
    public void onSkipToPrevious() {
        Log.d(TAG, "onSkipToPrevious() called");
        plugin.actionCallback("previoustrack");
    }

    @Override
    public void onSkipToNext() {
        Log.d(TAG, "onSkipToNext() called");
        plugin.actionCallback("nexttrack");
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop() called");
        plugin.actionCallback("stop");
    }
}
