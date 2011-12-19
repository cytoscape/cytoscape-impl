package org.cytoscape.webservice.internal.task;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.webservice.internal.ui.UnifiedNetworkImportDialog;

/**
 * Display Network Import GUI.
 * 
 */
public class ShowImportNetworkFromWebServiceDialogAction extends AbstractCyAction {

	private static final long serialVersionUID = -36712860667900147L;
	
	private static final String MENU_LABEL = "Public Databases...";
	private static final String MENU_LOCATION = "File.Import.Network";
	
	private UnifiedNetworkImportDialog dialog;
	private final Window parent;

	public ShowImportNetworkFromWebServiceDialogAction(final CySwingApplication app, final UnifiedNetworkImportDialog dialog) {
		super(MENU_LABEL);
		
		if (dialog == null)
			throw new NullPointerException("Dialog is null.");
		
		setPreferredMenu(MENU_LOCATION);
		
		// ALT (for Mac, it's Option)
		final KeyStroke shortcut = KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.ALT_DOWN_MASK);
		setAcceleratorKeyStroke(shortcut);

		this.parent = app.getJFrame();
		this.dialog = dialog;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		dialog.setLocationRelativeTo(parent);
		dialog.setVisible(true);
	}

}
