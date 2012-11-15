package net.java.sip.communicator.plugin.contactdetails;

import java.awt.*;

import net.java.sip.communicator.util.*;

/**
 * Handles access of internationalised strings and image resources by this
 * plugin
 */
public class Resources
{
    /**
     * Returns the resource string specified by the key
     *
     * @param key   The resource key
     * @return   The internationalised string specified by the resource key
     */
    public static String getString(String key)
    {
        return UtilActivator.getResources().getI18NString(key);
    }

    /**
     * Gets the resource char specified by the key
     *
     * @param key   The resource key
     * @return   The mnemonic char from an internationalised string specified
     * by the resource key
     */
    public static char getMnemonic(String key)
    {
        return UtilActivator.getResources().getI18nMnemonic(key);
    }

    /**
     * Loads an image from a given image identifier
     *
     * @param imageID   The resource key
     * @return   The image specified by the resource key
     */
    public static Image getImage(String imageID)
    {
        return UtilActivator.getResources().getImage(imageID).getImage();
    }
}