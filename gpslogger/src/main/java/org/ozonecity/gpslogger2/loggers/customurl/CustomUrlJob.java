package org.ozonecity.gpslogger2.loggers.customurl;

import android.location.Location;
import org.ozonecity.gpslogger2.common.SerializableLocation;
import org.ozonecity.gpslogger2.common.Session;
import org.ozonecity.gpslogger2.common.Utilities;
import org.ozonecity.gpslogger2.common.events.UploadEvents;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import de.greenrobot.event.EventBus;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class CustomUrlJob extends Job {

    private static final org.slf4j.Logger tracer = LoggerFactory.getLogger(CustomUrlJob.class.getSimpleName());
    private SerializableLocation loc;
    private String annotation;
    private int satellites;
    private String logUrl;
    private float batteryLevel;
    private String androidId;

    public CustomUrlJob(String customLoggingUrl, Location loc, String annotation, int satellites, float batteryLevel, String androidId) {
        super(new Params(1).requireNetwork().persist());
        this.loc = new SerializableLocation(loc);
        this.annotation = annotation;
        this.satellites = satellites;
        this.logUrl = customLoggingUrl;
        this.batteryLevel = batteryLevel;
        this.androidId = androidId;
    }

    @Override
    public void onAdded() {
    }

    @Override
    public void onRun() throws Throwable {
        HttpURLConnection conn;

        //String logUrl = "http://192.168.1.65:8000/test?lat=%LAT&lon=%LON&sat=%SAT&desc=%DESC&alt=%ALT&acc=%ACC&dir=%DIR&prov=%PROV
        // &spd=%SPD&time=%TIME&battery=%BATT&androidId=%AID&serial=%SER";

        logUrl = logUrl.replaceAll("(?i)%lat", String.valueOf(loc.getLatitude()));
        logUrl = logUrl.replaceAll("(?i)%lon", String.valueOf(loc.getLongitude()));
        logUrl = logUrl.replaceAll("(?i)%sat", String.valueOf(satellites));
        logUrl = logUrl.replaceAll("(?i)%desc", String.valueOf(URLEncoder.encode(Utilities.HtmlDecode(annotation), "UTF-8")));
        logUrl = logUrl.replaceAll("(?i)%alt", String.valueOf(loc.getAltitude()));
        logUrl = logUrl.replaceAll("(?i)%acc", String.valueOf(loc.getAccuracy()));
        logUrl = logUrl.replaceAll("(?i)%dir", String.valueOf(loc.getBearing()));
        logUrl = logUrl.replaceAll("(?i)%prov", String.valueOf(loc.getProvider()));
        logUrl = logUrl.replaceAll("(?i)%spd", String.valueOf(loc.getSpeed()));
        logUrl = logUrl.replaceAll("(?i)%time", String.valueOf(Utilities.GetIsoDateTime(new Date(loc.getTime()))));
        logUrl = logUrl.replaceAll("(?i)%batt", String.valueOf(batteryLevel));
        logUrl = logUrl.replaceAll("(?i)%aid", String.valueOf(androidId));
        logUrl = logUrl.replaceAll("(?i)%ser", String.valueOf(Utilities.GetBuildSerial()));

        // ViTy 26-5-2015 Add %FILE
        logUrl = logUrl.replaceAll("(?i)%file", String.valueOf(Session.getCurrentFileName()));

        // ViTy 27-5-2015 Add %DT
        // ViTy 30-6-2015 Fixed gpsTime to fixed datetime
        /*
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        logUrl = logUrl.replaceAll("(?i)%dt", sdf.format(new Date()));
        */
        Date localTime = new Date(loc.getTime());
        String format = "yyyyMMddHHmmss";
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        logUrl = logUrl.replaceAll("(?i)%dt", sdf.format(localTime));


        tracer.debug("Sending to URL: " + logUrl);

        // - begin ViTy 17-6-2015

        URL url = new URL(logUrl);

        conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        if(conn.getResponseCode() != 200){
            tracer.error("Status code: " + String.valueOf(conn.getResponseCode()));
        } else {
            tracer.debug("Status code: " + String.valueOf(conn.getResponseCode()));
        }

        /*
        try {
            String results = doHttpUrlConnectionAction(logUrl);
            //System.out.println(results);
            tracer.debug("Status code: (" + String.valueOf(Session.getCurrentFileName()) + ")" + results);
        }
        catch (Exception e) {
            // deal with the exception in your "controller"
            tracer.error("Status code: (" + String.valueOf(Session.getCurrentFileName()) + ")" + e.toString());
        }
        */
        // - end ViTy 17-6-2015 revise set timeout to 15 seconds

        EventBus.getDefault().post(new UploadEvents.CustomUrl(true));
    }

    @Override
    protected void onCancel() {
        EventBus.getDefault().post(new UploadEvents.CustomUrl(false));
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        tracer.error("Could not send to custom URL", throwable);
        return true;
    }

    /**
     * Returns the output from the given URL.
     *
     * I tried to hide some of the ugliness of the exception-handling
     * in this method, and just return a high level Exception from here.
     * Modify this behavior as desired.
     *
     * @param desiredUrl
     * @return
     * @throws Exception
     */
    private String doHttpUrlConnectionAction(String desiredUrl)
            throws Exception
    {
        URL url = null;
        BufferedReader reader = null;
        StringBuilder stringBuilder;

        try
        {
            // create the HttpURLConnection
            url = new URL(desiredUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // just want to do an HTTP GET here
            connection.setRequestMethod("GET");

            // uncomment this if you want to write output to this url
            //connection.setDoOutput(true);

            // give it 15 seconds to respond, change to 15 minutes
            connection.setReadTimeout(15*1000*60);
            connection.connect();

            // read the output from the server
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            stringBuilder = new StringBuilder();

            String line = null;
            while ((line = reader.readLine()) != null)
            {
                stringBuilder.append(line + "\n");
            }
            return stringBuilder.toString();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw e;
        }
        finally
        {
            // close the reader; this can throw an exception too, so
            // wrap it in another try/catch block.
            if (reader != null)
            {
                try
                {
                    reader.close();
                }
                catch (IOException ioe)
                {
                    ioe.printStackTrace();
                }
            }
        }
    }

}
