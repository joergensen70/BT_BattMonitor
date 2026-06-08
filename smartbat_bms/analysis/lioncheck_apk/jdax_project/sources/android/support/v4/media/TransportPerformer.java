package android.support.v4.media;

import android.os.SystemClock;
import android.view.KeyEvent;

/* JADX INFO: loaded from: classes.dex */
public abstract class TransportPerformer {
    static final int AUDIOFOCUS_GAIN = 1;
    static final int AUDIOFOCUS_GAIN_TRANSIENT = 2;
    static final int AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK = 3;
    static final int AUDIOFOCUS_LOSS = -1;
    static final int AUDIOFOCUS_LOSS_TRANSIENT = -2;
    static final int AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK = -3;

    public int onGetBufferPercentage() {
        return 100;
    }

    public abstract long onGetCurrentPosition();

    public abstract long onGetDuration();

    public int onGetTransportControlFlags() {
        return 60;
    }

    public abstract boolean onIsPlaying();

    public boolean onMediaButtonUp(int i, KeyEvent keyEvent) {
        return true;
    }

    public abstract void onPause();

    public abstract void onSeekTo(long j);

    public abstract void onStart();

    public abstract void onStop();

    public boolean onMediaButtonDown(int i, KeyEvent keyEvent) {
        if (i == 79 || i == 85) {
            if (onIsPlaying()) {
                onPause();
            } else {
                onStart();
            }
        } else {
            if (i == 86) {
                onStop();
                return true;
            }
            if (i == 126) {
                onStart();
                return true;
            }
            if (i == 127) {
                onPause();
                return true;
            }
        }
        return true;
    }

    public void onAudioFocusChange(int i) {
        int i2 = i != -1 ? 0 : TransportMediator.KEYCODE_MEDIA_PAUSE;
        if (i2 != 0) {
            long jUptimeMillis = SystemClock.uptimeMillis();
            onMediaButtonDown(i2, new KeyEvent(jUptimeMillis, jUptimeMillis, 0, i2, 0));
            onMediaButtonUp(i2, new KeyEvent(jUptimeMillis, jUptimeMillis, 1, i2, 0));
        }
    }
}
