package net.java.sip.communicator.impl.analytics;

import net.java.sip.communicator.service.analytics.*;
import net.java.sip.communicator.service.protocol.event.*;

/**
 * An implementation of the call change listener to listen for changes to
 * the call participants
 */
class LocalCallChangeListener implements CallChangeListener
{
    private AnalyticsService mAnalyticsService;
    private String mCallType;

    /**
     * Constructor
     *
     * @param analyticsService The analytics service to send events to
     * @param incoming If true then we are created for an incoming call
     */
    LocalCallChangeListener(AnalyticsService analyticsService, boolean incoming)
    {
        mAnalyticsService = analyticsService;
        mCallType = incoming ? "Incoming" : " Outgoing";
    }

    public void callPeerAdded(CallPeerEvent evt)
    {
        mAnalyticsService.onEvent("Call Peer Added to " + mCallType);
    }

    public void callPeerRemoved(CallPeerEvent evt)
    {
        mAnalyticsService.onEvent("Call Peer Removed from " + mCallType);
    }

    public void callStateChanged(CallChangeEvent evt)
    {
        // Nothing required
    }
}