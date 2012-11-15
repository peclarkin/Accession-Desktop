package net.java.sip.communicator.plugin.contactdetails;

import net.java.sip.communicator.util.*;
/**
 * Menu item for showing a 'View Contact' window with details about the
 * currently selected contact
 */
public class ViewContactMenuItem extends ContactDetailsMenuItem
{
    private static final Logger sLogger = Logger.getLogger(
                                                    ViewContactMenuItem.class);

    /**
     * Create a menu item in the contact right-click-menu, which opens a 'View
     * Contact' window for the current contact when clicked.
     */
    public ViewContactMenuItem()
    {
        super("plugin.contactdetails.VIEW_MENU_ITEM");
    }

    @Override
    public ContactDetailsWindow openWindow()
    {
        // Create a 'View Contact' window for the currently selected contact
        // and show it
        sLogger.info("Opening 'View Contact' window for " +
                     mMetaContact.getDisplayName());
        return new ContactDetailsWindow(mMetaContact, true);
    }
}
