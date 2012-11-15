package net.java.sip.communicator.plugin.callmanager;

import net.java.sip.communicator.service.gui.AbstractPluginComponent;
import net.java.sip.communicator.service.gui.Container;
import net.java.sip.communicator.util.swing.SIPCommMenuBar;

public class CallManagerComponent extends AbstractPluginComponent
{
    private final SIPCommMenuBar mComponent;
    private final CallManagerStatusBar mStatusBar;

    protected CallManagerComponent(Container container)
    {
        super(container);
        mComponent = new SIPCommMenuBar();
        mStatusBar = new CallManagerStatusBar();
        mComponent.add(mStatusBar);
    }

    public String getName()
    {
        return null;
    }

    public Object getComponent()
    {
        return mComponent;
    }

    /**
     * Called when this component is being shut down
     */
    public void stop()
    {
        mStatusBar.stop();
    }
}
