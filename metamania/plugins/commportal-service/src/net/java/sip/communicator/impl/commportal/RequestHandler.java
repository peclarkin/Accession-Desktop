package net.java.sip.communicator.impl.commportal;

import java.io.*;
import java.net.*;

import net.java.sip.communicator.util.*;

import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.util.*;

/**
 * Class to handle Http requests.
 */
abstract class RequestHandler
{
    private static final Logger sLog = Logger.getLogger(RequestHandler.class);

    /**
     * The HttpClient passed to the constructor.
     */
    HttpClient mHttpClient;

    RequestHandler(HttpClient httpClient)
    {
        mHttpClient = httpClient;
    }

    /**
     * Actually execute a request
     *
     * @param request The request to execute
     * @return true if the request was successfully executed
     */
    boolean execute(HttpUriRequest request) throws IOException
    {
        sLog.debug("Executing request");
        boolean requestSuccessful;
        HttpEntity entity = null;

        try
        {
            HttpResponse response = mHttpClient.execute(request);
            int responseCode = response.getStatusLine().getStatusCode();
            sLog.debug("Got response code " + responseCode);

            // Whatever happens we want the response entity
            entity = response.getEntity();

            if (responseCode == HttpURLConnection.HTTP_OK)
            {
                requestSuccessful = handleOKResponse(response, entity);
            }
            else
            {
                requestSuccessful = handleErrorResponse(response, entity);
            }
        }
        catch (RuntimeException e)
        {
            // For non-IO exceptions we need to abort the request
            request.abort();
            throw e;
        }
        finally
        {
            // Consume any remaining content in the response entity
            if (entity != null)
            {
                EntityUtils.consume(entity);
            }
        }

        return requestSuccessful;
    }

    /**
     * Handle the error responses
     *
     * @param response The response that was received
     * @param entity The entity of the response
     * @return true if the request was dealt with - i.e. if we are not required
     *         to do any more work
     */
    protected abstract boolean handleErrorResponse(HttpResponse response,
                                                   HttpEntity entity);

    /**
     * Handle an OK response
     *
     * @param response The response that was received
     * @param entity The entity of the response
     * @return true if the request was dealt with - i.e. if we are not required
     *         to do any more work
     */
    protected abstract boolean handleOKResponse(HttpResponse response,
                                                HttpEntity entity);
}