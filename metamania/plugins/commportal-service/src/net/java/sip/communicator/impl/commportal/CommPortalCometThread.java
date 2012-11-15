package net.java.sip.communicator.impl.commportal;

import java.security.*;
import java.util.concurrent.*;

import net.java.sip.communicator.service.commportal.*;
import net.java.sip.communicator.util.*;

import org.apache.http.client.*;

/**
 * A thread for doing COMET
 *
 * Implements CPOnNetworkErrorCallback and CPDataRegistrationCallback so that it
 * can serve as single point of contact for the WorkItem which actually does the
 * work.
 */
public class CommPortalCometThread extends Thread
                                           implements CPOnNetworkErrorCallback,
                                                      CPDataRegistrationCallback
{
    private final Logger mLog;

    /**
     * The service indication that we are created for
     */
    private final String mServiceIndication;

    /**
     * The call backs that we are created for
     */
    private final CopyOnWriteArrayList<CometObjectsContainer> mCometObjects =
                              new CopyOnWriteArrayList<CometObjectsContainer>();

    /**
     * The CommPortal service that created us - allows access to the session id.
     */
    private final CommPortalServiceImpl mCommPortalService;

    /**
     * HTTP client used for making requests
     */
    private final HttpClient mHttpClient;

    private boolean mKeepGoing = true;

    CommPortalCometThread(HttpClient httpClient,
                          CPDataRegistrationCallback callback,
                          CPOnNetworkErrorCallback networkErrorCallback,
                          CommPortalServiceImpl commPortalService)
    {
        mHttpClient = httpClient;
        mServiceIndication = callback.getSIName();
        mCommPortalService = commPortalService;

        // Add the work item that we need to do
        mCometObjects.add(new CometObjectsContainer(callback,
                                                    networkErrorCallback));

        mLog = Logger.getLogger("CommPortalCometThread" + mServiceIndication);
    }

    /**
     * Remove the callback from this COMET thread.  This essentially means that
     * this call back is no longer interested in this service indication.
     *
     * @param callback The callback to remove
     * @throws InvalidParameterException if we try to unregister a callback that
     *         was never registered
     * @return true if the list of callbacks is now empty - i.e. this thread
     *         will stop running as it is no longer required
     */
    boolean removeCallback(CPDataRegistrationCallback callback)
    {
        mLog.info("Removing callback");
        boolean callbackExisted = false;

        // Search for the callback in the list of containers
        for (CometObjectsContainer cometObjectContainer : mCometObjects)
        {
            if (cometObjectContainer.mCallback.equals(callback))
            {
                callbackExisted = true;
                mCometObjects.remove(cometObjectContainer);
                mLog.debug("Found callback");
                break;
            }
        }

        if (!callbackExisted)
        {
            mLog.error("Trying to remove non-existant callback");
            throw new InvalidParameterException(
                               "Trying to remove callback that does not exist");
        }

        boolean isEmpty = mCometObjects.isEmpty();

        if (isEmpty)
        {
            mLog.debug("Work list is now empty");
            mKeepGoing = false;
        }

        return isEmpty;
    }

    /**
     * Add a new callback to this thread.  This means that another component is
     * interested in the same service indication
     *
     * @param callback the callback to add
     * @param networkErrorCallback how network errors should be handled
     * @throws InvalidParameterException if the call back is for a different
     *         service indication
     */
    void addCallback(CPDataRegistrationCallback callback,
                     CPOnNetworkErrorCallback networkErrorCallback)
    {
        mLog.debug("Add new callback");

        if (!mServiceIndication.equals(callback.getSIName()))
        {
            mLog.error("Callback was invalid");
            throw new InvalidParameterException("Trying to add invalid callback");
        }

        // Only add the callback if it hasn't already been added
        boolean callbackAlreadyAdded = false;

        for (CometObjectsContainer cometObject : mCometObjects)
        {
            if (callback.equals(cometObject.mCallback))
            {
                mLog.debug("Trying to add callback that is registered");
                callbackAlreadyAdded = true;
                break;
            }
        }

        if (!callbackAlreadyAdded)
        {
            mLog.debug("Callback not already added so adding it");
            mCometObjects.add(new CometObjectsContainer(callback,
                                                        networkErrorCallback));
        }
    }

    @Override
    public void run()
    {
        // Create a work item to handle the request
        WorkItem work = new WorkItemComet(this,
                                          this,
                                          mCommPortalService,
                                          mHttpClient);

        while (mKeepGoing)
        {
            try
            {
                synchronized (this)
                {
                    if (mCommPortalService.getBackoff().shouldWait())
                    {
                        // We are waiting because there is a network or EAS issue
                        mLog.debug("Waiting as network or EAS problems");
                        wait(mCommPortalService.getBackoff().getBackOffTime());
                    }
                    else if (mCommPortalService.getSessionId() == null)
                    {
                        mLog.debug("No session id, waiting for one");
                        wait(0);
                    }
                }
            }
            catch (InterruptedException e)
            {
                mLog.info("Interrupted while waiting", e);
            }

            if (mKeepGoing)
            {
                work.doWork();
            }
        }
    }

    /**
     * A simple container for the objects required for handling COMET requests
     */
    private static class CometObjectsContainer
    {
        private final CPDataRegistrationCallback mCallback;
        private final CPOnNetworkErrorCallback mNetworkErrorCallback;

        private CometObjectsContainer(CPDataRegistrationCallback callback,
                                      CPOnNetworkErrorCallback networkErrorCallback)
        {
            mCallback = callback;
            mNetworkErrorCallback = networkErrorCallback;
        }
    }

    public void onDataError(CPDataError errorType)
    {
        mLog.info("Data error whilst doing COMET, type " + errorType);

        for (CometObjectsContainer container : mCometObjects)
        {
            container.mCallback.onDataError(errorType);

            // And unregister these callbacks. Call through the service so
            // that the thread can be removed from store once there are no
            // callbacks left.
            mCommPortalService.unregisterForNotifications(
                                                       container.mCallback);
        }

        // Finally finish this thread - there is some problem with the request
        // thus the only thing to do is stop.
        mKeepGoing = false;
    }

    public void onNetworkError(CPNetworkError errorType)
    {
        mLog.info("Network error whilst doing COMET, type " + errorType);

        // Just tell the CommPortal service - it tells everything (including the
        // COMET threads which includes this one)
        if (mCommPortalService != null)
        {
            mCommPortalService.onNetworkError(errorType);
        }
    }

    /**
     * Tell all interested listeners of a network issue we jut hit
     *
     * @param errorType The type of error
     */
    void tellOfNetworkError(CPNetworkError errorType)
    {
        for (CometObjectsContainer container : mCometObjects)
        {
            // Notify each thing that desires to be notified.
            CPOnNetworkErrorCallback networkCallback =
                                            container.mNetworkErrorCallback;

            if (networkCallback != null)
            {
                networkCallback.onNetworkError(errorType);
            }
        }
    }

    public void onDataChanged()
    {
        mLog.info("Data changed");

        for (CometObjectsContainer container : mCometObjects)
        {
            container.mCallback.onDataChanged();
        }
    }

    public String getSIName()
    {
        return mServiceIndication;
    }

    /**
     * Empty the work list and close this thread down
     */
    void closeThread()
    {
        mLog.debug("Closing the thread");
        mKeepGoing = false;

        synchronized (this)
        {
            notify();
        }
    }
}
