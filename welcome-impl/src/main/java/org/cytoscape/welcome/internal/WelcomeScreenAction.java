package org.cytoscape.welcome.internal;

import java.awt.event.ActionEvent;
import java.util.Properties;

import javax.swing.SwingUtilities;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.property.CyProperty;
import org.cytoscape.welcome.internal.panel.CreateNewNetworkPanel;
import org.cytoscape.welcome.internal.panel.HelpPanel;
import org.cytoscape.welcome.internal.panel.OpenPanel;
import org.cytoscape.welcome.internal.panel.StatusPanel;

public class WelcomeScreenAction extends AbstractCyAction {

	private static final long serialVersionUID = 2584201062371825221L;

	public static final String DO_NOT_DISPLAY_PROP_NAME = "hideWelcomeScreen";
	public static final String TEMP_DO_NOT_DISPLAY_PROP_NAME = "tempHideWelcomeScreen";
	private static final String MENU_NAME = "Show Welcome Screen...";
	private static final String PARENT_NAME = "Help";

	private boolean hide = false;

	// Child Panels
	private final CreateNewNetworkPanel importPanel;
	private final OpenPanel openPanel;
	private final HelpPanel helpPanel;
	private final StatusPanel statusPanel;

	private final CyProperty<Properties> cyProps;
	private final CySwingApplication cytoscapeDesktop;

	public WelcomeScreenAction(final CreateNewNetworkPanel importPanel, final OpenPanel openPanel,
			final HelpPanel helpPanel, final StatusPanel statusPanel, final CyProperty<Properties> cyProps,
			final CySwingApplication cytoscapeDesktop) {
		super(MENU_NAME);
		setPreferredMenu(PARENT_NAME);
		this.setMenuGravity(1.5f);

		this.importPanel = importPanel;
		this.openPanel = openPanel;
		this.helpPanel = helpPanel;
		this.statusPanel = statusPanel;
		this.cytoscapeDesktop = cytoscapeDesktop;

		this.cyProps = cyProps;

		// Show it if necessary
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				startup();
			}
		});
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		final WelcomeScreenDialog welcomeScreen = new WelcomeScreenDialog(importPanel, openPanel, helpPanel,
				statusPanel, cyProps, hide);
		welcomeScreen.setLocationRelativeTo(cytoscapeDesktop.getJFrame());
		welcomeScreen.setVisible(true);
		welcomeScreen.setModal(true);

		// Update property
		this.hide = welcomeScreen.getHideStatus();
		this.cyProps.getProperties().setProperty(DO_NOT_DISPLAY_PROP_NAME, ((Boolean) hide).toString());
	}

	public void startup() {

		// Displays the dialog after startup based on whether
		// the specified property has been set.
		final String hideString = this.cyProps.getProperties().getProperty(DO_NOT_DISPLAY_PROP_NAME);
		hide = parseBoolean(hideString);

		if (!hide) {
			final String tempHideString = this.cyProps.getProperties().getProperty(TEMP_DO_NOT_DISPLAY_PROP_NAME);
			hide = parseBoolean(tempHideString);
		}

		if (!hide) {
			final String systemHideString = System.getProperty(DO_NOT_DISPLAY_PROP_NAME);
			hide = parseBoolean(systemHideString);
		}

		// remove this property regardless!
		this.cyProps.getProperties().remove(TEMP_DO_NOT_DISPLAY_PROP_NAME);

		if (!hide)
			actionPerformed(null);
	}

	private boolean parseBoolean(String hideString) {
		boolean lhide = false;
		if (hideString == null)
			lhide = false;
		else {
			try {
				// might make it true!
				lhide = Boolean.parseBoolean(hideString);
			} catch (Exception ex) {
				lhide = false;
			}
		}
		return lhide;
	}
}
