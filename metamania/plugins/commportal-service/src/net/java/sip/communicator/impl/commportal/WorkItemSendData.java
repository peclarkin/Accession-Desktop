package net.java.sip.communicator.impl.commportal;

import java.io.*;

import net.java.sip.communicator.service.commportal.*;

import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.entity.*;
import org.json.*;

/**
 * Work item for sending data to CommPortal
 */
class WorkItemSendData extends WorkItem
{
    private final CPDataSenderCallback mCallback;

    protected WorkItemSendData(CPOnNetworkErrorCallback networkErrorCallback,
                               CPDataSenderCallback callback,
                               CommPortalServiceImpl commPortalService,
                               HttpClient httpClient)
    {
        super(networkErrorCallback,
              commPortalService,
              httpClient,
              "WorkItemSendData");
        mCallback = callback;
    }

    @Override
    protected StringBuilder constructUrlFromBase(StringBuilder baseUrl)
    {
        return baseUrl.append(mCallback.getDataFormat().getFormat())
                      .append("?returnerrorsnow=true");
    }

    @Override
    protected HttpUriRequest getHttpRequester(String url)
                                             throws UnsupportedEncodingException
    {
        HttpPost post = new HttpPost(url);

        // Create a string representation of the data (including the SI name)
        StringBuilder sb = new StringBuilder("{\"data\":[")
                            .append("{\"data\":")
                            .append(mCallback.getData())
                            .append(",")
                            .append("\"dataType\":\"")
                            .append(mCallback.getSIName())
                            .append("\"}")
                            .append("]}");

        post.setHeader("Content-Type", "application/json");
        post.setEntity(new StringEntity(sb.toString(), "UTF-8"));

        return post;
    }

    @Override
    protected boolean handleOKData(JSONArray data)
    {
        // Return true to consume this work item and reset the backoff
        mLog.debug("Data sent ok");
        mCommPortalService.getBackoff().onSuccess();
        mCallback.onDataSent();

        return true;
    }

    @Override
    protected void handleDataError(CPDataError errorType)
    {
        mLog.info("Data error " + errorType);
        mCallback.onDataError(errorType);
    }
}
