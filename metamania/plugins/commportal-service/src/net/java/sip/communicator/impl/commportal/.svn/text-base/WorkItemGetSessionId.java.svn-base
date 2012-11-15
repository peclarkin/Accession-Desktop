package net.java.sip.communicator.impl.commportal;

import java.io.*;
import java.util.*;

import net.java.sip.communicator.service.commportal.*;
import net.java.sip.communicator.util.swing.*;

import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.entity.*;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.*;
import org.apache.http.message.*;
import org.json.*;

/**
 * Work item for getting a session id
 */
class WorkItemGetSessionId extends WorkItem
{
    /**
     * Call back for when we have got a session id.
     */
    private final CPDataGetterCallback mCallback;

    protected WorkItemGetSessionId(CPOnNetworkErrorCallback networkErrorCallback,
                                   CPDataGetterCallback callback,
                                   CommPortalServiceImpl commPortalService,
                                   HttpClient httpClient)
    {
        super(networkErrorCallback,
              commPortalService,
              httpClient,
              "WorkItemGetSessionId");

        mCallback = callback;
    }

    @Override
    protected StringBuilder constructUrlFromBase(StringBuilder baseUrl)
                                           throws NoStoredCommPortalURLException
    {
        // Getting the session id does not use the baseURL as that is built
        // assuming that there is a session
        return new StringBuilder(getCommPortalURL()).append("/login?version=")
                                                    .append(getCommPortalVersion());
    }

    @Override
    protected HttpUriRequest getHttpRequester(String url)
                                             throws UnsupportedEncodingException
    {
        HttpPost post = new HttpPost(url);

        post.setHeader("User-Agent", getUserAgent());
        post.setHeader("Content-type", "application/x-www-form-urlencoded");
        post.setHeader("Cache-Control", "No-Transform");

        // Add all the parameters required for log in
        String password = mCommPortalService.getPassword();
        String number   = mCommPortalService.getUserNumber();
        String brandVersion = System.getProperty("sip-communicator.version");

        if (number == null || password == null)
        {
            mLog.debug("No stored credentials - asking user for them");
            requestUserCredentials(url);

            // Credentials are stored in the CommPortal service
            number = mCommPortalService.getUserNumber();
            password = mCommPortalService.getPassword();
        }

        Vector<BasicNameValuePair> postData = new Vector<BasicNameValuePair>();
        postData.add(new BasicNameValuePair("DirectoryNumber", number));
        postData.add(new BasicNameValuePair("Password", password));
        postData.add(new BasicNameValuePair("ApplicationID", "MS_Desktop_Acc"));
        postData.add(new BasicNameValuePair("ApplicationVersion", brandVersion));
        postData.add(new BasicNameValuePair("ContextInfo", "version=" + brandVersion));

        post.setEntity(new UrlEncodedFormEntity(postData));

        password = null;

        return post;
    }

    @Override
    protected boolean doWork()
    {
        // We override doWork as the default implementation asks for a session
        // ID which would schedule another WorkItemGetSessionId
        mLog.debug("Doing work");
        boolean workCompleted;

        try
        {
            String url = constructUrlFromBase(null).toString();
            workCompleted = requestUrl(url);
        }
        catch (NoStoredCommPortalURLException e)
        {
            mLog.error("No stored CommPortal URL");
            workCompleted = false;
            mCommPortalService.getBackoff().onError();
        }

        return workCompleted;
    }

    @Override
    protected boolean handleOKData(JSONArray data)
    {
        // This does nothing - the session id is not received in a JSON Object
        // thus the method is not required.
        return false;
    }

    @Override
    protected boolean handleOKResponse(HttpEntity entity)
    {
        mLog.debug("Got OK response");
        boolean dataValid = false;

        // OK response.  Get the session and any redirect domain
        if ((entity != null) && URLEncodedUtils.isEncoded(entity))
        {
            try
            {
                List<NameValuePair> data = URLEncodedUtils.parse(entity);

                for (NameValuePair nvp : data)
                {
                    String name = nvp.getName();
                    if ("session".equals(name))
                    {
                        mLog.debug("Got session");
                        dataValid = mCallback.onDataReceived(nvp.getValue());
                    }
                }
            }
            catch (IOException e)
            {
                // Probably an EAS error - something was wrong with the response
                mLog.error("Problem with entity while getting session");
            }
        }

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

    @Override
    protected void handleSessionExpired()
    {
        // No point in telling the service that the session has expired - this
        // probably means that we presented the wrong password. The only thing
        // we need to do is expire the stored password.  Otherwise we will try
        // again with the bad password
        mLog.error("Session expired reported while trying to get session");
        mCommPortalService.onInvalidCredentials();
    }

    /**
     * Request the credentials from the user
     *
     * @param commPortalURL the CommPortal URL
     */
    private void requestUserCredentials(String commPortalURL)
    {
        boolean rememberPassword = false;
        boolean gotCredentials;
        String password = null;

        do
        {
            String error =
                       mConfig.getString("plugin.commportal.PASSWORD_REQUIRED");
            AuthenticationWindow authWindow =
                new AuthenticationWindow(mCommPortalService.getUserNumber(),
                                         null,
                                         commPortalURL,
                                         false,
                                         null,
                                         error,
                                         null);
            authWindow.setVisible(true);

            if(!authWindow.isCanceled())
            {
                password = new String(authWindow.getPassword());
                rememberPassword = authWindow.isRememberPassword();
                gotCredentials = true;
            }
            else
            {
                // User cancelled the dialog so we don't have any credentials
                mLog.info("User cancelled the update credentials dialog");
                gotCredentials = false;
                break;
            }
        } while (!gotCredentials);

        if (gotCredentials)
        {
            mLog.info("User entered some credentials");

            // We don't allow the user name to change, so just get it from store
            String userName = mCommPortalService.getUserNumber();
            mCommPortalService.setCredentials(userName,
                                              password,
                                              rememberPassword);
        }
        else
        {
            // Nothing we can do if the user refuses to provide their login
            // details.  All we can do is give up.
            mLog.error("User refused to provide credentials, give up");
            mCommPortalService.stop();
        }
    }
}
