package net.java.sip.communicator.impl.commportal;

import java.util.concurrent.*;

import net.java.sip.communicator.util.*;

/**
 * A thread which does the work as defined by a list of WorkItems
 *
 * @see net.java.sip.communicator.impl.WorkItem
 */
public class CommPortalWorkThread extends Thread
{
    private final Logger mLog;

    /**
     * The instance of the CommPortalService that created us - allows access to
     * the session id and list of work.
     */
    private final CommPortalServiceImpl mCommPortalService;

    /**
     * Whether we are the foreground work thread or not
     */
    private final boolean mIsForeground;

    /**
     * While true, we will keep running
     */
    private boolean mKeepGoing = true;

    /**
     * While true, we will pause execution of this thread
     */
    private boolean mPause = false;

    CommPortalWorkThread(CommPortalServiceImpl commPortalService,
                         boolean isForeground)
    {
        mLog = Logger.getLogger("CommPortalWorkThread " +
                                  (isForeground ? "Foreground" : "Background"));

        mCommPortalService = commPortalService;
        mIsForeground = isForeground;

        mLog.debug("Creating CommPortal work thread");
    }

    @Override
    public void run()
    {
        while (mKeepGoing)
        {
            CopyOnWriteArrayList<WorkItem> workList =
                                  mCommPortalService.getWorkList(mIsForeground);

            try
            {
                synchronized (this)
                {
                    if (workList.isEmpty())
                    {
                        // We've got no work to do - just wait until we have
                        // some to do
                        mLog.debug("Waiting as no work to do");
                        wait(0);
                    }
                    else if (mPause)
                    {
                        mLog.debug("Waiting as told to pause");
                        wait(0);
                    }
                    else if (mCommPortalService.getBackoff().shouldWait())
                    {
                        // We are waiting because there is a network or EAS issue
                        mLog.debug("Waiting as network or EAS problems");
                        wait(mCommPortalService.getBackoff().getBackOffTime());
                    }
                }
            }
            catch (InterruptedException e)
            {
                mLog.info("Interrupted while waiting", e);
            }

            if (!workList.isEmpty())
            {
                // The work list may be empty because we were paused with an
                // empty list and told to un-pause.
                WorkItem work = workList.get(0);
                boolean remove = work.doWork();
                mLog.debug("Done work");

                if (remove)
                {
                    mLog.info("Completed work item, removing it");
                    workList.remove(work);
                }
            }
        }
    }

    /**
     * Empty the work list and close this thread down
     */
    void closeThread()
    {
        mLog.info("Closing the thread");
        mCommPortalService.getWorkList(mIsForeground).clear();
        mKeepGoing = false;

        synchronized (this)
        {
            notify();
        }
    }

    /**
     * Wake the thread up after it has been told to pause.  Does nothing if we
     * are not currently paused
     */
    void wake()
    {
        mLog.debug("Told to wake");

        if (mPause)
        {
            mPause = false;

            synchronized (this)
            {
                notify();
            }
        }
    }

    /**
     * Tell the thread to pause once it has completed its current work item
     */
    void pause()
    {
        mLog.debug("Told to pause");
        mPause = true;
    }
}
