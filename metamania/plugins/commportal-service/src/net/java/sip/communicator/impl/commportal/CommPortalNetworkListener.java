package net.java.sip.communicator.impl.commportal;

import java.util.HashSet;

import net.java.sip.communicator.service.commportal.CPNetworkError;
import net.java.sip.communicator.service.netaddr.NetworkAddressManagerService;
import net.java.sip.communicator.service.netaddr.event.ChangeEvent;
import net.java.sip.communicator.service.netaddr.event.NetworkConfigurationChangeListener;
import net.java.sip.communicator.util.Logger;

/**
 * A class for keeping track of the state of the network.  In particular, it
 * allows us to ask the question, "Are we connected"?
 */
class CommPortalNetworkListener implements NetworkConfigurationChangeListener
{
    private static final Logger sLog = Logger.getLogger(CommPortalNetworkListener.class);

    /**
     * A set of all our connected interfaces
     */
    private final HashSet<String> mConnectedInterfaces = new HashSet<String>();

    /**
     * The network management service
     */
    private final NetworkAddressManagerService mNetworkService;

    /**
     * The CommPortal service for which we were created
     */
    private final CommPortalServiceImpl mCommPortalService;

    public CommPortalNetworkListener(CommPortalServiceImpl commPortalService)
    {
        mCommPortalService = commPortalService;
        mNetworkService = CommPortalActivator.getNetworkService();

        if (mNetworkService != null)
        {
            mNetworkService.addNetworkConfigurationChangeListener(this);
        }
    }

    /**
     * Perform the clean up required to stop this object
     */
    public void stop()
    {
        if (mNetworkService != null)
        {
            mNetworkService.removeNetworkConfigurationChangeListener(this);
        }
    }

    public void configurationChanged(ChangeEvent event)
    {
        int type = event.getType();
        sLog.debug("Configuration has changed, type " + type);

        if (ChangeEvent.IFACE_UP == type)
        {
            if (mConnectedInterfaces.size() == 0)
            {
                // We were reporting that we were not connected, but now we are.
                // Tell the CommPortal service so that it can retry immediately.
                mCommPortalService.onNetworkRestored();
            }

            mConnectedInterfaces.add((String)event.getSource());
        }
        else if (ChangeEvent.IFACE_DOWN == type)
        {
            mConnectedInterfaces.remove(event.getSource());
        }

        if (!isConnected())
        {
            // No longer connected, tell the CommPortal service
            mCommPortalService.onNetworkError(CPNetworkError.NETWORK_UNAVAILABLE);
        }
    }

    /**
     * @return true if we have a network connection
     */
    public boolean isConnected()
    {
        return !mConnectedInterfaces.isEmpty();
    }
}
