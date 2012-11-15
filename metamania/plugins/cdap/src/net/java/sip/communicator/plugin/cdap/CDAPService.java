// CDAPService.java
// (C) COPYRIGHT METASWITCH NETWORKS 2012
package net.java.sip.communicator.plugin.cdap;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;

import net.java.sip.communicator.util.*;
import net.java.sip.communicator.util.swing.*;

import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.apache.http.util.*;
import org.jitsi.service.configuration.*;
import org.jitsi.service.resources.*;
import org.json.*;
import org.osgi.framework.*;

/**
 * Service provider branding service.
 *
 * Handles the following:
 * <ol>
 * <li>Fetching the service provider list from the CDAP server</li>
 * <li>Getting the user to select a service provider</li>
 * <li>Fetching the branding information for the chosen service provider from
 *     the CDAP server</li>
 * </ol>
 */
public class CDAPService
{
    /** Default logger. */
    private static final Logger logger = Logger.getLogger(CDAPService.class);

    /** String used for the default version of any CDAP config */
    public static final String DEFAULT = "default";

    /** Name of the CDAP URL in the default properties. */
    private static final String DEFAULT_CDAP_URL =
                                                 "plugin.cdap.DEFAULT_CDAP_URL";

    /** Name of the CDAP URL in the config. */
    private static final String PROPERTY_CDAP_URL =
                                    "net.java.sip.communicator.plugin.cdap.URL";

    /** Name of the length of time to wait between polls of CDAP config */
    private static final String PROPERTY_REFRESH_RATE =
                           "net.java.sip.communicator.plugin.cdap.REFRESH_RATE";

    /** The default length of time to wait between polls of CDAP config */
    private static final long DEFAULT_REFRESH_RATE = 24 * 60 * 60 * 1000;

    /** Name of the service provider ID in the config. */
    public static final String PROPERTY_SERVICE_PROVIDER_ID =
                    "net.java.sip.communicator.plugin.cdap.service_provider_id";

    /** Name of the service provider name in the config. */
    public static final String PROPERTY_SERVICE_PROVIDER_NAME =
                  "net.java.sip.communicator.plugin.cdap.service_provider_name";

    /** Name of the provisioning URL in the config. */
    private static final String PROPERTY_PROVISIONING_URL =
                            "net.java.sip.communicator.plugin.provisioning.URL";

    /** Name of the CDAP version number in the config */
    private static final String PROPERTY_CDAP_VERSION_NUMBER =
                         "net.java.sip.communicator.plugin.cdap.version_number";

    /** Suffix to add to provisioning URL for passing user parameters. */
    private static final String PROVISIONING_SUFFIX =
        "/communicator/login?Username=${username}&Password=${password}&" +
        "ComputerID=EXAMPLE_ID&Platform=windows&OSVersion=2.3.4&SPID=metaswitchuk/android";

    /** Passkey for CDAP server. */
    private static final String CDAP_PASSKEY = "0vCDX3iheXpSrLasiXe4zI4CymdgXG5R";

    /** URL path to retrieve carrier list. */
    private static final String CDAP_PATH_LIST = "list";

    /** URL path to retrieve update information of a carrier */
    private static final String CDAP_PATH_UPDATE = "poll";

    /** URL path to retrieve updated branding. */
    private static final String CDAP_PATH_BRANDING = "branding";

    /** Name of the file in branding.zip that contains useful URLs. */
    private static final String INFO_FILE_NAME = "branding.txt";

    /** The config service. */
    private final ConfigurationService mConfig;

    /** The resources service */
    private ResourceManagementService mResources;

    /** Timer for checking for updates to the CDAP data */
    private Timer mTimer;

    /**
     * Constructor.
     */
    public CDAPService()
    {
        mConfig = CDAPActivator.getConfigurationService();
        mResources = CDAPActivator.getResourceService();
    }

    /**
     * Starts provisioning.
     *
     * @param url provisioning URL
     */
    void start()
    {
        logger.logEntry();

        if (mConfig.getInt(PROPERTY_SERVICE_PROVIDER_ID, 0) == 0)
        {
            logger.debug("No service provider configured.");

            try
            {
                // Get a list of service providers.
                HashMap<String, ServiceProviderDetails> serviceProviders =
                                                        fetchServiceProviders();

                // Show the CDAP service provider selection window and wait for
                // the user to choose a provider or otherwise exit the window.
                // By returning the provider ID rather than saving it to config
                // immediately, we can make sure that we don't save an ID unless
                // we also have branding info, otherwise we could skip the
                // branding process in future even when there is no branding
                // information.
                //
                // NOTE: showCDAPWindow is a blocking method, which prevents the
                // provisioning process from starting until we have had a chance
                // to retrieve a provisioning URL for the service provider.
                String chosenProvider = showCDAPWindow(serviceProviders);

                // Get branding information from the CDAP server and save it.
                fetchAndSaveBrandingInfo(chosenProvider);
            }
            catch (JSONException e)
            {
                // There was a problem parsing branding information.  Show an
                // error dialog and close the app.
                logger.error("Caught JSONException.  " +
                                         "Client has no branding information.");
                String errorTextRes = "plugin.cdap.PROBLEM_RETRIEVING";
                closeAppWithDialog(errorTextRes);
            }
            catch (IOException e)
            {
                // There was a problem getting branding information.  Show an
                // error dialog and close the app.
                logger.error("Caught IOException.  " +
                "Client has no branding information.");
                String errorTextRes = "plugin.cdap.PROBLEM_RETRIEVING";
                closeAppWithDialog(errorTextRes);
            }
            catch (Exception e)
            {
                // The user cancelled service provider selection, which should
                // exit the app.
                logger.debug("User cancelled service provider selection.");
                closeApp();
            }
        }

        // Regardless of whether we have data or not, start a timer to check for
        // updates to the data:
        TimerTask timerTask = new TimerTask()
        {
            @Override
            public void run()
            {
                String spID = mConfig.getString(PROPERTY_SERVICE_PROVIDER_ID);

                if (updateAvailable() && spID != null)
                {
                    try
                    {
                        logger.info("New CDAP data available");
                        fetchAndSaveBrandingInfo(spID);
                    }
                    catch (Exception e)
                    {
                        logger.error("Error updating CDAP data", e);
                    }
                }
            }
        };

        long scheduleRate = mConfig.getLong(PROPERTY_REFRESH_RATE,
                                            DEFAULT_REFRESH_RATE);
        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(timerTask, scheduleRate, scheduleRate);

        logger.logExit();
    }

    /**
     * @return if the current CDAP data is out of date
     */
    private boolean updateAvailable()
    {
        boolean updateAvailable;

        try
        {
            String updateUrl = getCDAPUrl(CDAP_PATH_UPDATE);
            HttpGet get = new HttpGet(updateUrl);
            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse response = httpClient.execute(get);
            logger.debug("Got a response from CDAP server.");

            // Parse the response to a HashMap with service provider details.
            HttpEntity entity = response.getEntity();
            String responseString = EntityUtils.toString(entity);
            JSONObject jsonObject = new JSONObject(responseString);

            int currentRev = jsonObject.getInt("rev");
            int serverRev  = mConfig.getInt(PROPERTY_CDAP_VERSION_NUMBER, 0);

            updateAvailable = serverRev > currentRev;
        }
        catch (Exception e)
        {
            // Just log and assume that no update is available
            logger.error("Error getting CDAP revision number", e);
            updateAvailable = false;
        }

        return updateAvailable;
    }

    /**
     * Fetch a list of service providers from the CDAP server.
     *
     * @return a HashMap of service provider names and details
     */
    private HashMap<String, ServiceProviderDetails> fetchServiceProviders()
                                                              throws IOException
    {
        logger.logEntry();
        HashMap<String, ServiceProviderDetails> providers;

        try
        {
            // Make a GET request to the CDAP 'list' URL.
            String urlString = getCDAPUrl(CDAP_PATH_LIST);
            HttpGet get = new HttpGet(urlString);
            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse response = httpClient.execute(get);
            logger.debug("Got a response from CDAP server.");

            // Parse the response to a HashMap with service provider details.
            HttpEntity entity = response.getEntity();
            String responseString = EntityUtils.toString(entity);
            providers = parseServiceProviderDetails(responseString);
        }
        catch (IOException e)
        {
            // Log the specifics here and re-throw the exception.
            logger.error("IOException getting service providers from CDAP.", e);
            throw e;
        }

        logger.logExit();
        return providers;
    }

    /**
     * Parse the details of the service provider from a JSON array.
     * JSON is expected to look like:
     * [
     *   {
     *     "id": 121004,
     *     "name": {
     *       "enUS": "Foo",
     *       "enGB": "Bar"
     *       "default": "Foo"
     *     },
     *     "hidden": false,
     *     "region": "us"
     *   },
     *   {
     *     "id": 185003,
     *     "name": {
     *       "en": "Foo",
     *       "default": "Bar"
     *     },
     *     "hidden": false,
     *     "region": "us"
     *   }
     * ]
     *
     * This method has been copied almost verbatim from the AccessionAndroid
     * source.
     *
     * @param responseString - response from the CDAP server.
     * @throws JSONException - if data retrieved from server does not parse to a
     *                         JSONArray.
     */
    private static HashMap<String, ServiceProviderDetails>
                              parseServiceProviderDetails(String responseString)
    {
        HashMap<String, ServiceProviderDetails> serviceProviderDetailsMap =
            new HashMap<String, ServiceProviderDetails>();
        JSONArray data = null;

        try
        {
            data = new JSONArray(responseString);
        }
        catch (JSONException jsonEx)
        {
            // JSON array did not have expected structure.  This is unexpected
            // as it should be checked on the server.  Log the error and don't
            // add that service provider to the list.
            logger.error("JSON exception parsing service provider list. " +
                "Message: " + jsonEx.getMessage());
        }

        if (data != null)
        {
            String cdapLocale = Locale.getDefault().toString().replace("_", "");

            for (int i = 0; i < data.length(); i++)
            {
                try
                {
                    JSONObject jsonObject = data.getJSONObject(i);

                    String id = jsonObject.getString("id").toString();
                    String name = "";
                    String locale = cdapLocale;

                    name = jsonObject.getJSONObject("name")
                                     .optString(cdapLocale);

                    if ("".equals(name))
                    {
                        // See if there is a name present for the language but
                        // not the locale.
                        locale = cdapLocale.substring(0, 2);
                        name = jsonObject.getJSONObject("name")
                                         .optString(locale);

                        if ("".equals(name))
                        {
                            // If the phone's locale is not present, use the
                            // default.  Use getString this time because a
                            // default name is expected and if one is not
                            // present this represents an error in the JSON.
                            locale = "default";
                            name = jsonObject.getJSONObject("name")
                                             .getString(locale);
                        }
                    }

                    String region = jsonObject.getString("region").toString();
                    boolean hidden = jsonObject.getBoolean("hidden");

                    ServiceProviderDetails spDetails =
                           new ServiceProviderDetails(id, name, region, hidden);

                    logger.debug("Found name " + name + "for locale " + locale);
                    serviceProviderDetailsMap.put(name, spDetails);
                }
                catch (JSONException jsonEx)
                {
                    // JSON did not have expected structure.  This is unexpected
                    // as it should be checked on the server.  Log the error and
                    // don't add that service provider to the list.
                    logger.error("JSON exception parsing service provider "
                                + i + ". " + "Message: " + jsonEx.getMessage());
                }
            }
        }

        return serviceProviderDetailsMap;
    }

    /**
     * Create and show a service provider selection window, blocking until it is
     * closed.
     *
     * @param serviceProviders - a HashMap of service provider names and
     *                           details (as received from CDAP)
     * @return the ID of the user-selected service provider
     */
    private String showCDAPWindow(
        HashMap<String, ServiceProviderDetails> serviceProviders) throws Exception
    {
        // Create the CDAP dialog, where the list of service providers is
        // displayed and the user is asked to select one.
        logger.logEntry();
        String chosenServiceProvider = new String();
        String serviceProviderID = null;

        // setVisible is a blocking process, preventing this code from
        // continuing until the window is closed.
        logger.debug("Creating service provider selection window and waiting.");
        CDAPWindow cdapWindow = new CDAPWindow(serviceProviders);
        cdapWindow.setVisible(true);

        if(!cdapWindow.isCanceled())
        {
            // The user pressed 'OK', so get the selected service provider and
            // save it to config.
            logger.debug("User pressed 'OK' to close CDAP window.");
            chosenServiceProvider = cdapWindow.getServiceProvider();

            logger.debug("Service provider is " + chosenServiceProvider + "; " +
            "saving ID to config.");
            serviceProviderID = serviceProviders.get(chosenServiceProvider).getId();
        }
        else
        {
            // Throw an exception so that the calling method can close the app.
            logger.debug("User cancelled CDAP window.");
            throw new Exception("User cancelled service provider selection.");
        }

        logger.logExit();
        return serviceProviderID;
        }

    /**
     * Get branding information from the server and save it.
     *
     * @param serviceProviderID - the ID of the service provider to get branding
     * info for e.g. 71001
     * @throws Exception - exceptions are logged in the methods that throw them,
     *  and are passed straight to the calling method.
     */
    private void fetchAndSaveBrandingInfo(String serviceProviderID)
                                               throws IOException, JSONException
    {
        // Fetch branding info for the configured service provider from the CDAP
        // server, parse the branding.txt file to JSON, and save the relevant
        // bits in the config.
        logger.logEntry();
        File brandingZip = fetchBrandingInfo(serviceProviderID);
        JSONObject brandingInfo = parseBrandingInfo(brandingZip);
        saveBrandingInfo(brandingInfo);

        // Only save the service provider ID now that all other branding
        // information has been saved - otherwise if an exception was hit,
        // the app could launch without branding next time.
        mConfig.setProperty(PROPERTY_SERVICE_PROVIDER_ID, serviceProviderID);
        logger.logExit();
    }

    /**
     * Fetch branding information from the server and save it as a temporary
     * zip.
     *
     * @param serviceProviderID - the ID of the service provider to get branding
     * info for e.g. 71001
     * @return the zip to which the branding information has been saved.
     * @throws IOException
     */
    private File fetchBrandingInfo(String serviceProviderID) throws IOException
    {
        logger.logEntry();
        File zipFile;
        FileOutputStream zipFileOutputStream;

        try
        {
            // Set up a temporary zip file to save the data to.
            zipFile = File.createTempFile("cdap", ".zip");
            zipFileOutputStream = new FileOutputStream(zipFile);
        }
        catch (IOException ioe)
        {
            // We don't expect an exceptions here in normal use.  Log the specifics
            // here and re-throw the exception.
            logger.error(
                "Unexpected exception creating temp file for branding info.", ioe);
            throw ioe;
        }

        try
        {
            // Send a GET to the CDAP 'branding' URL, specifying the correct service
            // provider.
            URL brandingUrl = new URL(getCDAPUrl(CDAP_PATH_BRANDING, serviceProviderID));
            InputStream inputStream = brandingUrl.openConnection().getInputStream();

            logger.debug("Reading branding zip file into cdap.zip");
            int size;
            byte[] buffer = new byte[2048];
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
                zipFileOutputStream);

            try
            {
                // Read the response to our temporary file.
                while ((size = inputStream.read(buffer, 0, buffer.length)) != -1)
                {
                    bufferedOutputStream.write(buffer, 0, size);
                }

                bufferedOutputStream.flush();
            }
            finally
            {
                logger.debug("Closing streams.");
                bufferedOutputStream.close();
                inputStream.close();
            }
        }
        catch (IOException ioe)
        {
            // Error getting input stream from URL (bad URL, creating input stream
            // failed, or protocol doesn't support input).  Log the specifics here
            // and re-throw the exception.
            logger.error("IOException getting input stream from CDAP branding URL",
                ioe);
            throw ioe;
        }

        logger.logExit();
        return zipFile;
    }

    /**
     * Read the branding.txt file from our zip and parse the contents to JSON.
     *
     * @param brandingZip - the archive that branding info has been saved to.
     * @return a JSON object of the branding information in branding.txt
     * @throws IOException
     * @throws JSONException
     */
    private JSONObject parseBrandingInfo(File brandingZip)
    throws IOException, JSONException
    {
        logger.logEntry();
        JSONObject jsonBranding;

        try
        {
            String branding = readFileFromZip(INFO_FILE_NAME, brandingZip);
            jsonBranding = new JSONObject(branding);
        }
        catch (IOException ioe)
        {
            // Log the specifics here and re-throw the exception.
            logger.error("IOException reading " + INFO_FILE_NAME + " from " +
                brandingZip.getAbsolutePath());
            throw ioe;
        }
        catch (JSONException je)
        {
            // Log the specifics here and re-throw the exception.
            logger.error("JSONException parsing contents of " + INFO_FILE_NAME +
                " from " + brandingZip.getAbsolutePath());
            throw je;
        }

        logger.logExit();
        return jsonBranding;
    }

    /**
     * Read a file in a zip archive to a string e.g. branding.txt in branding.zip
     *
     * @param filename - the name of the file to be read.
     * @param archive - the zip archive containing the file to be read.
     * @return the contents of the file as a string
     * @throws IOException
     */
    private String readFileFromZip(String filename, File archive)
    throws IOException
    {
        // Get the contents of the file in the zip archive.
        logger.logEntry();
        ZipFile zipFile = new ZipFile(archive);
        InputStream inputStream = zipFile.getInputStream(new ZipEntry(filename));

        // Read the contents to a string buffer.
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(inputStream));
        String str;
        StringBuffer stringBuffer = new StringBuffer();

        while ((str = reader.readLine()) != null)
        {
            stringBuffer.append(str);
        }

        logger.logExit();
        return stringBuffer.toString();
    }

    /**
     * Save the necessary bits of the branding information to config (currently
     * only the provisioning URL is saved).
     *
     * @param brandingInfo - the branding information to save (from branding.txt
     * in branding.zip from the CDAP server)
     * @throws JSONException
     */
    private void saveBrandingInfo(JSONObject brandingInfo) throws JSONException
    {
        logger.logEntry();

        try
        {
            String provUrl = brandingInfo.getJSONObject("urls")
                                         .getJSONObject("provisioning-default")
                                         .getString("url");

            // SMK:  It would be better to only save the base provisioning URL here
            // and for the provisioning code to add on anything else it needs.
            mConfig.setProperty(PROPERTY_PROVISIONING_URL,
                                                 provUrl + PROVISIONING_SUFFIX);

            // Save the version number:
            mConfig.setProperty(PROPERTY_CDAP_VERSION_NUMBER,
                                                    brandingInfo.getInt("rev"));

            // Check there is a default entry for the service provider name.
            JSONObject nameJSONObject = brandingInfo.getJSONObject("name");
            Iterator localesIterator = nameJSONObject.keys();
            boolean foundDefaultName = false;

            while (localesIterator.hasNext())
            {
                String locale = (String)localesIterator.next();

                if (DEFAULT.equals(locale))
                {
                    logger.debug("Found a default service provider name");
                    String name = nameJSONObject.getString(locale);
                    mConfig.setProperty(PROPERTY_SERVICE_PROVIDER_NAME, name);
                    foundDefaultName = true;
                }
            }

            if (!foundDefaultName)
            {
                // A default URL is mandatory on the CDAP server so this is
                // unexpected.  This is not fatal as we can use the brand name but
                // should be logged as an exception.
                logger.error("No default name found in CDAP data");
            }

        }
        catch (JSONException je)
        {
            logger.error("JSONException getting provisioning info from branding.txt",
                je);
            throw je;
        }

        logger.logExit();
    }

    /**
     * Form a CDAP URL for a given path.
     *
     * @param cdapPath - the path of the required URL on the CDAP server i.e.
     *                   'list' or 'branding'.
     * @return a URL corresponding to the given path for requesting information
     *          from the CDAP server.
     */
    public String getCDAPUrl(String cdapPath)
    {
        // Get the service provider ID from the config and provide it as an
        // argument to getCDAPUrl(cdapPath, serviceProviderID)
        logger.logEntry();
        String serviceProviderID = mConfig.getString(PROPERTY_SERVICE_PROVIDER_ID);
        String cdapUrlString = getCDAPUrl(cdapPath, serviceProviderID);
        logger.logExit();
        return cdapUrlString;
    }

    /**
     * Form a CDAP URL for a given path.
     *
     * @param cdapPath - the path of the required URL on the CDAP server i.e.
     *                   'list' or 'branding'.
     * @param serviceProviderID - the ID of the service provider e.g. 71001
     * @return URL corresponding to the given path for requesting information
     *          from the CDAP server.
     */
    public String getCDAPUrl(String cdapPath, String serviceProviderID)
    {
        logger.logEntry();
        String cdapBaseUrl = mConfig.getString(PROPERTY_CDAP_URL);

        if (cdapBaseUrl == null || cdapBaseUrl.length() <= 0)
        {
            // The base URL is not in the config, so look in the default properties
            // resource.
            logger.debug("CDAP URL not found in config.");
            cdapBaseUrl = CDAPActivator.getResourceService().getSettingsString(
                DEFAULT_CDAP_URL);

            if (cdapBaseUrl != null && cdapBaseUrl.length() > 0)
            {
                logger.debug("CDAP URL found in defaults; saving in config.");
                mConfig.setProperty(PROPERTY_CDAP_URL, cdapBaseUrl);
            }
        }

        // Sanitise the end of the URL.
        if (!cdapBaseUrl.endsWith("/"))
        {
            cdapBaseUrl += "/";
        }

        // Add the URL parameters.
        // PPON:  Determine when these need changing.
        String cdapUrlString = cdapBaseUrl + cdapPath +
              "?version=1&app=cpmobandroid&passkey=" +
              CDAP_PASSKEY + "&cid=dummyID";

        if (cdapPath.equalsIgnoreCase(CDAP_PATH_BRANDING) ||
            cdapPath.equalsIgnoreCase(CDAP_PATH_UPDATE))
        {
            // Attach the service provider ID from the config.
            logger.debug("Adding service provider ID to CDAP URL.");
            cdapUrlString += "&id=" + serviceProviderID;
        }

        logger.debug("CDAP URL string " + cdapUrlString);
        logger.logExit();
        return cdapUrlString;
    }

    /**
     * Close the application after showing an error dialog to the user.
     *
     * @param errorTextRes - the message resource to be shown in the error dialog.
     */
    private void closeAppWithDialog(String errorTextRes)
    {
        logger.logEntry();

        // Create and show the dialog.
        String errorText = mResources.getI18NString(errorTextRes);
        ErrorDialog dialog = new ErrorDialog(null, "Error", errorText);
        dialog.setModal(true);
        dialog.showDialog();

        // The dialog has been dismissed - close the app.
        closeApp();
        logger.logExit();
    }

    /**
     * Close the application.
     */
    private void closeApp()
    {
        logger.logEntry();
        BundleContext cdapBundleContext = CDAPActivator.getBundleContext();

        // Get all the application's bundles and stop them one by one.
        for(Bundle bundle : cdapBundleContext.getBundles())
        {
            try
            {
                if(cdapBundleContext.equals(bundle.getBundleContext()))
                {
                    // Allow our bundle to exit gracefully, so that we can keep
                    // closing other bundles.
                    continue;
                }

                bundle.stop();
            }
            catch (BundleException be)
            {
                logger.error("BundleException stopping bundle " +
                                                      bundle.getLocation(), be);
            }
        }

        logger.logExit();
    }
}