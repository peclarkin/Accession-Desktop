package net.java.sip.communicator.plugin.nativebrowser;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

import net.java.sip.communicator.service.browserlauncher.BrowserLauncherService;
import net.java.sip.communicator.util.Logger;
import net.java.sip.communicator.util.UtilActivator;
import net.java.sip.communicator.util.swing.SIPCommButton;

import org.jitsi.service.resources.ResourceManagementService;
import org.jitsi.util.OSUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * Button that opens a new window displaying a native browser web page
 */
public class NativeBrowserButton extends SIPCommButton implements ActionListener
{
    private static final long serialVersionUID = 1L;
    private final Logger logger = Logger.getLogger(NativeBrowserButton.class);

    private final ResourceManagementService mResourceService =
                                                   UtilActivator.getResources();

    private JFrame mBrowserWindow = null;
    private final BundleContext mContext;

    /**
     * The URL of the page to display
     */
    private final String mUrl;
    private BrowserLauncherService mBrowserLauncherService;

    public NativeBrowserButton(BundleContext context,
                               String url,
                               String imageResource,
                               String toolTipTextResource)
    {
        logger.logEntry();
        mContext = context;
        mUrl = url;

        // Set the look of the button:
        ImageIcon image = mResourceService.getImage(imageResource);
        setBackgroundImage(image.getImage());
        this.setPreferredSize(new Dimension(image.getIconWidth() + 2,
                                            image.getIconHeight()));
        setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));

        String toolTipText = mResourceService.getI18NString(toolTipTextResource);
        setToolTipText(toolTipText);

        // And the onclick action
        addActionListener(this);
        logger.logExit();
    }

    public void actionPerformed(ActionEvent event)
    {
        logger.logEntry();

        // When the button is clicked, create the browser window unless it
        // already exists - we don't want multiple windows being created
        if (!OSUtils.IS_WINDOWS)
        {
            logger.info("Asked to show browser but not on windows");

            // Note that Mac's don't support the native browser so we open the
            // URL in the native browser
            getBrowserLauncher().openURL(mUrl);
        }
        else if (mBrowserWindow == null)
        {
            logger.info("Asked to show browser window - creating it");
            String title = mResourceService.
                                    getI18NString("plugin.nativebrowser.TITLE");
            mBrowserWindow = new JFrame(title);
            mBrowserWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            NativeBrowserPanel webView = new NativeBrowserPanel(mUrl);
            logger.info(String.format("Opening URL: %s", mUrl));

            // Set the size to be such that CommPortal web displays nicely
            webView.setPreferredSize(new Dimension(580, 429));
            mBrowserWindow.add(webView);
            mBrowserWindow.pack();
            mBrowserWindow.setVisible(true);
            mBrowserWindow.setLocationRelativeTo(null);
            mBrowserWindow.setResizable(false);

            ImageIcon image = mResourceService.
                            getImage("service.gui.SIP_COMMUNICATOR_LOGO_64x64");
            mBrowserWindow.setIconImage(image.getImage());

            // Listen for the window being dismissed so that we can
            // create a new one next tme the button is clicked
            mBrowserWindow.addWindowListener(new WindowAdapter()
            {
                @Override
                public void windowClosing(WindowEvent w)
                {
                    logger.debug("Browser window being dismissed");
                    mBrowserWindow = null;
                }
            });
        }
        else
        {
            // Window already exists - bring it to  the front
            logger.info("Asked to show window that already exists");
            mBrowserWindow.setState(Frame.NORMAL);
            mBrowserWindow.setVisible(true);
            mBrowserWindow.toFront();
            mBrowserWindow.repaint();
        }

        logger.logExit();
    }

    /**
     * @return the BrowserLauncherService obtained from the bundle
     * context
     */
    private BrowserLauncherService getBrowserLauncher()
    {
        if (mBrowserLauncherService == null)
        {
            String browserClassName = BrowserLauncherService.class.getName();
            ServiceReference serviceReference =
                                 mContext.getServiceReference(browserClassName);
            mBrowserLauncherService =
                  (BrowserLauncherService)mContext.getService(serviceReference);
        }

        return mBrowserLauncherService;
    }
}
