package net.java.sip.communicator.plugin.contactdetails;

import net.java.sip.communicator.util.*;
/**
 * Menu item for showing an 'Edit Contact' window with details about the
 * currently selected contact
 */
public class EditContactMenuItem extends ContactDetailsMenuItem
{
    private static final Logger sLogger = Logger.getLogger(
                                                    EditContactMenuItem.class);

    /**
     * Create a menu item in the contact right-click-menu, which opens an
     * 'Edit Contact' window for the current contact when clicked.
     */
    public EditContactMenuItem()
    {
        super("plugin.contactdetails.EDIT_MENU_ITEM");
    }

    @Override
    public ContactDetailsWindow openWindow()
    {
        // Create an 'Edit Contact' window for the currently selected contact
        // and show it
        sLogger.info("Opening 'Edit Contact' window for " +
                     mMetaContact.getDisplayName());
        return new ContactDetailsWindow(mMetaContact, false);
    }
}
