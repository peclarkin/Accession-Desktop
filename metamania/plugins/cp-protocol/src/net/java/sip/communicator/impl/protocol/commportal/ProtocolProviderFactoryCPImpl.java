package net.java.sip.communicator.impl.protocol.commportal;

import java.util.*;

import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.util.*;

import org.osgi.framework.*;

/**
 * Protocol provider factory implementation for the CommPortal Protocol Provider
 */
public class ProtocolProviderFactoryCPImpl extends ProtocolProviderFactory
{
    private static final Logger sLog =
                          Logger.getLogger(ProtocolProviderFactoryCPImpl.class);

    /**
     * The name of this protocol
     */
    static final String PROTOCOL_NAME = "CommPortal";

    protected ProtocolProviderFactoryCPImpl(BundleContext bundleContext)
    {
        super(bundleContext, PROTOCOL_NAME);
    }

    @Override
    public AccountID installAccount(String userID,
                                    Map<String, String> accountProperties)
                                                throws IllegalArgumentException,
                                                       IllegalStateException
    {
        accountProperties.put(USER_ID, userID);

        if (!accountProperties.containsKey(PROTOCOL))
        {
            accountProperties.put(PROTOCOL, ProtocolNames.SIP);
        }

        AccountID accountID = createAccountID(userID, accountProperties);

        // Make sure we haven't seen this account id before.
        if (registeredAccounts.containsKey(accountID))
        {
            throw new IllegalStateException(
                "An account for id " + userID + " was already installed!");
        }

        // First store the account and only then load it as the load generates
        // an osgi event, the osgi event triggers (through the UI) a call to
        // the register() method and it needs to access the configuration service
        // and check for a password.
        storeAccount(accountID, false);

        try
        {
            accountID = loadAccount(accountProperties);
        }
        catch(RuntimeException e)
        {
            // Sometimes loading the account fails due to bad initialisation.
            // In which case we need to make sure that the account is not added.
            sLog.error("Error loading account", e);
            removeStoredAccount(accountID);

            throw e;
        }

        return accountID;
    }

    @Override
    public void modifyAccount(ProtocolProviderService protocolProvider,
                              Map<String, String> accountProperties)
    {
        // Nothing required
    }

    @Override
    protected AccountID createAccountID(String userID,
                                        Map<String, String> accountProperties)
    {
        String serverAddress = accountProperties.get(SERVER_ADDRESS);

        return new CommPortalAccountID(userID, accountProperties, serverAddress);
    }

    @Override
    protected ProtocolProviderService createService(String userID,
                                                    AccountID accountID)
    {
        ProtocolProviderServiceCPImpl service =
                                            new ProtocolProviderServiceCPImpl();

        service.initialize(accountID);
        storeAccount(accountID);

        return service;
    }

}
