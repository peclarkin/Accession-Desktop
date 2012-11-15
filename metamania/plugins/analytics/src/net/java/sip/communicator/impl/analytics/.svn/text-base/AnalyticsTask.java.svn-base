package net.java.sip.communicator.impl.analytics;

import java.io.*;
import java.security.cert.*;
import java.util.*;

import net.java.sip.communicator.util.*;

import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.conn.scheme.*;
import org.apache.http.conn.ssl.*;
import org.apache.http.entity.*;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.tsccm.*;
import org.apache.http.params.*;
import org.jitsi.service.configuration.*;
import org.json.*;

/**
 * A thread for sending analytics events to the server
 */
class AnalyticsTask extends TimerTask
{
    private final Logger sLog = Logger.getLogger(AnalyticsTask.class);
    private static final ConfigurationService mConfigService =
                                          AnalyticsActivator.getConfigService();

    /**
     * The place in the config where the control of self-signed certificates is
     * stored
     */
    private static final String CONFIG_ALLOW_SELF_SIGNED =
                  "net.java.sip.communicator.impl.commportal.ALLOW_SELF_SIGNED";

    /**
     * Reference to the service that created us
     */
    private final AnalyticsServiceImpl mAnalyticsService;

    AnalyticsTask(AnalyticsServiceImpl analyticsService)
    {
        mAnalyticsService = analyticsService;
    }

    public void run()
    {
        Vector<AnalyticsEvent> events = mAnalyticsService.getEventsToSend();

        if (events == null || events.isEmpty())
        {
            return;
        }

        try
        {
            JSONObject dataToSend = new JSONObject();

            JSONObject environmentInfo = createEnvironmentInfo();
            dataToSend.put("environment", environmentInfo);

            JSONArray jsonEvents = new JSONArray();

            for (AnalyticsEvent event : events)
            {
                jsonEvents.put(event.toJSON());
            }

            dataToSend.put("events", jsonEvents);

            sendData(dataToSend);

        }
        catch (JSONException e)
        {
            // If it is a JSON error then there is probably something wrong with
            // the data that we have.  In which case just drop it
            sLog.error("JSON error", e);
        }
        catch (Exception e)
        {
            // Non-JSON error, most likely a network error, return the analytics
            // event to the list to try again later.
            sLog.error("Error in sending the data", e);
            mAnalyticsService.reAddEvents(events);
        }
    }

    protected static class TrustSelfSignedStrategy
    implements TrustStrategy
{
    public boolean isTrusted(X509Certificate[] xiArg0, String xiArg1)
    {
        return true;
    }
}

private SchemeRegistry getSelfSignedCertsRegistry()
{
    SchemeRegistry schemeRegistry = new SchemeRegistry();
    try
    {
        TrustStrategy trustStrategy = new TrustSelfSignedStrategy();
        X509HostnameVerifier hostnameVerifier =
            new AllowAllHostnameVerifier();
        SSLSocketFactory sslSf =
            new SSLSocketFactory(trustStrategy, hostnameVerifier);
        Scheme https = new Scheme("https", 443, sslSf);
        Scheme https2 = new Scheme("https", 18008, sslSf);
        schemeRegistry.register(https);
        schemeRegistry.register(https2);
    }
    catch (Exception e)
    {
        sLog.error("Couldn't set registry for self signed certs", e);
    }

    return schemeRegistry;
}

    /**
     * Send the data to the analytics server
     *
     * @param data the data to send
     * @throws IOException
     * @throws ClientProtocolException
     */
    private void sendData(JSONObject data)
                                     throws ClientProtocolException, IOException
    {
        String url = AnalyticsActivator.getAnalyticsUrl();

        HttpPost post = new HttpPost(url);
        post.setHeader("Content-Type", "application/json");
        post.setEntity(new StringEntity(data.toString(), "UTF-8"));

        // Create the connection manager
        SchemeRegistry registry = null;

        if (mConfigService.getBoolean(CONFIG_ALLOW_SELF_SIGNED, false))
        {
            sLog.info("Create connection manager allowing self signed certs");
            registry = getSelfSignedCertsRegistry();
        }

        ThreadSafeClientConnManager connMan = (registry != null) ?
                                     new ThreadSafeClientConnManager(registry) :
                                     new ThreadSafeClientConnManager();

        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, 2000);
        HttpClient httpClient = new DefaultHttpClient(connMan, params);

        HttpResponse response = httpClient.execute(post);

        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != HttpStatus.SC_OK)
        {
            throw new IOException("Status code not \"OK\": " + statusCode);
        }
    }

    /**
     * @return A JSON object containing the environment info to send
     * @throws JSONException
     */
    private JSONObject createEnvironmentInfo() throws JSONException
    {
        JSONObject environment = new JSONObject();
        environment.put("os_platform", System.getProperty("os.arch"));
        environment.put("os_version",  System.getProperty("os.name"));
        environment.put("app_version", System.getProperty("sip-communicator.version"));
        environment.put("uuid",
                    mConfigService.getString("net.java.sip.communicator.UUID"));
        String spName = "net.java.sip.communicator.plugin.cdap.service_provider_name";
        environment.put("service_provider",
                        mConfigService.getString(spName, "Unknown"));

        return environment;
    }

}
