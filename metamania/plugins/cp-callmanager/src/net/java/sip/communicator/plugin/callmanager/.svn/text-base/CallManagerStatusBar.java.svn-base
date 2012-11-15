package net.java.sip.communicator.plugin.callmanager;

import java.awt.Dialog.ModalityType;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import net.java.sip.communicator.plugin.callmanager.SimpleICM.SimpleICMProfile;
import net.java.sip.communicator.service.protocol.globalstatus.GlobalStatusEnum;
import net.java.sip.communicator.service.protocol.globalstatus.GlobalStatusService;
import net.java.sip.communicator.service.systray.SystrayService;
import net.java.sip.communicator.util.GuiUtils;
import net.java.sip.communicator.util.Logger;
import net.java.sip.communicator.util.UtilActivator;
import net.java.sip.communicator.util.swing.*;
import net.java.sip.communicator.util.swing.plaf.SIPCommStatusMenuUI;

import org.jitsi.service.resources.ResourceManagementService;

/**
 * The status bar that indicates the phone status
 */
public class CallManagerStatusBar extends SIPCommMenu implements ActionListener
{
    private static final long serialVersionUID = 1L;
    private static final Logger sLog = Logger.getLogger(CallManagerStatusBar.class);
    private final CallManagerDataHandler mDataHandler;

    /**
     * Class id key used in UIDefaults.
     */
    private static final String CLASS_ID =
             CallManagerStatusBar.class.getName() +  "CallManangerStatusMenuUI";

    /**
     * Adds the ui class to UIDefaults.
     */
    static
    {
        UIManager.getDefaults().put(CLASS_ID, SIPCommStatusMenuUI.class.getName());
    }

    /**
     * The data we are currently working with
     */
    private SimpleICM mSimpleICM;

    private final ResourceManagementService mResourceService =
                                                   UtilActivator.getResources();

    /**
     * The width of the text.
     */
    private int mTextWidth = 0;

    /**
     * The indent of the image.
     */
    private static final int IMAGE_INDENT = 10;

    /**
     * The arrow icon shown on the right of the status and indicating that
     * this is a menu.
     */
    private final Image mArrowIcon;

    public CallManagerStatusBar()
    {
        // First create an object for getting / setting data.
        mDataHandler = new CallManagerDataHandler(this);
        mArrowIcon = mResourceService.getImage("service.gui.icons.DOWN_ARROW_ICON")
                                     .getImage();

        // Set up the menu button
        updateMenuButton(false);
        String title = getText("plugin.callmanager.title");
        setIconTextGap(2);
        setOpaque(false);
        setToolTipText("<html><b>" + title + "</b></html>");

        // Set up the menu items
        JLabel titleLabel = new JLabel(title);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
        setFont(titleLabel.getFont().deriveFont(Font.PLAIN, 11f));

        add(titleLabel);
        addSeparator();

        // Add an item for each state
        for (CallManagerStatesEnum status : CallManagerStatesEnum.values())
        {
            createMenuItem(status);
        }

        fitSizeToText();
    }

    /**
     * Creates and adds a menu item for the passed in status type.
     *
     * @param status the status we are interested in
     */
    private void createMenuItem(CallManagerStatesEnum status)
    {
        ImageIcon image = mResourceService.getImage(status.getStatusIconRes());
        String lable = getText(status.getStatusNameRes());
        JMenuItem menuItem = new JMenuItem(lable, image);

        menuItem.setName(status.toString());
        menuItem.addActionListener(this);

        add(menuItem);
    }

    /**
     * Handles the <tt>ActionEvent</tt> triggered when one of the items
     * in the list is selected.
     * @param event the <tt>ActionEvent</tt> that notified us
     */
    public void actionPerformed(ActionEvent event)
    {
        JMenuItem menuItem = (JMenuItem)event.getSource();

        // The name is one of the CallManagerStatesEnum values which match the
        // SimpleICM names
        String itemName = menuItem.getName();
        sLog.info("User changing ECM status to " + itemName);
        SimpleICMProfile profileType = SimpleICMProfile.valueOf(itemName);
        boolean sendChange = true;

        if (profileType == SimpleICMProfile.FORWARD)
        {
            // Ask the user for the forwarding number
            String newForwardNumber = getForwardNumber();

            if (newForwardNumber != null)
            {
                sLog.info("User set forward number");
                mSimpleICM.mForward = newForwardNumber;
            }
            else
            {
                // User cancelled so we don't want to send anything to the
                // server or change the value or in fact do anything.
                sLog.info("User cancelled forward dialog");
                sendChange = false;
            }
        }

        if (sendChange)
        {
            sLog.debug("Sending change to user");
            mSimpleICM.mProfile = profileType;

            // Update the currently set item in the list and global store.
            menuItem.setSelected(true);
            updateGlobalStatus();

            // Finally send the changed data
            mDataHandler.sendChangedData(mSimpleICM);
        }
    }

    /**
     * Show a dialog to ask the user to enter a forwarding number
     *
     * @return the forwarding number or null if the dialog was cancelled.
     */
    private String getForwardNumber()
    {
        String newForwardNumber = null;

        do
        {
            sLog.debug("Asking for the forward number");

            // Need to ask for the forwarding number
            SIPCommEnterTextDialog forwardDialog =
                  new SIPCommEnterTextDialog("plugin.callmanager.forward.TITLE",
                                             "plugin.callmanager.forward.TEXT",
                                             mSimpleICM.mForward);
            forwardDialog.setModalityType(ModalityType.APPLICATION_MODAL);
            forwardDialog.setVisible(true);

            // Because the dialog is modal, the thread will block until the user
            // dismisses or accepts the dialog
            if (forwardDialog.wasCancelled())
            {
                sLog.info("Forward dialog was cancelled");
                break;
            }
            else
            {
                // Get the number entered and remove any decorative characters.
                // Note that we still need to check that the user has entered a
                // valid number. If the number is not valid then they will have
                // to re-enter it correctly.  We can't just remove the invalid
                // characters.
                newForwardNumber = forwardDialog.getValueEntered()
                                             .replaceAll("[\\s\\(\\)\\-]+", "");
                sLog.info("User entered a forward number " + newForwardNumber);

                if (!isForwardNumberValid(newForwardNumber))
                {
                    sLog.debug("New forward number is not valid");
                    String title = mResourceService.getI18NString(
                                                           "service.gui.ERROR");
                    String message = mResourceService.getI18NString(
                                   "plugin.callmanager.forward.INVALID_NUMBER");
                    ErrorDialog errorDialog = new ErrorDialog(null,
                                                              title,
                                                              message);
                    errorDialog.setModalityType(ModalityType.APPLICATION_MODAL);
                    errorDialog.setVisible(true);

                    // And null out the forwarding number so that we keep going
                    newForwardNumber = null;
                }
            }
        } while (newForwardNumber == null);

        return newForwardNumber;
    }

    /**
     * Check to see if the forward number is a valid number
     *
     * @param forwardNumber the forwarding number
     * @return true if the forward number is valid
     */
    private boolean isForwardNumberValid(String forwardNumber)
    {
        // Number must not be null or the empty string.  It must be made only of
        // digits, '#' and '*'. Note that '+' is not a valid digit as CommPortal
        // does not accept it.
        return forwardNumber != null &&
               forwardNumber.length() > 0 &&
               forwardNumber.matches("^[\\d#\\*]+$");
    }

    /**
     * Tell the rest of the world the status of this phone
     */
    private void updateGlobalStatus()
    {
        sLog.debug("updateGlobalStatus");
        SystrayService systray = CallManagerActivator.getSystrayService();

        if (systray != null)
        {
            boolean isAvailable =
                              (getTopLevelStatus() == SimpleICMProfile.DEFAULT);
            int imageType = isAvailable ? SystrayService.SC_IMG_TYPE :
                                          SystrayService.SC_IMG_OFFLINE_TYPE;

            systray.setSystrayIcon(imageType);
        }
    }

    /**
     * Convenience method for getting the top level profile status.  If either
     * of the stored ICM config, or the profile are null, then assumes the
     * profile is available
     *
     * @return the top level status
     */
    private SimpleICM.SimpleICMProfile getTopLevelStatus()
    {
        boolean profileUnknown = (mSimpleICM == null) ||
                                 (mSimpleICM.mProfile == null);

        return profileUnknown ? SimpleICMProfile.DEFAULT : mSimpleICM.mProfile;
    }

    /**
     * Computes the width of the text in pixels in order to position the arrow
     * during its painting.
     */
    private void fitSizeToText()
    {
        String text = getText();

        mTextWidth = (text == null) ? 0 : GuiUtils.getStringWidth(this, text);
        int arrowWidth = (mArrowIcon == null) ? 0 : mArrowIcon.getWidth(null);
        setPreferredSize(new Dimension(
                             mTextWidth + 2*IMAGE_INDENT + arrowWidth + 5, 20));
    }

    /**
     * Called when we failed to get the data that we need
     *
     * @param lastGoodData The last known good data (may be null)
     */
    void failedToGetData(SimpleICM lastGoodData)
    {
        // Update the UI, forcing it to be disabled
        sLog.debug("Failed to get any data");
        mSimpleICM = lastGoodData;
        updateMenuButton(true);
    }

    /**
     * Called when we failed to send the ECM data
     *
     * @param lastGoodData The last known good data (may be null)
     */
    void failedToSendData(SimpleICM lastGoodData)
    {
        sLog.debug("Failed to send data");

        // Show a pop-up informing that we failed to change the data
        String text = getText("plugin.callmanager.SEND_FAILED_TEXT");
        String title = getText("plugin.callmanager.SEND_FAILED_TITLE");
        ErrorDialog dialog = new ErrorDialog(null, title, text);
        dialog.setModal(true);
        dialog.showDialog();

        // Change back to the last known good data
        mSimpleICM = lastGoodData;
        updateMenuButton(false);
    }

    /**
     * Called when the ECM data has been received
     *
     * @param newIcmConfig The new data as received from the server
     */
    void onDataReceived(SimpleICM newIcmConfig)
    {
        sLog.debug("Data received from server");
        mSimpleICM = newIcmConfig;
        updateMenuButton(false);
    }

    /**
     * Update the top level UI to represent the current ECM status.  Guesses if
     * this is not known.
     *
     * @param forceDisableUI If true, UI will be disabled.  Otherwise, UI will
     *                       be disabled only if we have no ECM data
     */
    private void updateMenuButton(final boolean forceDisableUI)
    {
        sLog.debug("Updating menu button");

        // UI changes need to run on the UI thread:
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                CallManagerStatesEnum status =
                             CallManagerStatesEnum.convert(getTopLevelStatus());
                setText(getText(status.getStatusNameRes()));
                setIcon(mResourceService.getImage(status.getStatusIconRes()));
                fitSizeToText();

                // We are updating the menu button - global state needs updating
                updateGlobalStatus();

                // Finally, disable the UI if there is no data as it indicates
                // no data connection
                boolean dataAvailable = (mSimpleICM != null) &&
                                        (mSimpleICM.mProfile != null);
                setEnabled(!forceDisableUI && dataAvailable);
            }
        });

    }

    /**
     * Convenience method for getting a text string from resources
     *
     * @param res The resource of the string
     * @return the string
     */
    private String getText(String res)
    {
        return mResourceService.getI18NString(res);
    }

    /**
     * Overwrites the <tt>paintComponent(Graphics g)</tt> method in order to
     * provide a new look and the mouse moves over this component.
     * @param g the <tt>Graphics</tt> object used for painting
     */
    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        if (mTextWidth != 0)
        {
            g = g.create();
            try
            {
                AntialiasingManager.activateAntialiasing(g);

                int x = mTextWidth + 2*IMAGE_INDENT + 2;
                int y = getX() + (getHeight() - mArrowIcon.getHeight(null)) / 2 + 1;
                g.drawImage(mArrowIcon, x, y, null);
            }
            finally
            {
                g.dispose();
            }
        }
    }

    /**
     * Returns the name of the L&F class that renders this component.
     *
     * @return the string "TreeUI"
     * @see JComponent#getUIClassID
     * @see UIDefaults#getUI
     */
    @Override
    public String getUIClassID()
    {
        return CLASS_ID;
    }

    /**
     * Called in order to shut down this component
     */
    void stop()
    {
        sLog.debug("Stopping this component");
        mDataHandler.stop();
    }
}
