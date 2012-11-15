package net.java.sip.communicator.plugin.callmanager;


import net.java.sip.communicator.plugin.callmanager.SimpleICM.SimpleICMError;
import net.java.sip.communicator.service.commportal.*;
import net.java.sip.communicator.util.*;

import org.json.*;

/**
 * A class for managing and handling the data required by the ECM plugin
 * <p/>
 * Includes remembering the data that we have received from CommPortal
 */
class CallManagerDataHandler
{
    private static final Logger sLog =
                                 Logger.getLogger(CallManagerDataHandler.class);

    /** The name of the contact groups service indication */
    private static final String CONTACT_GROUPS_SI_NAME =
                                     "Meta_Subscriber_MetaSphere_ContactGroups";

    /** The name of the ICM service indication */
    private static final String ICM_SI_NAME =
                                         "Meta_SubscriberDevice_MetaSphere_ICM";

    private final CommPortalService mCommPortalService;
    private final CallManagerStatusBar mStatusBar;

    /**
     * The ECM data received from the server.  Stored in order to return known
     * good data to the client as required
     */
    private JSONObject mSimpleIcmData;

    /**
     * The contact group data from the server.  Stored in order to return known
     * good data to the client as required
     */
    private SIContactGroups mContactGrps;

    /**
     * The ECM data that we wish to send to the server in a string format
     */
    private String mChangedICMString;

    /**
     * True if we are registered for changes to the ECM status
     */
    private boolean mRegistered = false;

    /**
     * The listener for changes to the ECM status
     */
    private final CPDataRegistrationCallback mECMListener =
                                                new CPDataRegistrationCallback()
    {
        public void onDataError(CPDataError error)
        {
            // Treat as server error and try again later
            sLog.info("Problem listening for ECM changes");
            SimpleICM data = new SimpleICM(mSimpleIcmData, mContactGrps);
            mStatusBar.failedToGetData(data);

            // We're no longer registered as data errors automatically unregister
            // all listeners
            mRegistered = false;
            handleNetworkError();
        }

        public void onDataChanged()
        {
            sLog.debug("ECM listener says data changed");
            mCommPortalService.getServiceIndication(mECMConfigGetter,
                                                    mNetworkGetErrorCallback,
                                                    true);
        }

        public String getSIName()
        {
            return "ICMDeviceConfig";
        }
    };

    /**
     * Call back for getting the ECM config
     */
    private final CPDataGetterCallback mECMConfigGetter =
                                                      new CPDataGetterCallback()
    {
        public boolean onDataReceived(String data)
        {
            boolean dataValid = true;

            // The received data should look like:
            //   [
            //     {
            //       "objectIdentity": {"line":"..."},
            //       "dataType": "Meta_Subscriber_MetaSphere_ContactGroups",
            //       "data": {...}
            //     },
            //     {
            //       "objectIdentity": {"device":"...","line":"..."},
            //       "dataType": "Meta_SubscriberDevice_MetaSphere_ICM",
            //       "data": {...}
            //     }
            //   ]
            try
            {
                sLog.debug("Got data back from EAS");

                // First try to parse the data into a JSONObject and get the
                // relevant parts of the data:
                JSONArray jsonArray = new JSONArray(data);

                int length = jsonArray.length();
                for (int i = 0; i < length; i++)
                {
                    JSONObject dataObject = jsonArray.getJSONObject(i);
                    String dataType = dataObject.getString("dataType");

                    if (CONTACT_GROUPS_SI_NAME.equals(dataType))
                    {
                        sLog.debug("Found contact groups object");
                        JSONObject contactGrpData = dataObject.getJSONObject("data");
                        mContactGrps = new SIContactGroups(contactGrpData);
                    }
                    else if (ICM_SI_NAME.equals(dataType))
                    {
                        sLog.debug("Found ICM object");
                        mSimpleIcmData = dataObject.getJSONObject("data");
                    }
                }


                // Send a copy of the ICM data back to the UI.  This is so that
                // the UI can make changes to it and that we can revert them it
                // we fail to make the corresponding update
                SimpleICM newData = new SimpleICM(mSimpleIcmData, mContactGrps);
                mStatusBar.onDataReceived(newData);

                // Finally make sure that we are registered
                if (!mRegistered)
                {
                    sLog.debug("Registering");
                    mRegistered = true;
                    mCommPortalService.registerForNotifications(mECMListener,
                                                      mNetworkGetErrorCallback);
                }
            }
            catch (JSONException e)
            {
                sLog.info("Got bad JSON", e);
                dataValid = false;
            }

            return dataValid;
        }

        public void onDataError(CPDataError error)
        {
            sLog.info("Problem getting the data");
            SimpleICM data = new SimpleICM(mSimpleIcmData, mContactGrps);
            mStatusBar.failedToGetData(data);
            handleNetworkError();
        }

        public String getSIName()
        {
            // We need 2 service indications, the contact groups, and the actual
            // ICM data.
            return CONTACT_GROUPS_SI_NAME + "," + ICM_SI_NAME;
        }

        public DataFormat getDataFormat()
        {
            return DataFormat.DATA;
        }
    };

    /**
     * Call back for sending the ECM config
     */
    private final CPDataSenderCallback mECMConfigSender =
                                                      new CPDataSenderCallback()
    {
        public void onDataError(CPDataError error)
        {
            SimpleICM data = new SimpleICM(mSimpleIcmData, mContactGrps);
            mStatusBar.failedToSendData(data);
        }

        public String getData()
        {
            return mChangedICMString;
        }

        public DataFormat getDataFormat()
        {
            return DataFormat.DATA;
        }

        public String getSIName()
        {
            return ICM_SI_NAME;
        }

        public void onDataSent()
        {
            // We don't care - COMET polling will ensure that the UI is updated
            // to the new status
        }
    };

    /**
     * Call back for when there is a network error getting the data
     */
    private final CPOnNetworkErrorCallback mNetworkGetErrorCallback =
                                                  new CPOnNetworkErrorCallback()
    {
        public void onNetworkError(CPNetworkError error)
        {
            sLog.info("Failed to get data due to network issue");
            SimpleICM data = new SimpleICM(mSimpleIcmData, mContactGrps);
            mStatusBar.failedToGetData(data);
            handleNetworkError();
        }
    };

    /**
     * Call back for when there is a network error sending the data
     */
    private final CPOnNetworkErrorCallback mNetworkSendErrorCallback =
                                                  new CPOnNetworkErrorCallback()
    {
        public void onNetworkError(CPNetworkError error)
        {
            // No need to schedule a retry as we tell the user that we failed to
            // make the change
            sLog.info("Failed to send data due to network issue");
            SimpleICM data = new SimpleICM(mSimpleIcmData, mContactGrps);
            mStatusBar.failedToSendData(data);
        }
    };

    public CallManagerDataHandler(CallManagerStatusBar statusBar)
    {
        mCommPortalService = CallManagerActivator.getCommPortalService();
        mStatusBar = statusBar;

        // Get the ICM data and start listening for changes to it:
        mRegistered = true;
        mCommPortalService.registerForNotifications(mECMListener,
                                                    mNetworkGetErrorCallback);
        mCommPortalService.getServiceIndication(mECMConfigGetter,
                                                mNetworkGetErrorCallback,
                                                true);
    }

    /**
     * Handle network errors.
     */
    private void handleNetworkError()
    {
        // We handle errors by asking the CommPortalService to get the ECM data
        // but without any network error callback.  This means that any network
        // error will be handled by the Service's own backoff process. We don't
        // care about subsequent failures, because we already know that we have
        // failed.
        mCommPortalService.getServiceIndication(mECMConfigGetter, null, true);
    }

    public void sendChangedData(SimpleICM changedData)
    {
        try
        {
            sLog.info("Sending changed data to server");
            mChangedICMString = changedData.fetchConfig();
            mCommPortalService.postServiceIndication(mECMConfigSender,
                                                     mNetworkSendErrorCallback,
                                                     true);
        }
        catch (JSONException e)
        {
            sLog.debug("JSON Error parsing new data", e);
            SimpleICM data = new SimpleICM(mSimpleIcmData, mContactGrps);
            mStatusBar.failedToSendData(data);
        }
        catch (SimpleICMError e)
        {
            sLog.info("ICM error parsing new data", e);
            SimpleICM data = new SimpleICM(mSimpleIcmData, mContactGrps);
            mStatusBar.failedToSendData(data);
        }
    }

    /**
     * Shut down this component
     */
    void stop()
    {
        sLog.debug("Stop service");
        mCommPortalService.unregisterForNotifications(mECMListener);
    }
}
