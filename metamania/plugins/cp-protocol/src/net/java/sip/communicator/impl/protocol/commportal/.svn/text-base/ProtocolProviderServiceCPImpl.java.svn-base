package net.java.sip.communicator.impl.protocol.commportal;

import net.java.sip.communicator.impl.protocol.commportal.contacts.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.util.*;

/**
 * We treat CommPortal as a Protocol Provider as it allows us to do contacts
 * easily
 */
public class ProtocolProviderServiceCPImpl
    extends AbstractProtocolProviderService
{
    private static final Logger sLog =
                          Logger.getLogger(ProtocolProviderServiceCPImpl.class);

    /** Object to handle getting and sending contact from or to CommPortal */
    private CommPortalContactDataHandler mDataHandler;

    /** The group to which all contacts should be added */
    private final CommPortalContactGroup mGroup;

    /** Icon for this protocol */
    private final ProtocolIcon mProtocolIcon = new CommPortalProtocolIcon();

    /** The ID of this account */
    private AccountID mAccountID;

    ProtocolProviderServiceCPImpl()
    {
        mDataHandler = new CommPortalContactDataHandler(this);
        mGroup = new CommPortalContactGroup(this, mDataHandler);
    }

    public void shutdown()
    {
        sLog.debug("Shutting down");

        if (mDataHandler != null)
        {
            mDataHandler.destroy();
            mDataHandler = null;
        }
    }

    public void initialize(AccountID account)
    {
        mAccountID = account;

        // Finally, store the CommPortal credentials:
        CommPortalProtocolActivator.getCommPortalService().
                            setCredentials(mAccountID.getUserID(), null, false);

        mDataHandler.init();

        // Add an operation set for presence
        addSupportedOperationSet(OperationSetPersistentPresence.class,
                                 mDataHandler);

        // Add an operation set for contact details
        addSupportedOperationSet(OperationSetServerStoredContactInfo.class,
                                 mDataHandler);

        // And finally, an operation set for editing the contact details.
        addSupportedOperationSet(
                             OperationSetServerStoredUpdatableContactInfo.class,
                             mDataHandler);
    }

    public void register(SecurityAuthority authority)
        throws OperationFailedException
    {
        // Nothing required
    }

    public void unregister() throws OperationFailedException
    {
        // Nothing required
    }

    public RegistrationState getRegistrationState()
    {
        return RegistrationState.REGISTERED;
    }

    public String getProtocolName()
    {
        return ProtocolProviderFactoryCPImpl.PROTOCOL_NAME;
    }

    public ProtocolIcon getProtocolIcon()
    {
        return mProtocolIcon;
    }

    public AccountID getAccountID()
    {
        return mAccountID;
    }

    public boolean isSignalingTransportSecure()
    {
        // We don't do signalling so it doesn't matter
        return false;
    }

    public TransportProtocol getTransportProtocol()
    {
        // Again, we don't do signalling
        return TransportProtocol.UNKNOWN;
    }

    public CommPortalContactGroup getGroup()
    {
        return mGroup;
    }

    @Override
    public boolean supportsStatus()
    {
        // Status is done elsewhere - thus return false
        return false;
    }
}
