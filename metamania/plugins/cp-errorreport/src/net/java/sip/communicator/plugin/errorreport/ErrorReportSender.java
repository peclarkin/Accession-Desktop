// ErrorReportSender.java
// (C) COPYRIGHT METASWITCH NETWORKS 2012
package net.java.sip.communicator.plugin.errorreport;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.java.sip.communicator.service.commportal.CPDataError;
import net.java.sip.communicator.service.commportal.CPFileUploadCallback;
import net.java.sip.communicator.service.commportal.CommPortalService;
import net.java.sip.communicator.util.Logger;
import net.java.sip.communicator.util.ServiceUtils;

import org.jitsi.service.configuration.ConfigurationService;
import org.osgi.framework.BundleContext;

/**
 * Class for checking that zip files exist and, if so, uploading them to
 * CommPortal.
 */
public class ErrorReportSender
    implements Runnable
{
    private static final Logger sLogger = Logger
        .getLogger(ErrorReportSender.class);

    /**
     * The property containing the length of time we should wait for between
     * thread cycles. The value of the property should be in milliseconds.
     */
    private static final String PROPERTY_REPORT_SENDER_WAIT_TIME =
        "plugin.errorreport.REPORT_SENDER_WAIT_TIME";

    /**
     * The property containing the maximum number of report zips that we
     * store at once.
     */
    private static final String PROPERTY_REPORT_SENDER_MAX_REPORTS =
        "plugin.errorreport.REPORT_SENDER_MAX_REPORTS";

    /**
     * The time we should wait for between cycles in the absence of a configured
     * value. Given in milliseconds. (802200L is 13.37 mins).
     */
    private static final long DEFAULT_WAIT_TIME = 802200L;

    /**
     * The default maximum for the number of zip files we store at once.
     */
    private static final int DEFAULT_MAX_REPORTS = 5;

    /**
     * The length of time we should wait for between thread cycles. Given in
     * milliseconds.
     */
    private long threadWaitTime;

    /**
     * The maximum number of zip files we store at once.
     */
    private int maxReports;

    private final CommPortalService commPortalService;

    public ErrorReportSender(BundleContext context)
    {
        super();
        setUpParameters();

        commPortalService = ServiceUtils.getService(context,
                                                    CommPortalService.class);
    }

    public void run()
    {
        sLogger.debug("ErrorReportSender thread main loop starts.");

        boolean firstCycle = true;

        while (true)
        {
            // If this is the first cycle ever, we want to upload immediately.
            // This block is at the start so that we can 'continue' if something
            // goes wrong.
            if (!firstCycle)
            {
                try
                {
                    sLogger.debug("ErrorReportSender going to sleep.");
                    Thread.sleep(threadWaitTime);
                }
                catch (InterruptedException iex)
                {
                    // If we're woken up early, it's an error but go ahead and
                    // // upload anyway.
                    sLogger.error("ErrorReportSender woken up prematurely.",
                        iex);
                }

            }

            // Now look for error reports.
            firstCycle = false;
            sLogger.debug("Checking for error reports to send...");
            String homeDirPath;
            try
            {
                homeDirPath = getHomeDirectory();
            }
            catch (IOException ioex)
            {
                sLogger.error("Could not obtain home directory", ioex);
                continue;
            }
            ArrayList<String> zipFiles = getReportZipFiles(homeDirPath);

            if (zipFiles.size() > 0)
            {
                sLogger.debug("Found " + zipFiles.size() + " error reports.");

                // If there are too many zip files, delete the oldest ones
                // and remove them from zipList.
                boolean success = deleteOldZipFiles(zipFiles);
                if (!success)
                {
                    sLogger.error("Could not delete old zip files."
                        + " Skipping upload.");
                    continue;
                }

                // Now upload each file in turn to the server.
                for (String fileName : zipFiles)
                {
                    final File file = new File(fileName);
                    CPFileUploadCallback callback = new CPFileUploadCallback()
                    {
                        public void onUploadSuccess()
                        {
                            sLogger.debug("Uploaded correctly - deleting");
                            deleteFile(file);
                        }

                        public void onDataFailure(CPDataError error)
                        {
                            // Something went wrong with the upload. Just
                            // delete the file
                            sLogger.error("Something wrong with file, deleting");
                            deleteFile(file);
                        }

                        public String getUploadLocation()
                        {
                            return "/line/logupload?filename=" + file.getName();
                        }

                        public File getFile()
                        {
                            return file;
                        }
                    };

                    commPortalService.uploadFile(callback,
                                                 null,
                                                 false);
                }
            }
            else
            {
                sLogger.debug("No error reports found.");
            }
        }
    }

    /**
     * If we succeeded in our upload, or failed in such a way as that we never
     * expect to succeed, delete the zip file from the disk.
     *
     * @param file The file to delete
     */
    private void deleteFile(final File file)
    {
        boolean deleteSuccess = file.delete();
        if (!deleteSuccess)
        {
            sLogger.error("Could not delete file "
                + file.getName() + ".");
        }
    }

    /**
     * Given a List of paths to zip files, delete the oldest among
     * them until we have <= maxReports left.
     * We expect the ArrayList to be sorted lexicographically, so the
     * files will be in decreasing order of age.
     * @param zipFiles The ArrayList<String> of paths to zip files.
     * @return A success or failure boolean.
     */
    private boolean deleteOldZipFiles(List<String> zipFiles)
    {
        boolean success = true;

        if (zipFiles.size() <= maxReports)
        {
            sLogger.debug("No old error reports to delete.");
        }

        while (zipFiles.size() > maxReports)
        {
            // Try to delete the file.
            // If we hit an error, skip the upload.
            File file = new File(zipFiles.get(0));
            sLogger.debug("Deleting " + zipFiles.get(0));
            success = file.delete();

            if (!success)
            {
                sLogger.error("Could not delete old report file "
                    + file.getName());
                break;
            }
            else
            {
                zipFiles.remove(0);
            }
        }

        return success;
    }

    /**
     * Gets the application home folder, where we go to look for .zip files.
     *
     * @return The directory path, as a string.
     */
    private String getHomeDirectory() throws IOException
    {
        String path = null;
        String homeLocation =
            System
                .getProperty("net.java.sip.communicator.SC_HOME_DIR_LOCATION");
        String dirName =
            System.getProperty("net.java.sip.communicator.SC_HOME_DIR_NAME");
        if (homeLocation != null && dirName != null)
        {
            path = homeLocation + "/" + dirName + "/";
        }
        else
        {
            throw new IOException("Could not obtain home directory path.");
        }
        return path;
    }

    /**
     * Pull the configurable parameters from config, or else use the default
     * values.
     */
    private void setUpParameters()
    {
        ConfigurationService cs = ErrorReportActivator.getConfigurationService();

        // The time to sleep between cycles of the main thread (in ms).
        threadWaitTime = cs.getLong(PROPERTY_REPORT_SENDER_WAIT_TIME,
            DEFAULT_WAIT_TIME);

        // The number of report files we allow to queue at any one time.
        maxReports = cs.getInt(PROPERTY_REPORT_SENDER_MAX_REPORTS,
            DEFAULT_MAX_REPORTS);
    }

    /**
     * Gets all zip files of the form "report-*.zip" in the given directory.
     *
     * @param dirPath The path to the directory to look in, as a string.
     * @return An ArrayList<String> of absolute file addresses.
     */
    private ArrayList<String> getReportZipFiles(String dirPath)
    {
        // Look in the directory for all matching zip files
        // and store them in an ArrayList.
        File dir = new File(dirPath);
        String[] dirContents = dir.list();
        ArrayList<String> reportZipsList = new ArrayList<String>();
        for (int ii = 0; ii < dirContents.length; ii++)
        {
            String file = dirContents[ii];
            if (file.endsWith(".zip") && file.startsWith("report-"))
            {
                reportZipsList.add(dirPath + "/" + file);
            }
        }

        // Sort the ArrayList lexicographically.
        // This has the effect of ordering the files oldest->youngest
        // since the files are datestamped yymmdd-hhmmss in the file name.
        Collections.sort(reportZipsList);

        return reportZipsList;
    }
}
