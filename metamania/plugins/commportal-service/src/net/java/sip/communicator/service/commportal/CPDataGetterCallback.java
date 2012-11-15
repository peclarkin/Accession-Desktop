package net.java.sip.communicator.service.commportal;

/**
 * Defines a call back for getting data from CommPortal
 */
public interface CPDataGetterCallback extends CPDataCallback
{
    /**
     * Called when we have received the data we asked for from CommPortal
     *
     * @param data the data from CommPortal
     * @return false if the data is bad (in such a way as to indicate a server
     *         error).  In this case, the service will try again later after a
     *         delay.
     */
    public abstract boolean onDataReceived(String data);
}
