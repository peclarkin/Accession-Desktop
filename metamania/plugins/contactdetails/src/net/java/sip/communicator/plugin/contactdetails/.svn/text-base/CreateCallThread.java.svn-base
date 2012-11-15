// TODO finish
package net.java.sip.communicator.plugin.contactdetails;

import java.util.*;

import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.util.*;
import net.java.sip.communicator.util.swing.*;

import org.jitsi.service.configuration.*;
import org.osgi.framework.*;

/**
 * Creates a new (audio-only or video) <tt>Call</tt> to a contact specified
 * as a <tt>Contact</tt> instance or a <tt>String</tt> contact
 * address/identifier.
 */
public class CreateCallThread extends Thread
{
    private final ProtocolProviderService fProtocolProvider;
    private final String fAddress;
    private final boolean fVideo;

    private CreateCallThread(
        ProtocolProviderService protocolProvider,
        String address,
        boolean video)
    {
        fProtocolProvider = protocolProvider;
        fAddress = address;
        fVideo = video;
    }

    @Override
    public void run()
    {
        String stringContact = this.fAddress;

        ConfigurationService configService = UtilActivator.getConfigurationService();
        boolean normalize = configService.getBoolean("impl.gui.NORMALIZE_PHONE_NUMBER", true);

        if (normalize)
        {
             stringContact = PhoneNumberI18nService.normalize(stringContact);
        }

        try
        {
            if (fVideo)
            {
                OperationSetVideoTelephony telephony = fProtocolProvider.getOperationSet(
                    OperationSetVideoTelephony.class);

                if (telephony != null && stringContact != null)
                {
                    telephony.createVideoCall(stringContact);
                }
            }
            else
            {
                OperationSetBasicTelephony<?> telephony = fProtocolProvider.getOperationSet(
                    OperationSetBasicTelephony.class);

                if (telephony != null && stringContact != null)
                {
                    telephony.createCall(stringContact);
                }
            }
        }
        catch (Throwable thr)
        {
            if (thr instanceof ThreadDeath)
            {
                throw (ThreadDeath) thr;
            }

            ErrorDialog dialog = new ErrorDialog(
                                      null,
                                      Resources.getString("service.gui.ERROR"),
                                      thr.getMessage(),
                                      thr);
            dialog.showDialog();
        }
    }

    protected static void createCall(String callString, boolean video)
    {
        callString = callString.trim();

        // Removes special characters from phone numbers.
        ConfigurationService configService = UtilActivator.getConfigurationService();
        boolean normalize = configService.getBoolean("impl.gui.NORMALIZE_PHONE_NUMBER", true);

        if (normalize)
        {
            callString = PhoneNumberI18nService.normalize(callString);
        }

        Class<? extends OperationSet> opSet =
                                     video ? OperationSetVideoTelephony.class :
                                             OperationSetBasicTelephony.class;
        List<ProtocolProviderService> telephonyProviders = getRegisteredProviders(opSet);

        if (telephonyProviders.size() > 0)
        {
            new CreateCallThread(telephonyProviders.get(0), callString, video).start();
        }
        else
        {
            ErrorDialog dialog = new ErrorDialog(
               null,
               Resources.getString("service.gui.WARNING"),
               Resources.getString("service.gui.NO_ONLINE_TELEPHONY_ACCOUNT"));
            dialog.showDialog();
        }
    }

    private static List<ProtocolProviderService> getRegisteredProviders(
        Class<? extends OperationSet> opSetClass)
    {
        List<ProtocolProviderService> opSetProviders
            = new LinkedList<ProtocolProviderService>();

        for (ProtocolProviderFactory providerFactory : getProtocolProviderFactories().values())
        {
            ServiceReference serRef;
            ProtocolProviderService protocolProvider;

            for (AccountID accountID : providerFactory.getRegisteredAccounts())
            {
                serRef = providerFactory.getProviderForAccount(accountID);

                protocolProvider
                    = (ProtocolProviderService) ContactDetailsActivator.getBundleContext()
                        .getService(serRef);

                if (protocolProvider.getOperationSet(opSetClass) != null
                    && protocolProvider.isRegistered())
                {
                    opSetProviders.add(protocolProvider);
                }
            }
        }
        return opSetProviders;
    }

    private static Map<Object, ProtocolProviderFactory> getProtocolProviderFactories()
    {
        ServiceReference[] serRefs = null;
        BundleContext bc = ContactDetailsActivator.getBundleContext();
        Map<Object, ProtocolProviderFactory> providerFactoriesMap = new Hashtable<Object, ProtocolProviderFactory>();

        try
        {
            // get all registered provider factories
            serRefs = bc.getServiceReferences(
                ProtocolProviderFactory.class.getName(),
                null);
        }
        catch (InvalidSyntaxException e)
        {
            // logger.error("LoginManager : " + e);
        }

        if (serRefs != null)
        {
            for (ServiceReference serRef : serRefs)
            {
                ProtocolProviderFactory providerFactory = (ProtocolProviderFactory)bc.getService(serRef);

                providerFactoriesMap.put(
                    serRef.getProperty(ProtocolProviderFactory.PROTOCOL),
                    providerFactory);
            }
        }

        return providerFactoriesMap;
    }
}