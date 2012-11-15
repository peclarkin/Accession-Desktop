package net.java.sip.communicator.impl.analytics;

import org.json.*;

/**
 * Class for containing an Analytics event
 */
class AnalyticsEvent
{
    /**
     * The event name
     */
    private final String mEvent;

    /**
     * The time at which the event happened
     */
    private final long mTimestamp;

    AnalyticsEvent(String event)
    {
        mEvent = event;
        mTimestamp = System.currentTimeMillis();
    }

    /**
     * @return this object encoded in JSON
     * @throws JSONException
     */
    JSONObject toJSON() throws JSONException
    {
        JSONObject object = new JSONObject();
        object.put("name", mEvent);
        object.put("timestamp", mTimestamp);

        return object;
    }

}
