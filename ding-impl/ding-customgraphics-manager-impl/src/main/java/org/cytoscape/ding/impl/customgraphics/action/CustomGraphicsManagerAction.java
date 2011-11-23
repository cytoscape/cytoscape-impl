package org.cytoscape.ding.impl.customgraphics.action;


import java.awt.event.ActionEvent;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.ding.customgraphics.CustomGraphicsManager;
import org.cytoscape.ding.impl.customgraphics.ui.CustomGraphicsManagerDialog;


public class CustomGraphicsManagerAction extends AbstractCyAction {
	private static final long serialVersionUID = -4582671383878015609L;
	private final CustomGraphicsManagerDialog dialog;

	public CustomGraphicsManagerAction(final CustomGraphicsManager manager,
	                                   final CyApplicationManager applicationManager)
	{
		super("Open Custom Graphics Manager", applicationManager);
		setPreferredMenu("View");
		
		this.dialog = new CustomGraphicsManagerDialog(manager, applicationManager);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		this.dialog.setVisible(true);
	}

}
