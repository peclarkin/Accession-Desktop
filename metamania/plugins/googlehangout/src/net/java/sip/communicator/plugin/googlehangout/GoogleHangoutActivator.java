// GoogleHangoutActivator.java
// (C) COPYRIGHT METASWITCH NETWORKS 2012
package net.java.sip.communicator.plugin.googlehangout;

import java.util.Hashtable;

import net.java.sip.communicator.service.gui.Container;
import net.java.sip.communicator.service.gui.PluginComponent;
import net.java.sip.communicator.util.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Entering point for the Google Hangout bundle, called by the felix framework.
 */
public class GoogleHangoutActivator implements BundleActivator
{
  //---------------------------------------------------------------------------
  // Default logger.
  //---------------------------------------------------------------------------
  private static final Logger logger = Logger.getLogger(
                                                 GoogleHangoutActivator.class);

  //---------------------------------------------------------------------------
  // The context in which the one and only instance of this class has started
  // executing.
  //---------------------------------------------------------------------------
  private static BundleContext sBundleContext;

  /**
   * Called when this bundle is started.  Create our menu item and register it
   * as a plug-in component in the contact right mouse click menu.
   */
  public void start(BundleContext bundleContext) throws Exception
  {
    logger.logEntry();
    sBundleContext = bundleContext;

    //-------------------------------------------------------------------------
    // Create a filter specifying that the menu item should appear in the
    // contact right mouse click menu.
    //-------------------------------------------------------------------------
    Hashtable<String, String> filter = new Hashtable<String, String>();
    filter.put(Container.CONTAINER_ID,
               Container.CONTAINER_TOOLS_MENU.getID());

    //-------------------------------------------------------------------------
    // Create the menu item and register as a GUI plugin component.
    //-------------------------------------------------------------------------
    GoogleHangoutMenuItem googleHangoutPlugin = new GoogleHangoutMenuItem();
    sBundleContext.registerService(PluginComponent.class.getName(),
                                   googleHangoutPlugin,
                                   filter);
    logger.logExit();
  }

  /**
   * @return sBundleContext
   */
  protected static BundleContext getBundleContext()
  {
    logger.logEntry();
    logger.logExit();
    return sBundleContext;
  }

  /**
   * Stop this bundle.
   *
   * @param bundleContext BundleContext
   */
  public void stop(BundleContext bundleContext)
  {
    logger.logEntry();
    sBundleContext = null;
    logger.logExit();
  }
}
