package net.java.sip.communicator.service.commportal;

/**
 * An interface for getting or sending data to CommPortal
 */
interface CPDataCallback
{
    /**
     * @return the name of the service indication that we are requesting
     */
    public abstract String getSIName();

    /**
     * @return the format with which to request the data
     */
    public abstract DataFormat getDataFormat();

    /**
     * Called if there is a problem with the request
     *
     * @param error The error that we hit
     */
    public abstract void onDataError(CPDataError error);

    /**
     * An enum of the different ways in which we can request data
     */
    public enum DataFormat
    {
        DATA("data"),
        DATA_JS("data.js");

        /**
         * The format to use when requesting the data
         */
        private final String mFormat;

        DataFormat(String format)
        {
            mFormat = format;
        }

        public String getFormat()
        {
            return mFormat;
        }
    }
}
