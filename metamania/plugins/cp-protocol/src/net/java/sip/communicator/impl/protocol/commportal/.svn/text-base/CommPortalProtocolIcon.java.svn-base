package net.java.sip.communicator.impl.protocol.commportal;

import java.io.*;
import java.util.*;

import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.util.*;

/**
 * Implementation of the Protocol Icon for the CommPortal Protocol Provider
 */
public class CommPortalProtocolIcon implements ProtocolIcon
{
    private static final Logger sLog =
                                 Logger.getLogger(CommPortalProtocolIcon.class);

    /**
     * A hash table from the size of the icon, to the icon to use for that size
     */
    private static Hashtable<String, String> SUPPORTED_ICONS
                                              = new Hashtable<String, String>();

    static
    {
        // Note that the 16*16 icon is stored under "SIP_COMMUNICATOR_LOGO"
        SUPPORTED_ICONS.put(ProtocolIcon.ICON_SIZE_16x16,
                            "service.gui.SIP_COMMUNICATOR_LOGO");
        SUPPORTED_ICONS.put(ProtocolIcon.ICON_SIZE_32x32,
                            "service.gui.SIP_COMMUNICATOR_LOGO_32x32");
        SUPPORTED_ICONS.put(ProtocolIcon.ICON_SIZE_48x48,
                            "service.gui.SIP_COMMUNICATOR_LOGO_48x48");
        SUPPORTED_ICONS.put(ProtocolIcon.ICON_SIZE_64x64,
                            "service.gui.SIP_COMMUNICATOR_LOGO_64x64");
    }

    public Iterator<String> getSupportedSizes()
    {
        return SUPPORTED_ICONS.keySet().iterator();
    }

    public boolean isSizeSupported(String iconSize)
    {
        return SUPPORTED_ICONS.containsKey(iconSize);
    }

    public byte[] getIcon(String iconSize)
    {
        if (!isSizeSupported(iconSize))
        {
            sLog.error("Being asked for unsupported icon image");
            return new byte[0];
        }

        String imageID = SUPPORTED_ICONS.get(iconSize);
        InputStream in = CommPortalProtocolActivator.getResourceService()
                                                  .getImageInputStream(imageID);
        byte[] image = null;

        if (in != null)
        {
            try
            {
                image = new byte[in.available()];

                in.read(image);
            }
            catch (IOException e)
            {
                sLog.error("Failed to load image: " + imageID, e);
            }
        }

        return image;
    }

    public String getIconPath(String iconSize)
    {
        return CommPortalProtocolActivator.getResourceService().
                                    getImagePath(SUPPORTED_ICONS.get(iconSize));
    }

    public byte[] getConnectingIcon()
    {
        // Just use the medium icon for this
        return getIcon(ProtocolIcon.ICON_SIZE_48x48);
    }

}
