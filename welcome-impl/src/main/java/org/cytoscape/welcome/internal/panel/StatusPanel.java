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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import org.cytoscape.application.CyVersion;
import org.cytoscape.welcome.internal.WelcomeScreenDialog;

public final class StatusPanel extends AbstractWelcomeScreenChildPanel {

	private static final long serialVersionUID = 54718654342142203L;
	
	private static final String UP_TO_DATE_ICON_LOCATION = "images/Icons/check-circle-icon.png";
	private static final String NEW_VER_AVAILABLE_ICON_LOCATION = "images/Icons/warn-icon.png";
    private static final String NEWS_URL = "http://chianti.ucsd.edu/cytoscape-news/news.html";

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

		this.setLayout(new BorderLayout());

		final JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		final JLabel status = new JLabel();
		status.setAlignmentX(Component.LEFT_ALIGNMENT);
		status.setBackground(this.getBackground());
		status.setFont(REGULAR_FONT);

        if (isUpToDate()) {
			status.setIcon(upToDateIcon);
			status.setText("Cytoscape " + versionStr + " is up to date.");
		} else {
			status.setIcon(newVersionAvailableIcon);
			status.setText("New version is available: " + versionStr);
		}
        
		final JEditorPane news = new JEditorPane() {
			@Override
			public boolean getScrollableTracksViewportHeight() {
				return true;
			}
		};
		news.setAlignmentX(Component.LEFT_ALIGNMENT);
		news.setFont(REGULAR_FONT);
		news.setBackground(this.getBackground());
		news.setPreferredSize(new Dimension(this.getPreferredSize().width, 500));
		news.setMinimumSize(new Dimension(10, 10));
		news.setEditable(false);
		
		Runnable getNews = new Runnable()
		{

			@Override
			public void run()
			{
				try
				{
					news.setPage(NEWS_URL);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		};
		(new Thread(getNews)).start();
		
		panel.add(status);
		panel.add(Box.createVerticalStrut(10));
		panel.add(news);

		final JScrollPane sp = new JScrollPane(panel);
		sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		sp.setBorder(BorderFactory.createEmptyBorder());
		sp.setViewportBorder(BorderFactory.createEmptyBorder());
		
		this.add(sp, BorderLayout.CENTER);
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
