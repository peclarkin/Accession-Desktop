package net.java.sip.communicator.impl.analytics;

import java.util.*;

import net.java.sip.communicator.service.analytics.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.service.protocol.event.*;
import net.java.sip.communicator.util.*;

import org.jitsi.service.configuration.*;
import org.osgi.framework.*;

/**
 * Activator for the Analytics service
 *
 * @implements BundleActivator so that we can act as the activator for the service
 * @implements ServiceListener so that we can register for call states
 * @implements CallListener so that we can register for call states
 */
public class AnalyticsActivator implements BundleActivator,
                                           ServiceListener,
                                           CallListener
{
    private static ConfigurationService sConfigService;
    private AnalyticsServiceImpl mAnalyticsService;

    private BundleContext mContext;

    /**
     * A map between a call and the listener that we have registered for it
     */
    private final Map<Call, LocalCallChangeListener> mCallListenerMap =
                                  new HashMap<Call, LocalCallChangeListener>(2);

    /**
     * The place in the config where the analytics url is stored
     */
    private static String CONFIG_SERVER_URL =
                           "net.java.sip.communicator.impl.analytics.serverurl";

    public void start(BundleContext context) throws Exception
    {
        if (sConfigService == null)
        {
            sConfigService =
                   ServiceUtils.getService(context, ConfigurationService.class);
        }

        mAnalyticsService = new AnalyticsServiceImpl();
        context.registerService(AnalyticsService.class.getName(),
                                mAnalyticsService,
                                null);

        mAnalyticsService.onEvent("Application started");
        mContext = context;

        // Listen for certain service events
        String filter = '(' + Constants.OBJECTCLASS + '=' +
                                  ProtocolProviderService.class.getName() + ')';
        context.addServiceListener(this, filter);
    }

    public void stop(BundleContext context) throws Exception
    {
        mAnalyticsService.stop();
        context.removeServiceListener(this);
    }

    public static ConfigurationService getConfigService()
    {
        return sConfigService;
    }

    static String getAnalyticsUrl()
    {
        return sConfigService.getString(CONFIG_SERVER_URL);
    }

    public void serviceChanged(ServiceEvent event)
    {
        ProtocolProviderService service = (ProtocolProviderService)
                               mContext.getService(event.getServiceReference());

        if (event.getType() == ServiceEvent.REGISTERED)
        {
            handleProviderAdded(service);
        }
        else if (event.getType() == ServiceEvent.UNREGISTERING)
        {
            handleProviderRemoved(service);
        }
    }

    /**
     * Used to attach the Call History Service to existing or
     * just registered protocol provider. Checks if the provider has
     * implementation of OperationSetBasicTelephony
     *
     * @param provider ProtocolProviderService
     */
    private void handleProviderAdded(ProtocolProviderService provider)
    {
        // Check whether the provider has a basic telephony operation set
        OperationSetBasicTelephony<?> opSetTelephony =
                     provider.getOperationSet(OperationSetBasicTelephony.class);

        if (opSetTelephony != null)
        {
            opSetTelephony.addCallListener(this);
        }
    }

    /**
     * Removes the specified provider from the list of currently known providers
     * and ignores all the calls made by it
     *
     * @param provider the ProtocolProviderService that has been unregistered.
     */
    private void handleProviderRemoved(ProtocolProviderService provider)
    {
        OperationSetBasicTelephony<?> opSetTelephony =
                     provider.getOperationSet(OperationSetBasicTelephony.class);

        if (opSetTelephony != null)
        {
            opSetTelephony.removeCallListener(this);
        }
    }

    public void incomingCallReceived(CallEvent event)
    {
        mAnalyticsService.onEvent("Incoming Call Received");
        addCallListener(event, true);
    }

    public void outgoingCallCreated(CallEvent event)
    {
        mAnalyticsService.onEvent("Outgoing Call Created");
        addCallListener(event, false);
    }

    private void addCallListener(CallEvent event, boolean isIncoming)
    {
        Call call = event.getSourceCall();
        LocalCallChangeListener listener =
                     new LocalCallChangeListener(mAnalyticsService, isIncoming);

        mCallListenerMap.put(call, listener);
        call.addCallChangeListener(listener);
    }

    public void callEnded(CallEvent event)
    {
        mAnalyticsService.onEvent("Call Ended");
        Call call = event.getSourceCall();
        call.removeCallChangeListener(mCallListenerMap.remove(call));
    }
}
