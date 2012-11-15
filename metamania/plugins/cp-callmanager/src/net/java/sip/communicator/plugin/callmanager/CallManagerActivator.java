package net.java.sip.communicator.plugin.callmanager;

import java.util.*;

import net.java.sip.communicator.service.commportal.*;
import net.java.sip.communicator.service.gui.*;
import net.java.sip.communicator.service.systray.*;
import net.java.sip.communicator.util.*;

import org.osgi.framework.*;

/**
 * Activator for the call manager plugin
 * <p/>
 * Implements
 * <li>BundleActivator as we are an Activator</li>
 * <li>CPCosGetterCallback so that we can get the CoS</li>
 *
 */
public class CallManagerActivator implements BundleActivator, CPCosGetterCallback
{
    private static final Logger sLog = Logger.getLogger(CallManagerActivator.class);
    private static CommPortalService sCommPortalService;
    private static ClassOfServiceService sCosService;
    private static SystrayService sSytemTrayService;
    private BundleContext mContext;
    private CallManagerComponent mComponent;


    public void start(BundleContext context)
    {
        if (sCommPortalService == null)
        {
            sCommPortalService =
                      ServiceUtils.getService(context, CommPortalService.class);
        }

        if (sCosService == null)
        {
            sCosService =
                  ServiceUtils.getService(context, ClassOfServiceService.class);
        }

        if (sSytemTrayService == null)
        {
            sSytemTrayService =
                         ServiceUtils.getService(context, SystrayService.class);
        }

        // The first thing we need to do is get the CoS so that we can work out
        // if ECM is enabled or not.
        mContext = context;
        sCosService.getClassOfService(this, null, true);
    }

    public void stop(BundleContext context) throws Exception
    {
        sLog.debug("Stop");

        if (mComponent != null)
        {
            mComponent.stop();
        }
    }

    static CommPortalService getCommPortalService()
    {
        return sCommPortalService;
    }

    static SystrayService getSystrayService()
    {
        return sSytemTrayService;
    }

    public void onCosReceived(CPCos classOfService)
    {
        if (classOfService.getIchAllowed() &&
            classOfService.getIchServiceLevel().equals("ecm"))
        {
            sLog.info("Cos allows ECM");
            mComponent = new CallManagerComponent(
                                      Container.CONTAINER_ACCOUNT_STATUS_SOUTH);

            Hashtable<String, String> filter = new Hashtable<String, String>();
            filter.put(Container.CONTAINER_ID,
                       Container.CONTAINER_ACCOUNT_STATUS_SOUTH.getID());

            mContext.registerService(PluginComponent.class.getName(),
                                     mComponent,
                                     filter);
        }
        else
        {
            sLog.info("Cos received but does not allow ECM");
        }
    }

}
