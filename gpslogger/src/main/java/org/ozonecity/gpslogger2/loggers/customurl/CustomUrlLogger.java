/*
*    This file is part of GPSLogger for Android.
*
*    GPSLogger for Android is free software: you can redistribute it and/or modify
*    it under the terms of the GNU General Public License as published by
*    the Free Software Foundation, either version 2 of the License, or
*    (at your option) any later version.
*
*    GPSLogger for Android is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU General Public License for more details.
*
*    You should have received a copy of the GNU General Public License
*    along with GPSLogger for Android.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.ozonecity.gpslogger2.loggers.customurl;

import android.location.Location;
import org.ozonecity.gpslogger2.common.AppSettings;
import org.ozonecity.gpslogger2.common.Session;
import org.ozonecity.gpslogger2.loggers.IFileLogger;
import com.path.android.jobqueue.JobManager;

public class CustomUrlLogger implements IFileLogger {

    private final String name = "URL";
    private final int satellites;
    private final String customLoggingUrl;
    private final float batteryLevel;
    private final String androidId;

    public CustomUrlLogger(String customLoggingUrl, int satellites, float batteryLevel, String androidId) {
        this.satellites = satellites;
        this.customLoggingUrl = customLoggingUrl;
        this.batteryLevel = batteryLevel;
        this.androidId = androidId;
    }

    @Override
    public void Write(Location loc) throws Exception {
        if (!Session.hasDescription()) {
            Annotate("", loc);
        }
    }

    @Override
    public void Annotate(String description, Location loc) throws Exception {
        JobManager jobManager = AppSettings.GetJobManager();
        jobManager.addJobInBackground(new CustomUrlJob(customLoggingUrl, loc, description, satellites, batteryLevel, androidId));
    }

    @Override
    public String getName() {
        return name;
    }
}


