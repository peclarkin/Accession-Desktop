package net.java.sip.communicator.impl.commportal;

import java.io.IOException;

import net.java.sip.communicator.service.commportal.CPDataError;
import net.java.sip.communicator.service.commportal.CPDataGetterCallback;
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
 * Work item for getting data from CommPortal
 */
class WorkItemGetData extends WorkItem
{
    private final CPDataGetterCallback mCallback;

    protected WorkItemGetData(CPOnNetworkErrorCallback networkErrorCallback,
                              CPDataGetterCallback callback,
                              CommPortalServiceImpl commPortalService,
                              HttpClient httpClient)
    {
        super(networkErrorCallback,
              commPortalService,
              httpClient,
              "WorkItemGet" + callback.getSIName());
        mCallback = callback;
    }

    @Override
    protected StringBuilder constructUrlFromBase(StringBuilder baseUrl)
    {
        return baseUrl.append(mCallback.getDataFormat().getFormat())
                      .append("?data=")
                      .append(mCallback.getSIName());
    }

    @Override
    protected HttpUriRequest getHttpRequester(String url)
    {
        return new HttpGet(url);
    }

    @Override
    protected boolean handleOKData(JSONArray data)
    {
        mLog.debug("Got data ok");
        boolean dataValid = mCallback.onDataReceived(data.toString());

        if (dataValid)
        {
            // The data is good so reset the backoff
            mLog.debug("Good data received");
            mCommPortalService.getBackoff().onSuccess();
        }
        else
        {
            // Invalid data that indicates a server error, increase the backoff
            mLog.info("The callback did not like the data!");
            mCommPortalService.getBackoff().onError();
        }

        return dataValid;
    }

    @Override
    protected void handleDataError(CPDataError errorType)
    {
        mLog.info("Data error " + errorType);
        mCallback.onDataError(errorType);
    }
}
