package net.java.sip.communicator.service.analytics;

public interface AnalyticsService
{
    /**
     * Called in order to log an event
     *
     * @param event the event to log
     */
    public abstract void onEvent(String event);
}
