package org.cytoscape.app.internal.action;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.util.swing.OpenBrowser;

import java.awt.event.ActionEvent;

public class YFilesAction extends AbstractCyAction {

    private static final String YFILES_URL = "https://apps.cytoscape.org/apps/yfileslayoutalgorithms";

    private final OpenBrowser openBrowser;

    public YFilesAction(OpenBrowser openBrowser) {
        super("Install yFiles Layouts...");
        super.setPreferredMenu("Layout");
        super.setMenuGravity(2000.0f);

        this.openBrowser = openBrowser;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        openBrowser.openURL(YFILES_URL);
    }
}
