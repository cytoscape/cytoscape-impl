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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
import javax.swing.text.AttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLDocument.Iterator;

import org.cytoscape.application.CyVersion;
import org.cytoscape.welcome.internal.WelcomeScreenDialog;

public final class StatusPanel extends AbstractWelcomeScreenChildPanel {

	private static final long serialVersionUID = 54718654342142203L;
	
	private static final String CHECK_ICON_LOCATION = "images/Icons/check-circle-icon.png";
	private static final String WARNING_ICON_LOCATION = "images/Icons/warn-icon.png";
    private static final String NEWS_URL = "http://chianti.ucsd.edu/cytoscape-news/news.html";

	private final CyVersion cyVersion;
	
	private final Icon checkIcon;
	private final Icon warningIcon;

	public StatusPanel(final CyVersion cyVersion) {
		this.cyVersion = cyVersion;

		checkIcon= new ImageIcon(WelcomeScreenDialog.class.getClassLoader().getResource(CHECK_ICON_LOCATION));
		warningIcon= new ImageIcon(WelcomeScreenDialog.class.getClassLoader().getResource(WARNING_ICON_LOCATION));
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
		news.addPropertyChangeListener("page", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				HTMLDocument doc = (HTMLDocument) news.getDocument();
				for(Iterator i = doc.getIterator(HTML.Tag.META); i.isValid(); i.next() ) {
					AttributeSet attrs = i.getAttributes();
					if(attrs.containsAttribute(HTML.Attribute.NAME, "latestVersion")) {
						String latestVersion = (String) attrs.getAttribute(HTML.Attribute.CONTENT);
						if (versionStr.equals(latestVersion)) {
							status.setIcon(checkIcon);
							status.setText("Cytoscape " + versionStr + " is up to date.");
						} else {
							status.setIcon(warningIcon);
							if(versionStr.contains("-")) 
								status.setText("This is a pre-release version of Cytoscape.");
							else
								status.setText("New version is available: " + latestVersion);
						}
						break;
					}
				}
			}
		});
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
}
