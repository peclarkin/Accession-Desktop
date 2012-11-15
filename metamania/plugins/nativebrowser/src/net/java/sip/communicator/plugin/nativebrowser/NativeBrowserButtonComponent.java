package net.java.sip.communicator.plugin.nativebrowser;

import java.awt.Component;

import net.java.sip.communicator.service.gui.AbstractPluginComponent;
import net.java.sip.communicator.service.gui.Container;

import org.osgi.framework.BundleContext;

/**
 * A class to create a button which opens a URL in the native browser
 */
public class NativeBrowserButtonComponent extends AbstractPluginComponent
{
    private final Component mComponent;

    protected NativeBrowserButtonComponent(Container container,
                                           BundleContext context,
                                           String url,
                                           String imageResource,
                                           String toolTipResource)
    {
        super(container);

        mComponent = new NativeBrowserButton(context,
                                             url,
                                             imageResource,
                                             toolTipResource);
    }

    public String getName()
    {
        return null;
    }

    public Component getComponent()
    {
        return mComponent;
    }

}
