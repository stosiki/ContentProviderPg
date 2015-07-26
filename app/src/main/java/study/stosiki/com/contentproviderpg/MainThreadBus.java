package study.stosiki.com.contentproviderpg;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;

/**
 * Taken from stackoverflow answer to the question
 * about posting events on UI thread from a service
 *
 * http://stackoverflow.com/questions/15431768/how-to-send-event-from-service-to-activity-with-otto-event-bus
 */
public class MainThreadBus extends Bus {
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override public void post(final Object event) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            super.post(event);
        } else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    MainThreadBus.super.post(event);
                }
            });
        }
    }
}
