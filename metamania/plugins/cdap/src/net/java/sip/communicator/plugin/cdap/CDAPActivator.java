// CDAPActivator.java
// (C) COPYRIGHT METASWITCH NETWORKS 2012
package net.java.sip.communicator.plugin.cdap;

import net.java.sip.communicator.service.gui.UIService;
import net.java.sip.communicator.util.Logger;

import org.jitsi.service.configuration.ConfigurationService;
import org.jitsi.service.resources.ResourceManagementService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * Entering point for the CDAP bundle, called by the felix framework.
 */
public class CDAPActivator implements BundleActivator
{
  //---------------------------------------------------------------------------
  // Default logger.
  //---------------------------------------------------------------------------
  private static final Logger logger = Logger.getLogger(CDAPActivator.class);

  //---------------------------------------------------------------------------
  // The context in which the one and only instance of this class has started
  // executing.
  //---------------------------------------------------------------------------
  private static BundleContext sBundleContext;

  //---------------------------------------------------------------------------
  // The configuration service.
  //---------------------------------------------------------------------------
  private static ConfigurationService sConfigurationService = null;

  //---------------------------------------------------------------------------
  // The user interface service.
  //---------------------------------------------------------------------------
  private static UIService sUIService;

  //---------------------------------------------------------------------------
  // The resource service.
  //---------------------------------------------------------------------------
  private static ResourceManagementService sResourceService;

  /**
   * Called when this bundle is started.  Start the CDAP service provider
   * selection process and do not return until selection is complete.
   */
  public void start(BundleContext bundleContext)
  {
    logger.logEntry();
    sBundleContext = bundleContext;

    //-------------------------------------------------------------------------
    // Create and start the CDAPService.
    //-------------------------------------------------------------------------
    CDAPService cdapService = new CDAPService();
    cdapService.start();
    logger.logExit();
  }

  /**
   * @return the UIService
   */
  public static UIService getUIService()
  {
    logger.logEntry();

    if (sUIService == null)
    {
      logger.debug("sUIService is null, finding from classname.");
      sUIService = (UIService)getService(UIService.class.getName());
    }

    return sUIService;
  }

  /**
   * @return the ResourceManagementService
   */
  public static ResourceManagementService getResourceService()
  {
    logger.logEntry();

    if (sResourceService == null)
    {
      logger.debug("sResourceService is null, finding from classname.");
      sResourceService = (ResourceManagementService)getService(
                                    ResourceManagementService.class.getName());
    }

    logger.logExit();
    return sResourceService;
  }

  /**
   * @return the ConfigurationService
   */
  public static ConfigurationService getConfigurationService()
  {
    logger.logEntry();

    if (sConfigurationService == null)
    {
      logger.debug("sConfigurationService is null, finding from classname.");
      sConfigurationService = (ConfigurationService)getService(
                                         ConfigurationService.class.getName());
    }

    logger.logExit();
    return sConfigurationService;
  }

  /**
   * Find a service from its class name.
   *
   * @param className - the class name of the desired service.
   * @return the desired service.
   */
  private static Object getService(String className)
  {
    logger.logEntry();
    ServiceReference reference = sBundleContext.getServiceReference(className);
    Object service = sBundleContext.getService(reference);
    logger.logExit();
    return service;
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

  public static BundleContext getBundleContext()
  {
    // PPON Auto-generated method stub
    return sBundleContext;
  }
}
