// CDAPWindow.java
// (C) COPYRIGHT METASWITCH NETWORKS 2012
package net.java.sip.communicator.plugin.cdap;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import net.java.sip.communicator.util.*;
import net.java.sip.communicator.util.Logger;
import net.java.sip.communicator.util.swing.*;

import org.jitsi.service.resources.*;
import org.jitsi.util.*;

/**
 * A window where the user can select his/her service provider from a list.
 */
public class CDAPWindow extends SIPCommFrame implements ActionListener
{
  private static final long serialVersionUID = 1L;
  private static final Logger logger = Logger.getLogger(CDAPWindow.class);

  /** Name of the default service provider. */
  private static final String DEFAULT_PROVIDER = "EMEA PBX";

  /** The ResourceManagementService, for getting resources from file. */
  private final ResourceManagementService mResources =
                                                  UtilActivator.getResources();

  /** A lock used to synchronize data setting. */
  private final Object fLock = new Object();

  /** Whether the window has been cancelled. */
  private boolean mIsCanceled = false;

  /** The condition that decides whether to continue waiting for data. */
  private boolean mWindowIsBlocking = true;

  /** The drop down selection box containing service providers. */
  private JComboBox mProviderList;

  /**
   * Creates an instance of the CDAPWindow.
   */
  protected CDAPWindow(
                      HashMap<String, ServiceProviderDetails> serviceProviders)
  {
    super(false);
    logger.logEntry();
    init(serviceProviders);
    logger.logExit();
  }

  /**
   * Initializes this CDAPWindow.
   *
   * @param serviceProviders - a HashMap of service provider names and details.
   */
  private void init(HashMap<String, ServiceProviderDetails> serviceProviders)
  {
    logger.logEntry();
    initContent(serviceProviders);
    this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    this.setResizable(false);

    /*
     * Workaround for the following bug:
     * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4446522
     * Need to pack() the window after it's opened in order to obtain the
     * correct size of our infoTextArea, otherwise window size is wrong and
     * buttons on the south are cut.
     */
    this.addWindowListener(new WindowAdapter()
    {
      public void windowOpened(WindowEvent e)
      {
        logger.logEntry();
        pack();
        removeWindowListener(this);
        logger.logExit();
      }
    });

    if (OSUtils.IS_MAC)
    {
      logger.debug("Setting brush metal style for Macs.");
      getRootPane().putClientProperty("apple.awt.brushMetalLook",
                                      Boolean.TRUE);
    }

    logger.logExit();
  }

  /**
   * Initializes the icon image.
   *
   * @param icon the icon to show on the left of the window
   */
  private void initIcon()
  {
    logger.logEntry();
    // Load the icon from the resources.
    ImageIcon icon = mResources.getImage(
                                    "service.gui.SIP_COMMUNICATOR_LOGO_64x64");

    // Set the layout and appearance of the icon.
    JLabel iconLabel = new JLabel(icon);
    iconLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    iconLabel.setAlignmentY(Component.TOP_ALIGNMENT);
    JPanel iconPanel = new TransparentPanel(new BorderLayout());
    iconPanel.add(iconLabel, BorderLayout.NORTH);
    getContentPane().add(iconPanel, BorderLayout.WEST);
    logger.logExit();
  }

  /**
   * Constructs the window and all its components.
   *
   * @param serviceProviders - a HashMap of service provider names and details.
   */
  private void initContent(HashMap<String, ServiceProviderDetails> serviceProviders)
  {
    logger.logEntry();
    setTitle(mResources.getI18NString("plugin.cdap.selection.TITLE"));
    initIcon();

    // Set the style of the text area.
    JTextArea textArea = new JTextArea();
    textArea.setEditable(false);
    textArea.setOpaque(false);
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);
    textArea.setFont(textArea.getFont().deriveFont(Font.BOLD));
    textArea.setText(mResources.getI18NString("plugin.cdap.selection.TEXT"));
    textArea.setAlignmentX(0.5f);

    // Create an array of service provider names for the selection drop-down.
    Object[] serviceProviderNames = serviceProviders.keySet().toArray();
    int index = 0;

    // Find the service provider with the default name, and use the index to
    // set it as the default selection for the drop down.
    for (index = 0; index < serviceProviderNames.length; index++)
    {
      if (DEFAULT_PROVIDER.equalsIgnoreCase(
                                       serviceProviderNames[index].toString()))
      {
        // Stop searching - this is the index of the default service provider.
        break;
      }
    }

    // Create the selection drop-down, set the default and add it to a panel.
    mProviderList = new JComboBox(serviceProviderNames);
    mProviderList.setSelectedIndex(index);
    TransparentPanel comboBoxPanel = new TransparentPanel(
                                                   new GridLayout(0, 1, 8, 8));
    comboBoxPanel.add(mProviderList);

    // Create the 'OK' and 'cancel' buttons and add them to a panel.
    JButton okButton = new JButton(
                             mResources.getI18NString("service.gui.OK"));
    JButton cancelButton = new JButton(
                         mResources.getI18NString("service.gui.CANCEL"));
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
    mainPanel.add(comboBoxPanel, BorderLayout.CENTER);
    mainPanel.add(southEastPanel, BorderLayout.SOUTH);
    getContentPane().add(mainPanel, BorderLayout.EAST);

    // Set the names and hotkeys for the buttons, and the default button.
    okButton.setName(mResources.getI18NString("service.gui.OK"));
    cancelButton.setName(mResources.getI18NString("service.gui.CANCEL"));
    okButton.setMnemonic(mResources.getI18nMnemonic("service.gui.OK"));
    cancelButton.setMnemonic(mResources.getI18nMnemonic("service.gui.CANCEL"));
    getRootPane().setDefaultButton(okButton);

    // Add this class as an action listener for the buttons.
    okButton.addActionListener(this);
    cancelButton.addActionListener(this);

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

    logger.logExit();
  }

  /**
   * Handles the ActionEvent triggered when one of the buttons is
   * clicked.
   *
   * @param event - the action event that has just occurred.
   */
  public void actionPerformed(ActionEvent event)
  {
    logger.logEntry();
    JButton button = (JButton)event.getSource();
    String buttonName = button.getName();

    if (mResources.getI18NString("service.gui.CANCEL").equals(buttonName))
    {
      // Mark the window as having been cancelled.
      logger.debug("'Cancel' button pressed.");
      mIsCanceled = true;
    }

    exitWindowAndContinue();
    logger.logExit();
  }

  /**
   * Releases the process and closes the window.
   */
  private void exitWindowAndContinue()
  {
    logger.logEntry();
    mWindowIsBlocking = false;

    synchronized (fLock)
    {
      fLock.notify();
    }

    dispose();
    logger.logExit();
  }

  /**
   * Marks the window as cancelled and closes the window.
   *
   * @param isEscaped - whether the window has been closed by pressing the
   *                    Escape key
   */
  @Override
  protected void close(boolean isEscaped)
  {
    logger.logEntry();
    mIsCanceled = true;
    exitWindowAndContinue();
    logger.logEntry();
  }

  /**
   * Shows this modal dialog.
   *
   * @param isVisible - whether the window should be hidden or made visible
   */
  @Override
  public void setVisible(final boolean isVisible)
  {
    logger.logEntry();
    super.setVisible(isVisible);

    if (isVisible)
    {
      // Make this window blocking until the user interaction releases it
      // (i.e. mWindowIsBlocking is made false)
      synchronized (fLock)
      {
        while(mWindowIsBlocking)
        {
          try
          {
            fLock.wait();
          }
          catch (InterruptedException e)
          {
            // Just retry.
          }
        }
      }
    }

    logger.logExit();
  }

  /**
   * Indicates whether this window has been cancelled.
   *
   * @return true if this window has been cancelled, and false if not
   */
  protected boolean isCanceled()
  {
    logger.logEntry();
    logger.logExit();
    return mIsCanceled;
  }

  /**
   * @return the service provider selected by the user
   */
  protected String getServiceProvider()
  {
    logger.logEntry();
    logger.logExit();
    return mProviderList.getSelectedItem().toString();
  }
}