// GoogleHangoutMenuItem.java
// (C) COPYRIGHT METASWITCH NETWORKS 2012
package net.java.sip.communicator.plugin.googlehangout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

import net.java.sip.communicator.service.browserlauncher.BrowserLauncherService;
import net.java.sip.communicator.service.gui.AbstractPluginComponent;
import net.java.sip.communicator.service.gui.Container;
import net.java.sip.communicator.util.Logger;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * A menu item that launches a Google+ Hangout session in the OS default
 * browser, and that appears in the contact right click menu.
 *
 * If the user is not logged into Google upon launch, a login / registration
 * screen will be shown instead (behaviour defined by Google+).
 */
public class GoogleHangoutMenuItem extends AbstractPluginComponent
                                   implements ActionListener
{
  //---------------------------------------------------------------------------
  // Default logger.
  //---------------------------------------------------------------------------
  private static final Logger logger = Logger.getLogger(
                                                  GoogleHangoutMenuItem.class);

  //---------------------------------------------------------------------------
  // The Hangout menu item.
  //---------------------------------------------------------------------------
  private JMenuItem mMenuItem;

  //---------------------------------------------------------------------------
  // URL for a new Google+ Hangout.
  //---------------------------------------------------------------------------
  private static final String HANGOUT_URL =
                                          "https://plus.google.com/hangouts/_";

  /**
   * Constructor
   */
  public GoogleHangoutMenuItem()
  {
    //-------------------------------------------------------------------------
    // Create a plugin component to be added to the contact right click menu.
    //-------------------------------------------------------------------------
    super(Container.CONTAINER_TOOLS_MENU);
    logger.logEntry();
    logger.logExit();
  }

  /**
   * Listens for events triggered by user clicks on the menu item. Opens a
   * browser window to a new Google+ Hangout.
   */
  @SuppressWarnings("unused")
  public void actionPerformed(ActionEvent event)
  {
  //-------------------------------------------------------------------------
    // Get a reference to the browser launcher service.
    //-------------------------------------------------------------------------
    logger.logEntry();
    BundleContext bundleContext = GoogleHangoutActivator.getBundleContext();
    ServiceReference serviceReference = bundleContext.getServiceReference(
                                       BrowserLauncherService.class.getName());

    if (serviceReference != null)
    {
      //-----------------------------------------------------------------------
      // Get the service itself and launch browser to the Hangout URL.
      //-----------------------------------------------------------------------
      logger.debug("Starting Hangout.");
      BrowserLauncherService service =
           (BrowserLauncherService) bundleContext.getService(serviceReference);
      service.openURL(HANGOUT_URL);
    }
    else
    {
      logger.error("Could not find BrowserLauncherService to launch Hangout.");
    }

    logger.logExit();
  }

  /**
   * Creates a menu item if there isn't one, and returns it.
   * @return mMenuItem
   */
  public Object getComponent()
  {
    logger.logEntry();

    if (mMenuItem == null)
    {
      logger.debug("Create new menu item.");
      mMenuItem = new JMenuItem(getName());
      mMenuItem.addActionListener(this);
    }

    logger.logExit();
    return mMenuItem;
  }

  /**
   * @return "Start Google+ Hangout"
   */
  public String getName()
  {
    logger.logEntry();
    logger.logExit();
    return "Start Google+ Hangout";
  }
}
