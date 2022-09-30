package org.cytoscape.cg.internal.action;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.cg.internal.ui.CustomGraphicsManagerDialog;
import org.cytoscape.cg.model.CustomGraphicsManager;
import org.cytoscape.service.util.CyServiceRegistrar;

@SuppressWarnings("serial")
public class CustomGraphicsManagerAction extends AbstractCyAction {
	
	private final CustomGraphicsManager cgManager;
	private final CyServiceRegistrar serviceRegistrar;

	public CustomGraphicsManagerAction(CustomGraphicsManager cgManager, CyServiceRegistrar serviceRegistrar) {
		super("Open Image Manager");
		setPreferredMenu("View");
		setMenuGravity(10.0f);
		
		this.cgManager = cgManager;
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		var swingApplication = serviceRegistrar.getService(CySwingApplication.class);
		Window owner = null;

		if (evt.getSource() instanceof JMenuItem) {
			if (swingApplication.getJMenuBar() != null)
				owner = SwingUtilities.getWindowAncestor(swingApplication.getJMenuBar());
		} else if (evt.getSource() instanceof Component) {
			owner = SwingUtilities.getWindowAncestor((Component) evt.getSource());
		}

		if (owner == null)
			owner = swingApplication.getJFrame();

		var dialog = new CustomGraphicsManagerDialog(owner, cgManager, serviceRegistrar);
		dialog.setLocationRelativeTo(owner);
		dialog.setVisible(true);
	}
}
