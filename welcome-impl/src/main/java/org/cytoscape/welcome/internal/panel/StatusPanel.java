package org.cytoscape.welcome.internal.panel;

import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JTextField;

import org.cytoscape.application.CyVersion;

public final class StatusPanel extends AbstractWelcomeScreenChildPanel {

	private static final long serialVersionUID = 54718654342142203L;

	private final CyVersion cyVersion;
	
	public StatusPanel(final CyVersion cyVersion) {
		this.cyVersion = cyVersion;
		
		initComponents();
	}

	private void initComponents() {
		final String versionStr = cyVersion.getVersion();
		
		this.setLayout(new GridLayout(5,1));
		
		final JTextField isUpToDate = new JTextField();
		isUpToDate.setEditable(false);
		isUpToDate.setOpaque(false);
		isUpToDate.setFont(REGULAR_FONT);
		isUpToDate.setForeground(REGULAR_FONT_COLOR);
		isUpToDate.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		isUpToDate.setText("Cytoscape " + versionStr + " is the latest version.");
		
		this.add(isUpToDate);
		// TODO: add feed reader to show latest news about Cytoscape.
	}

}
