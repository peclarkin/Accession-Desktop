package net.java.sip.communicator.impl.commportal;

import java.io.*;
import java.net.*;
import java.util.*;

import net.java.sip.communicator.service.commportal.*;
import net.java.sip.communicator.util.*;

import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.*;
import org.apache.http.util.*;
import org.jitsi.service.configuration.*;
import org.json.*;

/**
 * Class representing work that needs to be done - for example getting some data
 * from CommPortal, or uploading a file
 */
abstract class WorkItem
{
    /**
     * Where to look in the config for the CommPortal URL
     */
    private static final String CONFIG_COMMPORTAL_URL =
                                "net.java.sip.communicator.impl.commportal.URL";

    /**
     * Where to look in the config for the CommPortal Version (not required -
     * we have a default)
     */
    private static final String CONFIG_COMMPORTAL_VERSION =
                            "net.java.sip.communicator.impl.commportal.VERSION";

    /**
     * The default CommPortal version to use if no version is set
     */
    private static final String DEFAULT_COMMPORTAL_VERSION = "8.0";

    protected final Logger mLog;

    /**
     * Call back for handling network errors
     */
    protected final CPOnNetworkErrorCallback mNetworkErrorCallback;

    /**
     * CommPortal service - used for getting the session id
     */
    protected final CommPortalServiceImpl mCommPortalService;

    /**
     * Service for getting config
     */
    protected final ConfigurationService mConfig;

    /**
     * Client for making HTTP requests
     */
    protected final HttpClient mHttpClient;

    protected WorkItem(CPOnNetworkErrorCallback networkErrorCallback,
                       CommPortalServiceImpl commPortalService,
                       HttpClient httpClient,
                       String loggerName)
    {
        mNetworkErrorCallback = networkErrorCallback;
        mCommPortalService = commPortalService;
        mHttpClient = httpClient;
        mConfig = CommPortalActivator.getConfigService();

        mLog = Logger.getLogger(loggerName);
    }

    /**
     * Create the URL to request from the base url
     *
     * @param baseUrl the stub of the URL in the form:
     *        http(s)://(domain)/(cust)/session(session id)/
     * @return the URL to request
     * @throws NoStoredCommPortalURLException if there is no stored CommPortal
     *         URL to make the request
     */
    protected abstract StringBuilder constructUrlFromBase(StringBuilder baseUrl)
                                          throws NoStoredCommPortalURLException;

    /**
     * Get the URI request object to use to connect to CommPortal
     *
     * @param the URL to connect to
     * @return the URI request object to use to connect to CommPortal
     */
    protected abstract HttpUriRequest getHttpRequester(String url)
                                            throws UnsupportedEncodingException,
                                                   FileNotFoundException;

    /**
     * Called in order to do some work
     *
     * @return true if the work has been completed and thus the work item should
     *         not be run again.  Note completed could mean that the work failed
     *         but that the error has been handled.
     */
    protected boolean doWork()
    {
        mLog.debug("doWork");
        boolean workCompleted;

        String sessionId = mCommPortalService.getSessionId();
        if (sessionId == null)
        {
            // No session id so can't do this work.
            workCompleted = false;
            mLog.warn("Unable to do work as no session id");
        }
        else
        {
            mLog.debug("Got a session ID so doing work");

            // Construct the URL
            try
            {
                StringBuilder baseUrl = new StringBuilder();
                baseUrl.append(getCommPortalURL())
                       .append("/session")
                       .append(sessionId)
                       .append('/');
                String url = constructUrlFromBase(baseUrl).toString();

                // And request it
                workCompleted = requestUrl(url);
            }
            catch (NoStoredCommPortalURLException e)
            {
                mLog.error("No stored CP URL");
                workCompleted = false;

                // Back-off: The URL may be restored later
                mCommPortalService.getBackoff().onError();
            }
        }

        return workCompleted;
    }

    /**
     * Request the URL (creates a requestHandler to do so)
     *
     * @param url The URL to request
     * @return true if the work has been completed and thus the work item should
     *         not be run again.  Note completed could mean that the work failed
     *         but that the error has been handled.
     */
    final protected boolean requestUrl(String url)
    {
        mLog.debug("requestUrl " + url);
        boolean workCompleted;

        RequestHandler request = new RequestHandler(mHttpClient)
        {
            @Override
            protected boolean handleErrorResponse(HttpResponse response,
                                                  HttpEntity entity)
            {
                mLog.info("Error completing work item");
                return WorkItem.this.processError(response, entity);
            }

            @Override
            protected boolean handleOKResponse(HttpResponse response,
                                               HttpEntity entity)
            {
                mLog.debug("Completed work item OK");
                return WorkItem.this.handleOKResponse(entity);
            }
        };

        try
        {
            workCompleted = request.execute(getHttpRequester(url));
            mLog.debug("Requested URL correctly");
        }
        catch (SocketTimeoutException e)
        {
            mLog.info("Socket timeout exception");
            workCompleted = handleSocketTimeout();
        }
        catch (FileNotFoundException e)
        {
            mLog.info("File not found");
            workCompleted = false;
            handleDataError(CPDataError.fileNotFound);
        }
        catch (IOException e)
        {
            mLog.error("IO Exception communicating with server", e);
            workCompleted =
                         handleNetworkError(CPNetworkError.NETWORK_UNAVAILABLE);
        }

        mLog.debug("Work done, is completed? " + workCompleted);
        return workCompleted;
    }

    /**
     * Handle a socket timeout exception while getting the data
     *
     * @return true if the error has been handled outside this service (and that
     *         this work item should be considered complete)
     */
    protected boolean handleSocketTimeout()
    {
        // Default to just treating this as another network issue
        return handleNetworkError(CPNetworkError.NETWORK_UNAVAILABLE);
    }

    /**
     * Handle network errors
     *
     * @param errorType the type of error
     * @return true if the error has been handled outside this service (and that
     *         this work item should be considered complete)
     */
    protected boolean handleNetworkError(CPNetworkError errorType)
    {
        boolean handledExternally;

        if (mNetworkErrorCallback != null)
        {
            mLog.debug("Passing network error to callback");
            mNetworkErrorCallback.onNetworkError(errorType);
            handledExternally = true;
        }
        else
        {
            mLog.debug("Passing network error to service");
            mCommPortalService.onNetworkError(errorType);
            handledExternally = false;
        }

        return handledExternally;
    }

    private boolean processError(HttpResponse response, HttpEntity entity)
    {
        boolean workCompleted;

        // First look at the entity that we were passed
        if ((entity != null) && URLEncodedUtils.isEncoded(entity))
        {
            mLog.debug("Examining entity for error");

            try
            {
                List<NameValuePair> responseData = URLEncodedUtils.parse(entity);
                workCompleted = false;

                for (NameValuePair nvp : responseData)
                {
                    String name = nvp.getName();
                    String value = nvp.getValue();

                    if ("error".equals(name))
                    {
                        // CommPortal returned an error
                        workCompleted = processErrorString(value);
                        mLog.info("CommPortal returned error " + value);
                        break;
                    }
                }
            }
            catch (IOException e)
            {
                // Treat as a network error - as there was something wrong with
                // the data that we got back
                workCompleted =
                         handleNetworkError(CPNetworkError.NETWORK_UNAVAILABLE);
            }
        }
        else
        {
            if (entity != null)
            {
                logEntity(entity);
            }

            // Process the error based on the status code alone.
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            mLog.debug("Examining status code for error: " + statusCode);

            switch (statusCode)
            {
                case HttpStatus.SC_NOT_FOUND:
                case HttpStatus.SC_FORBIDDEN:
                    // This is the only return code we receive to a POST.  Most
                    // likely a session failure.
                    handleSessionExpired();
                    workCompleted = false;
                    break;

                case HttpStatus.SC_BAD_REQUEST:
                    mCommPortalService.getBackoff().onError();
                    handleDataError(CPDataError.badRequest);
                    workCompleted = true;
                    break;

                default:
                    // Must be a server error - treat as network (so that we
                    // wait for the server to recover)
                    workCompleted =
                          handleNetworkError(CPNetworkError.SERVER_UNAVAILABLE);
                    break;
            }
        }

        return workCompleted;
    }

    /**
     * Handle the case that the session has expired
     */
    protected void handleSessionExpired()
    {
        mLog.info("Session expired");
        mCommPortalService.onSessionExpired();
    }

    /**
     * Convenience method converting an entity to a String and logging it
     *
     * @param entity the entity to convert
     */
    private void logEntity(HttpEntity entity)
    {
        String contentString;
        InputStream is = null;

        // Log the response at error level.
        try
        {
            is = entity.getContent();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            StringBuilder sb = new StringBuilder();

            while ((line = br.readLine()) != null)
            {
                sb.append(line);
                sb.append('\n');
            }

            contentString = sb.toString();
            mLog.info("Entity contents are " + contentString);
        }
        catch (IOException e)
        {
            mLog.info("Problem trying to log entity");
        }
        finally
        {
            try
            {
                if (is != null)
                {
                    is.close();
                }
            }
            catch (IOException e)
            {
                mLog.info("Error closing input stream");
            }
        }
    }

    /**
     * Process the error string that has been returned by CommPortal by turning
     * it into an error type and then handle it.
     *
     * @param errorString The error
     * @return true if the error has been handled outside this service (and that
     *         this work item should be considered complete)
     */
    private boolean processErrorString(String errorString)
    {
        mLog.info("Processing error string: " + errorString);

        // We only get an authentication failed error when trying to login, i.e.
        // when trying to get a session - treat as session ID.
        boolean isSessionExpiry = "sessionExpired".equals(errorString) ||
                                  "authenticationFailed".equals(errorString);

        if (isSessionExpiry)
        {
            mLog.debug("Session expired");
            handleSessionExpired();
        }
        else
        {
            CPDataError error = null;

            // Any other error CommPortal specifically returns is assumed to be
            // a data error:
            try
            {
                error = CPDataError.valueOf(errorString);
            }
            catch (IllegalArgumentException e)
            {
                mLog.debug("Unknown error type: " + errorString);
            }

            mCommPortalService.getBackoff().onError();
            handleDataError(error);
        }

        // Session expired should be treated as a non-fatal error as we want the
        // work item to be run once we have a session
        return !isSessionExpiry;
    }

    /**
     * Handle the error returned from CommPortal
     *
     * @param errorType The error
     */
    protected abstract void handleDataError(CPDataError errorType);

    /**
     * @return the CommPortal URL without any attributes or parameters
     * @throws NoStoredCommPortalURLException If we have no stored RUL
     */
    protected String getCommPortalURL() throws NoStoredCommPortalURLException
    {
        String cpUrl = mConfig.getString(CONFIG_COMMPORTAL_URL);

        if (cpUrl == null)
        {
            mLog.warn("No stored CommPortal URL");
            throw new NoStoredCommPortalURLException();
        }

        return cpUrl;
    }

    /**
     * Examine the response and entity when CommPortal has responded with a 200
     * OK.  Note that this does not indicate that the request succeeded as some
     * errors are returned in a 200 OK.error
     *
     * @param entity The entity got back from CommPortal
     * @return true if the work has been completed and thus the work item should
     *         not be run again.  Note completed could mean that the work failed
     *         but that the error has been handled.
     */
    protected boolean handleOKResponse(HttpEntity entity)
    {
        mLog.debug("handleOKResponse");
        boolean consumeWorkItem = true;

        // The server has responded with an OK.  However, the response may
        // contain an error (such as session expired) - look for these and
        // process them if found.
        try
        {
            // The data is returned in an array, we should look through it until
            // we know that there were no errors.  Once we know that we can tell
            // the callbacks that everything is ok.
            JSONArray data = getJSONFromEntity(entity);

            boolean dataError = false;
            int dataLength = data.length();
            for (int i = 0; i < dataLength; i++)
            {
                JSONObject singleData = data.getJSONObject(i);

                JSONArray errors = singleData.optJSONArray("errors");
                if (errors != null && errors.length() > 0)
                {
                    mLog.error("Got some errors: " + errors);

                    // Got some errors, process just the first one
                    JSONObject error = errors.getJSONObject(0);
                    String errString = error.getString("type");
                    consumeWorkItem = processErrorString(errString);
                    dataError = true;
                }
            }

            if (!dataError)
            {
                mLog.debug("No problems with received data - handle it");
                consumeWorkItem = handleOKData(data);
            }
        }
        catch (ParseException e)
        {
            mLog.error("Error parsing CommPortal reponse", e);
            consumeWorkItem = handleNetworkError(CPNetworkError.SERVER_ERROR);
        }
        catch (JSONException e)
        {
            mLog.error("Error invalid CommPortal JSON", e);
            consumeWorkItem = handleNetworkError(CPNetworkError.SERVER_ERROR);
        }
        catch (IOException e)
        {
            mLog.error("Error invalid entity", e);
            consumeWorkItem = handleNetworkError(CPNetworkError.SERVER_ERROR);
        }

        return consumeWorkItem;
    }

    /**
     * Parse the entity received from the CommPortal server to get the data as a
     * JSON array
     *
     * @param entity The entity received from CommPortal
     * @return The parsed entity as a JSON array
     * @throws JSONException If the entity contains invalid JSON
     * @throws IOException If there was a problem parsing the entity
     */
    protected JSONArray getJSONFromEntity(HttpEntity entity)
                                               throws JSONException, IOException
    {
        return new JSONObject(EntityUtils.toString(entity)).getJSONArray("data");
    }

    /**
     * Handle the data that we have got back from CommPortal
     *
     * @param data The data that we have received from CommPortal
     * @return true if the work has been completed and thus the work item should
     *         not be run again.  Note completed could mean that the work failed
     *         but that the error has been handled.
     */
    protected abstract boolean handleOKData(JSONArray data);

    /**
     * @return the user agent
     */
    protected String getUserAgent()
    {
        return System.getProperty("sip-communicator.application.name") + "/" +
               System.getProperty("sip-communicator.version");
    }

    /**
     * @return the version to use when getting data from CommPortal
     */
    protected String getCommPortalVersion()
    {
        return mConfig.getString(CONFIG_COMMPORTAL_VERSION,
                                 DEFAULT_COMMPORTAL_VERSION);
    }

    /** Exception thrown for when there is no stored CommPortal URL */
    static class NoStoredCommPortalURLException extends Exception
    {
        private static final long serialVersionUID = 1L;

    }
}
