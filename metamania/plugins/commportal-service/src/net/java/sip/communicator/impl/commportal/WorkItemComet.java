package net.java.sip.communicator.impl.commportal;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import net.java.sip.communicator.service.commportal.CPDataError;
import net.java.sip.communicator.service.commportal.CPDataRegistrationCallback;
import net.java.sip.communicator.service.commportal.CPNetworkError;
import net.java.sip.communicator.service.commportal.CPOnNetworkErrorCallback;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.util.EntityUtils;
import org.json.*;

/**
 * Class for handling COMET requests
 */
public class WorkItemComet extends WorkItem
{
    private final CPDataRegistrationCallback mCallback;

    protected WorkItemComet(CPOnNetworkErrorCallback networkErrorCallback,
                            CPDataRegistrationCallback callback,
                            CommPortalServiceImpl commPortalService,
                            HttpClient httpClient)
    {
        super(networkErrorCallback,
              commPortalService,
              httpClient,
              "WorkItemComet" + callback.getSIName());
        mCallback = callback;
    }

    @Override
    protected StringBuilder constructUrlFromBase(StringBuilder baseUrl)
    {
        long pollTimeout = mCommPortalService.getCometTimeout().getTimeout();
        String line = mCommPortalService.getUserNumber();

        baseUrl.append("line").append(line)
               .append("/events.js?version=").append(getCommPortalVersion())
               .append("&events=").append(mCallback.getSIName())
               .append("&timeout=").append(pollTimeout);

        return baseUrl;
    }

    @Override
    protected HttpUriRequest getHttpRequester(String url)
                                             throws UnsupportedEncodingException
    {
        mLog.debug("Open url " + url);
        return new HttpGet(url);
    }

    @Override
    protected void handleDataError(CPDataError errorType)
    {
        mLog.info("Data error " + errorType);
        mCallback.onDataError(errorType);
    }

    @Override
    protected boolean handleSocketTimeout()
    {
        // Override the default implementation as this indicates our timeout is
        // too large
        mCommPortalService.getCometTimeout().onCometSocketTimeout();

        return false;
    }

    @Override
    protected JSONArray getJSONFromEntity(HttpEntity entity)
                                               throws JSONException, IOException
    {
        // Data returned from a COMET poll is of the form:
        // {
        //      events: [...]
        //      errors: [...]
        // }
        // However we expect it in the form of an array, hence return it as such
        return new JSONArray("[" + EntityUtils.toString(entity) + "]");
    }

    @Override
    protected boolean handleOKData(JSONArray data)
    {
        // Timeout expired so we may be able to go larger:
        mCommPortalService.getCometTimeout().onServerTimeout();

        // The data is good so reset the backoff
        mCommPortalService.getBackoff().onSuccess();

        try
        {
            JSONArray events = data.getJSONObject(0).optJSONArray("events");

            if (events != null)
            {
                mLog.info("Events data: " + events);

                if (events.length() != 0)
                {
                    mLog.debug("Have some data");
                    mCallback.onDataChanged();
                }
            }
        }
        catch (JSONException e)
        {
            // This should never happen - we've always should get an array with
            // a single element
            mLog.error("Invalid JSON while getting events", e);
        }

        // This work item should never be destroyed - we always want to carry on
        return false;
    }
}
