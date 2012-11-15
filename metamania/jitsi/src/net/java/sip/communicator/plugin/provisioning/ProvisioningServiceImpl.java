package net.java.sip.communicator.plugin.provisioning;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.util.regex.*;

import javax.swing.*;

import net.java.sip.communicator.service.httputil.*;
import net.java.sip.communicator.service.provisioning.*;
import net.java.sip.communicator.util.*;
import net.java.sip.communicator.util.Logger;
import net.java.sip.communicator.util.swing.*;

import org.jitsi.service.configuration.*;
import org.jitsi.util.*;
import org.osgi.framework.*;

/**
 * Provisioning service.
 *
 * @author Sebastien Vincent
 */
public class ProvisioningServiceImpl
    implements ProvisioningService
{
    /**
     * Logger of this class
     */
    private static final Logger logger
        = Logger.getLogger(ProvisioningServiceImpl.class);

    /**
     * Name of the UUID property.
     */
    public static final String PROVISIONING_UUID_PROP
        = "net.java.sip.communicator.UUID";

    /**
     * Name of the provisioning URL in the configuration service.
     */
    private static final String PROPERTY_PROVISIONING_URL
        = "net.java.sip.communicator.plugin.provisioning.URL";

    /**
     * Name of the provisioning username in the configuration service
     * authentication).
     */
    static final String PROPERTY_PROVISIONING_USERNAME
        = "net.java.sip.communicator.plugin.provisioning.auth.USERNAME";

    /**
     * Name of the provisioning password in the configuration service (HTTP
     * authentication).
     */
    static final String PROPERTY_PROVISIONING_PASSWORD
        = "net.java.sip.communicator.plugin.provisioning.auth";

    /**
     * Name of the property that contains the provisioning method (i.e. DHCP,
     * DNS, manual, ...).
     */
    private static final String PROVISIONING_METHOD_PROP
        = "net.java.sip.communicator.plugin.provisioning.METHOD";

    /**
     * Name of the property, whether provisioning is mandatory.
     */
    private static final String PROPERTY_PROVISIONING_MANDATORY
        = "net.java.sip.communicator.plugin.provisioning.MANDATORY";

    /**
     * Name of the property that contains enforce prefix list (separated by
     * pipe) for the provisioning. The retrieved configuration properties will
     * be checked against these prefixes to avoid having incorrect content in
     * the configuration file (such as HTML content resulting of HTTP error).
     */
    private static final String PROVISIONING_ALLOW_PREFIX_PROP
        = "provisioning.ALLOW_PREFIX";

    /**
     * Name of the enforce prefix property.
     */
    private static final String PROVISIONING_ENFORCE_PREFIX_PROP
        = "provisioning.ENFORCE_PREFIX";


    /**
     * List of allowed configuration prefixes.
     */
    private List<String> allowedPrefixes = new ArrayList<String>();

    /**
     * Timer for getting config
     */
    private Timer mTimer;

    /**
     * Access to the config service
     */
    private final ConfigurationService mConfig;

    /**
     * If true, then we have got some config stored and valid:
     */
    private boolean mStoredConfig = false;

    /**
     * Authentication username.
     */
     private static String provUsername = null;

    /**
     * Authentication password.
     */
     private static String provPassword = null;

     /**
      * Constructor.
      */
     public ProvisioningServiceImpl()
     {
         // check if UUID is already configured
         mConfig = ProvisioningActivator.getConfigurationService();
         String uuid = mConfig.getString(PROVISIONING_UUID_PROP);

         if(uuid == null || uuid.equals(""))
         {
             uuid = UUID.randomUUID().toString();
             mConfig.setProperty(PROVISIONING_UUID_PROP, uuid);
         }

         mStoredConfig =
                    mConfig.getProperty(PROPERTY_PROVISIONING_USERNAME) != null;
     }

     /**
      * Starts provisioning.
      *
      * @param url provisioning URL
      */
     void start(String url)
     {
         getAndStoreConfig(url);

         if (mStoredConfig)
         {
             // We got some config, now set up a task to poll for config:
             mTimer = new Timer();
             TimerTask task = new TimerTask()
             {

                @Override
                public void run()
                {
                    logger.info("Running a scheduled update check");
                    getAndStoreConfig(null);
                }
             };

             //  Schedule an update 1 day after now:
             long scheduleRate = 86400000;
             mTimer.scheduleAtFixedRate(task,
                                        scheduleRate,
                                        scheduleRate);
         }
     }

     /**
      * Get the config from the presented URL and store it in the config file
      *
      * @param url The URL whence to get config
      */
     private void getAndStoreConfig(String url)
     {
         if(url == null)
         {
             /* try to see if provisioning URL is stored in properties */
             url = getProvisioningUri();
         }

         if(!StringUtils.isNullOrEmpty(url))
         {
             File file = retrieveConfigurationFile(url);

             if(file != null)
             {
                 /* store the provisioning URL in local configuration in case
                  * the provisioning discovery failed (DHCP/DNS unavailable, ...)
                  */
                 mConfig.setProperty(PROPERTY_PROVISIONING_URL, url);

                 updateConfiguration(file);
             }
         }
     }

     /**
      * Indicates if the provisioning has been enabled.
      *
      * @return <tt>true</tt> if the provisioning is enabled, <tt>false</tt> -
      * otherwise
      */
     public String getProvisioningMethod()
     {
         String provMethod = mConfig.getString(PROVISIONING_METHOD_PROP);

         if (provMethod == null || provMethod.length() <= 0)
         {
             provMethod = ProvisioningActivator.getResourceService().
                 getSettingsString(
                     "plugin.provisioning.DEFAULT_PROVISIONING_METHOD");

             if (provMethod != null && provMethod.length() > 0)
                 setProvisioningMethod(provMethod);
         }

         return provMethod;
     }

     /**
      * Enables the provisioning with the given method. If the provisioningMethod
      * is null disables the provisioning.
      *
      * @param provisioningMethod the provisioning method
      */
     public void setProvisioningMethod(String provisioningMethod)
     {
         mConfig.setProperty(PROVISIONING_METHOD_PROP, provisioningMethod);
     }

     /**
      * Returns the provisioning URI.
      *
      * @return the provisioning URI
      */
     public String getProvisioningUri()
     {
         String provUri = mConfig.getString(PROPERTY_PROVISIONING_URL);

         if (provUri == null || provUri.length() <= 0)
         {
             provUri = ProvisioningActivator.getResourceService().
                 getSettingsString(
                     "plugin.provisioning.DEFAULT_PROVISIONING_URI");

             if (provUri != null && provUri.length() > 0)
                 setProvisioningUri(provUri);
         }
         return provUri;
     }

     /**
      * Sets the provisioning URI.
      *
      * @param uri the provisioning URI to set
      */
     public void setProvisioningUri(String uri)
     {
         mConfig.setProperty(PROPERTY_PROVISIONING_URL, uri);
     }

    /**
     * Returns provisioning username.
     *
     * @return provisioning username
     */
    public String getProvisioningUsername()
    {
        return provUsername;
    }

    /**
     * Returns provisioning password.
     *
     * @return provisioning password
     */
    public String getProvisioningPassword()
    {
        return provPassword;
    }

    /**
     * Retrieve configuration file from provisioning URL.
     * This method is blocking until configuration file is retrieved from the
     * network or if an exception happen
     *
     * @param url provisioning URL
     * @return provisioning file downloaded
     */
    private File retrieveConfigurationFile(String url)
    {
        File tmpFile = null;
        try
        {
            String arg = null;
            String args[] = null;
            final File temp = File.createTempFile("provisioning",
                    ".properties");

            tmpFile = temp;

            URL u = new URL(url);
            InetAddress ipaddr =
                ProvisioningActivator.getNetworkAddressManagerService().
                    getLocalHost(InetAddress.getByName(u.getHost()));

            // Get any system environment identified by ${env.xyz}
            Pattern p = Pattern.compile("\\$\\{env\\.([^\\}]*)\\}");
            Matcher m = p.matcher(url);
            StringBuffer sb = new StringBuffer();
            while(m.find())
            {
                String value = System.getenv(m.group(1));
                if(value != null)
                {
                    m.appendReplacement(sb, Matcher.quoteReplacement(value));
                }
            }
            m.appendTail(sb);
            url = sb.toString();

            // Get any system property variable identified by ${system.xyz}
            p = Pattern.compile("\\$\\{system\\.([^\\}]*)\\}");
            m = p.matcher(url);
            sb = new StringBuffer();
            while(m.find())
            {
                String value = System.getProperty(m.group(1));
                if(value != null)
                {
                    m.appendReplacement(sb, Matcher.quoteReplacement(value));
                }
            }
            m.appendTail(sb);
            url = sb.toString();

            if(url.indexOf("${home.location}") != -1)
            {
                url = url.replace("${home.location}",
                                  mConfig.getScHomeDirLocation());
            }

            if(url.indexOf("${home.name}") != -1)
            {
                url = url.replace("${home.name}", mConfig.getScHomeDirName());
            }

            if(url.indexOf("${uuid}") != -1)
            {
                url = url.replace("${uuid}",
                                  mConfig.getString(PROVISIONING_UUID_PROP));
            }

            if(url.indexOf("${osname}") != -1)
            {
                url = url.replace("${osname}", System.getProperty("os.name"));
            }

            if(url.indexOf("${arch}") != -1)
            {
                url = url.replace("${arch}", System.getProperty("os.arch"));
            }

            if(url.indexOf("${resx}") != -1 || url.indexOf("${resy}") != -1)
            {
                Rectangle screen = ScreenInformation.getScreenBounds();

                if(url.indexOf("${resx}") != -1)
                {
                    url = url.replace("${resx}", String.valueOf(screen.width));
                }

                if(url.indexOf("${resy}") != -1)
                {
                    url = url.replace("${resy}", String.valueOf(screen.height));
                }
            }

            if(url.indexOf("${build}") != -1)
            {
                url = url.replace("${build}",
                        System.getProperty("sip-communicator.version"));
            }

            if(url.indexOf("${ipaddr}") != -1)
            {
                url = url.replace("${ipaddr}", ipaddr.getHostAddress());
            }

            if(url.indexOf("${hostname}") != -1)
            {
                String name;
                if(OSUtils.IS_WINDOWS)
                {
                    // avoid reverse DNS lookup
                    name = System.getenv("COMPUTERNAME");
                }
                else
                {
                    name = ipaddr.getHostName();
                }
                url = url.replace("${hostname}", name);
            }

            if(url.indexOf("${hwaddr}") != -1)
            {
                if(ipaddr != null)
                {
                    /* find the hardware address of the interface
                     * that has this IP address
                     */
                    Enumeration<NetworkInterface> en =
                        NetworkInterface.getNetworkInterfaces();

                    while(en.hasMoreElements())
                    {
                        NetworkInterface iface = en.nextElement();

                        Enumeration<InetAddress> enInet =
                            iface.getInetAddresses();

                        while(enInet.hasMoreElements())
                        {
                            InetAddress inet = enInet.nextElement();

                            if(inet.equals(ipaddr))
                            {
                                byte hw[] =
                                    ProvisioningActivator.
                                        getNetworkAddressManagerService().
                                            getHardwareAddress(iface);

                                if(hw == null)
                                    continue;

                                StringBuffer buf =
                                    new StringBuffer();

                                for(byte h : hw)
                                {
                                    int hi = h >= 0 ? h : h + 256;
                                    String t = new String(
                                            (hi <= 0xf) ? "0" : "");
                                    t += Integer.toHexString(hi);
                                    buf.append(t);
                                    buf.append(":");
                                }

                                buf.deleteCharAt(buf.length() - 1);

                                url = url.replace("${hwaddr}",
                                        buf.toString());

                                break;
                            }
                        }
                    }
                }
            }

            if(url.contains("?"))
            {
                /* do not handle URL of type http://domain/index.php? (no
                 * parameters)
                 */
                if((url.indexOf('?') + 1) != url.length())
                {
                    arg = url.substring(url.indexOf('?') + 1);
                    args = arg.split("&");
                }
                url = url.substring(0, url.indexOf('?'));
            }

            ArrayList<String> paramNames = null;
            ArrayList<String> paramValues = null;
            int usernameIx = -1;
            int passwordIx = -1;

            if(args != null && args.length > 0)
            {
                paramNames = new ArrayList<String>(args.length);
                paramValues = new ArrayList<String>(args.length);

                for(int i = 0; i < args.length; i++)
                {
                    String s = args[i];

                    String usernameParam = "${username}";
                    String passwordParam = "${password}";

                    // If we find the username or password parameter at this
                    // stage we replace it with an empty string.
                    if(s.indexOf(usernameParam) != -1)
                    {
                        s = s.replace(usernameParam, "");
                        usernameIx = paramNames.size();
                    }
                    else if(s.indexOf(passwordParam) != -1)
                    {
                        s = s.replace(passwordParam, "");
                        passwordIx = paramNames.size();
                    }

                    int equalsIndex = s.indexOf("=");
                    if (equalsIndex > -1)
                    {
                        paramNames.add(s.substring(0, equalsIndex));
                        paramValues.add(s.substring(equalsIndex + 1));
                    }
                    else
                    {
                        if(logger.isInfoEnabled())
                        {
                            logger.info(
                                    "Invalid provisioning request parameter: \""
                                    + s + "\", is replaced by \"" + s + "=\"");
                        }
                        paramNames.add(s);
                        paramValues.add("");
                    }
                }
            }

            while (true)
            {
                HttpUtils.HTTPResponseResult res = null;
                try
                {
                    String savePassword = "net.java.sip.communicator.util" +
                                             ".swing.auth.SAVE_PASSWORD_TICKED";
                    mConfig.setProperty(savePassword, true);

                    res =
                        HttpUtils.postForm(
                            url,
                            PROPERTY_PROVISIONING_USERNAME,
                            PROPERTY_PROVISIONING_PASSWORD,
                            paramNames,
                            paramValues,
                            usernameIx,
                            passwordIx);
                }
                catch(Throwable t)
                {
                    logger.error("Error posting form", t);
                }

                // if there was an error in retrieving stop
                if(res == null)
                {

                    // if canceled, lets check whether provisioning is
                    // mandatory
                    boolean provisioningMandatory = true;

                    String defaultSettingsProp =
                        ProvisioningActivator.getResourceService()
                            .getSettingsString(PROPERTY_PROVISIONING_MANDATORY);
                    if(defaultSettingsProp != null
                        && Boolean.parseBoolean(defaultSettingsProp))
                        provisioningMandatory = true;

                    if (mConfig.getBoolean(PROPERTY_PROVISIONING_MANDATORY,
                                           provisioningMandatory) &&
                                                                 !mStoredConfig)
                    {
                        // Provisioning failed and there is no stored config -
                        // therefore we need to exit.

                        String errorText = ProvisioningActivator.getResourceService().getI18NString("plugin.provisioning.PROV_FAIL_TEXT");
                        String errorTitle = ProvisioningActivator.getResourceService().getI18NString("plugin.provisioning.PROV_FAIL_TITLE");
                        //if(errorWhileProvisioning != null)
                        //    errorMsg = errorWhileProvisioning.getLocalizedMessage();
                        //else
                        //    errorMsg = "";

                        ErrorDialog ed = new ErrorDialog(
                            null,
                            errorTitle,                            //
                            errorText);
                        ed.setModal(true);
                        ed.showDialog();
                        // as shutdown service is not started and other bundles
                        // are scheduled to start, stop all of them

                        for(Bundle b : ProvisioningActivator.bundleContext
                                            .getBundles())
                        {
                            try
                            {
                                // skip our Bundle avoiding stopping us while
                                // starting and NPE in felix
                                if(ProvisioningActivator.bundleContext
                                    .equals(b.getBundleContext()))
                                {
                                    continue;
                                }
                                b.stop();
                            }
                            catch (BundleException ex)
                            {
                                logger.error(
                                    "Failed to being gentle stop " +
                                        b.getLocation(), ex);
                            }
                        }

                        // Finally, reset the stored service provider - it could be
                        // that the user just selected the wrong one:
                        String cdapKey =  "net.java.sip.communicator.plugin" +
                                                    ".cdap.service_provider_id";
                        mConfig.setProperty(cdapKey, 0);

                    }
                    else if (mStoredConfig)
                    {
                        logger.info("Error refreshing config");
                    }

                    // stop processing
                    return null;
                }

                String userPass[] = res.getCredentials();
                if(userPass[0] != null && userPass[1] != null)
                {
                    provUsername = userPass[0];
                    provPassword = userPass[1];
                }

                InputStream in = res.getContent();

                // Chain a ProgressMonitorInputStream to the
                // URLConnection's InputStream
                final ProgressMonitorInputStream pin
                    = new ProgressMonitorInputStream(null, u.toString(), in);

                // Set the maximum value of the ProgressMonitor
                ProgressMonitor pm = pin.getProgressMonitor();
                pm.setMaximum((int)res.getContentLength());

                final File tempFromServer = File.createTempFile("provisioningfromserver",
                    ".properties");


                final BufferedOutputStream bout
                    = new BufferedOutputStream(new FileOutputStream(tempFromServer));

                ByteArrayOutputStream logStream = new ByteArrayOutputStream();

                try
                {
                    int read = -1;
                    byte[] buff = new byte[1024];

                    while((read = pin.read(buff)) != -1)
                    {
                        bout.write(buff, 0, read);
                        logStream.write(buff, 0, read);
                    }

                    pin.close();
                    bout.flush();
                    bout.close();

                    String configFromServer = readFile(tempFromServer);
//                  http://emeapbx.metaswitch.com/pps/communicator/login?Username=PHONENUMBER&Password=PASSWORD&ComputerID=EXAMPLE_ID&Platform=Android&OSVersion=2.3.4&SPID=metaswitchuk/android

                    logger.info("Config from server\n" + configFromServer);

                    Pattern errorPattern = Pattern.compile(".*^Failure=\"(.*)\"$.*" , Pattern.MULTILINE | Pattern.DOTALL);
                    Matcher errorMatcher = errorPattern.matcher(configFromServer);

                    if (errorMatcher.matches())
                    {
                        ErrorDialog ed = new ErrorDialog(null, "Problem logging in", errorMatcher.group(1));
                        ed.setModal(true);
                        ed.showDialog();
                        mConfig.removeProperty(PROPERTY_PROVISIONING_PASSWORD);
                        continue;
                    }

                    String conferenceURL;

                    if (url.contains("www.emeapbx.metaswitch.com"))
                    {
                        conferenceURL = "https://www.emeapbx.metaswitch.com/gadgets/conference/ConferenceWidget.html?custurl=https%3A%2F%2Fwww.emeapbx.metaswitch.com&id=conference0&env=vista";
                    }
                    else if (url.contains("www.pbx.metaswitch.com"))
                    {
                        conferenceURL = "https://www.pbx.metaswitch.com/gadgets/conference/ConferenceWidget.html?custurl=https%3A%2F%2Fwww.pbx.metaswitch.com&id=conference0&env=vista";
                    }
                    else
                    {
                        conferenceURL = "";
                    }

                    // Do a succession of dirty hacks to parse XML config.
                    String configTemplate =
                        "net.java.sip.communicator.impl.analytics.serverurl=https\\://desktopanalytics.metaswitch.com:18008/upload\n" +
                        "net.java.sip.communicator.impl.analytics.schedulerate=15000\n" +
                        "net.java.sip.communicator.impl.commportal.ALLOW_SELF_SIGNED=true\n" +
                        "net.java.sip.communicator.impl.protocol.sip.acc{dn}=acc{dn}\n" +
                        "net.java.sip.communicator.impl.protocol.commportal.acc{dn}=acc{dn}\n" +
                        "net.java.sip.communicator.impl.protocol.commportal.acc{dn}.USER_ID={dn}\n" +
                        "net.java.sip.communicator.impl.protocol.commportal.acc{dn}.ACCOUNT_UID={dn}\n" +
                            "net.java.sip.communicator.impl.protocol.sip.acc{dn}.ACCOUNT_UID=SIP\\:{dn}@\n" +
                            "net.java.sip.communicator.impl.protocol.sip.acc{dn}.DEFAULT_ENCRYPTION=false\n" +
                            "net.java.sip.communicator.impl.protocol.sip.acc{dn}.DEFAULT_SIPZRTP_ATTRIBUTE=false\n" +
                            "net.java.sip.communicator.impl.protocol.sip.acc{dn}.DISPLAY_NAME={name}\n" +
                            "net.java.sip.communicator.impl.protocol.sip.acc{dn}.DTMF_METHOD=AUTO_DTMF\n" +
                            "net.java.sip.communicator.impl.protocol.sip.acc{dn}.FORCE_P2P_MODE=true\n" +
                            "net.java.sip.communicator.impl.protocol.sip.acc{dn}.IS_PRESENCE_ENABLED=false\n" +
                            "net.java.sip.communicator.impl.protocol.sip.acc{dn}.IS_SERVER_OVERRIDDEN=true\n" +
                            "net.java.sip.communicator.impl.protocol.sip.acc{dn}.KEEP_ALIVE_INTERVAL=25\n" +
                            "net.java.sip.communicator.impl.protocol.sip.acc{dn}.KEEP_ALIVE_METHOD=NONE\n" +
                            "net.java.sip.communicator.impl.protocol.sip.acc{dn}.PASSWORD={password}\n" +
                            "net.java.sip.communicator.impl.protocol.sip.acc{dn}.POLLING_PERIOD=30\n" +
                            "net.java.sip.communicator.impl.protocol.sip.acc{dn}.PREFERRED_TRANSPORT={transport}\n" +
                            "net.java.sip.communicator.impl.protocol.sip.acc{dn}.PROTOCOL_NAME=SIP\n" +
                            "net.java.sip.communicator.impl.protocol.sip.acc{dn}.PROXY_ADDRESS={proxyaddress}\n" +
                            "net.java.sip.communicator.impl.protocol.sip.acc{dn}.PROXY_AUTO_CONFIG={autoconfigproxy}\n" +
                            "net.java.sip.communicator.impl.protocol.sip.acc{dn}.PROXY_PORT={proxyport}\n" +
                            "net.java.sip.communicator.impl.protocol.sip.acc{dn}.SAVP_OPTION=0\n" +
                            "net.java.sip.communicator.impl.protocol.sip.acc{dn}.SDES_ENABLED=false\n" +
                            "net.java.sip.communicator.impl.protocol.sip.acc{dn}.SERVER_ADDRESS={sipdomain}\n" +
                            "net.java.sip.communicator.impl.protocol.sip.acc{dn}.SERVER_PORT=5060\n" +
                            "net.java.sip.communicator.impl.protocol.sip.acc{dn}.SUBSCRIPTION_EXPIRATION=3600\n" +
                            "net.java.sip.communicator.impl.protocol.sip.acc{dn}.USER_ID={dn}\n" +
                            "net.java.sip.communicator.impl.protocol.sip.acc{dn}.VOICEMAIL_CHECK_URI=1571@sip.local\n" +
                            "net.java.sip.communicator.impl.protocol.sip.acc{dn}.VOICEMAIL_URI=1571@sip.local\n" +
                            "net.java.sip.communicator.impl.protocol.sip.acc{dn}.XCAP_ENABLE=false\n" +
                            "net.java.sip.communicator.impl.protocol.sip.acc{dn}.XIVO_ENABLE=false\n" +
                            "net.java.sip.communicator.impl.neomedia.codec.EncodingConfiguration.DVI4/16000=0\n" +
                            "net.java.sip.communicator.impl.neomedia.codec.EncodingConfiguration.DVI4/8000=0\n" +
                            "net.java.sip.communicator.impl.neomedia.codec.EncodingConfiguration.G722/8000=0\n" +
                            "net.java.sip.communicator.impl.neomedia.codec.EncodingConfiguration.G723/8000=0\n" +
                            "net.java.sip.communicator.impl.neomedia.codec.EncodingConfiguration.GSM/8000=0\n" +
                            "net.java.sip.communicator.impl.neomedia.codec.EncodingConfiguration.PCMA/8000=13\n" +
                            "net.java.sip.communicator.impl.neomedia.codec.EncodingConfiguration.PCMU/8000=14\n" +
                            "net.java.sip.communicator.impl.neomedia.codec.EncodingConfiguration.SILK/12000=0\n" +
                            "net.java.sip.communicator.impl.neomedia.codec.EncodingConfiguration.SILK/16000=16\n" +
                            "net.java.sip.communicator.impl.neomedia.codec.EncodingConfiguration.SILK/24000=0\n" +
                            "net.java.sip.communicator.impl.neomedia.codec.EncodingConfiguration.SILK/8000=15\n" +
                            "net.java.sip.communicator.impl.neomedia.codec.EncodingConfiguration.iLBC/8000=0\n" +
                            "net.java.sip.communicator.impl.neomedia.codec.EncodingConfiguration.speex/16000=0\n" +
                            "net.java.sip.communicator.impl.neomedia.codec.EncodingConfiguration.speex/32000=0\n" +
                            "net.java.sip.communicator.impl.neomedia.codec.EncodingConfiguration.speex/8000=0\n" +
                            "net.java.sip.communicator.impl.neomedia.codec.EncodingConfiguration.opus/48000=0\n" +
                            "net.java.sip.communicator.impl.neomedia.codec.EncodingConfiguration.telephone-event/8000=12\n" +
                            "net.java.sip.communicator.impl.gui.main.MainFrame.height=600\n" +
                            "net.java.sip.communicator.impl.gui.main.MainFrame.width=281\n" +
                            "net.java.sip.communicator.impl.gui.main.chat.ChatWindow.height=284\n" +
                            "net.java.sip.communicator.impl.gui.main.chat.ChatWindow.width=480\n" +
                            "net.java.sip.communicator.plugin.googleaccountprompter.GoogleAccountPrompterFrame.height=312\n" +
                            "net.java.sip.communicator.plugin.googleaccountprompter.GoogleAccountPrompterFrame.width=334\n" +
                            "net.java.sip.communicator.impl.gui.main.VMAddress={vmaddress}\n" +
                            "net.java.sip.communicator.impl.gui.main.presence.GLOBAL_DISPLAY_NAME={displayname}\n" +
                            "net.java.sip.communicator.impl.neomedia.codec.video.h264.packetization-mode-1.enabled=false\n" +
                            "net.java.sip.communicator.impl.neomedia.dynamicPayloadTypePreferences.120={\"encoding\":\"SILK\",\"clockRate\":\"8000\"}\n" +
                            "net.java.sip.communicator.impl.neomedia.dynamicPayloadTypePreferences.121={\"encoding\":\"SILK\",\"clockRate\":\"16000\"}\n" +
                            "net.java.sip.communicator.impl.neomedia.codec.video.h264.defaultProfile=baseline\n" +
                            "net.java.sip.communicator.impl.protocol.jabber.CALLING_DISABLED=true\n" +
                            "net.java.sip.communicator.impl.protocol.sip.MESSAGING_DISABLED=true\n" +
                            "net.java.sip.communicator.impl.protocol.sip.REGISTRATION_EXPIRATION={regrefresh}\n" +
                            "net.java.sip.communicator.impl.protocol.sip.DESKTOP_STREAMING_DISABLED=true\n" +
                            "net.java.sip.communicator.impl.gui.main.Conference.url=" + conferenceURL + "\n" +
                            "net.java.sip.communicator.service.gui.IS_MULTI_CHAT_WINDOW_ENABLED=false\n" +
                            "net.java.sip.communicator.service.protocol.VIDEO_BRIDGE_DISABLED=true\n" +
                            "net.java.sip.communicator.impl.protocol.RTP_AUDIO_DSCP={audio_dscp}\n" +
                            "net.java.sip.communicator.impl.protocol.RTP_VIDEO_DSCP={video_dscp}\n" +
                            "net.java.sip.communicator.impl.protocol.SIP_DSCP={sip_dscp}\n" +
                            "net.java.sip.communicator.impl.keybinding.global.answer.1=shift ctrl pressed A\n" +
                            "net.java.sip.communicator.impl.keybinding.global.answer_hangup.1=shift ctrl pressed P\n" +
                            "net.java.sip.communicator.impl.keybinding.global.configured=true\n" +
                            "net.java.sip.communicator.impl.keybinding.global.hangup.1=shift ctrl pressed H\n" +
                            "net.java.sip.communicator.impl.keybinding.global.mute.1=shift ctrl pressed M\n" +
                            "net.java.sip.communicator.impl.commportal.URL={commportal_url}\n" +
                            "net.java.sip.communicator.plugin.update.checkforupdatesmenu.daily.ENABLED=true\n" +
                            "net.java.sip.communicator.plugin.update.checkforupdatesmenu.daily.HOUR=2\n" +
                            "net.java.sip.communicator.impl.notifications.eventType0000000000001=ProactiveNotification\n" +
                            "net.java.sip.communicator.impl.notifications.eventType0000000000001.actions.actionType0000000000002=PopupMessageAction\n" +
                            "net.java.sip.communicator.impl.notifications.eventType0000000000001.actions.actionType0000000000002.default=false\n" +
                            "net.java.sip.communicator.impl.notifications.eventType0000000000001.actions.actionType0000000000002.enabled=false\n" +
                            "net.java.sip.communicator.impl.notifications.eventType0000000000003=IncomingCall\n" +
                            "net.java.sip.communicator.impl.notifications.eventType0000000000003.actions.actionType0000000000004=SoundAction\n" +
                            "net.java.sip.communicator.impl.notifications.eventType0000000000003.actions.actionType0000000000004.default=false\n" +
                            "net.java.sip.communicator.impl.notifications.eventType0000000000003.actions.actionType0000000000004.enabled=true\n" +
                            "net.java.sip.communicator.impl.notifications.eventType0000000000003.actions.actionType0000000000004.isSoundNotificationEnabled=true\n" +
                            "net.java.sip.communicator.impl.notifications.eventType0000000000003.actions.actionType0000000000004.isSoundPCSpeakerEnabled=false\n" +
                            "net.java.sip.communicator.impl.notifications.eventType0000000000003.actions.actionType0000000000004.isSoundPlaybackEnabled=true\n" +
                            "net.java.sip.communicator.impl.notifications.eventType0000000000003.actions.actionType0000000000004.loopInterval=2000\n" +
                            "net.java.sip.communicator.impl.notifications.eventType0000000000003.actions.actionType0000000000004.soundFileDescriptor=resources/sounds/incomingCall.wav\n";
                    String ldapConfigTemplate =
                        "net.java.sip.communicator.impl.ldap.directories.dir{ldapacc}={ldapdirname}\n" +
                            "net.java.sip.communicator.impl.ldap.directories.dir{ldapacc}.PASSWORD={ldappwd}\n" +
                            "net.java.sip.communicator.impl.ldap.directories.dir{ldapacc}.auth=SIMPLE\n" +
                            "net.java.sip.communicator.impl.ldap.directories.dir{ldapacc}.baseDN={ldaproot}\n" +
                            "net.java.sip.communicator.impl.ldap.directories.dir{ldapacc}.bindDN={ldapusername}\n" +
                            "net.java.sip.communicator.impl.ldap.directories.dir{ldapacc}.enabled={ldapenabled}\n" +
                            "net.java.sip.communicator.impl.ldap.directories.dir{ldapacc}.encryption=CLEAR\n" +
                            "net.java.sip.communicator.impl.ldap.directories.dir{ldapacc}.hostname={ldapserver}\n" +
                            "net.java.sip.communicator.impl.ldap.directories.dir{ldapacc}.overridehomephone={ldaphomekey} \n" +
                            "net.java.sip.communicator.impl.ldap.directories.dir{ldapacc}.overridemail={ldapemailkey} \n" +
                            "net.java.sip.communicator.impl.ldap.directories.dir{ldapacc}.overridemailsuffix=\n" +
                            "net.java.sip.communicator.impl.ldap.directories.dir{ldapacc}.overridemobilephone={ldapmobilekey} \n" +
                            "net.java.sip.communicator.impl.ldap.directories.dir{ldapacc}.overrideworkphone={ldapworkkey} \n" +
                            "net.java.sip.communicator.impl.ldap.directories.dir{ldapacc}.port={ldapport}\n" +
                            "net.java.sip.communicator.impl.ldap.directories.dir{ldapacc}.scope=SUB\n";

                    //-------------------------------------------------------------
                    // At this stage, there is a temp file called
                    // provisioningfromserver.properties which contains the config
                    // from the server.
                    //
                    // This now needs to be parsed and to have the following extracted
                    //
                    // <data name="display" value="\w+"/>
                    // <data name="username" value="\d+"/>
                    // <data value="\w+" name="password"/>
                    //
                    // The values should then be written into a template.
                    // And that template written to the file "temp"
                    //-------------------------------------------------------------
                    String displayName = extractText("proxies:proxy\\d+:display_name=\"([^\"]+)\"", configFromServer);
                    String SIPUserName = extractText("proxies:proxy\\d+:username=\"([^\"]+)\"", configFromServer);
                    String SIPPassword = extractText("proxies:proxy\\d+:password=\"([^\"]+)\"", configFromServer);
                    String SIPRefresh = extractText("proxies:proxy\\d+:reregister_in_seconds=\"([^\"]+)\"", configFromServer);
                    String SIPDomain = extractText("proxies:proxy\\d+:domain=\"([^\"]+)\"", configFromServer);
                    String overrideProxy = extractText("proxies:proxy\\d+:override_outbound_proxy=\"([^\"]+)\"", configFromServer);
                    String autoConfigureProxy = overrideProxy.equals("1") ? "false" : "true";
                    String SIPProxyAddress = extractText("proxies:proxy\\d+:proxy=\"([^\":]+):\\d+\"", configFromServer);
                    String SIPProxyPort = extractText("proxies:proxy\\d+:proxy=\"[^\":]+:(\\d+)\"", configFromServer);
                    String sipDscp = extractText("system:qos:signaling=\"(\\d+)\"", configFromServer);
                    String audioDscp = extractText("system:qos:audio=\"(\\d+)\"", configFromServer);
                    String videoDscp = extractText("system:qos:video=\"(\\d+)\"", configFromServer);
                    //String voicemailNum = extractText("<data name=\"vmNumber\" value=\"(\\d+)\"/>", configFromServer);

                    String ldapDirectoryName = "Metaswitch LDAP";
                    String ldapAccNumber = "" + Math.abs(ldapDirectoryName.hashCode());
                    String ldapPassword = extractText("feature:ldap:password=\"([^\"]*)\"", configFromServer);
                    String ldapRoot = extractText("feature:ldap:root=\"([^\"]*)\"", configFromServer);
                    String ldapUsername = extractText("feature:ldap:username=\"([^\"]*)\"", configFromServer);
                    String ldapEnabled = extractText("feature:ldap:enable=\"([^\"]*)\"", configFromServer);
                    String ldapEnabledBool = ldapEnabled.equals("1") ? "true" : "false";
                    String ldapServer = extractText("feature:ldap:server=\"([^\":]*):\\d+\"", configFromServer);
                    String ldapPort = extractText("feature:ldap:server=\"[^\":]*:(\\d+)\"", configFromServer);
                    String ldapHomePhoneKey = extractText("feature:ldap:phoneKey=\"([^\"]*)\"", configFromServer);
                    String ldapMobileKey = extractText("feature:ldap:mobileKey=\"([^\"]*)\"", configFromServer);
                    String ldapWorkPhoneKey = extractText("feature:ldap:workKey=\"([^\"]*)\"", configFromServer);
                    String ldapEmailKey = extractText("feature:ldap:emailKey=\"([^\"]*)\"", configFromServer);
                    String vmAddress = extractText("feature:voicemail:http_url=\"([^\"]*)\"", configFromServer).replaceAll("#", "");
                    String commPortalUrl = vmAddress.split("login")[0];

                    // Get the transport type - it could be TCP, UDP or Auto.
                    // If it's auto, default to UDP.
                    String transportType = extractText("proxies:proxy\\d+:transport=\"([^\"]+)\"", configFromServer);
                    transportType = transportType.toUpperCase();
                    if (transportType.equals("AUTO"))
                    {
                        transportType="UDP";
                    }

                    String config = configTemplate.replaceAll("\\{dn\\}", Matcher.quoteReplacement(SIPUserName));
                    config = config.replaceAll("\\{name\\}", Matcher.quoteReplacement(displayName));
                    config = config.replaceAll("\\{password\\}", Matcher.quoteReplacement(SIPPassword));
                    config = config.replaceAll("\\{regrefresh\\}", Matcher.quoteReplacement(SIPRefresh));
                    config = config.replaceAll("\\{sipdomain\\}", Matcher.quoteReplacement(SIPDomain));
                    //config = config.replaceAll("\\{vm\\}", Matcher.quoteReplacement(voicemailNum));
                    config = config.replaceAll("\\{transport\\}", Matcher.quoteReplacement(transportType));
                    config = config.replaceAll("\\{provpassword\\}",  Matcher.quoteReplacement(provPassword));
                    config = config.replaceAll("\\{autoconfigproxy\\}", Matcher.quoteReplacement(autoConfigureProxy));
                    config = config.replaceAll("\\{proxyaddress\\}", Matcher.quoteReplacement(SIPProxyAddress));
                    config = config.replaceAll("\\{proxyport\\}", Matcher.quoteReplacement(SIPProxyPort));
                    config = config.replaceAll("\\{displayname\\}", Matcher.quoteReplacement(displayName));
                    config = config.replaceAll("\\{vmaddress\\}", Matcher.quoteReplacement(vmAddress));
                    config = config.replaceAll("\\{sip_dscp\\}", Matcher.quoteReplacement(sipDscp));
                    config = config.replaceAll("\\{audio_dscp\\}", Matcher.quoteReplacement(audioDscp));
                    config = config.replaceAll("\\{video_dscp\\}", Matcher.quoteReplacement(videoDscp));
                    config = config.replaceAll("\\{commportal_url\\}", commPortalUrl);

                    String ldapConfig = ldapConfigTemplate.replaceAll("\\{ldapacc\\}",  Matcher.quoteReplacement(ldapAccNumber));
                    ldapConfig = ldapConfig.replaceAll("\\{ldapdirname\\}",  Matcher.quoteReplacement(ldapDirectoryName));
                    ldapConfig = ldapConfig.replaceAll("\\{ldapenabled\\}",  Matcher.quoteReplacement(ldapEnabledBool));
                    ldapConfig = ldapConfig.replaceAll("\\{ldappwd\\}",  Matcher.quoteReplacement(ldapPassword));
                    ldapConfig = ldapConfig.replaceAll("\\{ldaproot\\}",  Matcher.quoteReplacement(ldapRoot));
                    ldapConfig = ldapConfig.replaceAll("\\{ldapusername\\}",  Matcher.quoteReplacement(ldapUsername));
                    ldapConfig = ldapConfig.replaceAll("\\{ldapenabled\\}",  Matcher.quoteReplacement(ldapEnabledBool));
                    ldapConfig = ldapConfig.replaceAll("\\{ldapserver\\}",  Matcher.quoteReplacement(ldapServer));
                    ldapConfig = ldapConfig.replaceAll("\\{ldaphomekey\\}",  Matcher.quoteReplacement(ldapHomePhoneKey));
                    ldapConfig = ldapConfig.replaceAll("\\{ldapemailkey\\}",  Matcher.quoteReplacement(ldapEmailKey));
                    ldapConfig = ldapConfig.replaceAll("\\{ldapmobilekey\\}",  Matcher.quoteReplacement(ldapMobileKey));
                    ldapConfig = ldapConfig.replaceAll("\\{ldapworkkey\\}",  Matcher.quoteReplacement(ldapWorkPhoneKey));
                    ldapConfig = ldapConfig.replaceAll("\\{ldapport\\}",  Matcher.quoteReplacement(ldapPort));
                    ldapConfig = ldapConfig.replaceAll("\\{ldapport\\}",  Matcher.quoteReplacement(ldapPort));

                    BufferedWriter writer = null;
                    try
                    {
                        writer = new BufferedWriter( new FileWriter( temp));
                        writer.write( config);

                        if ( ldapEnabled.equals("1"))
                            writer.write( ldapConfig);
                    }
                    catch ( IOException e)
                    {
                        System.out.println("Oops");
                        e.printStackTrace();
                    }
                    finally
                    {
                        try
                        {
                            if ( writer != null)
                                writer.close( );
                        }
                        catch ( IOException e)
                        {
                        }
                    }
                    return temp;
                }
                catch (Exception e)
                {
                    logger.error("Error saving", e);

                    try
                    {
                        pin.close();
                        bout.close();
                    }
                    catch (Exception e1)
                    {
                    }

                    return null;
                }
            }
        }
        catch (Exception e)
        {
            if (logger.isInfoEnabled())
                logger.info("Error retrieving provisioning file!", e);
            tmpFile.delete();
            return null;
        }
    }

    private String extractText(String xiRegex, String xiConfigFromServer)
    {
        Pattern pattern = Pattern.compile(xiRegex);

        Matcher m = pattern.matcher(xiConfigFromServer);

        if (m.find())
        {
            return (m.group(1));
        }

        return "";
    }

    private String readFile(File file) throws IOException
    {
        StringBuilder fileContents = new StringBuilder((int) file.length());
        Scanner scanner = new Scanner(file);
        String lineSeparator = System.getProperty("line.separator");

        try
        {
            while (scanner.hasNextLine())
            {
                fileContents.append(scanner.nextLine() + lineSeparator);
            }
            return fileContents.toString();
        }
        finally
        {
            scanner.close();
        }
    }


    /**
     * Update configuration with properties retrieved from provisioning URL.
     *
     * @param file provisioning file
     */
    private void updateConfiguration(final File file)
    {
        Properties fileProps = new OrderedProperties();
        InputStream in = null;

        try
        {
            in = new BufferedInputStream(new FileInputStream(file));
            fileProps.load(in);

            Iterator<Map.Entry<Object, Object> > it
                = fileProps.entrySet().iterator();

            while(it.hasNext())
            {
                Map.Entry<Object, Object> entry = it.next();
                String key = (String)entry.getKey();
                Object value = entry.getValue();

                // skip empty keys, prevent them going into the configuration
                if(key.trim().length() == 0)
                    continue;

                if(key.equals(PROVISIONING_ALLOW_PREFIX_PROP))
                {
                    String prefixes[] = ((String)value).split("\\|");

                    /* updates allowed prefixes list */
                    for(String s : prefixes)
                    {
                        allowedPrefixes.add(s);
                    }
                    continue;
                }
                else if(key.equals(PROVISIONING_ENFORCE_PREFIX_PROP))
                {
                    checkEnforcePrefix((String)value);
                    continue;
                }

                /* check that properties is allowed */
                if(!isPrefixAllowed(key))
                {
                    continue;
                }

                processProperty(key, value);
            }

            try
            {
                /* save and reload the "new" configuration */
                mConfig.storeConfiguration();
                mConfig.reloadConfiguration();
                mStoredConfig = true;
            }
            catch(Exception e)
            {
                logger.error("Cannot reload configuration");
            }
        }
        catch(IOException e)
        {
            logger.warn("Error during load of provisioning file");
        }
        finally
        {
            try
            {
                in.close();
                file.delete();
            }
            catch(IOException e)
            {
            }
        }
    }

    /**
     * Check if a property name belongs to the allowed prefixes.
     *
     * @param key property key name
     * @return true if key is allowed, false otherwise
     */
    private boolean isPrefixAllowed(String key)
    {
        if(allowedPrefixes.size() > 0)
        {
            for(String s : allowedPrefixes)
            {
                if(key.startsWith(s))
                {
                    return true;
                }
            }

            /* current property prefix is not allowed */
            return false;
        }
        else
        {
            /* no allowed prefixes configured so key is valid by default */
            return true;
        }
    }

    /**
     * Process a new property. If value equals "${null}", it means to remove the
     * property in the configuration service. If the key name end with
     * "PASSWORD", its value is encrypted through credentials storage service,
     * otherwise the property is added/updated in the configuration service.
     *
     * @param key property key name
     * @param value property value
     */
    private void processProperty(String key, Object value)
    {
        if((value instanceof String) && value.equals("${null}"))
        {
            mConfig.removeProperty(key);

            if(logger.isInfoEnabled())
                logger.info(key + "=" + value);
        }
        else if(key.endsWith(".PASSWORD"))
        {
            /* password => credentials storage service */
            ProvisioningActivator.getCredentialsStorageService().storePassword(
                    key.substring(0, key.lastIndexOf(".")),
                    (String)value);

            if(logger.isInfoEnabled())
                logger.info(key +"=<password hidden>");
        }
        else
        {
            mConfig.setProperty(key, value);

            if(logger.isInfoEnabled())
                logger.info(key + "=" + value);
        }
    }

    /**
     * Walk through all properties and make sure all properties keys match
     * a specific set of prefixes defined in configuration.
     *
     * @param enforcePrefix list of enforce prefix.
     */
    private void checkEnforcePrefix(String enforcePrefix)
    {
        String prefixes[] = null;

        if(enforcePrefix == null)
        {
            return;
        }

        /* must escape the | character */
        prefixes = enforcePrefix.split("\\|");

        /* get all properties */
        for (String key : mConfig.getAllPropertyNames())
        {
            boolean isValid = false;

            for(String k : prefixes)
            {
                if(key.startsWith(k))
                {
                    isValid = true;
                    break;
                }
            }

            /* property name does is not in the enforce prefix list
             * so remove it
             */
            if(!isValid)
            {
                mConfig.removeProperty(key);
            }
        }
    }

    /**
     * Stop the provisioning service
     */
    void stop()
    {
        if (mTimer != null)
        {
            mTimer.cancel();
            mTimer = null;
        }
    }
}
