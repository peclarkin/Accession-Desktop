package net.java.sip.communicator.impl.commportal;

import net.java.sip.communicator.service.commportal.CPCosGetterCallback;
import net.java.sip.communicator.service.commportal.CPDataError;
import net.java.sip.communicator.service.commportal.CPDataGetterCallback;
import net.java.sip.communicator.service.commportal.CPOnNetworkErrorCallback;
import net.java.sip.communicator.service.commportal.ClassOfServiceService;
import net.java.sip.communicator.util.Logger;

import org.json.JSONException;

/**
 * Implementation of the Class of Service service - it stores the class of
 * service, getting it if it is not already stored
 * <p/>
 * Stores the CommPortal session id and handles requests for a new one.
 */
public class ClassOfServiceImpl implements ClassOfServiceService
{
    private static final Logger sLog = Logger.getLogger(ClassOfServiceImpl.class);
    private final CommPortalServiceImpl mCommPortalService;
    private ParsedClassOfService mStoredClassOfService;

    public ClassOfServiceImpl(CommPortalServiceImpl commPortalService)
    {
        mCommPortalService = commPortalService;
    }

    public void getClassOfService(final CPCosGetterCallback callback,
                                  final CPOnNetworkErrorCallback networkErrorCallback,
                                  final boolean isForeground)
    {
        if (mStoredClassOfService != null)
        {
            // We've already got it, return it
            sLog.debug("Asked to get CoS but already have it stored");
            callback.onCosReceived(mStoredClassOfService);
        }
        else
        {
            sLog.debug("Asked to get CoS, requesting it");
            CPDataGetterCallback cosGetterCallback = new CPDataGetterCallback()
            {
                public String getSIName()
                {
                    return "ClassOfService";
                }

                public DataFormat getDataFormat()
                {
                    return DataFormat.DATA_JS;
                }

                public void onDataError(CPDataError error)
                {
                    // Shouldn't happen - try again, we've already backed off
                    sLog.error("Data error getting CoS");
                    mCommPortalService.getServiceIndication(this,
                                                            networkErrorCallback,
                                                            isForeground);
                }

                public boolean onDataReceived(String data)
                {
                    boolean dataValid = false;

                    try
                    {
                        sLog.debug("Received data");
                        mStoredClassOfService = new ParsedClassOfService(data);
                        callback.onCosReceived(mStoredClassOfService);
                        dataValid = true;
                    }
                    catch (JSONException e)
                    {
                        sLog.error("Error parsing the returned JSON ", e);
                    }

                    return dataValid;
                }
            };

            mCommPortalService.getServiceIndication(cosGetterCallback,
                                                    networkErrorCallback,
                                                    isForeground);
        }
    }

    public void invalidateStoredCos()
    {
        sLog.info("Asked to invalidate the stored CoS");
        mStoredClassOfService = null;
    }
}
