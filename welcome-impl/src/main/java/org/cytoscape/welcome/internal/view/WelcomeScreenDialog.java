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
import static javax.swing.GroupLayout.Alignment.LEADING;

import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.cytoscape.property.CyProperty;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.util.swing.OpenBrowser;

@SuppressWarnings("serial")
public class WelcomeScreenDialog extends JDialog {
	
	private static final String TITLE = "Welcome to Cytoscape";

	private JCheckBox checkBox;

	private final CyProperty<Properties> cyProps;

	// Child Panels
	private final NewNetworkPanel newNetPanel;
	private final OpenSessionPanel openPanel;
	private final NewsPanel newsPanel;
	private JPanel linksPanel;
	
	private JLabel about;
	private JLabel manual;
	private JLabel tutorial;

	private final List<JLabel> labelSet;
	private final Map<JLabel, String> urlMap;

	private final OpenBrowser openBrowser;
	
	public WelcomeScreenDialog(
			final NewNetworkPanel newNetPanel,
			final OpenSessionPanel openPanel,
			final NewsPanel newsPanel,
			final CyProperty<Properties> cyProps,
			final boolean hide,
			final OpenBrowser openBrowser,
			final Window owner
	) {
		super(owner);
		this.newNetPanel = newNetPanel;
		this.openPanel = openPanel;
		this.newsPanel = newsPanel;

		this.newNetPanel.setParentWindow(this);
		this.openPanel.setParentWindow(this);
		this.newsPanel.setParentWindow(this);

		this.cyProps = cyProps;
		this.openBrowser = openBrowser;

		labelSet = new ArrayList<>();
		urlMap = new HashMap<>();
		
		initComponents();
		checkBox.setSelected(hide);
	}

	public boolean getHideStatus() {
		return checkBox.isSelected();
	}

	private void initComponents() {
		this.setTitle(TITLE);
		this.setResizable(false);
		this.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
		
		linksPanel = new JPanel();
		
		about = new JLabel("<html><u>About Cytoscape</u></html>");
		manual = new JLabel("<html><u>Documentation</u></html>");
		tutorial = new JLabel("<html><u>Tutorials</u></html>");
		
		labelSet.add(about);
		labelSet.add(manual);
		labelSet.add(tutorial);
		
		// get OS string
		String os_str = System.getProperty("os.name") + "_" + System.getProperty("os.version");
		os_str = os_str.replace(" ", "_");
		
		urlMap.put(about, "http://www.cytoscape.org/what_is_cytoscape.html");
		urlMap.put(manual, "http://www.cytoscape.org/documentation_users.html");
		urlMap.put(tutorial, "http://opentutorials.cgl.ucsf.edu/index.php/Portal:Cytoscape3");

		for (final JLabel label : labelSet) {
			label.setFont(label.getFont().deriveFont(LookAndFeelUtil.INFO_FONT_SIZE));
			label.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
			label.setForeground(WelcomeScreenChildPanel.LINK_FONT_COLOR);
			label.setHorizontalAlignment(SwingConstants.LEFT);
			label.setHorizontalTextPosition(SwingConstants.LEFT);
			label.setCursor(new Cursor(Cursor.HAND_CURSOR));
			label.addMouseListener(new LabelMouseListener(urlMap.get(label)));
			linksPanel.add(label);
		}
		
		checkBox = new JCheckBox("Don't show again");
		checkBox.setHorizontalAlignment(SwingConstants.LEFT);
		checkBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				cyProps.getProperties().setProperty(WelcomeScreenAction.DO_NOT_DISPLAY_PROP_NAME,
						((Boolean) checkBox.isSelected()).toString());
			}
		});

		final JButton closeButton = new JButton(new AbstractAction("Close") {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		
		final JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(1, 2));
		
		final JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
		rightPanel.add(openPanel);
		rightPanel.add(newsPanel);

		mainPanel.add(newNetPanel);
		mainPanel.add(rightPanel);
		
		final JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
		bottomPanel.setBorder(new EmptyBorder(10, 10, 5, 10));
		bottomPanel.add(checkBox);
		bottomPanel.add(Box.createHorizontalGlue());
		bottomPanel.add(closeButton);

		final JSeparator sep = new JSeparator();
		
		final JPanel contents = new JPanel();
		final GroupLayout layout = new GroupLayout(contents);
		contents.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
		
		layout.setHorizontalGroup(layout.createParallelGroup(LEADING, true)
				.addComponent(mainPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(linksPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(sep, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(bottomPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(mainPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(linksPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(sep, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(bottomPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		
		getContentPane().add(contents);
		
		LookAndFeelUtil.setDefaultOkCancelKeyStrokes(getRootPane(), closeButton.getAction(), closeButton.getAction());
		getRootPane().setDefaultButton(closeButton);
		closeButton.requestFocusInWindow();
		
		pack();
	}

	private final class LabelMouseListener extends MouseAdapter {

		private final String url;

		LabelMouseListener(final String url) {
			this.url = url;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			dispose();
			openBrowser.openURL(url);
		}
	}
}
