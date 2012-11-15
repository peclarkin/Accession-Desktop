package net.java.sip.communicator.service.commportal;

/**
 * Service for dealing with the CoS.
 */
public interface ClassOfServiceService
{
    /**
     * Request the class of service from CommPortal
     * <p/>
     * Note that this may return on either the same thread, or a different
     * thread depending on if the CoS is available to be returned or must
     * be requested first
     *
     * @param callback the callback for the request
     * @param networkErrorCallback optional parameter which will handle network
     *        errors if present.  Otherwise, these are handled by the service
     * @param isForeground If true then this will happen with high priority
     */
    public abstract void getClassOfService(CPCosGetterCallback callback,
                                           CPOnNetworkErrorCallback networkErrorCallback,
                                           boolean isForeground);

    /**
     * Invalidate the cached CoS
     */
    public abstract void invalidateStoredCos();
}
