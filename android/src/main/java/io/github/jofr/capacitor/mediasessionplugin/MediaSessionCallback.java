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
                plugin.actionCallback("play");
                return true;
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
                plugin.actionCallback("pause");
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
        // Since we don't have direct access to playback state here, we'll trigger
        // both actions and let the plugin handle the logic
        Log.d(TAG, "Handling play/pause toggle");
        
        // Check if we have both play and pause handlers
        if (plugin.hasActionHandler("play") && plugin.hasActionHandler("pause")) {
            // Send a special toggle action that the plugin can handle
            JSObject data = new JSObject();
            data.put("toggle", true);
            plugin.actionCallback("pause", data); // Default to pause for Bluetooth compatibility
        } else if (plugin.hasActionHandler("play")) {
            plugin.actionCallback("play");
        } else if (plugin.hasActionHandler("pause")) {
            plugin.actionCallback("pause");
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
