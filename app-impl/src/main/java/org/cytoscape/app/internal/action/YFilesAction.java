package org.cytoscape.app.internal.action;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.util.swing.OpenBrowser;

import java.awt.event.ActionEvent;

public class YFilesAction extends AbstractCyAction {

	/**
	 *
	 */
	private static final long serialVersionUID = 5418899541794179713L;
	private static float GRAVITY = 2000.0f;
	private static final String YFILES_URL = "https://apps.cytoscape.org/apps/yfileslayoutalgorithms";

    private final OpenBrowser openBrowser;


    public YFilesAction(String name, OpenBrowser openBrowser) {
        super(name);
        super.setPreferredMenu("Layout");
        super.setMenuGravity(GRAVITY);

        GRAVITY += 0.1f;

        this.openBrowser = openBrowser;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        openBrowser.openURL(YFILES_URL, false);
    }
}
