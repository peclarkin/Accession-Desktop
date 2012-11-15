/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jitsi.plugin.example;

import net.java.sip.communicator.service.gui.*;
import net.java.sip.communicator.service.resources.*;
import org.jitsi.service.resources.*;
import org.osgi.framework.*;

import java.util.*;

/**
 * Registers an example menu in file menu.
 * @author Damian Minkov
 */
public class ExampleActivator
    implements BundleActivator
{
    /**
     * osgi context.
     */
    private static BundleContext bundleContext;

    /**
     * The resources.
     */
    private static ResourceManagementService resourcesService;

    /**
     * Starts the bundle registering a menu.
     * @param bundleContext
     * @throws Exception
     */
    public void start(BundleContext bundleContext)
        throws
        Exception
    {
        this.bundleContext = bundleContext;

        // Registers plugin component in the main file menu.
        Hashtable<String, String> exampleMenuFilter
            = new Hashtable<String, String>();
        exampleMenuFilter.put( Container.CONTAINER_ID,
                            Container.CONTAINER_HELP_MENU.getID());

        bundleContext.registerService(  PluginComponent.class.getName(),
                                        new ExamplePluginComponent(
                                            Container.CONTAINER_HELP_MENU),
                                        exampleMenuFilter);
    }

    /**
     * Stops the bundle. Do nothing as it is example.
     * @param bundleContext
     * @throws Exception
     */
    public void stop(BundleContext bundleContext)
        throws
        Exception
    {
    }

    /**
     * Returns the <tt>ResourceManagementService</tt>.
     *
     * @return the <tt>ResourceManagementService</tt>.
     */
    public static ResourceManagementService getResources()
    {
        if (resourcesService == null)
            resourcesService
                = ResourceManagementServiceUtils.getService(bundleContext);
        return resourcesService;
    }
}
