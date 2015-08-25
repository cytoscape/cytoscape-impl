package org.cytoscape.welcome.internal.view;

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

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static javax.swing.GroupLayout.Alignment.LEADING;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.text.AttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLDocument.Iterator;

import org.cytoscape.application.CyVersion;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")
public final class NewsPanel extends AbstractWelcomeScreenChildPanel {

    private static final String NEWS_URL = "http://chianti.ucsd.edu/cytoscape-news/news.html";

	private final CyVersion cyVersion;
	private final IconManager iconManager;
	
	public NewsPanel(final CyVersion cyVersion, final IconManager iconManager) {
		super("Latest News");
		this.cyVersion = cyVersion;
		this.iconManager = iconManager;

		initComponents();
	}

	private void initComponents() {
		final String versionStr = cyVersion.getVersion();
		
		final JLabel statusIconLabel = new JLabel(IconManager.ICON_REFRESH);
		statusIconLabel.setFont(iconManager.getIconFont(16.0f));
		
		final JLabel statusLabel = new JLabel("Checking for updates...");
		statusLabel.setFont(statusLabel.getFont().deriveFont(LookAndFeelUtil.INFO_FONT_SIZE));
        
		final JEditorPane newsPane = new JEditorPane() {
			@Override
			public boolean getScrollableTracksViewportWidth() {
				return true;
			}
		};
		newsPane.setFont(newsPane.getFont().deriveFont(LookAndFeelUtil.INFO_FONT_SIZE));
		newsPane.setEditable(false);
		newsPane.setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createCompoundBorder(
								BorderFactory.createEmptyBorder(5, 0, 0, 0),
								BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Separator.foreground"))
						),
						BorderFactory.createEmptyBorder(5, 0, 0, 0)
				)
		);
		
		if (LookAndFeelUtil.isNimbusLAF()) {
			// Nimbus does not respect background color settings for JEditorPane,
			// so this is necessary to override its color:
			final UIDefaults defaults = new UIDefaults();
			defaults.put("EditorPane[Enabled].backgroundPainter", getBackground());
			newsPane.putClientProperty("Nimbus.Overrides", defaults);
			newsPane.putClientProperty("Nimbus.Overrides.InheritDefaults", true);
			newsPane.setBackground(getBackground());
		} else {
			newsPane.setBackground(getBackground());
		}
		
		final JPanel statusPanel = new JPanel();
		{
			final GroupLayout layout = new GroupLayout(statusPanel);
			statusPanel.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(false);
			
			layout.setHorizontalGroup(layout.createParallelGroup(LEADING, true)
					.addGroup(layout.createSequentialGroup()
							.addComponent(statusIconLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(statusLabel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					)
					.addComponent(newsPane, 200, 300, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(CENTER, false)
							.addComponent(statusIconLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(statusLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addComponent(newsPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
		}
		
		final JScrollPane scrollPane = new JScrollPane(statusPanel);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
		
		if (LookAndFeelUtil.isAquaLAF()) {
			scrollPane.setOpaque(false);
			scrollPane.getViewport().setOpaque(false);
			newsPane.setOpaque(false);
			statusPanel.setOpaque(false);
		}
		
		Runnable getNews = new Runnable() {
			@Override
			public void run() {
				try {
					newsPane.setPage(NEWS_URL);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		newsPane.addPropertyChangeListener("page", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				HTMLDocument doc = (HTMLDocument) newsPane.getDocument();
				
				for (Iterator i = doc.getIterator(HTML.Tag.META); i.isValid(); i.next()) {
					AttributeSet attrs = i.getAttributes();
					
					if (attrs.containsAttribute(HTML.Attribute.NAME, "latestVersion")) {
						String latestVersion = (String) attrs.getAttribute(HTML.Attribute.CONTENT);
						
						if (versionStr.equals(latestVersion)) {
							statusIconLabel.setText(IconManager.ICON_CHECK_CIRCLE);
							statusIconLabel.setForeground(OK_COLOR);
							statusLabel.setText("Cytoscape " + versionStr + " is up to date.");
						} else {
							statusIconLabel.setText(IconManager.ICON_WARNING);
							statusIconLabel.setForeground(LookAndFeelUtil.WARN_COLOR);
							
							if (versionStr.contains("-"))
								statusLabel.setText("This is a pre-release version of Cytoscape.");
							else
								statusLabel.setText("New version is available: " + latestVersion);
						}
						
						break;
					}
				}
			}
		});
		(new Thread(getNews)).start();

		{
			final GroupLayout layout = new GroupLayout(this);
			this.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(false);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addComponent(scrollPane, DEFAULT_SIZE, 300, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addContainerGap()
					.addComponent(scrollPane, DEFAULT_SIZE, 120, Short.MAX_VALUE)
			);
		}
	}
}
