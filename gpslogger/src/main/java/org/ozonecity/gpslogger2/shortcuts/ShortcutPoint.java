package org.ozonecity.gpslogger2.shortcuts;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import org.ozonecity.gpslogger2.GpsLoggingService;
import org.ozonecity.gpslogger2.common.events.CommandEvents;
import de.greenrobot.event.EventBus;
import org.slf4j.LoggerFactory;

public class ShortcutPoint extends Activity {
    private static final org.slf4j.Logger tracer = LoggerFactory.getLogger(ShortcutStart.class.getSimpleName());

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tracer.info("Shortcut - point logging");
        //EventBus.getDefault().postSticky(new CommandEvents.RequestStartStop(true));
        EventBus.getDefault().post(new CommandEvents.LogOnce());

        Intent serviceIntent = new Intent(getApplicationContext(), GpsLoggingService.class);
        getApplicationContext().startService(serviceIntent);

        finish();

    }
}