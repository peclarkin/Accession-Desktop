/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jitsi.plugin.example;

import net.java.sip.communicator.service.gui.*;

import javax.swing.*;

/**
 * The menu item.
 * @author Damian Minkov
 */
public class ExamplePluginComponent
    extends AbstractPluginComponent
{
    /**
     * The menu item.
     */
    private JMenuItem exampleMenuItem;

    /**
     * Constructor.
     *
     * @param container parent container
     */
    public ExamplePluginComponent(Container container)
    {
        super(container);
    }

    /**
     * Returns the name of this plugin component. This name could be used as a
     * label when the component is added to a container, which requires a title.
     * A container that could request a name is for example a tabbed pane.
     *
     * @return the name of this plugin component
     */
    public String getName()
    {
        return
            ExampleActivator
                .getResources()
                    .getI18NString("service.gui.EXAMPLE_TEXT_1")
            + " "
            + ExampleActivator
                            .getResources()
                                .getI18NString("service.gui.EXAMPLE_TEXT_2");
    }

    /**
     * Returns the component that should be added. This method should return a
     * valid AWT, SWT or Swing object in order to appear properly in the user
     * interface.
     *
     * @return the component that should be added.
     */
    public Object getComponent()
    {
        if (exampleMenuItem == null)
        {
            exampleMenuItem = new JMenuItem(getName());
        }
        return exampleMenuItem;
    }
}
