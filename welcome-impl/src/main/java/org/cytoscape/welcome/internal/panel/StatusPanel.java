package org.cytoscape.welcome.internal.panel;

import java.awt.GridLayout;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.cytoscape.application.CyVersion;
import org.cytoscape.welcome.internal.WelcomeScreenDialog;

public final class StatusPanel extends AbstractWelcomeScreenChildPanel {

	private static final long serialVersionUID = 54718654342142203L;
	
	private static final String UP_TO_DATE_ICON_LOCATION = "images/Icons/accept.png";
	private static final String NEW_VER_AVAILABLE_ICON_LOCATION = "images/Icons/error.png";

	private final CyVersion cyVersion;
	
	private final Icon upToDateIcon;
	private final Icon newVersionAvailableIcon;

	public StatusPanel(final CyVersion cyVersion) {
		this.cyVersion = cyVersion;

		upToDateIcon= new ImageIcon(WelcomeScreenDialog.class.getClassLoader().getResource(UP_TO_DATE_ICON_LOCATION));
		newVersionAvailableIcon= new ImageIcon(WelcomeScreenDialog.class.getClassLoader().getResource(NEW_VER_AVAILABLE_ICON_LOCATION));
		initComponents();
	}

	private void initComponents() {
		final String versionStr = cyVersion.getVersion();

		this.setLayout(new GridLayout(5, 1));

		final JLabel status = new JLabel();
		status.setOpaque(false);
		status.setFont(REGULAR_FONT);
		status.setForeground(REGULAR_FONT_COLOR);
		
		if(isUpToDate()) {
			status.setIcon(upToDateIcon);
			status.setText("Cytoscape " + versionStr + " is up to date.");
		} else {
			status.setIcon(newVersionAvailableIcon);
			status.setText("New version is available: " + versionStr);
		}
		this.add(status);
		// TODO: add feed reader to show latest news about Cytoscape.
	}
	
	private boolean isUpToDate() {
		// TODO: Implement this!
		return true;
	}
	
	private String getNewVersionNumber() {
		// TODO: implement this!
		return "3.1.0";
	}
	

}
