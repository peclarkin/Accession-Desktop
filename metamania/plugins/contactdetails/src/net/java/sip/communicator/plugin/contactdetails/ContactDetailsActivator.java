package net.java.sip.communicator.plugin.contactdetails;

import java.util.*;

import net.java.sip.communicator.service.gui.*;
import net.java.sip.communicator.util.*;

import org.osgi.framework.*;

/**
 * Class used to initialise this bundle and register relevant hooks in the GUI
 * e.g. a contact rght-click-menu-item
 */
public class ContactDetailsActivator implements BundleActivator
{
    private static final Logger sLogger = Logger.getLogger(
        ContactDetailsActivator.class);

    /**
     * The BundleContext in which the ContactDetails plug-in is started.
     */
    private static BundleContext sBundleContext;

    public void start(BundleContext context) throws Exception
    {
        sBundleContext = context;

        // Register the 'View contact' menu item in the contact
        // right-click-menu
        ContactDetailsMenuItem viewContactMenuItem = new ViewContactMenuItem();
        ContactDetailsMenuItem editContactMenuItem = new EditContactMenuItem();

        Hashtable<String, String> containerFilter
                                             = new Hashtable<String, String>();
        containerFilter.put(
                        Container.CONTAINER_ID,
                        Container.CONTAINER_CONTACT_RIGHT_BUTTON_MENU.getID());

        sBundleContext.registerService(PluginComponent.class.getName(),
                                       viewContactMenuItem,
                                       containerFilter);
        sBundleContext.registerService(PluginComponent.class.getName(),
                                       editContactMenuItem,
                                       containerFilter);

        sLogger.info("CONTACT DETAILS... [REGISTERED]");
    }

    public void stop(BundleContext arg0) throws Exception
    {
        sLogger.debug("Plugin stopping");
    }

    /**
     * @return   The BundleContact the plug-in was started in
     */
    protected static BundleContext getBundleContext()
    {
        return sBundleContext;
    }
}