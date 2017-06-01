package org.cytoscape.webservice.internal.ui;

/*
 * #%L
 * Cytoscape Webservice Impl (webservice-impl)
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

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIDefaults;
import javax.swing.WindowConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.util.swing.OpenBrowser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * About page for web service clients or other plugins. Accepts HTML as the
 * argument.
 */
public final class AboutDialog extends JDialog implements HyperlinkListener {

	private static final long serialVersionUID = 8966870102741519552L;

	private static final Logger logger = LoggerFactory.getLogger(AboutDialog.class);

	private static final Dimension WINDOW_SIZE = new Dimension(500, 400);

	private final CyServiceRegistrar serviceRegistrar;

	private JLabel titleLabel;
	private JEditorPane mainEditorPane;
	private JScrollPane mainScrollPane;

	/** Creates new form WSAboutDialog */
	public AboutDialog(Window parent, Dialog.ModalityType modal, final CyServiceRegistrar serviceRegistrar) {
		super(parent, modal);
		this.serviceRegistrar = serviceRegistrar;
		initComponents();
		mainEditorPane.setEditable(false);
		mainEditorPane.addHyperlinkListener(this);
		setLocationRelativeTo(parent);
		setModalityType(DEFAULT_MODALITY_TYPE);
		this.setPreferredSize(WINDOW_SIZE);
		this.setSize(WINDOW_SIZE);
	}

	public AboutDialog(Window parent, Dialog.ModalityType modal, String title, Icon icon, URL contentURL,
			final CyServiceRegistrar serviceRegistrar) {
		super(parent, modal);
		this.serviceRegistrar = serviceRegistrar;
		initComponents();
		mainEditorPane.setContentType("text/html");
		this.setPreferredSize(WINDOW_SIZE);
		this.setSize(WINDOW_SIZE);
	}

	public void showDialog(final String title, final String description) {
		setTitle("About " + title);
		titleLabel.setText(title);

		URL target = null;
		mainEditorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		try {
			target = new URL(description);
		} catch (MalformedURLException e) {
			mainEditorPane.setContentType("text/html");
			mainEditorPane.setText(description);
			repaint();
			setVisible(true);
			return;
		}

		try {
			mainEditorPane.setPage(target);
			pack();
			repaint();
			setVisible(true);
		} catch (IOException e) {
			mainEditorPane.setText("Could not connect to " + target.toString());
			pack();
			repaint();
			setVisible(true);
		}
	}

	@SuppressWarnings("serial")
	private void initComponents() {
		setTitle("About");
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		mainEditorPane = new JEditorPane();
		mainEditorPane.setBorder(BorderFactory.createEmptyBorder());
		mainEditorPane.setEditable(false);
		mainEditorPane.setBackground(getBackground());
		
		if (LookAndFeelUtil.isNimbusLAF()) {
			// Nimbus does not respect background color settings for JEditorPane,
			// so this is necessary to override its color:
			final UIDefaults defaults = new UIDefaults();
			defaults.put("EditorPane[Enabled].backgroundPainter", getBackground());
			mainEditorPane.putClientProperty("Nimbus.Overrides", defaults);
			mainEditorPane.putClientProperty("Nimbus.Overrides.InheritDefaults", true);
		}
		
		mainScrollPane = new JScrollPane();
		mainScrollPane.setBorder(BorderFactory.createEmptyBorder());
		mainScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		mainScrollPane.setViewportView(mainEditorPane);
		
		titleLabel = new JLabel(); // Client name to be set here...
		titleLabel.setFont(new Font(titleLabel.getName(), Font.BOLD, 18));
		titleLabel.setHorizontalAlignment(JLabel.CENTER);

		final JButton closeButton = new JButton(new AbstractAction("Close") {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		
		final JPanel buttonPanel = LookAndFeelUtil.createOkCancelPanel(closeButton, null);
		final JPanel contents = new JPanel();
		
		final JSeparator sep1 = new JSeparator();
		final JSeparator sep2 = new JSeparator();
		
		final GroupLayout layout = new GroupLayout(contents);
		contents.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGap(20)
				.addGroup(layout.createParallelGroup(Alignment.LEADING)
						.addComponent(titleLabel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(sep1, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(mainScrollPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(sep2, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(buttonPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				)
				.addGap(20)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(titleLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(sep1, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(mainScrollPane, DEFAULT_SIZE, 215, Short.MAX_VALUE)
				.addComponent(sep2, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(buttonPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);

		getContentPane().add(contents);
		
		LookAndFeelUtil.setDefaultOkCancelKeyStrokes(getRootPane(), closeButton.getAction(), closeButton.getAction());
		getRootPane().setDefaultButton(closeButton);
		pack();
	}

	@Override
	public void hyperlinkUpdate(HyperlinkEvent e) {
		if (e.getEventType() != HyperlinkEvent.EventType.ACTIVATED)
			return;

		String url = e.getURL().toString();

		try {
			serviceRegistrar.getService(OpenBrowser.class).openURL(url);
		} catch (Exception err) {
			logger.warn("Unable to open browser for " + url, err);
		}
	}
}