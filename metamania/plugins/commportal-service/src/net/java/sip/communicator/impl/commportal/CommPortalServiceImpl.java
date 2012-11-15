package net.java.sip.communicator.impl.commportal;

import java.security.*;
import java.security.cert.*;
import java.util.concurrent.*;

import net.java.sip.communicator.service.commportal.*;
import net.java.sip.communicator.service.credentialsstorage.*;
import net.java.sip.communicator.util.*;

import org.apache.http.client.*;
import org.apache.http.conn.scheme.*;
import org.apache.http.conn.ssl.*;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.tsccm.*;
import org.apache.http.params.*;
import org.jitsi.service.configuration.*;

/**
 * Implementation of the CommPortal service - it connects to CommPortal and gets
 * and sends data.
 *
 * Stores the CommPortal session id and handles requests for a new one.
 */
public class CommPortalServiceImpl implements CommPortalService,
                                              CPOnNetworkErrorCallback
{
    private static final Logger sLog = Logger.getLogger(CommPortalServiceImpl.class);

    /**
     * The place in the config where the password is stored
     */
    private static final String CONFIG_PASSWORD =
                           "net.java.sip.communicator.plugin.provisioning.auth";

    /**
     * The place in the config where the username is stored
     */
    private static final String CONFIG_USERNAME =
                  "net.java.sip.communicator.plugin.provisioning.auth.USERNAME";

    /**
     * The place in the config where the control of self-signed certificates is
     * stored
     */
    private static final String CONFIG_ALLOW_SELF_SIGNED =
                  "net.java.sip.communicator.impl.commportal.ALLOW_SELF_SIGNED";

    /**
     * The list of work that has been requested from the foreground
     */
    private final CopyOnWriteArrayList<WorkItem> mForegroundWork =
                                           new CopyOnWriteArrayList<WorkItem>();

    /**
     * The list of work that has been requested from the background
     */
    private final CopyOnWriteArrayList<WorkItem> mBackgroundWork =
                                           new CopyOnWriteArrayList<WorkItem>();

    /**
     * Get the correct list of work items depending on whether this is a
     * foreground or background operation
     *
     * @param isForeground true if this is a foreground operation
     * @return the list in question
     */
    protected CopyOnWriteArrayList<WorkItem> getWorkList(boolean isForeground)
    {
        return isForeground ? mForegroundWork : mBackgroundWork;
    }

    /**
     * Service for storing or getting the credentials
     */
    private final CredentialsStorageService mCredsService;

    /**
     * Service for getting or storing config
     */
    private final ConfigurationService mConfigService;

    /**
     * The thread which does the foreground work
     */
    private final CommPortalWorkThread mForegroundWorker;

    /**
     * The thread which does the background work
     */
    private final CommPortalWorkThread mBackgroundWorker;

    /**
     * Get the work thread for whether we are foreground or background
     *
     * @param isForeground True if we are in the foreground
     * @return the work thread
     */
    private CommPortalWorkThread getWorker(boolean isForeground)
    {
        return isForeground ? mForegroundWorker : mBackgroundWorker;
    }

    /**
     * A map from service indications to a thread which is doing COMET for that
     * indication
     *
     * A concurrent hash map as it may be modified from multiple threads.
     * Created with initial size of 5 as we don't expect there to be more than
     * that many requests
     */
    private final ConcurrentHashMap<String, CommPortalCometThread> mCometMap =
                        new ConcurrentHashMap<String, CommPortalCometThread>(5);

    /**
     * The number of this subscriber
     */
    private String mNumber;

    /**
     * The password for this subscriber
     */
    private String mPassword;

    /**
     * The CommPortal Session Id.
     */
    private String mSessionId = null;

    /**
     * Indicates if we are getting the session Id or not
     *
     * If we don't have a session but are in the process of getting one, then we
     * don't want to schedule getting another session.
     */
    private boolean mGettingSession = false;

    /**
     * The backoff for dealing with network errors
     */
    private final CommPortalBackoff mBackoff = new CommPortalBackoff();

    /**
     * The COMET timeout to use
     */
    private final CommPortalCometTimeout mCometTimeout =
                                                   new CommPortalCometTimeout();

    /**
     * Client for making all http(s) requests
     */
    private final HttpClient mHttpClient;

    /**
     * A network listener to track if we are connected to the internet or not
     */
    private final CommPortalNetworkListener mNetworkListener;

    /**
     * Factory for creating work threads and COMET threads
     */
    private final CommPortalThreadFactory mThreadFactory;

    /**
     * The number of connections that we need.  Start at 2 as there are two
     * threads that can do work (foreground and background).
     * <p/>
     * Note that this must be increased when we need extra connections (if, for
     * example, a new COMET thread is created)
     */
    private int mConnections = 2;

    /**
     * Connection manager
     *
     * Need to be a thread safe connection manager as we make requests on
     * multiple different threads.
     */
    private final ThreadSafeClientConnManager mConnectionManager;

    protected static class TrustSelfSignedStrategy
        implements TrustStrategy
    {
        public boolean isTrusted(X509Certificate[] xiArg0, String xiArg1)
        {
            return true;
        }
    }

    /**
     * Constructor
     *
     * @param threadFactory factory to create work threads
     */
    CommPortalServiceImpl(CommPortalThreadFactory threadFactory)
    {
        mThreadFactory = threadFactory;
        mCredsService  = CommPortalActivator.getCredentialsService();
        mConfigService = CommPortalActivator.getConfigService();

        // Create the connection manager - may require a new scheme registry to
        // allow self-signed certificates to be supported
        SchemeRegistry registry = null;

        if (mConfigService.getBoolean(CONFIG_ALLOW_SELF_SIGNED, false))
        {
            sLog.info("Create connection manager allowing self signed certs");

            try
            {
                TrustStrategy trustStrategy = new TrustSelfSignedStrategy();
                X509HostnameVerifier verifier = new AllowAllHostnameVerifier();
                SSLSocketFactory httpsFactory =
                                  new SSLSocketFactory(trustStrategy, verifier);
                PlainSocketFactory httpFactory =
                                          PlainSocketFactory.getSocketFactory();

                registry = new SchemeRegistry();
                registry.register(new Scheme("https", 443, httpsFactory));
                registry.register(new Scheme("http", 80, httpFactory));

                sLog.error("=== Allowing self signed certificates ===");
            }
            catch (Exception e)
            {
                sLog.error("Couldn't set registry for self signed certs", e);
            }
        }

        mConnectionManager = (registry != null) ?
                                     new ThreadSafeClientConnManager(registry) :
                                     new ThreadSafeClientConnManager();
        updateConnectionManager();

        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, 2000);
        mHttpClient = new DefaultHttpClient(mConnectionManager, params);

        // Create and start the worker threads
        mForegroundWorker = mThreadFactory.createWorker(this, true);
        mBackgroundWorker = mThreadFactory.createWorker(this, false);

        // And create a network listener to listen for network issues
        mNetworkListener = new CommPortalNetworkListener(this);
    }

    public void getServiceIndication(CPDataGetterCallback callback,
                                     CPOnNetworkErrorCallback networkErrorCallback,
                                     boolean isForeground)
    {
        sLog.info("Asked to get SI " + callback.getSIName());

        if (shouldAddWork(networkErrorCallback))
        {
            WorkItem workItem = new WorkItemGetData(networkErrorCallback,
                                                    callback,
                                                    this,
                                                    mHttpClient);
            getWorkList(isForeground).add(workItem);
            notifyThread(isForeground);
        }
    }

    public void postServiceIndication(CPDataSenderCallback callback,
                                      CPOnNetworkErrorCallback networkErrorCallback,
                                      boolean isForeground)
    {
        sLog.info("Asked to send data for SI");

        if (shouldAddWork(networkErrorCallback))
        {
            WorkItem workItem = new WorkItemSendData(networkErrorCallback,
                                                     callback,
                                                     this,
                                                     mHttpClient);
            getWorkList(isForeground).add(workItem);
            notifyThread(isForeground);
        }
    }

    public void uploadFile(CPFileUploadCallback callback,
                           CPOnNetworkErrorCallback networkErrorCallback,
                           boolean isForeground)
    {
        sLog.info("Asked to upload file");

        if (shouldAddWork(networkErrorCallback))
        {
            WorkItem workItem = new WorkItemUploadFile(networkErrorCallback,
                                                       callback,
                                                       this,
                                                       mHttpClient);
            getWorkList(isForeground).add(workItem);
            notifyThread(isForeground);
        }
    }

    /**
     * Notify a thread that there is work to be done.
     *
     * @param isForeground true if we are in the foreground
     */
    private void notifyThread(boolean isForeground)
    {
        CommPortalWorkThread worker = getWorker(isForeground);

        synchronized (worker)
        {
            worker.notify();
        }
    }

    public void registerForNotifications(CPDataRegistrationCallback callback,
                                         CPOnNetworkErrorCallback networkErrorCallback)
    {
        String key = callback.getSIName();
        sLog.debug("Registering for notifications on " + key);

        // We always want to add a COMET WorkItem.  However, shouldAddWork will
        // also notify the call back if there is a known network issue
        shouldAddWork(networkErrorCallback);

        if (mCometMap.containsKey(key))
        {
            sLog.debug("Already doing COMET, so just adding the call back");
            mCometMap.get(key).addCallback(callback, networkErrorCallback);
        }
        else
        {
            sLog.debug("Create new COMET thread");
            CommPortalCometThread cometThread =
                          mThreadFactory.createCometWorker(mHttpClient,
                                                           callback,
                                                           networkErrorCallback,
                                                           this);

            mCometMap.put(key, cometThread);

            // We've got a new COMET thread so need to increase the number of
            // allowed connections
            mConnections++;
            updateConnectionManager();
        }
    }

    /**
     * Change the number of connections that are allowed.
     */
    private void updateConnectionManager()
    {
        // We only connect to a single host / route thus the max per route, and
        // max total can be the same.
        mConnectionManager.setDefaultMaxPerRoute(mConnections);
        mConnectionManager.setMaxTotal(mConnections);
    }

    public void unregisterForNotifications(CPDataRegistrationCallback callback)
    {
        String siName = callback.getSIName();
        sLog.debug("Unregistering for callback " + siName);
        CommPortalCometThread thread = mCometMap.get(siName);

        if (thread == null)
        {
            sLog.error("Call back not registered");
            throw new InvalidParameterException("Call back not registered");
        }

        boolean isEmpty = thread.removeCallback(callback);

        if (isEmpty)
        {
            sLog.debug("Removing comet thread for SI " + siName);
            mCometMap.remove(siName);

            // We've killed a COMET thread so we need to decrease the number of
            // allowed connections
            mConnections--;
            updateConnectionManager();
        }
    }

    public void setCredentials(String userName,
                               String password,
                               boolean rememberMe)
    {
        mNumber = userName;
        mPassword = password;

        if (rememberMe)
        {
            mCredsService.storePassword(CONFIG_PASSWORD, password);
            mConfigService.setProperty(CONFIG_USERNAME, userName);
        }
    }

    /**
     * Called when we learn that the credentials we have are invalid
     */
    void onInvalidCredentials()
    {
        sLog.info("Credentials are invalid");

        // The username won't have changed but the password might have done just
        // remove it from the store so that we re-request it.
        mPassword = null;
        mCredsService.removePassword(CONFIG_PASSWORD);
    }

    /**
     * Check to see if we should add a new piece of work onto the list of work.
     * <p/>
     * Notifies the call back of any known network issues
     *
     * @param networkCallback The network call back for this work
     * @return True if we should add the work
     */
    private boolean shouldAddWork(CPOnNetworkErrorCallback networkCallback)
    {
        boolean shouldAdd;

        if (networkCallback != null && !mNetworkListener.isConnected())
        {
            // No network and a network call back was provided. Therefore the
            // work should not be added.  This is because it might sit at the
            // back of a list of work, not being looked at, until the network
            // is restored and the blocking items are completed.  The calling
            // code provided the callback thus it wants to know if this can't
            // complete now.
            sLog.debug("Not adding work item as no network");
            networkCallback.onNetworkError(CPNetworkError.NETWORK_UNAVAILABLE);
            shouldAdd = false;
        }
        else
        {
            sLog.debug("Should add the work item");
            shouldAdd = true;
        }

        return shouldAdd;
    }


    /**
     * Called when the network has been restored after a pause. Notifies each of
     * the worker threads so that they try again.
     */
    void onNetworkRestored()
    {
        sLog.debug("Network restored");

        // Notify both the worker threads
        notifyThread(true);
        notifyThread(false);

        // And the COMET threads
        for (String key : mCometMap.keySet())
        {
            CommPortalCometThread worker = mCometMap.get(key);

            synchronized (worker)
            {
                worker.notify();
            }
        }
    }

    public void onNetworkError(CPNetworkError errorType)
    {
        // Increase the backoff
        sLog.debug("Network error");
        getBackoff().onError();

        // Tell each of the queued items that the network has failed
        notifyNetworkError(errorType, mForegroundWork);
        notifyNetworkError(errorType, mBackgroundWork);

        // And tell the COMET handlers
        for (String key : mCometMap.keySet())
        {
            mCometMap.get(key).tellOfNetworkError(errorType);
        }
    }

    /**
     * Takes a list of work items and tells each item with a non-null network
     * error listener that there has been a network error. Removes those work
     * items.
     *
     * @param errorType The type of error
     * @param workList  The list of work to look through
     */
    private void notifyNetworkError(CPNetworkError errorType,
                                    CopyOnWriteArrayList<WorkItem> workList)
    {
        for (WorkItem work : workList)
        {
            if (work.mNetworkErrorCallback != null)
            {
                work.mNetworkErrorCallback.onNetworkError(errorType);
                workList.remove(work);
            }

            // We don't care about work items which don't have a network
            // error call back - they are handled by this class which
            // already knows.
        }
    }

    /**
     * Get the session id or request it (and return null) if it does not exist.
     *
     * @return a CommPortal session id or null if it does not exist
     */
    synchronized String getSessionId()
    {
        if (mSessionId == null && !mGettingSession)
        {
            mGettingSession = true;

            // Pause the background worker otherwise it will sit and spin trying
            // to do work without a session
            mBackgroundWorker.pause();

            // We've got no session ID - add a work item to get one to the very
            // front of the list:
            CPDataGetterCallback callback = new CPDataGetterCallback()
            {
                public boolean onDataReceived(String data)
                {
                    sLog.info("Got a session id");
                    mSessionId = data;

                    // Wake the COMET threads as they auto-pause when they have
                    // no session ID:
                    for (String key : mCometMap.keySet())
                    {
                        Thread thread = mCometMap.get(key);
                        synchronized (thread)
                        {
                            thread.notify();
                        }
                    }

                    // And similarly, wake the background thread
                    mBackgroundWorker.wake();

                    return true;
                }

                public void onDataError(CPDataError errorType)
                {
                    // Log and then try again.  We backoff elsewhere
                    sLog.error("Got a data error getting session: " + errorType);

                    // And make sure we try again
                    mGettingSession = false;
                    getSessionId();
                }

                public String getSIName()
                {
                    // Not required - the session id is not got with a service
                    // indication
                    return null;
                }

                public DataFormat getDataFormat()
                {
                    // Again not required as the session id is not got with a
                    // service indication.
                    return null;
                }
            };

            WorkItem workItem = new WorkItemGetSessionId(null,
                                                         callback,
                                                         this,
                                                         mHttpClient);

            // Get the session _now_ (i.e. on the foreground thread)
            mForegroundWork.add(0, workItem);
            notifyThread(true);
        }

        if (mSessionId != null)
        {
            // If we've got a session then we are not getting one
            mGettingSession = false;
        }

        return mSessionId;
    }

    /**
     * Called when the session has expired
     */
    void onSessionExpired()
    {
        sLog.info("Told that session expired");
        mSessionId = null;

        // Pause the backend thread otherwise it sits and spins. No need to
        // pause the COMET threads as they auto pause when there is no
        // session ID.
        mBackgroundWorker.pause();
    }

    /**
     * @return the object for dealing with backing off from the EAS.
     */
    synchronized CommPortalBackoff getBackoff()
    {
        return mBackoff;
    }

    /**
     * @return the object for dealing with COMET timeouts
     */
    synchronized CommPortalCometTimeout getCometTimeout()
    {
        return mCometTimeout;
    }

    /**
     * Stop the service from running by letting all threads complete their
     * current item
     */
    void stop()
    {
        sLog.info("Stopping");
        mBackgroundWorker.closeThread();
        mForegroundWorker.closeThread();

        for (String key : mCometMap.keySet())
        {
            mCometMap.get(key).closeThread();
        }
    }

    /**
     * @return the users phone number
     */
    String getUserNumber()
    {
        if (mNumber == null)
        {
            mNumber = mConfigService.getString(CONFIG_USERNAME);
        }

        return mNumber;
    }

    /**
     * @return the subscribers password
     */
    String getPassword()
    {
        // Update the remembered password if we can.
        String storedPassword = mCredsService.loadPassword(CONFIG_PASSWORD);
        if (storedPassword != null)
        {
            mPassword = storedPassword;
        }

        return mPassword;
    }
}
