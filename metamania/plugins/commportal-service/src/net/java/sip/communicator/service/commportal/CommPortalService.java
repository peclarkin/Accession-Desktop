package net.java.sip.communicator.service.commportal;

import java.security.InvalidParameterException;

/**
 * A service which allows interaction with CommPortal.  Including
 * <li>requesting service indications from CommPortal</li>
 * <li>sending data to CommPortal</li>
 * <li>registering for notifications to changes to a service indication</li>
 * <li>unregistering for notifications</li>
 * <li>uploading files to CommPortal</li>
 */
public interface CommPortalService
{
    /**
     * Request a service indication(s) from CommPortal
     *
     * @param callback the callback for the request
     * @param networkErrorCallback optional parameter which will handle network
     *        errors if present.  Otherwise, these are handled by the service
     * @param isForeground If true then this will happen with high priority
     */
    public abstract void getServiceIndication(CPDataGetterCallback callback,
                                              CPOnNetworkErrorCallback networkErrorCallback,
                                              boolean isForeground);

    /**
     * Post a service indication to CommPortal
     *
     * @param callback the callback for the request
     * @param networkErrorCallback optional parameter which will handle network
     *        errors if present.  Otherwise, these are handled by the service
     * @param isForeground If true then this will happen with high priority
     */
    public abstract void postServiceIndication(CPDataSenderCallback callback,
                                               CPOnNetworkErrorCallback networkErrorCallback,
                                               boolean isForeground);

    /**
     * Upload a file to CommPortal
     *
     * @param callback the callback for the request
     * @param networkErrorCallback optional parameter which will handle network
     *        errors if present.  Otherwise, these are handled by the service
     * @param isForeground If true then this will happen with high priority
     */
    public abstract void uploadFile(CPFileUploadCallback callback,
                                    CPOnNetworkErrorCallback networkErrorCallback,
                                    boolean isForeground);

    /**
     * Register for notifications to changes to a service indication(s)
     * <p/>
     * This registration will continue regardless of any network errors until
     * unregister is called.  However, any data errors will unregister all
     * listeners.
     * <p/>
     * Does nothing if the callback is already registered
     *
     * @param callback The callback for the request
     * @param networkErrorCallback optional parameter which will handle network
     *        errors if present.  Otherwise, these are handled by the service
     */
    public abstract void registerForNotifications(CPDataRegistrationCallback callback,
                                                  CPOnNetworkErrorCallback networkErrorCallback);

    /**
     * Unregister for notifications to changes to a particular service indication
     *
     * @param callback The callback which we originally registered on.  Must be
     *        a registered callback otherwise an exception will be thrown
     * @throws InvalidParameterException if we try to unregister a callback that
     *         isn't registered
     */
    public abstract void unregisterForNotifications(CPDataRegistrationCallback callback);

    /**
     * Store the credentials so that the service can use them
     *
     * @param userName The users account name
     * @param password The users password
     * @param rememberMe If true then the service should make sure the
     *                   credentials are stored
     */
    public abstract void setCredentials(String userName,
                                        String password,
                                        boolean rememberMe);
}
