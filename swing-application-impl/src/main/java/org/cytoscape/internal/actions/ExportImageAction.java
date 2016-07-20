package org.cytoscape.internal.actions;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.Map;

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
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ExportImageAction(Map props, Window owner, CyServiceRegistrar registrar) {
		super(props, registrar.getService(CyApplicationManager.class), registrar.getService(CyNetworkViewManager.class));
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
