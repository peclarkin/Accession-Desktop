/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.util.swing;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.jitsi.service.resources.*;
import org.jitsi.util.*;

import net.java.sip.communicator.util.*;
import net.java.sip.communicator.util.Logger;

/**
 * Implements a <tt>JDialog</tt> which allows the user to enter some text. Has
 * a cancel button which dismisses the dialog without recording the value that
 * the user entered and an OK button which dismisses the dialog and records the
 * value that was entered.
 *
 * @author Timothy Price
 */
public class SIPCommEnterTextDialog extends SIPCommDialog
{
    private static final long serialVersionUID = 1L;

    private static final Logger sLog =
                                 Logger.getLogger(SIPCommEnterTextDialog.class);

    private final ResourceManagementService mResourceService =
                                                   UtilActivator.getResources();

    /**
     * The resource of the title to display
     */
    private final String mTitleRes;

    /**
     * The resource of the text to display
     */
    private final String mTextRes;

    /**
     * The inital value to display in the text box
     */
    private final String mInitialValue;

    /**
     * True if the user cancelled the result.  Defaults to true so that if the
     * user closes the window we assume that the operation was cancelled
     */
    private boolean mCancelled = true;

    /**
     * The value entered by the user
     */
    private String mNewValue;

    /**
     * Creates an instance of the dialog
     *
     * @param titleRes The resource of the title to display
     * @param textRes The resource of the text to display
     * @param value The initial value to display in the text
     */
    public SIPCommEnterTextDialog(String titleRes,
                                  String textRes,
                                  String value)
    {
      super(false);

      mTitleRes = titleRes;
      mTextRes = textRes;
      mInitialValue = value;
      mNewValue = mInitialValue;

      init();
    }

    /**
     * Initializes this window
     */
    private void init()
    {
        initIcon();
        initContent();
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);

        if (OSUtils.IS_MAC)
        {
            sLog.debug("On mac, styling appropriately");
            getRootPane().putClientProperty("apple.awt.brushMetalLook", true);
        }
    }

    /**
     * Initializes the icon image.
     *
     * @param icon the icon to show on the left of the window
     */
    private void initIcon()
    {
        // Load the icon from the resources.
        ImageIcon icon = mResourceService.getImage(
                                     "service.gui.SIP_COMMUNICATOR_LOGO_64x64");

        // Set the layout and appearance of the icon.
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        iconLabel.setAlignmentY(Component.TOP_ALIGNMENT);

        JPanel iconPanel = new TransparentPanel(new BorderLayout());
        iconPanel.add(iconLabel, BorderLayout.NORTH);
        getContentPane().add(iconPanel, BorderLayout.WEST);
    }

    /**
     * Constructs the window and all its components.
     */
    private void initContent()
    {
        setTitle(mResourceService.getI18NString(mTitleRes));

        // Set the style of the hint text area.
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setOpaque(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(textArea.getFont().deriveFont(Font.BOLD));
        textArea.setText(mResourceService.getI18NString(mTextRes));
        textArea.setAlignmentX(0.5f);

        // Create the text entry area:
        final JTextField textField = new JTextField(mInitialValue);
        Dimension size = new Dimension(200,textField.getPreferredSize().height);
        textField.setPreferredSize(size);

        // Add listener so that we can focus on the text field when it is made
        addWindowListener(new WindowAdapter()
        {
            public void windowOpened(WindowEvent arg0)
            {
                textField.requestFocus();
                removeWindowListener(this);
            }
        });

        TransparentPanel textBoxPanel = new TransparentPanel(
                                                    new GridLayout(0, 1, 8, 8));
        textBoxPanel.add(textField);

        // Buttons
        JButton okButton =
                  new JButton(mResourceService.getI18NString("service.gui.OK"));
        JButton cancelButton =
              new JButton(mResourceService.getI18NString("service.gui.CANCEL"));
        JPanel buttonPanel = new TransparentPanel(
                                             new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        JPanel southEastPanel = new TransparentPanel(new BorderLayout());
        southEastPanel.add(buttonPanel, BorderLayout.EAST);

        // Create a main panel for the window and add the other panels to it.
        TransparentPanel mainPanel = new TransparentPanel(
                                                      new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 20));

        mainPanel.add(textArea, BorderLayout.NORTH);
        mainPanel.add(textBoxPanel, BorderLayout.CENTER);
        mainPanel.add(southEastPanel, BorderLayout.SOUTH);

        // Add the UI to the main dialog
        getContentPane().add(mainPanel, BorderLayout.CENTER);
        getRootPane().setDefaultButton(okButton);

        // Add an action listener for the buttons.
        okButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent arg0)
            {
                sLog.info("User pressed the ok button");
                mCancelled = false;
                mNewValue = textField.getText();
                SIPCommEnterTextDialog.this.setVisible(false);
            }
        });

        cancelButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                sLog.info("User pressed the cancel button");
                mCancelled = true;
                SIPCommEnterTextDialog.this.setVisible(false);
            }
        });

        // Set both buttons to be the same size (using the largest 'preferred
        // size').
        if (okButton.getPreferredSize().width >
                                          cancelButton.getPreferredSize().width)
        {
            cancelButton.setPreferredSize(okButton.getPreferredSize());
        }
        else
        {
            okButton.setPreferredSize(cancelButton.getPreferredSize());
        }
    }

    /**
     * @return true if the user cancelled the dialog
     */
    public boolean wasCancelled()
    {
        return mCancelled;
    }

    /**
     * @return the value entered by the user
     */
    public String getValueEntered()
    {
        return mNewValue;
    }
}