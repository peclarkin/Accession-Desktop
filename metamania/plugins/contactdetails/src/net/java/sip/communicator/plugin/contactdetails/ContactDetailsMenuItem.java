package net.java.sip.communicator.plugin.contactdetails;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import net.java.sip.communicator.service.contactlist.*;
import net.java.sip.communicator.service.gui.*;
import net.java.sip.communicator.service.gui.Container;

/**
 * Abstract class for menu items that open contact detail windows.
 */
abstract public class ContactDetailsMenuItem extends AbstractPluginComponent
    implements ActionListener
{
    /**
     * The text of the menu item
     */
    protected String fLabel;

    /**
     * The currently selected metacontact
     */
    protected MetaContact mMetaContact;

    private JMenuItem mMenuItem;

    /**
     * Abstract contact details menu item in the contact right-click-menu.
     */
    public ContactDetailsMenuItem(String resourceKey)
    {
        super(Container.CONTAINER_CONTACT_RIGHT_BUTTON_MENU);
        fLabel = Resources.getString(resourceKey);
    }

    @Override
    public void setCurrentContact(MetaContact metaContact)
    {
        mMetaContact = metaContact;
    }

    public void actionPerformed(ActionEvent event)
    {
        // Create a new window
        ContactDetailsWindow window = openWindow();

        // Show the window in the middle of the screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    }

    /**
     * Creates and fills a contact details window.
     * @return the contact details window
     */
    protected abstract ContactDetailsWindow openWindow();

    public Object getComponent()
    {
        mMenuItem = new JMenuItem(fLabel);
        mMenuItem.addActionListener(this);
        return mMenuItem;
    }

    public String getName()
    {
        return mMenuItem.getText();
    }
}