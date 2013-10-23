package org.cytoscape.welcome.internal.panel;

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

import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import org.cytoscape.application.CyVersion;
import org.cytoscape.util.swing.OpenBrowser;

public class NewsAndLinkPanel extends AbstractWelcomeScreenChildPanel {

	private static final long serialVersionUID = -1685752658901305871L;

	private JLabel about;
	private JLabel manual;
	private JLabel tutorial;
	private JLabel bugReport;

	private final List<JLabel> labelSet;
	private final Map<JLabel, String> urlMap;

	private final OpenBrowser openBrowserServiceRef;
	private final CyVersion version;

	private final StatusPanel statusPanel;

	public NewsAndLinkPanel(final StatusPanel statusPanel, final OpenBrowser openBrowserServiceRef,
			CyVersion version) {
		labelSet = new ArrayList<JLabel>();
		urlMap = new HashMap<JLabel, String>();
		this.openBrowserServiceRef = openBrowserServiceRef;
		this.version = version;
		this.statusPanel = statusPanel;
		initComponents();
	}

	private void initComponents() {
		this.setLayout(new GridLayout(2,1));
		setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		
		
		final JPanel linkPanel = new JPanel();
		linkPanel.setOpaque(false);
		linkPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(REGULAR_FONT_COLOR, 1),
				"Web Links", TitledBorder.LEFT, TitledBorder.CENTER, REGULAR_FONT, REGULAR_FONT_COLOR));
		linkPanel.setLayout(new GridLayout(2, 2));

		
		about = new JLabel("<html><u>About Cytoscape</u></html>");
		manual = new JLabel("<html><u>Documentation</u></html>");
		tutorial = new JLabel("<html><u>Tutorials</u></html>");
		bugReport = new JLabel("<html><u>Report a bug</u></html>");

		// get Cytoscape version
		String cyversion = version.getVersion();

		// get OS string
		String os_str = System.getProperty("os.name") + "_" + System.getProperty("os.version");
		os_str = os_str.replace(" ", "_");

		labelSet.add(about);
		labelSet.add(manual);
		labelSet.add(tutorial);
		labelSet.add(bugReport);
		urlMap.put(about, "http://www.cytoscape.org/what_is_cytoscape.html");
		urlMap.put(manual, "http://www.cytoscape.org/documentation_users.html");
		urlMap.put(tutorial, "http://opentutorials.cgl.ucsf.edu/index.php/Portal:Cytoscape3");
		urlMap.put(bugReport, "http://chianti.ucsd.edu/cyto_web/bugreport/bugreport.php?cyversion=" + cyversion
				+ "&os=" + os_str);

		for (final JLabel label : labelSet) {
			label.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
			label.setFont(LINK_FONT);
			label.setForeground(LINK_FONT_COLOR);
			label.setHorizontalAlignment(SwingConstants.LEFT);
			label.setHorizontalTextPosition(SwingConstants.LEFT);
			label.setCursor(new Cursor(Cursor.HAND_CURSOR));
			label.addMouseListener(new LabelMouseListener(label, urlMap.get(label), this));
			linkPanel.add(label);
		}

		statusPanel.setOpaque(false);
		statusPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(REGULAR_FONT_COLOR, 1),
				"Latest News", TitledBorder.LEFT, TitledBorder.CENTER, REGULAR_FONT, REGULAR_FONT_COLOR));
		
		add(linkPanel);
        JScrollPane statusScrollPane = new JScrollPane(statusPanel);
		add(statusScrollPane);
		
	}

	private final class LabelMouseListener extends MouseAdapter {

		private final String url;

		LabelMouseListener(final JLabel label, final String url, final JPanel parent) {
			this.url = url;
		}

		@Override
		public void mouseClicked(MouseEvent arg0) {
			closeParentWindow();
			openBrowserServiceRef.openURL(url);
		}
	}

}
