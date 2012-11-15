package net.java.sip.communicator.plugin.nativebrowser;

import java.util.Hashtable;

import net.java.sip.communicator.service.gui.Container;
import net.java.sip.communicator.service.gui.PluginComponent;
import net.java.sip.communicator.util.Logger;

import org.jitsi.service.configuration.ConfigurationService;
import org.jitsi.util.OSUtils;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import chrriis.dj.nativeswing.NativeSwing;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.core.SWTNativeInterface;

public class NativeBrowserActivator implements BundleActivator
{
    /**
     * Name of the property where the conference URL is stored
     */
    private static final String CONFIG_CONFERENCE_URL =
                       "net.java.sip.communicator.impl.gui.main.Conference.url";

    /**
     * Name of property where the commportal URL is stored
     */
    private static final String CONFIG_COMMPORTAL_URL =
                            "net.java.sip.communicator.impl.gui.main.VMAddress";

    private static final Logger logger =
                                 Logger.getLogger(NativeBrowserActivator.class);

    public void start(BundleContext bundleContext) throws Exception
    {
        logger.logEntry();

        if (OSUtils.IS_WINDOWS)
        {
            // Prepare the native swing component - only on windows as it
            // doesn't work on Mac
            logger.debug("Preparing native swing");
            NativeSwing.initialize();
            NativeInterface.open();
            SWTNativeInterface.open();
        }

        ServiceReference configRef = bundleContext.
                      getServiceReference(ConfigurationService.class.getName());
        ConfigurationService cfg =
                      (ConfigurationService)bundleContext.getService(configRef);

        // Set-up the conference button
        String confUrl = cfg.getString(CONFIG_CONFERENCE_URL);

        if (confUrl != null && confUrl.length() > 0)
        {
            String imageResource = "service.gui.buttons.CONFERENCE_WINDOW_BUTTON";
            String toolTipResource = "plugin.nativebrowser.tooltip.CONFERENCE";
            addComponent(bundleContext, confUrl, imageResource, toolTipResource);
        }

        // And set-up the commportal button
        String vmUrl = cfg.getString(CONFIG_COMMPORTAL_URL);

        if (vmUrl != null && vmUrl.length() > 0)
        {
            String imageResource = "service.gui.buttons.VM_CLICKTHROUGH_BUTTON";
            String toolTipResource = "plugin.nativebrowser.tooltip.VOICEMAILS";
            addComponent(bundleContext, vmUrl, imageResource, toolTipResource);
        }

        logger.logExit();
    }

    /**
     * Create and register a native browser button component
     *
     * @param bundleContext   context
     * @param url             url of the button
     * @param imageResource   image of the button
     * @param toolTipResource text to display in a tool tip over the button
     */
    private void addComponent(BundleContext bundleContext,
                              String url,
                              String imageResource,
                              String toolTipResource)
    {
        logger.logEntry();
        NativeBrowserButtonComponent component =
             new NativeBrowserButtonComponent(Container.CONTAINER_MAIN_TOOL_BAR,
                                              bundleContext,
                                              url,
                                              imageResource,
                                              toolTipResource);

        Hashtable<String, String> containerFilter =
                                                new Hashtable<String, String>();
        containerFilter.put(Container.CONTAINER_ID,
                            Container.CONTAINER_MAIN_TOOL_BAR.getID());

        bundleContext.registerService(PluginComponent.class.getName(),
                                      component,
                                      containerFilter);
        logger.logExit();
    }

    public void stop(BundleContext arg0) throws Exception
    {
        logger.logEntry();

        if (OSUtils.IS_WINDOWS)
        {
            if (NativeInterface.isOpen())
            {
                NativeInterface.close();
            }

            if (SWTNativeInterface.isOpen())
            {
                SWTNativeInterface.close();
            }
        }

        logger.logExit();
    }

}
