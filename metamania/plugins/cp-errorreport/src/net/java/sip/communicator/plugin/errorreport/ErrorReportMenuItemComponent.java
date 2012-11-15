package net.java.sip.communicator.plugin.errorreport;

import java.awt.event.*;

import javax.swing.*;

import net.java.sip.communicator.service.gui.*;
import net.java.sip.communicator.util.*;

import org.jitsi.service.resources.*;

/**
 * Plugin which creates a menu item to facilitate sending error reports
 */
public class ErrorReportMenuItemComponent extends AbstractPluginComponent
    implements ActionListener
{
    private static final Logger sLogger =
                           Logger.getLogger(ErrorReportMenuItemComponent.class);

    private final ResourceManagementService mResourceService =
                                                   UtilActivator.getResources();

    /**
     * The "Send error report" menu item.
     */
    private JMenuItem mErrorReportMenuItem;

    protected ErrorReportMenuItemComponent(Container container)
    {
        super(container);
    }

    public String getName()
    {
        return null;
    }

    public Object getComponent()
    {
        if (mErrorReportMenuItem == null)
        {
            String menuTitle = mResourceService
                           .getI18NString("plugin.errorreport.REPORT_AN_ISSUE");
            mErrorReportMenuItem = new JMenuItem(menuTitle);
            mErrorReportMenuItem.addActionListener(this);
        }

        return mErrorReportMenuItem;
    }

    public void actionPerformed(ActionEvent event)
    {
        String preambleRes = "plugin.errorreport.POP_UP_PREAMBLE_MANUAL_REPORT";
        sLogger.logEntry();
        new ErrorReportDialog(preambleRes, "User prompted").showDialog();
        sLogger.logExit();
    }
}
