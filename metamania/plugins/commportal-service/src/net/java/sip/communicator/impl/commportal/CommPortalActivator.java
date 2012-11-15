package net.java.sip.communicator.impl.commportal;

import net.java.sip.communicator.service.commportal.ClassOfServiceService;
import net.java.sip.communicator.service.commportal.CommPortalService;
import net.java.sip.communicator.service.credentialsstorage.CredentialsStorageService;
import net.java.sip.communicator.service.netaddr.NetworkAddressManagerService;
import net.java.sip.communicator.util.Logger;
import net.java.sip.communicator.util.ServiceUtils;

import org.jitsi.service.configuration.ConfigurationService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Activator for the CommPortal plugin
 */
public class CommPortalActivator implements BundleActivator
{
    private static final Logger sLog = Logger.getLogger(CommPortalActivator.class);

    // The services that we require to work
    private static ConfigurationService sConfigService;
    private static CredentialsStorageService sCredService;
    private static NetworkAddressManagerService sNetworkService;

    private BundleContext mContext;
    private CommPortalServiceImpl mCommPortalService;

    public void start(BundleContext context)
    {
        sLog.info("Starting");
        mContext = context;

        if (sConfigService == null)
        {
            sLog.debug("Getting config service");
            sConfigService =
                  ServiceUtils.getService(mContext, ConfigurationService.class);
        }

        if (sCredService == null)
        {
            sLog.debug("Getting cred service");
            sCredService =
             ServiceUtils.getService(mContext, CredentialsStorageService.class);
        }

        if (sNetworkService == null)
        {
            sLog.debug("Getting network service");
            sNetworkService = ServiceUtils.getService(mContext,
                                            NetworkAddressManagerService.class);
        }

        // Register the commportal service
        CommPortalThreadFactory factory = new CommPortalThreadFactory();
        mCommPortalService = new CommPortalServiceImpl(factory);
        context.registerService(CommPortalService.class.getName(),
                                mCommPortalService,
                                null);

        // Register the CoS service
        ClassOfServiceImpl cosService = new ClassOfServiceImpl(mCommPortalService);
        context.registerService(ClassOfServiceService.class.getName(),
                                cosService,
                                null);
    }

    public void stop(BundleContext context)
    {
        sLog.info("Stopping");
        if (mCommPortalService != null)
        {
            mCommPortalService.stop();
        }
    }

    /**
     * @return the <tt>ConfigurationService</tt> obtained from the bundle
     * context
     */
    static ConfigurationService getConfigService()
    {
        return sConfigService;
    }

    /**
     * @return the <tt>CredentialsService</tt> obtained from the bundle
     * context
     */
    static CredentialsStorageService getCredentialsService()
    {
        return sCredService;
    }

    /**
     * @return the <tt>NetworkAddressManagerService</tt> obtained from the
     * bundle context
     */
    static NetworkAddressManagerService getNetworkService()
    {
        return sNetworkService;
    }
}
