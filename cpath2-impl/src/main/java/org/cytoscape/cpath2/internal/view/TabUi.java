package org.cytoscape.cpath2.internal.view;

import javax.swing.*;

/**
 * TabUI Singleton.
 * 
 */
public class TabUi extends JTabbedPane {
    private static TabUi tabs;

    /**
     * Gets Instance of TabUI Object.
     * @return TabUI Object.
     */
    public static TabUi getInstance() {
        if (tabs == null) {
            tabs = new TabUi();
        }
        return tabs;
    }

    /**
     * Private Constructor.
     * Enforces Singelton Pattern.
     */
    private TabUi() {
    }
}
