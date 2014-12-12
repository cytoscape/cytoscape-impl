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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

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

	private final OpenBrowser openBrowser;

	private JEditorPane mainEditorPane;
	private JScrollPane mainScrollPane;
	private JLabel titleLabel;
	private JPanel titlePanel;
	private JPanel mainPanel;

	/** Creates new form WSAboutDialog */
	public AboutDialog(Window parent, Dialog.ModalityType modal, final OpenBrowser openBrowser) {
		super(parent, modal);
		this.openBrowser = openBrowser;
		initComponents();
		mainEditorPane.setEditable(false);
		mainEditorPane.addHyperlinkListener(this);
		setLocationRelativeTo(parent);
		setModalityType(DEFAULT_MODALITY_TYPE);
		this.setPreferredSize(WINDOW_SIZE);
		this.setSize(WINDOW_SIZE);
	}

	public AboutDialog(Window parent, Dialog.ModalityType modal, String title, Icon icon, URL contentURL,
			final OpenBrowser openBrowser) {
		super(parent, modal);
		this.openBrowser = openBrowser;
		initComponents();
		mainEditorPane.setContentType("text/html");
		this.setPreferredSize(WINDOW_SIZE);
		this.setSize(WINDOW_SIZE);
	}

	public void showDialog(String title, Icon icon, String description) {
		titleLabel.setText(title);
		titleLabel.setIcon(icon);

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

	private void initComponents() {
		setTitle("About");
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		mainPanel = new JPanel();
		
		mainEditorPane = new JEditorPane();
		mainEditorPane.setBorder(null);
		mainEditorPane.setEditable(false);
		
		mainScrollPane = new JScrollPane();
		mainScrollPane.setBorder(null);
		mainScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		mainScrollPane.setViewportView(mainEditorPane);
		
		titleLabel = new JLabel();
		titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
		titleLabel.setText("Client Name Here");

		titlePanel = new JPanel();
		
		GroupLayout titlePanelLayout = new GroupLayout(titlePanel);
		titlePanel.setLayout(titlePanelLayout);
		
		titlePanelLayout.setHorizontalGroup(titlePanelLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(titlePanelLayout.createSequentialGroup()
						.addContainerGap()
						.addComponent(titleLabel, DEFAULT_SIZE, 340, Short.MAX_VALUE)
						.addContainerGap()));
		titlePanelLayout.setVerticalGroup(titlePanelLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(titlePanelLayout.createSequentialGroup().addContainerGap()
						.addComponent(titleLabel, DEFAULT_SIZE, 32, Short.MAX_VALUE)
						.addContainerGap()));

		final GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
		mainPanel.setLayout(mainPanelLayout);
		
		mainPanelLayout.setHorizontalGroup(mainPanelLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(mainPanelLayout.createSequentialGroup()
						.addContainerGap()
						.addComponent(mainScrollPane)
						.addContainerGap()));
		mainPanelLayout.setVerticalGroup(mainPanelLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(mainPanelLayout.createSequentialGroup()
						.addContainerGap()
						.addComponent(mainScrollPane, DEFAULT_SIZE, 215, Short.MAX_VALUE)
						.addContainerGap()));

		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
				.addComponent(titlePanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(mainPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE));
		layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addComponent(titlePanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(mainPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)));

		pack();
	}

	@Override
	public void hyperlinkUpdate(HyperlinkEvent e) {
		if (e.getEventType() != HyperlinkEvent.EventType.ACTIVATED)
			return;

		String url = e.getURL().toString();

		try {
			openBrowser.openURL(url);
		} catch (Exception err) {
			logger.warn("Unable to open browser for " + url.toString(), err);
		}
	}
}