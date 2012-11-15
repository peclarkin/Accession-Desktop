package net.java.sip.communicator.impl.protocol.commportal;

import java.util.*;

import net.java.sip.communicator.service.commportal.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.util.*;

import org.jitsi.service.resources.*;
import org.osgi.framework.*;

/**
 * Activator for the CommPortal Protocol
 */
public class CommPortalProtocolActivator implements BundleActivator
{
    private static CommPortalService sCommPortalService;
    private static ResourceManagementService sResourceService;

    private static BundleContext sBundleContext;
    private ServiceRegistration mServiceRegistration;

    public void start(BundleContext bundleContext)
    {
        sBundleContext = bundleContext;

        ProtocolProviderFactoryCPImpl providerFactory =
                       new ProtocolProviderFactoryCPImpl(bundleContext);

        // Register the account:
        Dictionary<String, String> properties = new Hashtable<String, String>();
        properties.put(ProtocolProviderFactory.PROTOCOL,
                       ProtocolProviderFactoryCPImpl.PROTOCOL_NAME);

        mServiceRegistration = bundleContext.registerService(
                                        ProtocolProviderFactory.class.getName(),
                                        providerFactory,
                                        properties);
    }

    public void stop(BundleContext bundleContext)
    {
        mServiceRegistration.unregister();
    }

    /**
     * Return the commportal service impl
     *
     * @return the CommPortal Service
     */
    public static CommPortalService getCommPortalService()
    {
        if (sCommPortalService == null)
        {
            sCommPortalService = ServiceUtils.getService(sBundleContext,
                                                       CommPortalService.class);
        }

        return sCommPortalService;
    }

    /**
     * Return the resource management service impl
     *
     * @return the resources service
     */
    public static ResourceManagementService getResourceService()
    {
        if (sResourceService == null)
        {
            sResourceService = ServiceUtils.getService(sBundleContext,
                                               ResourceManagementService.class);
        }

        return sResourceService;
    }
}
