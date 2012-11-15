package net.java.sip.communicator.plugin.errorreport;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import net.java.sip.communicator.util.Logger;
import net.java.sip.communicator.util.swing.SwingWorker;
import net.java.sip.communicator.plugin.loggingutils.*;

/**
 * A class to zip up the logs
 */
public class ErrorReportZipper extends SwingWorker
{
    private final static Logger sLogger =
                                      Logger.getLogger(ErrorReportZipper.class);

    /**
     * The text entered by the user
     */
    private final String mUserText;

    /**
     * The name of the folder in which logs are stored.
     */
    private final static String mLogFolderName =  "log";

    /**
     * The name of the configuration file.
     */
    private final static String mConfigFileName = "sip-communicator.properties";

    /**
     * Create an error report zipper.
     *
     * @param userMessage The error message entered by the user
     */
    ErrorReportZipper(String userMessage)
    {
        super();
        mUserText = userMessage;
    }

    /**
     * Write the user's error text to file, and zip that along with the log
     * files and the config file into a .zip archive.
     */
    @Override
    public Object construct()
    {
        sLogger.info("Beginning report creation process.");
        boolean abort = false;

        // Work out the home directory, and any info we need
        // to put in the file name.
        String homeDirPath = null;
        try
        {
            homeDirPath = getHomeDirectory();
        }
        catch (IOException e)
        {
            sLogger.error("Failed to find the application home directory" +
                          "- aborting report creation.", e);
            abort = true;
        }

        String reportFileName = null;
        String reportFileAddress = null;
        String fileStamp = null;
        String clientVersionNumber = System.getProperty("sip-communicator.version");
        String userOS = System.getProperty("os.name");
        String username = System.getProperty("user.name");
        String usefulInfo = null;
        PrintWriter out = null;
        if (!abort)
        {
            fileStamp = getDateStamp(); // @@SMK@@ Need the directory number too.
            sLogger.debug("Home directory found to be " + homeDirPath);
            sLogger.debug("Generated file stamp " + fileStamp);

            // Write the error report text to a file.
            reportFileName = "report-" + fileStamp + ".txt";
            reportFileAddress = homeDirPath + "/" + reportFileName;
            usefulInfo = "Client Version Number: " + clientVersionNumber +
                 "\nUser OS: " + userOS + "\nUsername: " + username;
            try
            {
                out = new PrintWriter(
                    new FileWriter(reportFileAddress));
                out.println(mUserText);
                out.println(usefulInfo);
            }
            catch (IOException ioex)
            {
                sLogger.error("Could not create user report text file." +
                    " Aborting report creation.", ioex);
                abort = true;
                // Don't need to close PrintWriter as the exception will be in
                // its creation.
            }
        }

        if (!abort)
        {
            // Close the PrintWriter for the report text file.
            out.close();

            // Now build the report zip file.
            String[] inputFiles = new String[]{reportFileName, mConfigFileName};
            String outFileName = "report-" + fileStamp + ".zip";
            try
            {
                buildZipFile(outFileName, inputFiles, mLogFolderName, homeDirPath);
            }
            catch (IOException ioex)
            {
                sLogger.error("Could not create report zip file." +
                    " Aborting report creation.",
                    ioex);
                abort = true;
            }
        }

        if (!abort)
        {
            // Finally, delete the report text file.
            File reportFile = new File(reportFileAddress);
            boolean success = reportFile.delete();
            if (!success)
            {
                sLogger.error("Failed to delete leftover report text file "
                    + reportFileName);
            }
        }

        if (!abort)
        {
            sLogger.info("Report created successfully.");
            return true;
        }
        else
        {
            sLogger.error("Report was not created successfully.");
            return false;
        }
    }

    /**
     * Zips up the given files and the log folder.
     *
     * @param outFileName The desired name of the zip archive.
     * @param inputFiles A list of file addresses to zip.
     * @param logFolderName The folder containing the logs we want to zip.
     * @param path The path to the project root directory.
     * @throws IOException
     */
    private void buildZipFile(String outFileName,
                              String[] filenames,
                              String logFolderName,
                              String path)
        throws IOException
    {
        // Build the zip output stream.
        // We call the zip archive '*.zip.tmp' at first
        // because we don't want the ErrorReportSender to upload
        // any half-finished zips.
        sLogger.debug("Creating zip archive " + outFileName + ".tmp");
        String outFileAddress = path + "/" + outFileName;
        FileOutputStream fout = new FileOutputStream(outFileAddress + ".tmp");
        ZipOutputStream zout = new ZipOutputStream(new BufferedOutputStream(fout));

        try
        {
            // Zip up all the files in 'filenames'.
            sLogger.debug("Zipping non-log files...");
            addFilesToZip(zout, filenames, path);

            // Zip up the contents of the log folder,
            // excluding any .lck files.
            String logFolderAddress = path + File.separator + logFolderName;
            File logFolder = new File(logFolderAddress);
            String[] logDirContents = logFolder.list();
            ArrayList<String> logFilesList = new ArrayList<String>();
            for (int ii = 0; ii < logDirContents.length; ii++)
            {
                if (!logDirContents[ii].endsWith(".lck"))
                {
                    logFilesList.add(logDirContents[ii]);
                }
            }
            String[] logFiles = logFilesList.toArray(new String[0]);
            addFilesToZip(zout, logFiles, logFolderAddress);

            // Add the java crash dumps to the archive.
            LogsCollector.collectJavaCrashLogs(zout);
        }
        catch (IOException ioex)
        {
            // We need to close zout before the calling method sees this exception.
            zout.close();
            throw ioex;
        }

        zout.close();

        // Rename the archive to '*.zip' instead of '*.zip.tmp'.
        File zipTmpFile = new File(outFileAddress + ".tmp");
        File zipFile = new File(outFileAddress);
        boolean success = zipTmpFile.renameTo(zipFile);
        if (!success)
        {
            // We couldn't rename the file, so raise an exception.
            throw new IOException("Could not rename the temp zip file " +
                                    zipTmpFile.getName() +
                                    " to " + zipFile.getName() + ".");

        }
    }

    /**
     * Adds the given files to a specified zip archive.
     * @param zout The ZipOutputStream to write to.
     * @param filenames The names of the files to add to the archive.
     * @param path The path to the directory containing the files.
     * @throws IOException
     */
    private void addFilesToZip(ZipOutputStream zout,
                               String[] filenames,
                               String path)
        throws IOException
    {
        // Set up the input buffer.
        final int BUFFER_SIZE = 1024;
        byte data[] = new byte[BUFFER_SIZE];

        // Loop through the files and zip each one.
        for (String filename : filenames)
        {
            sLogger.debug("Zipping file " + filename);
            String fileAddr = path + File.separator + filename;

            // Get an input stream for the file.
            FileInputStream fin = new FileInputStream(fileAddr);

            // Add an entry to the zip archive for the file.
            ZipEntry zipEntry = new ZipEntry(filename);
            zipEntry.setTime(new File(fileAddr).lastModified());
            zout.putNextEntry(zipEntry);

            // Read the file in and write it to the zip.
            int count;
            while ((count = fin.read(data)) > 0)
            {
                zout.write(data, 0, count);
            }

            zout.closeEntry();
            fin.close();
        }
    }

    /**
     * Gets the application home folder, which is where we should be
     * writing the .zip file.
     * @return The directory path, as a string.
     */
    private String getHomeDirectory() throws IOException
    {
        String path = null;
        String homeLocation = System.getProperty(
            "net.java.sip.communicator.SC_HOME_DIR_LOCATION");
        String dirName = System.getProperty(
            "net.java.sip.communicator.SC_HOME_DIR_NAME");
        if(homeLocation != null && dirName != null)
        {
            path = homeLocation + File.separator + dirName + File.separator;
        }
        else
        {
            throw new IOException("Could not find the application home directory.");
        }
        return path;
    }

    /**
     * Gets a date stamp for naming the zip file.
     * The date stamp is of the form yymmdd-hhmmss.
     *
     * @return The date stamp, as a string.
     */
    private String getDateStamp()
    {
        DateFormat dateFormat = new SimpleDateFormat("yyMMdd-HHmmss");
        Date date = new Date();
        return dateFormat.format(date);
    }

}
