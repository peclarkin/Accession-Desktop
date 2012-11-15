// ErrorReportDialog.java
// (C) COPYRIGHT METASWITCH NETWORKS 2012
package net.java.sip.communicator.plugin.errorreport;

import java.awt.*;
import java.awt.Dialog.ModalityType;
import java.awt.event.*;

import javax.swing.*;

import net.java.sip.communicator.util.*;
import net.java.sip.communicator.util.swing.*;

import org.jitsi.service.resources.*;

/**
 * Error report dialog - asks user for an explanation and then starts the zip
 * process
 */
public class ErrorReportDialog
{
    /**
     * The string to place at the beginning of the explanation.  If null then
     * no preamble is created
     */
    private final String mPreambleResource;

    /**
     * The reason that we are creating this
     */
    private final String mCause;

    /**
     * If true then we will display the cause of the exception in the dialog
     */
    private final boolean mShowCause;

    /**
     * The maximum width that we allow message dialogs to have.
     */
    private static final int MAX_MSG_PANE_WIDTH = 450;

    /**
     * The maximum height that we allow message dialogs to have.
     */
    private static final int MAX_MSG_PANE_HEIGHT = 800;

    private final ResourceManagementService mResourceService =
                                                   UtilActivator.getResources();

    private static final Logger sLog =
                           Logger.getLogger(ErrorReportMenuItemComponent.class);

    /**
     * Create an error report dialog with no text already present in the input
     * field when it is first displayed.
     *
     * @param preambleResource The resource of the string to display as
     *                         an explanation
     * @param cause The reason this pop-up is created.
     */
    public ErrorReportDialog(String preambleResource, String cause)
    {
        mPreambleResource = preambleResource;
        mCause = cause;
        mShowCause = false;
    }

    /**
     * Create an error report dialog with a cause and the option to display
     * that cause
     *
     * @param cause The reason we are showing this error report dialog
     * @param showCause If true then the reason should be displayed
     */
    public ErrorReportDialog(String cause, boolean showCause)
    {
        mPreambleResource = null;
        mCause = cause;
        mShowCause = showCause;
    }

    /**
     * Shows the dialog for the user to report an error
     */
    void showDialog()
    {
        final String windowTitle = mResourceService.getI18NString(
            "plugin.errorreport.REPORT_AN_ISSUE");
        sLog.logEntry();
        final JDialog dialog = new JDialog(null,
                                           windowTitle,
                                           ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Set up the hint displayer.
        JEditorPane text = new JEditorPane();
        text.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
        text.setEditable(false);
        text.setOpaque(false);
        text.setContentType("text/html");
        text.setText(createHint());
        text.setForeground(Color.WHITE);
        text.setAlignmentX(Component.CENTER_ALIGNMENT);
        text.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        // JEditorPanes expect themselves to be infinitely wide by default,
        // and so have a default height of one row.
        // Thus to get them to correctly calculate their preferred size, we set
        // the size to be maximally high but constrained in width, and then
        // recalculate the preferred size.
        // It looks very odd, but it works.
        text.setSize(
            new Dimension(MAX_MSG_PANE_WIDTH, MAX_MSG_PANE_HEIGHT));
        text.setPreferredSize(
            new Dimension(
                    MAX_MSG_PANE_WIDTH,
                    text.getPreferredSize().height));

        // Set up the text entry area. In a scroll pane so that if the user
        // enters a large amount of text, they can see it all.
        String inputText = mShowCause ? mCause : "";
        final NoTabTextArea entry = new NoTabTextArea(inputText, 4, 32);
        entry.setEditable(true);
        entry.setLineWrap(true);
        entry.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(entry);
        scrollPane
          .setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 30));
        scrollPane.setOpaque(false);

        // Create a pane to hold the buttons.
        JPanel buttonsPane = new JPanel();

        JButton cancelButton =
              new JButton(mResourceService.getI18NString("service.gui.CANCEL"));
        cancelButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent xiArg0)
            {
                // Just dismiss the dialog.
                dialog.setVisible(false);
                sLog.info("User dismissing dialog without sending report");
            }
        });

        JButton sendButton =
                  new JButton(mResourceService.getI18NString("service.gui.SEND"));
        sendButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent xiArg0)
            {
                sLog.info("User submitting report");
                dialog.setVisible(false);

                // Start the zip generator:
                String userMessage = entry.getText() + "\n\n" + mCause;
                new ErrorReportZipper(userMessage).start();
            }
        });

        buttonsPane.add(sendButton);
        buttonsPane.add(cancelButton);
        buttonsPane.setOpaque(false);
        buttonsPane.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add the contents to the pane
        JPanel mainContents = new JPanel();
        mainContents.setLayout(new BoxLayout(mainContents, BoxLayout.Y_AXIS));
        mainContents.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        mainContents.setOpaque(false);

        mainContents.add(text, BorderLayout.CENTER);
        mainContents.add(scrollPane, BorderLayout.SOUTH);
        mainContents.setAlignmentY(Component.TOP_ALIGNMENT);

        // Show the app image on the left of the screen
        JLabel imageLabel = new JLabel();
        ImageIcon image =
           mResourceService.getImage("service.gui.SIP_COMMUNICATOR_LOGO_64x64");
        imageLabel.setIcon(image);
        imageLabel.setAlignmentY(Component.TOP_ALIGNMENT);

        // Put the logo and the main contents in a panel, and create an overall
        // panel holding that and the buttons.
        // (The reason for this is that the buttons should be center-aligned
        // with respect to the whole window, not just the mainContents.)
        JPanel topContents = new TransparentPanel();
        topContents.setLayout(new BoxLayout(topContents, BoxLayout.X_AXIS));
        topContents.add(imageLabel);
        topContents.add(mainContents);
        JPanel allContents = new JPanel();
        allContents.setLayout(new BoxLayout(allContents, BoxLayout.Y_AXIS));
        allContents.setBackground(new Color(98, 141, 217));
        allContents.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        allContents.add(topContents);
        allContents.add(buttonsPane);

        // And setup the dialog
        dialog.setLayout(new BoxLayout(dialog.getContentPane(), BoxLayout.Y_AXIS));
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(null);
        dialog.setIconImage(image.getImage());
        dialog.add(allContents, BorderLayout.CENTER);
        dialog.pack();
        entry.requestFocusInWindow();
        dialog.setVisible(true);
        dialog.pack();
    }

    /**
     * @return The hint to display at the top of the dialog
     */
    private String createHint()
    {
        String hintPreamble = "";
        if (mPreambleResource != null)
        {
            hintPreamble = mResourceService.getI18NString(mPreambleResource);
        }

        String hint =
               mResourceService.getI18NString("plugin.errorreport.POP_UP_HINT");

        return "<html><body>" + hintPreamble + hint + "</body></html>";
    }

    public static int getContentHeight(String content, int width) {
        JTextArea dummyEditorPane=new JTextArea();
        dummyEditorPane.setSize(width, Short.MAX_VALUE);
        dummyEditorPane.setText(content);

        return dummyEditorPane.getPreferredSize().height;
    }

    /**
     * NoTabTextArea is exactly like a JTextArea except that pressing Tab
     * changes focus to the next GUI element instead of typing \t.
     *
     */
    private class NoTabTextArea extends JTextArea
    {
        public NoTabTextArea(String text, int rows, int cols)
        {
            super(text, rows, cols);
        }

        protected void processComponentKeyEvent( KeyEvent e ) {
            if ( e.getKeyCode() == KeyEvent.VK_TAB ) {
                transferFocus();
                e.consume();
            }
            else {
                super.processComponentKeyEvent( e );
            }
        }
    }
}
