package net.java.sip.communicator.plugin.nativebrowser;

import java.awt.BorderLayout;

import javax.swing.SwingUtilities;

import net.java.sip.communicator.util.swing.TransparentPanel;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;

/**
 * A class to display a web page as part of the native UI.  Similar to JEditorPane
 * but with support for CSS, Javascript and flash.
 */
public class NativeBrowserPanel extends TransparentPanel
{
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 0L;
    private final JWebBrowser webView;

    public NativeBrowserPanel(final String url)
    {
        super(new BorderLayout());

        webView = new JWebBrowser();

        // Some functions can't be called on the main thread as they may block
        // it.  Thus we have to invoke them later
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                webView.navigate(url);

                webView.setEnabled(true);
                webView.setBounds(getBounds());
                webView.setJavascriptEnabled(true);
            }
        });

        // Hide anything that makes it look like a webpage:
        webView.setButtonBarVisible(false);
        webView.setStatusBarVisible(false);
        webView.setLocationBarVisible(false);
        webView.setBarsVisible(false);

        add(webView);
    }

}
