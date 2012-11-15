package net.java.sip.communicator.service.commportal;

/**
 * Defines a callback for sending data to CP
 */
public interface CPDataSenderCallback extends CPDataCallback
{
    /**
     * Implementations should return the data that we wish to send to CP.
     *
     * @return the data to send to CP
     */
    public abstract String getData();

    /**
     * Called when the data has been sent to CommPortal successfully
     */
    public abstract void onDataSent();
}
