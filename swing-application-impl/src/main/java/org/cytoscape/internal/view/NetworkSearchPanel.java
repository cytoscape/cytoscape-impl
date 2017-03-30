package org.cytoscape.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.text.Collator;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2017 The Cytoscape Consortium
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

@SuppressWarnings("serial")
public class NetworkSearchPanel extends JPanel {

	private static final String DEFAULT_SOURCE_PROP_KEY = "networkSearch.defaultSource";
	private static final int ICON_SIZE = 32;
	private static final String DEF_SEARCH_TEXT = "Type your query here...";
	
	private JButton sourcesButton;
	private JButton sourcesSelectorButton;
	private JPanel contentPane;
	private JTextField searchTextField;
	private JButton optionsButton;
	private JButton searchButton;
	private JDialog sourcesDialog;
	private SourcesPanel sourcesPanel;
	
	private final EmptyIcon emptyIcon = new EmptyIcon(ICON_SIZE, ICON_SIZE);
	
	private final Set<NetworkSearchSource> sources;
	private NetworkSearchSource defaultSource;
	private NetworkSearchSource selectedSource;
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public NetworkSearchPanel(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
		
		final Collator collator = Collator.getInstance();
		sources = new TreeSet<>((NetworkSearchSource o1, NetworkSearchSource o2) -> {
			return collator.compare(o1.getName(), o2.getName());
		});
		
		init();
		// TODO: remove this test data ============================
		update(Arrays.asList(new NetworkSearchSource[]{ 
				new NetworkSearchSource("a", "GeneMANIA", emptyIcon),
				new NetworkSearchSource("b", "String", emptyIcon),
				new NetworkSearchSource("c", "Another ONE", emptyIcon),
		}));
	}
	
	public void setDefaultSource(NetworkSearchSource suggestedSource) {
		Properties props = (Properties) serviceRegistrar
				.getService(CyProperty.class, "(cyPropertyName=cytoscape3.props)").getProperties();
		
		if (suggestedSource == null || !sources.contains(suggestedSource)) {
			// Check if there is a CyProperty for this
			String id = props.getProperty(DEFAULT_SOURCE_PROP_KEY);
			
			if (id != null)
				suggestedSource = getSource(id);
		}
		
		if (suggestedSource != null) {
			// Update the CyProperty as well;
			props.setProperty(DEFAULT_SOURCE_PROP_KEY, suggestedSource.getId());
		} else {
			if (!sources.isEmpty())
				suggestedSource = sources.iterator().next();
			// Do not set this source as default in the CyProperty,
			// because it may be provided by an app that is missing right now,
			// but can be installed later.
		}
		
		defaultSource = suggestedSource;
	}
	
	public NetworkSearchSource getDefaultSource() {
		return defaultSource;
	}
	
	public void setSelectedSource(NetworkSearchSource selectedSource) {
		if (selectedSource != this.selectedSource) {
			this.selectedSource = selectedSource;
			setEnabled(selectedSource != null);
			updateSourcesButton();
		}
	}
	
	public NetworkSearchSource getSelectedSource() {
		return selectedSource;
	}
	
	public NetworkSearchSource getSource(String id) {
		for (NetworkSearchSource src : sources) {
			if (src.getId().equals(id))
				return src;
		}
		
		return null;
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		getSourcesButton().setEnabled(enabled);
		getSourcesSelectorButton().setEnabled(enabled);
		getSearchTextField().setEnabled(enabled);
		getOptionsButton().setEnabled(enabled);
		getSearchButton().setEnabled(enabled);
	}
	
	void update(Collection<NetworkSearchSource> newSources) {
		sources.clear();
		
		if (newSources != null)
			sources.addAll(newSources);
		
		setDefaultSource(defaultSource);
		
		if (selectedSource != null && sources.contains(selectedSource))
			setSelectedSource(selectedSource);
		else
			setSelectedSource(defaultSource);
	}
	
	private void updateSourcesButton() {
		if (selectedSource != null) {
			getSourcesButton().setIcon(selectedSource.getIcon());
			getSourcesButton().setToolTipText(selectedSource.getName());
		} else {
			getSourcesButton().setIcon(emptyIcon);
			getSourcesButton().setToolTipText("Please select a network source...");
		}
		
		getOptionsButton().setEnabled(!sources.isEmpty());
		getSearchButton().setEnabled(!sources.isEmpty());
	}
	
	private void showSourcesDialog() {
		if (sources.isEmpty())
			return;
		
		setEnabled(false); // Disable the search components to prevent accidental repeated clicks
		disposeSourcesDialog(); // Just to make sure there will never be more than one dialog
		
		sourcesDialog = new JDialog(SwingUtilities.getWindowAncestor(this), ModalityType.MODELESS);
		sourcesDialog.setUndecorated(true);
		sourcesDialog.setBackground(getBackground());
		sourcesDialog.getContentPane().add(getSourcesPanel(), BorderLayout.CENTER);
		sourcesDialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowDeactivated(WindowEvent e) {
				disposeSourcesDialog();
			}
			@Override
			public void windowClosed(WindowEvent e) {
				setSelectedSource(getSourcesPanel().getSourcesList().getSelectedValue());
				sourcesDialog = null;
			}
		});
		
		getSourcesPanel().update();
		
		final Point pt = getSourcesButton().getLocationOnScreen(); 
		sourcesDialog.setLocation(pt.x, pt.y + getSourcesButton().getHeight());
		sourcesDialog.pack();
		sourcesDialog.setVisible(true);
		sourcesDialog.requestFocus();
		getSourcesPanel().getSourcesList().requestFocusInWindow();
	}
	
	private void disposeSourcesDialog() {
		if (sourcesDialog != null)
			sourcesDialog.dispose();
	}

	private void init() {
		setBackground(UIManager.getColor("Table.background"));
		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(UIManager.getColor("Separator.foreground")),
				BorderFactory.createEmptyBorder(2, 1, 2, 1)
		));
		
		final GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(false);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addComponent(getSourcesButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(getSourcesSelectorButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(getContentPane(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getSearchButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		layout.setVerticalGroup(layout.createParallelGroup(CENTER, true)
				.addComponent(getSourcesButton(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getSourcesSelectorButton(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getContentPane(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getSearchButton(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		
		update(Collections.emptyList());
	}
	
	JButton getSourcesButton() {
		if (sourcesButton == null) {
			sourcesButton = new JButton(emptyIcon);
			styleButton(sourcesButton, ICON_SIZE, sourcesButton.getFont());
			sourcesButton.addActionListener(evt -> {
				showSourcesDialog();
			});
			updateSourcesButton();
		}
		
		return sourcesButton;
	}
	
	JButton getSourcesSelectorButton() {
		if (sourcesSelectorButton == null) {
			sourcesSelectorButton = new JButton(IconManager.ICON_SORT_DOWN);
			sourcesSelectorButton.setToolTipText("Click to select a network source...");
			styleButton(sourcesSelectorButton, 12, serviceRegistrar.getService(IconManager.class).getIconFont(10.0f));
			sourcesSelectorButton.addActionListener(evt -> {
				getSourcesButton().doClick();
			});
		}
		
		return sourcesSelectorButton;
	}
	
	JPanel getContentPane() {
		if (contentPane == null) {
			contentPane = new JPanel();
			contentPane.setBackground(getBackground());
			
			final GroupLayout layout = new GroupLayout(contentPane);
			contentPane.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(false);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addComponent(getSearchTextField(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getOptionsButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
			layout.setVerticalGroup(layout.createParallelGroup(CENTER, true)
					.addComponent(getSearchTextField(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getOptionsButton(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
		}
		
		return contentPane;
	}
	
	JTextField getSearchTextField() {
		if (searchTextField == null) {
			final Color msgColor = UIManager.getColor("Label.disabledForeground");
			final int hgap = 5;
			
			searchTextField = new JTextField() {
				@Override
				public void paint(Graphics g) {
					super.paint(g);
					
					if (getText() == null || getText().trim().isEmpty()) {
						// Set antialiasing
						Graphics2D g2 = (Graphics2D) g.create();
						g2.setRenderingHints(
								new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON));
						// Set the font
					    g2.setFont(getFont());
						// Get the FontMetrics
					    FontMetrics metrics = g2.getFontMetrics(getFont());
					    // Determine the X coordinate for the text
					    int x = hgap;
					    // Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
					    int y = metrics.getHeight() / 2 + metrics.getAscent();
						// Draw
						g2.setColor(msgColor);
						g2.drawString(DEF_SEARCH_TEXT, x, y);
						g2.dispose();
					}
				}
			};
			searchTextField.setMinimumSize(searchTextField.getPreferredSize());
			searchTextField.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createMatteBorder(0, 1, 0, 1, UIManager.getColor("Separator.foreground")),
					BorderFactory.createEmptyBorder(1, hgap-1, 1, hgap-1)
			));
			searchTextField.setFont(searchTextField.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
		}
		
		return searchTextField;
	}
	
	JButton getOptionsButton() {
		if (optionsButton == null) {
			optionsButton = new JButton(IconManager.ICON_ELLIPSIS_V);
			styleButton(optionsButton, 32, serviceRegistrar.getService(IconManager.class).getIconFont(16.0f));
		}
		
		return optionsButton;
	}
	
	JButton getSearchButton() {
		if (searchButton == null) {
			searchButton = new JButton(IconManager.ICON_SEARCH);
			styleButton(searchButton, 32, serviceRegistrar.getService(IconManager.class).getIconFont(16.0f));
			searchButton.setBorder(
					BorderFactory.createMatteBorder(0, 1, 0, 0, UIManager.getColor("Separator.foreground")));
		}
		
		return searchButton;
	}
	
	SourcesPanel getSourcesPanel() {
		if (sourcesPanel == null) {
			sourcesPanel = new SourcesPanel();
		}
		
		return sourcesPanel;
	}
	
	private void styleButton(AbstractButton btn, int width, Font font) {
		btn.setFont(font);
		btn.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		btn.setContentAreaFilled(false);
		
		Dimension d = new Dimension(width, getSearchTextField().getPreferredSize().height);
		btn.setMinimumSize(d);
		btn.setPreferredSize(d);
	}
	
	class SourcesPanel extends JPanel {
		
		private JScrollPane scrollPane;
		private JList<NetworkSearchSource> sourcesList;
		
		SourcesPanel() {
			setLayout(new BorderLayout());
			add(getScrollPane(), BorderLayout.CENTER);
		}
		
		JScrollPane getScrollPane() {
			if (scrollPane == null) {
				scrollPane = new JScrollPane(getSourcesList());
				scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
				scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			}
			
			return scrollPane;
		}
		
		JList<NetworkSearchSource> getSourcesList() {
			if (sourcesList == null) {
				sourcesList = new JList<>(new DefaultListModel<>());
			}
			
			return sourcesList;
		}
		
		void update() {
			DefaultListModel<NetworkSearchSource> model =
					(DefaultListModel<NetworkSearchSource>) getSourcesList().getModel();
			model.removeAllElements();
			
			for (NetworkSearchSource src : sources)
				model.addElement(src);
			
			getSourcesList().setSelectedValue(selectedSource, true);
		}
	}
	
	private class EmptyIcon implements Icon {

		private final String text = IconManager.ICON_BAN;
		private final Color fgColor;
		
		private final int width;
		private final int height;

		public EmptyIcon(int width, int height) {
			this.width = width;
			this.height = height;
			
			Color c = UIManager.getColor("Label.disabledForeground");
			fgColor = new Color(c.getRed(), c.getGreen(), c.getBlue(), 60);
		}
		
		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			// Set antialiasing
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHints(
					new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON));
			
			// Set the font
			Font font = serviceRegistrar.getService(IconManager.class).getIconFont(28.0f);
		    g2.setFont(font);
			// Get the FontMetrics
		    FontMetrics metrics = g2.getFontMetrics(font);
		    // Determine the X coordinate for the text
		    int sx = x + 2 + (width - metrics.stringWidth(text)) / 2;
		    // Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
		    int sy = y + 1 + ((height - metrics.getHeight()) / 2) + metrics.getAscent();
		    
			// Draw
			g2.setColor(fgColor);
			g2.drawString(text, sx, sy);
		}

		@Override
		public int getIconWidth() {
			return width;
		}

		@Override
		public int getIconHeight() {
			// TODO Auto-generated method stub
			return height;
		}
	}
	
	// =================================================================================================================
	// TODO Make it API
	public static final class NetworkSearchSource {
		
		private final String id;
		private final String name;
		private final String description;
		private final Icon icon;
		private final URL website;
		
		public NetworkSearchSource(String id, String name, Icon icon) {
			this(id, name, null, icon, null);
		}
		
		public NetworkSearchSource(String id, String name, String description, Icon icon) {
			this(id, name, description, icon, null);
		}
		
		public NetworkSearchSource(String id, String name, String description, Icon icon, URL website) {
			if (id == null || id.trim().isEmpty())
				throw new IllegalArgumentException("'id' must not be null or blank.");
			if (name == null || name.trim().isEmpty())
				throw new IllegalArgumentException("'name' must not be null or blank.");
			if (icon == null)
				throw new IllegalArgumentException("'icon' must not be null.");
			
			this.id = id;
			this.name = name;
			this.description = description;
			this.icon = icon;
			this.website = website;
		}

		public String getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public String getDescription() {
			return description;
		}

		public Icon getIcon() {
			return icon;
		}

		public URL getWebsite() {
			return website;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 11;
			result = prime * result + ((id == null) ? 0 : id.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			NetworkSearchSource other = (NetworkSearchSource) obj;
			if (id == null) {
				if (other.id != null)
					return false;
			} else if (!id.equals(other.id))
				return false;
			return true;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
}
