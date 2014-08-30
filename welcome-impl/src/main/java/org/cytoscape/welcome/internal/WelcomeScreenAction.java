package org.cytoscape.welcome.internal;

/*
 * #%L
 * Cytoscape Welcome Screen Impl (welcome-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.awt.event.ActionEvent;
import java.util.Properties;

import javax.swing.SwingUtilities;

import org.cytoscape.application.CyVersion;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.property.CyProperty;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.welcome.internal.panel.CreateNewNetworkPanel;
import org.cytoscape.welcome.internal.panel.GeneSearchPanel;
import org.cytoscape.welcome.internal.panel.NewsPanel;

public class WelcomeScreenAction extends AbstractCyAction {

	private static final long serialVersionUID = 2584201062371825221L;

	public static final String DO_NOT_DISPLAY_PROP_NAME = "hideWelcomeScreen";
	public static final String TEMP_DO_NOT_DISPLAY_PROP_NAME = "tempHideWelcomeScreen";
	private static final String MENU_NAME = "Show Welcome Screen...";
	private static final String PARENT_NAME = "Help";

	private boolean hide = false;

	// Child Panels
	private final CreateNewNetworkPanel importPanel;
	private final GeneSearchPanel geneSearchPanel;
	private final NewsPanel newsPanel;

	private final CyProperty<Properties> cyProps;
	private final CySwingApplication cytoscapeDesktop;
	private final OpenBrowser openBrowser;
	private final CyVersion version;

	public WelcomeScreenAction(final CreateNewNetworkPanel importPanel,
							   final GeneSearchPanel geneSearchPanel,
							   final NewsPanel newsPanel,
							   final CyProperty<Properties> cyProps,
							   final CySwingApplication cytoscapeDesktop,
							   final OpenBrowser openBrowser,
							   final CyVersion version) {
		super(MENU_NAME);
		setPreferredMenu(PARENT_NAME);
		this.setMenuGravity(1.5f);

		this.importPanel = importPanel;
		this.geneSearchPanel = geneSearchPanel;
		this.newsPanel = newsPanel;
		this.cytoscapeDesktop = cytoscapeDesktop;
		this.openBrowser = openBrowser;
		this.version = version;

		this.cyProps = cyProps;

		Properties systemProperties = System.getProperties();

		// Show it if necessary
		if( !systemProperties.containsKey("cytoscape.welcome.already.started.once") )
		{
			systemProperties.setProperty("cytoscape.welcome.already.started.once", "true");
			System.setProperties(systemProperties);
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					startup();
				}
			});
		}
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		final WelcomeScreenDialog welcomeScreen = new WelcomeScreenDialog(importPanel, geneSearchPanel, newsPanel, cyProps,
				hide, openBrowser, version);
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
