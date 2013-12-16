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

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.text.BadLocationException;

import org.cytoscape.application.CyVersion;
import org.cytoscape.welcome.internal.WelcomeScreenDialog;

public final class StatusPanel extends AbstractWelcomeScreenChildPanel {

	private static final long serialVersionUID = 54718654342142203L;
	
	private static final String UP_TO_DATE_ICON_LOCATION = "images/Icons/accept.png";
	private static final String NEW_VER_AVAILABLE_ICON_LOCATION = "images/Icons/error.png";
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

		this.setLayout( new BoxLayout(this, BoxLayout.PAGE_AXIS));

		JPanel panel = new JPanel();
		panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.setLayout( new BorderLayout(0,3));


		this.setLayout( new BorderLayout(0,3) );
		final JLabel status = new JLabel();
		status.setAlignmentX(Component.LEFT_ALIGNMENT);
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
		panel.add(status, BorderLayout.NORTH);
//		this.add(status);
//		this.add(Box.createRigidArea(new Dimension(0, 3)));


        final JEditorPane news = new JEditorPane()
		{
			@Override
			public boolean getScrollableTracksViewportHeight()
			{
				return true;
			}
		};
		news.setAlignmentX(Component.LEFT_ALIGNMENT);
        news.setOpaque(false);
        news.setFont(REGULAR_FONT);
	    news.setPreferredSize( new Dimension(this.getPreferredSize().width,500) );
		news.setMinimumSize( new Dimension(10,10));
		news.setEditable(false);
		//news.setPreferredSize( new Dimension(50,5) );
        try
		{
            news.setPage(NEWS_URL);
        }
		catch (IOException e)
		{
			e.printStackTrace();
		}
		panel.add(news);

		JScrollPane sp = new JScrollPane(panel);
		sp.setAlignmentX(Component.LEFT_ALIGNMENT);
		sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		sp.setOpaque(false);
		sp.setBorder( BorderFactory.createEmptyBorder() );
		sp.getViewport().setOpaque(false);
		sp.setViewportBorder( BorderFactory.createEmptyBorder() );
		this.add(sp);
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
