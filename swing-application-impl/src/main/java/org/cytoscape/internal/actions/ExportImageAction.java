package org.cytoscape.internal.actions;

import java.awt.Window;
import java.awt.event.ActionEvent;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.internal.dialogs.ExportImageDialog;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkViewManager;

public class ExportImageAction extends AbstractCyAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 658710037737217181L;
	private Window owner;
	private CyServiceRegistrar registrar;

	public ExportImageAction(Window owner, CyServiceRegistrar registrar) {
		super("Export as Image...", registrar.getService(CyApplicationManager.class), "networkAndView", registrar.getService(CyNetworkViewManager.class));
		setPreferredMenu("File");
		setEnabled(true);
		setMenuGravity(5.2f);
		this.owner = owner;
		this.registrar = registrar;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		ExportImageDialog dialog = new ExportImageDialog(owner, registrar);
		dialog.setVisible(true);
	}

}
