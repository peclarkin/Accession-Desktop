package net.java.sip.communicator.impl.commportal;

import net.java.sip.communicator.util.Logger;

import org.jitsi.service.configuration.ConfigurationService;

/**
 * A class for storing and handling the intricacies of the COMET timeout to use
 * <p/>
 * Note that all units used by this class are in milliseconds
 */
class CommPortalCometTimeout
{
    private static final Logger sLog = Logger.getLogger(CommPortalCometTimeout.class);

    // The following is a list of places where config for this class is stored:
    private static final String CONFIG_COMET_INITIAL_POLL =
                  "net.java.sip.communicator.impl.commportal.comet.initialpoll";
    private static final String CONFIG_COMET_INCREMENT =
                  "net.java.sip.communicator.impl.commportal.comet.increment";
    private static final String CONFIG_COMET_DECREMENT =
                  "net.java.sip.communicator.impl.commportal.comet.decrement";
    private static final String CONFIG_COMET_MAX_POLL =
                  "net.java.sip.communicator.impl.commportal.comet.maxpoll";
    private static final String CONFIG_COMET_MIN_POLL =
                  "net.java.sip.communicator.impl.commportal.comet.minpoll";
    private static final String CONFIG_COMET_GROWTH_PAUSE =
                  "net.java.sip.communicator.impl.commportal.comet.growthpause";

    // The following are the default values for all of the above - all units in
    // milliseconds
    private static final int DEFAULT_COMET_INITIAL_POLL = 120000;  // 2 minutes
    private static final int DEFAULT_COMET_INCREMENT    = 30000;   // 30 seconds
    private static final int DEFAULT_COMET_DECREMENT    = 10000;   // 10 seconds
    private static final int DEFAULT_COMET_MAX_POLL     = 890000;  // ~15 minutes
    private static final int DEFAULT_COMET_MIN_POLL     = 60000;   // 1 minute
    private static final int DEFAULT_COMET_GROWTH_PAUSE = 3600000; // 1 hour

    /** COMET Poll time discovery - initial poll value */
    private final int mInitialPoll;

    /** COMET Poll time discovery - poll increment
        The amount to increase the poll by if we suspect the server can
        cope. */
    private final int mIncrement;

    /** COMET Poll time discovery - poll decrement
        The amount to decrease the poll by if the previous poll failed */
    private final int mDecrement;

    /** COMET Poll time discovery - maximum poll period
        The maximum length that a poll can be increased to. */
    private final int mMaxPoll;

    /** COMET Poll time discovery - minimum poll period
        The minimum length that a poll can be decreased to */
    private final int mMinPoll;

    /** COMET Poll time discovery - how long we prevent the poll from growth
        for after an initial socket timeout */
    private final int mGrowthPause;

    /**
     * The last time that we hit a timeout
     */
    private long mLastTimeoutTime = 0;

    /**
     * Tracks the number of consecutive times that we have had to reduce the
     * timeout.  If this gets too large (larger than the time increase) then
     * we re-set the timeout to the initial value
     */
    private int mConsecDec;

    /**
     * The current timeout
     */
    private int mRequestTimeout;

    CommPortalCometTimeout()
    {
        ConfigurationService cfg = CommPortalActivator.getConfigService();

        // Set up the parameters we need
        mInitialPoll = getIntFromConfig(cfg, CONFIG_COMET_INITIAL_POLL, DEFAULT_COMET_INITIAL_POLL);
        mIncrement   = getIntFromConfig(cfg, CONFIG_COMET_INCREMENT,    DEFAULT_COMET_INCREMENT);
        mDecrement   = getIntFromConfig(cfg, CONFIG_COMET_DECREMENT,    DEFAULT_COMET_DECREMENT);
        mMaxPoll     = getIntFromConfig(cfg, CONFIG_COMET_MAX_POLL,     DEFAULT_COMET_MAX_POLL);
        mMinPoll     = getIntFromConfig(cfg, CONFIG_COMET_MIN_POLL,     DEFAULT_COMET_MIN_POLL);
        mGrowthPause = getIntFromConfig(cfg, CONFIG_COMET_GROWTH_PAUSE, DEFAULT_COMET_GROWTH_PAUSE);

        mRequestTimeout = mInitialPoll;
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
     * What timeout should we use for COMET requests?
     * @return The timeout in milliseconds
     */
    long getTimeout()
    {
        return mRequestTimeout;
    }

    /**
     * Called when a COMET request hits a socket timeout.
     */
    void onCometSocketTimeout()
    {
        // Typically we can build long timeouts on WiFi that are too big for
        // mobile data.  It can then take a long time to drop back to sensible
        // values for mobile data (say 2 minutes as opposed to 30) as we need
        // to go through a large number of consecutive timeouts which each decrement
        // only a small amount.
        //
        // We do need to decrement a small amount when in one network so we can find
        // the optimal level.
        //
        // Possible solutions are either to have an accelerating decrement, or
        // to be notified when the network changes so we can switch back to the
        // initial position.  Our caller could do that by creating a new instance
        // of this class.
        //
        // We've chosen to spot when we've whittled away a whole increment with
        // decrements, and use that to reset to the initial poll value.
        mConsecDec += mDecrement;
        mRequestTimeout -= mDecrement;
        sLog.debug("onSocketTimeout, decrementing to " + mRequestTimeout);

        if (mRequestTimeout < mMinPoll)
        {
            sLog.debug("Already at minimum poll time, restricting to " + mMinPoll);
            mRequestTimeout = mMinPoll;
        }
        else if ((mConsecDec >= mIncrement) &&
                 (mRequestTimeout >= mInitialPoll))
        {
            sLog.info("reset to initial poll time");
            mRequestTimeout = mInitialPoll;
            mConsecDec = 0;
        }

        mLastTimeoutTime = System.currentTimeMillis();
    }

    /**
     * Called when a COMET request hits a server-side timeout.  This indicates
     * that the network can cope with the current poll value. If we haven't hit
     * a socket timeout for a while we will therefore try a larger poll next
     * time.
     */
    void onServerTimeout()
    {
        // We increment the poll time for the next COMET poll only if we haven't
        // hit a socket timeout for a while
        sLog.debug("Server timeout " + System.currentTimeMillis() +
            " " + mRequestTimeout +
            " " + mLastTimeoutTime +
            ", will start growing again at: " + mGrowthPause);
        mConsecDec = 0;

        if (System.currentTimeMillis() > (mLastTimeoutTime + mGrowthPause))
        {
            mRequestTimeout += mIncrement;
            sLog.debug("Growing the poll time to " + mRequestTimeout);

            if (mRequestTimeout > mMaxPoll)
            {
                sLog.debug("Limiting poll to " + mMaxPoll);
                mRequestTimeout = mMaxPoll;
            }
        }
    }
}
