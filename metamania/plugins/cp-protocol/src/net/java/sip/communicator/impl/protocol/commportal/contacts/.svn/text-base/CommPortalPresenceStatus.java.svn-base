package net.java.sip.communicator.impl.protocol.commportal.contacts;

import java.util.*;

import net.java.sip.communicator.impl.protocol.commportal.*;
import net.java.sip.communicator.service.protocol.*;

public class CommPortalPresenceStatus extends PresenceStatus
{
    public static final CommPortalPresenceStatus NA =
                                 new CommPortalPresenceStatus(100, "Available");

    private static ArrayList<PresenceStatus> VALUES =
                                                new ArrayList<PresenceStatus>();
    static
    {
        VALUES.add(NA);
    };

    private CommPortalPresenceStatus(int status, String statusName)
    {
        super(status,
              statusName,
              createIcon());
    }

    public static Iterator<PresenceStatus> values()
    {
        return VALUES.iterator();
    }

    /**
     * @return the icon to use for this status
     */
    private static byte[] createIcon()
    {
        CommPortalProtocolIcon icon = new CommPortalProtocolIcon();
        return icon.getIcon(CommPortalProtocolIcon.ICON_SIZE_16x16);
    }
}