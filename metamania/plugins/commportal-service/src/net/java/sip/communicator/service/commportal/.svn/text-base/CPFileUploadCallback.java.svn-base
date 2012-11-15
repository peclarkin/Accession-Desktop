package net.java.sip.communicator.service.commportal;

import java.io.File;

/**
 * Defines a callback for uploading files to CP
 */
public interface CPFileUploadCallback
{
    /**
     * @return the file to be uploaded
     */
    public abstract File getFile();

    /**
     * @return the server location to upload the file to (e.g. "/upload/file.zip")
     */
    public abstract String getUploadLocation();

    /**
     * Called when we have successfully uploaded the file
     */
    public abstract void onUploadSuccess();

    /**
     * Called if we fail to upload the file.
     *
     * @param error the reason why we failed to upload the file
     */
    public abstract void onDataFailure(CPDataError error);
}
