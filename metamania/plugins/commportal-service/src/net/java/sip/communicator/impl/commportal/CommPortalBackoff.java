package net.java.sip.communicator.impl.commportal;

import net.java.sip.communicator.util.*;

import org.jitsi.service.configuration.*;

/**
 * Class for backing off from the EAS in the event of network errors
 *
 * Note that this class is NOT threadsafe - callers should make sure that it is
 * accessed in a threadsafe way.
 */
class CommPortalBackoff
{
    // The following is a list of places where config pertinent to this class is
    // stored
    private static final String CONFIG_INITIAL_FAIL_BACKOFF =
                  "net.java.sip.communicator.impl.commportal.intialfailbackoff";
    private static final String CONFIG_MAX_NUMBER_FAILURE_DOUBLES =
            "net.java.sip.communicator.impl.commportal.maxnumberfailuredoubles";

    // The following are the default values for the above
    private static final int DEFAULT_INITIAL_FAIL_BACKOFF = 1000;
    private static final int DEFAULT_MAX_NUMBER_FAILURE_DOUBLES = 10;

    private static final Logger sLog = Logger.getLogger(CommPortalBackoff.class);

    /**
     * The maximum number of times that we double the backoff.  Defaults to 10
     * which is about 20 minutes
     */
    private final int mMaxNumberFailureDoubles;

    /**
     * What the initial backoff should be after 1 failure.  Defaults to 1000 ms
     */
    private final int mInitialFailBackoff;

    /**
     * The current backoff
     */
    private long mBackoff = -1;

    /**
     * The current number of failures
     */
    private int mNumberFailures = 0;

    CommPortalBackoff()
    {
        ConfigurationService cfg = CommPortalActivator.getConfigService();


        mMaxNumberFailureDoubles = getIntFromConfig(cfg,
                                            CONFIG_MAX_NUMBER_FAILURE_DOUBLES,
                                            DEFAULT_MAX_NUMBER_FAILURE_DOUBLES);
        mInitialFailBackoff = getIntFromConfig(cfg,
                                               CONFIG_INITIAL_FAIL_BACKOFF,
                                               DEFAULT_INITIAL_FAIL_BACKOFF);
    }

    /**
     * Convenience method to get an integer from the config
     *
     * @param cfg The config object, might be null
     * @param configName The name of the config value to retrieve
     * @param defaultValue The default value
     * @return The value to use
     */
    private int getIntFromConfig(ConfigurationService cfg,
                                 String configName,
                                 int defaultValue)
    {
        return cfg == null ? defaultValue : cfg.getInt(configName, defaultValue);
    }

    /**
     * Should be called when there is an error - will increase the backoff
     */
    void onError()
    {
        sLog.debug("Error reported");

        if (mNumberFailures < mMaxNumberFailureDoubles)
        {
            mNumberFailures++;
            sLog.debug("Increasing failure count, now " + mNumberFailures);
            calcBackoff();
        }
    }

    /**
     * Called if we have successfully communicated with the EAS
     */
    void onSuccess()
    {
        sLog.debug("Success reported");
        mNumberFailures = 0;
        calcBackoff();
    }

    /**
     * Recalculate the value of the back off.
     */
    private void calcBackoff()
    {
        if (mNumberFailures == 0)
        {
            // No failures - reset the backoff
            mBackoff = -1;
        }
        else
        {
            // Some failures - set the back off to be the initial fail backoff
            // doubled for each failure
            mBackoff = mInitialFailBackoff << (mNumberFailures - 1);
        }
    }

    /**
     * @return the back off that we should wait until trying again
     */
    long getBackOffTime()
    {
        return mBackoff;
    }

    /**
     * @return true if we should be waiting before our next communication -
     *         i.e. if the back off is greater than 0
     */
    boolean shouldWait()
    {
        return (mBackoff != -1);
    }
}
