package net.java.sip.communicator.plugin.callmanager;

import net.java.sip.communicator.plugin.callmanager.SimpleICM.SimpleICMProfile;

/**
 * A enum containing all the states that the ECM state can be in.  Also serves
 * as a convenient mapping from that state to certain UI elements
 */
public enum CallManagerStatesEnum
{
    DEFAULT("plugin.callmanager.status.AVAILABLE",
            "service.gui.statusicons.USER_ONLINE_ICON"),

    DND("plugin.callmanager.status.DND",
        "service.gui.statusicons.USER_DND_ICON"),

    FORWARD("plugin.callmanager.status.FORWARDING",
            "service.gui.statusicons.USER_FORWARD_ICON");

    private final String mStatusNameRes;
    private final String mStatusIconRes;

    private CallManagerStatesEnum(String statusNameRes, String statusIconRes)
    {
        mStatusNameRes = statusNameRes;
        mStatusIconRes = statusIconRes;
    }

    public String getStatusNameRes()
    {
        return mStatusNameRes;
    }

    public String getStatusIconRes()
    {
        return mStatusIconRes;
    }

    /**
     * Convert the SimpleICMProfile object into a CallManagerStatesEnum object.
     * The two are essentially identical, however the CallManagerStatesEnum has
     * knowledge of UI where as the SimpleICMProfile object is a straight port
     * from a javascript object.
     *
     * @param profile The profile to convert
     * @return The converted profile
     */
    public static CallManagerStatesEnum convert(SimpleICMProfile profile)
    {
        switch (profile)
        {
            case DND:
                return DND;
            case FORWARD:
                return FORWARD;
            default:
            case DEFAULT:
                return DEFAULT;
        }
    }
}
