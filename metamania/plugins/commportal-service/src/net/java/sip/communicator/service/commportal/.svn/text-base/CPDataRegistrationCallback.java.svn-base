package net.java.sip.communicator.service.commportal;

/**
 * Defines a callback for being notified of changes to a particular service
 * indication.
 */
public interface CPDataRegistrationCallback
{
    /**
     * @return the name of the service indication that we are requesting changes
     * to
     */
    public abstract String getSIName();

    /**
     * Called if there is a problem with the request.
     * <p/>
     * This will unregister the listener from the callback
     *
     * @param error The error that has occurred
     */
    public abstract void onDataError(CPDataError error);

    /**
     * Called when the data has changed in some way
     */
    public abstract void onDataChanged();
}
