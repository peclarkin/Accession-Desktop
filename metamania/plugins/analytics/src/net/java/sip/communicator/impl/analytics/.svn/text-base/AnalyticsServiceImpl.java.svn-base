package net.java.sip.communicator.impl.analytics;

import java.util.*;

import net.java.sip.communicator.service.analytics.*;
import net.java.sip.communicator.util.*;

/**
 * Implementation of the analytics service
 */
public class AnalyticsServiceImpl implements AnalyticsService
{
    private static final Logger sLog =
                                   Logger.getLogger(AnalyticsServiceImpl.class);
    /**
     * The name of the config where we store the event schedule rate
     */
    private static final String CONFIG_SCHEDULE_RATE =
                        "net.java.sip.communicator.impl.analytics.schedulerate";

    /**
     * The length of time to wait before sending the events - 15 minutes
     */
    private static final long DEFAULT_SCHEDULE_RATE = 15 * 60 * 1000;

    /**
     * A list of the events that we need to send
     */
    private final Vector<AnalyticsEvent> mEvents = new Vector<AnalyticsEvent>();

    /**
     * A timer task which schedules sending of analytics data
     */
    private final Timer mTimer;

    AnalyticsServiceImpl()
    {
        String url = AnalyticsActivator.getAnalyticsUrl();

        if (url == null || url.length() == 0)
        {
            // We've got no URL so don't actually do anything
            sLog.info("No analytics url");
            mTimer = null;
        }
        else
        {
            mTimer = new Timer();
            long scheduleRate = AnalyticsActivator.getConfigService()
                          .getLong(CONFIG_SCHEDULE_RATE, DEFAULT_SCHEDULE_RATE);
            mTimer.scheduleAtFixedRate(new AnalyticsTask(this),
                                       scheduleRate,
                                       scheduleRate);
        }
    }

    public void onEvent(String event)
    {
        if (mTimer != null)
        {
            mEvents.add(new AnalyticsEvent(event));
        }
    }

    /**
     * Stop the Analytics service
     */
    void stop()
    {
        if (mTimer != null)
        {
            mEvents.add(new AnalyticsEvent("Application being shut down"));

            // Force the currently queued events to be sent
            new AnalyticsTask(this).run();

            mTimer.cancel();
        }
    }

    /**
     * @return a list of the events that need to be sent
     */
    Vector<AnalyticsEvent> getEventsToSend()
    {
        Vector<AnalyticsEvent> events = new Vector<AnalyticsEvent>(mEvents);
        mEvents.clear();

        return events;
    }

    /**
     * Re-add the events to the list of events to send - called when we've
     * removed some events in order to send them but not been able to.
     *
     * @param events The events to re-add
     */
    void reAddEvents(Vector<AnalyticsEvent> events)
    {
        mEvents.addAll(events);
    }
}
