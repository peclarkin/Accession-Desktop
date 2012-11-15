package net.java.sip.communicator.plugin.errorreport;

import java.io.*;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.*;

import net.java.sip.communicator.service.gui.*;
import net.java.sip.communicator.util.*;

import org.jitsi.service.configuration.*;
import org.osgi.framework.*;

public class ErrorReportActivator
    implements BundleActivator, UncaughtExceptionHandler
{
    /**
     * The resource for the preamble to the crash dialog.
     * e.g. "Looks like Accession crashed last time it ran".
     */
    private static final String PREAMBLE_RES =
        "plugin.errorreport.POP_UP_PREAMBLE_CRASH_LAST_TIME";

    /**
     * The name of the property which holds whether the program is running.
     * This should be set to false when the program is closed, so if it's
     * true on the next run we know we had a crash.
     */
    private static final String PROPERTY_PROGRAM_IS_RUNNING =
        "plugin.errorreport.PROGRAM_IS_RUNNING";

    /**
     * Debug mode determines whether we should send crash error reports silently
     * or show a pop-up containing the stack trace.
     */
    static final String PROPERTY_DEBUG_MODE =
        "net.java.sip.communicator.DEBUG_MODE";

    /**
     * If true, then we should create a pop-up for all error reports.  Otherwise
     * uncaught exceptions will be silently sent
     */
    private boolean debugMode;

    private static final Logger sLogger =
                                   Logger.getLogger(ErrorReportActivator.class);

    private static BundleContext bundleContext;

    private static ConfigurationService configurationService;

    /**
     * The original exception handler for the application.
     */
    private static UncaughtExceptionHandler sOriginalExceptionHandler;

    public void start(BundleContext context) throws Exception
    {
        sLogger.logEntry();
        bundleContext = context;
        getConfigurationService(context);

        debugMode = configurationService.getBoolean(PROPERTY_DEBUG_MODE, false);

        // Add the send report item to the help menu
        ErrorReportMenuItemComponent menuItem =
            new ErrorReportMenuItemComponent(Container.CONTAINER_HELP_MENU);

        Hashtable<String, String> helpMenuFilter =
            new Hashtable<String, String>();
        helpMenuFilter.put(Container.CONTAINER_ID,
                           Container.CONTAINER_HELP_MENU.getID());

        context.registerService(PluginComponent.class.getName(), menuItem,
            helpMenuFilter);

        // Update the uncaught exception handler
        if (sOriginalExceptionHandler == null)
        {
            sOriginalExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
            Thread.setDefaultUncaughtExceptionHandler(this);
        }

        // If the last run of the program crashed, prepare an error report.
        // Otherwise, record that the program is now running, so that
        // if we crash we will notice next time we run.
        boolean crashedLastTime = configurationService.getBoolean(
            PROPERTY_PROGRAM_IS_RUNNING,
            false);
        if (crashedLastTime)
        {
            sLogger.error("=== Creating log for previous crash ===");

            new ErrorReportDialog(PREAMBLE_RES, "Previous crash").showDialog();
        }
        else
        {
            sLogger.debug("Previous run appears to have shut down correctly.");
            configurationService.setProperty(PROPERTY_PROGRAM_IS_RUNNING,
                true);
        }

        // Create and start the sender thread
        new Thread(new ErrorReportSender(bundleContext)).start();

        sLogger.logExit();

    }

    public void stop(BundleContext arg0) throws Exception
    {
        sLogger.debug("Plugin stopping");

        // Restore the original exception handler
        if (sOriginalExceptionHandler != null)
        {
            Thread.setDefaultUncaughtExceptionHandler(sOriginalExceptionHandler);
        }

        // Record correct shutdown so that we don't detect a crash on next run.
        sLogger.debug("Recording that we've shut down correctly.");
        configurationService.setProperty(PROPERTY_PROGRAM_IS_RUNNING, false);
    }

    /**
     * Our own handler for dealing with uncaught exceptions.
     */
    public void uncaughtException(Thread thread, Throwable exception)
    {
        sLogger.error("Uncaught exception hit: " + exception
            + " Sending error report.");

        try
        {
            // Get the stack trace from the exception to include in the error
            // report.
            StringWriter sw = new StringWriter();
            exception.printStackTrace(new PrintWriter(sw));
            String errorMessage = "Uncaught exception\n\n" + sw.toString();

            if (!debugMode)
            {
                // As we're not in debug mode, we don't need to show the
                // uncaught exception dialog so we can just zip up the error
                // report and send it silently.
                new ErrorReportZipper(errorMessage).start();
            }
            else
            {
                // We're in debug mode, so show the uncaught exception dialog
                // to ask whether we should send the error report.
                new ErrorReportDialog(errorMessage, true).showDialog();
            }
        }
        catch (Throwable e)
        {
            // Do nothing - we don't want to throw any exceptions from this
            // method.
        }

        sLogger.debug("Passing exception to the exception handler.");
        // Allow the original handler to deal with the exception
        sOriginalExceptionHandler.uncaughtException(thread, exception);
    }

    /**
     * Returns a reference to a ConfigurationService implementation currently
     * registered in the bundle context or null if no such implementation was
     * found.
     *
     * @return a currently valid implementation of the ConfigurationService.
     */
    public static ConfigurationService getConfigurationService(
        BundleContext context)
    {
        if (configurationService == null)
        {
            ServiceReference confReference =
                context.getServiceReference(ConfigurationService.class
                    .getName());
            configurationService =
                (ConfigurationService) context.getService(confReference);
        }
        return configurationService;
    }

    public static ConfigurationService getConfigurationService()
    {
        return getConfigurationService(bundleContext);
    }

}
