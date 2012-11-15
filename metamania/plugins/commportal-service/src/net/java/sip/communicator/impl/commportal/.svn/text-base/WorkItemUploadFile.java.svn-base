package net.java.sip.communicator.impl.commportal;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import net.java.sip.communicator.service.commportal.*;

import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.*;
import org.apache.http.entity.*;
import org.apache.http.impl.client.*;
import org.apache.http.message.*;
import org.apache.http.params.*;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.*;
import org.json.*;

/**
 * Work item for uploading a file to CommPortal
 *
 * XXX Note that this class is quite hacky as it is only used to upload error
 * reports to jetfire, rather than doing anything properly.
 */
class WorkItemUploadFile extends WorkItem
{
    private final CPFileUploadCallback mCallback;

    protected WorkItemUploadFile(CPOnNetworkErrorCallback networkErrorCallback,
                                 CPFileUploadCallback callback,
                                 CommPortalServiceImpl commPortalService,
                                 HttpClient httpClient)
    {
        super(networkErrorCallback,
              commPortalService,
              httpClient,
              "WorkItemUploadFile");
        mCallback = callback;
    }

    @Override
    protected StringBuilder constructUrlFromBase(StringBuilder baseUrl)
    {
        // XXX for now, as a hack we have to do some unpleasant things to get
        // the jetfire URL (as we use this exclusively to upload error reports).
        //
        // It should be:
//        baseUrl.append(mCallback.getUploadLocation());

        try
        {
            String sessionId = getSessionID();
            baseUrl = new StringBuilder("http://jetfire.datcon.co.uk/cust/session")
                            .append(sessionId)
                            .append(mCallback.getUploadLocation());
        }
        catch (IOException e)
        {
            // Unable to get session id, treat as network error
            mLog.error("IO Exception getting session id from jetfire", e);
            handleNetworkError(CPNetworkError.NETWORK_UNAVAILABLE);
        }

        return baseUrl;
    }

    @Override
    protected HttpUriRequest getHttpRequester(String url)
                      throws UnsupportedEncodingException, FileNotFoundException
    {
        HttpPost post = new HttpPost(url);
        post.setHeader("User-Agent", getUserAgent());

        // Add the file to the post
        File file = mCallback.getFile();
        FileInputStream fin = new FileInputStream(file);
        BasicHttpEntity entity = new BasicHttpEntity();
        entity.setContent(fin);
        entity.setContentLength(file.length());
        post.setEntity(entity);

        return post;
    }

    @Override
    protected void handleDataError(CPDataError error)
    {
        mLog.info("Upload failed " + error);
        mCallback.onDataFailure(error);
    }

    @Override
    protected boolean handleOKData(JSONArray data)
    {
        mLog.debug("Upload successful");
        mCallback.onUploadSuccess();

        // The data is good so reset the backoff
        mCommPortalService.getBackoff().onSuccess();

        return true;
    }

    @Override
    protected JSONArray getJSONFromEntity(HttpEntity entity)
                                               throws JSONException, IOException
    {
        // The entity is empty unless there is an error.
        return new JSONArray("[" + EntityUtils.toString(entity) + "]");
    }



    // XXX methods which we should not require follow:

    /**
     * Logs into CommPortal via POST, and obtains a session ID to use when we
     * POST our problem report.
     * @return A session ID, as a string.
     * @throws IOException
     */
    private String getSessionID() throws IOException
    {
        // These shouldn't be hardcoded. @@SMK@@
        String directoryNumber = "2345550216";
        String password = "0684";
        String urlBase = "http://jetfire.datcon.co.uk/cust/";

        String urlLogin = urlBase + "login?version=7.4";

        // Build a list of name/value pairs corresponding to the form
        // data that we POST to log in.
        List<NameValuePair> postData = new ArrayList<NameValuePair>();
        postData
            .add(new BasicNameValuePair("DirectoryNumber", directoryNumber));
        postData.add(new BasicNameValuePair("Password", password));
        postData.add(new BasicNameValuePair("ApplicationID", "MS_Android_Acc"));
        postData.add(new BasicNameValuePair("ApplicationVersion", "2.0.0.0"));
        postData
            .add(new BasicNameValuePair("ContextInfo", "version=2.0.0.0.0"));

        // Set up the HttpClient.
        DefaultHttpClient httpClient = null;
        HttpPost postMethod = new HttpPost(urlLogin);
        HttpParams params = new BasicHttpParams();
        httpClient = new DefaultHttpClient(params);

        // Correctly escape the form data and include it in the request.
        String s = URLEncodedUtils.format(postData, HTTP.UTF_8);
        StringEntity entity = new StringEntity(s, HTTP.UTF_8);
        entity.setContentType(URLEncodedUtils.CONTENT_TYPE);
        postMethod.setEntity(entity);

        // Send the request, and attempt to obtain any session ID in the
        // response body.
        HttpResponse response = httpClient.execute(postMethod);
        HttpEntity responseEntity = response.getEntity();
        String responseContent =
            convertStreamToString(responseEntity.getContent());
        Pattern pattern = Pattern.compile("session=(.*)");
        Matcher m = pattern.matcher(responseContent);
        if (m.find())
        {
            return m.group(1);
        }
        else
        {
            throw new IOException("No session ID found in server response.");
        }
    }

    /**
     * Takes an InputStream and writes it to a string.
     * @param is The InputStream.
     * @return A String containing the data passed by the stream.
     */
    private String convertStreamToString(InputStream is)
    {
        String result;
        try
        {
            result = new Scanner(is).useDelimiter("\\A").next();
        }
        catch (NoSuchElementException e)
        {
            result = "";
        }
        return result;
    }

    @Override
    protected String getUserAgent()
    {
        return "CommPortal Communicator";
    }
}
