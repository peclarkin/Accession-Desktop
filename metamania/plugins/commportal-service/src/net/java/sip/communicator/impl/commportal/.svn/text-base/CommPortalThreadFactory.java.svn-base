package net.java.sip.communicator.impl.commportal;

import net.java.sip.communicator.service.commportal.*;

import org.apache.http.client.*;

/**
 * A simple class for creating CommPortal threads
 */
public class CommPortalThreadFactory
{
    /**
     * Create and start a CommPortal work thread
     *
     * @param commPortalService The CommPortal service which wants the thread
     * @param isForeground if this thread should be created for the foreground
     * @return the new thread
     */
    CommPortalWorkThread createWorker(CommPortalServiceImpl commPortalService,
                                      boolean isForeground)
    {
        CommPortalWorkThread worker =
                      new CommPortalWorkThread(commPortalService, isForeground);
        worker.start();

        return worker;
    }

    /**
     * Create and start a CommPortal COMET thread
     *
     * @param httpClient http client used by the thread
     * @param callback the first callback for which we are created
     * @param networkCallback the network callback for which we are created
     * @param commPortalServiceImpl the service which wants the thread
     * @return the new thread, started
     */
    public CommPortalCometThread createCometWorker(HttpClient httpClient,
                                                   CPDataRegistrationCallback callback,
                                                   CPOnNetworkErrorCallback networkCallback,
                                                   CommPortalServiceImpl cpService)
    {
        CommPortalCometThread worker = new CommPortalCometThread(httpClient,
                                                                 callback,
                                                                 networkCallback,
                                                                 cpService);
        worker.start();

        return worker;
    }

}
