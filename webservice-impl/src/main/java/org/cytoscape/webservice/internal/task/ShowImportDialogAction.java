package org.cytoscape.webservice.internal.task;

import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.KeyStroke;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.webservice.internal.ui.WebServiceImportDialog;

/**
 * Display Network Import GUI.
 * 
 */
public class ShowImportDialogAction extends AbstractCyAction {

	private static final long serialVersionUID = -36712860667900147L;


	private WebServiceImportDialog<?> dialog;

	private final Window parent;

	public ShowImportDialogAction(final CySwingApplication app,
			final WebServiceImportDialog<?> dialog, final String menuLocation, final String menuLabel, final KeyStroke shortcut) {
		super(menuLabel);

		if (dialog == null)
			throw new NullPointerException("Dialog is null.");

		if (menuLocation == null)
			throw new NullPointerException("Menu Location is null.");
		
		if (menuLabel == null)
			throw new NullPointerException("Menu Label is null.");

		setPreferredMenu(menuLocation);

		if(shortcut != null)
			setAcceleratorKeyStroke(shortcut);

		this.parent = app.getJFrame();
		this.dialog = dialog;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		dialog.prepareForDisplay();
		dialog.setLocationRelativeTo(parent);
		dialog.setVisible(true);
	}

}
