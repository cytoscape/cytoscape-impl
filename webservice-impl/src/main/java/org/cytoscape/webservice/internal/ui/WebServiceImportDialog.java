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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.WindowConstants;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.io.webservice.WebServiceClient;
import org.cytoscape.io.webservice.swing.WebServiceGUIClient;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.work.TaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebServiceImportDialog<T> extends JDialog {

	private static final long serialVersionUID = 4454012178961756787L;
	private static final Logger logger = LoggerFactory.getLogger(WebServiceImportDialog.class);

	// Default icon for about dialog
	private static final Icon DEF_ICON = new ImageIcon(
			WebServiceImportDialog.class.getResource("/images/stock_internet-32.png"));

	private static final String NO_CLIENT = "No Service Client";

	private JButton cancelButton;
	private JComboBox datasourceComboBox;
	private JLabel datasourceLabel;
	private JTabbedPane mainTabbedPane;
	private JPanel propertyPanel;
	private JScrollPane propertyScrollPane;
	private JButton searchButton;
	private JScrollPane searchTermScrollPane;
	private JTextPane queryTextPane;
	private JButton aboutButton;
	private JPanel buttonPanel;
	private JPanel queryPanel;
	private JButton clearButton;
	private JPanel dataQueryPanel;
	private JPanel datasourcePanel;
	private JLabel titleIconLabel;
	private JPanel titlePanel;

	// Registered web service clients
	private Set<WebServiceClient> clients;

	// Client-Dependent GUI panels
	private Map<WebServiceClient, Container> serviceUIPanels = new HashMap<WebServiceClient, Container>();
	private int numClients;

	private final TaskManager<?, ?> taskManager;
	private final Class<T> type;
	private final OpenBrowser openBrowser;
	
	boolean readyToShow;
	
	public WebServiceImportDialog(final Class<T> type,
								  final String title,
								  final CySwingApplication cySwingApplicationServiceRef,
								  final TaskManager<?, ?> taskManager,
								  final OpenBrowser openBrowser) {
		super(cySwingApplicationServiceRef.getJFrame(), false);
		
		if (taskManager == null)
			throw new NullPointerException("TaskManager is null.");

		this.type = type;
		this.taskManager = taskManager;
		this.openBrowser = openBrowser;

		numClients = 0;
		this.clients = new HashSet<WebServiceClient>();

		initGUI();

		datasourceComboBox.addItem(NO_CLIENT);
		setComponentsEnabled(false);
		
		this.setTitle(title);
	}
	
	@SuppressWarnings("rawtypes")
	public void addClient(final WebServiceClient client, Map props) {
		if (!typeCheck(client))
			return;
		
		if (this.numClients == 0)
			this.datasourceComboBox.removeAllItems();
		
		datasourceComboBox.addItem(client);
		this.clients.add((WebServiceClient) client);
		numClients++;
		setComponentsEnabled(true);
		
		if (client instanceof WebServiceGUIClient) {
			serviceUIPanels.put((WebServiceClient) client, null);
		}
//		datasourceComboBox.setSelectedItem(client);
		if (datasourceComboBox.getModel().getSize() != 0)
			datasourceComboBox.setSelectedIndex(0);
		datasourceComboBoxActionPerformed(null);
		logger.info("New network import client registered: " + client);
	}
	
	@SuppressWarnings("rawtypes")
	public void removeClient(final WebServiceClient client, Map props) {
		if (!typeCheck(client))
			return;
		
		datasourceComboBox.removeItem(client);
		this.clients.remove(client);
		serviceUIPanels.remove(client);
		numClients--;
		
		if (numClients == 0) {
			this.datasourceComboBox.removeAllItems();
			this.datasourceComboBox.addItem(NO_CLIENT);
			setComponentsEnabled(false);
		}
	}

	private boolean typeCheck(final WebServiceClient client) {
		final Class<?>[] interfaces = client.getClass().getInterfaces();
		boolean found = false;
		for(final Class<?> inf: interfaces) {
			if(inf.equals(type)) {
				found = true;
				break;
			}
		}
		return found;
	}

	private void initGUI() {
		initComponents();

		// If we have no data sources, show the install panel
		getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(queryPanel, BorderLayout.CENTER);
		this.pack();

		// Initialize GUI panel.
		datasourceComboBoxActionPerformed(null);
	}
	
	private void setComponentsEnabled(boolean enable) {
		datasourceComboBox.setEnabled(enable);
		this.searchButton.setEnabled(enable);
		this.aboutButton.setEnabled(enable);
		this.cancelButton.setEnabled(enable);
	}

	private void initComponents() {
		mainTabbedPane = new JTabbedPane();
		searchTermScrollPane = new JScrollPane();
		queryTextPane = new JTextPane();
		propertyPanel = new JPanel();

		queryTextPane.setFont(new java.awt.Font("SansSerif", 0, 12));
		queryTextPane.setText("Please enter search terms...");
		searchTermScrollPane.setViewportView(queryTextPane);

		mainTabbedPane.addTab("Query", searchTermScrollPane);

		GroupLayout propertyPanelLayout = new GroupLayout(propertyPanel);
		propertyPanel.setLayout(propertyPanelLayout);
		propertyPanelLayout.setHorizontalGroup(propertyPanelLayout.createParallelGroup(Alignment.LEADING)
				.addGap(0, 408, Short.MAX_VALUE));
		propertyPanelLayout.setVerticalGroup(propertyPanelLayout.createParallelGroup(Alignment.LEADING)
				.addGap(0, 303, Short.MAX_VALUE));

		propertyScrollPane = new JScrollPane();
		propertyScrollPane.setViewportView(propertyPanel);
		mainTabbedPane.addTab("Search Property", propertyScrollPane);

		titlePanel = new JPanel();
		titleIconLabel = new JLabel();
		datasourcePanel = new JPanel();
		datasourceLabel = new JLabel();
		datasourceComboBox = new JComboBox();
		datasourceComboBox.setRenderer(new ClientComboBoxCellRenderer());
		aboutButton = new JButton();
		searchButton = new JButton();
		cancelButton = new JButton();
		clearButton = new JButton();
		dataQueryPanel = new JPanel();

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		GroupLayout titlePanelLayout = new GroupLayout(titlePanel);
		titlePanel.setLayout(titlePanelLayout);
		titlePanelLayout.setHorizontalGroup(titlePanelLayout.createParallelGroup(Alignment.LEADING)
				.addComponent(titleIconLabel, PREFERRED_SIZE, 461, PREFERRED_SIZE));
		titlePanelLayout.setVerticalGroup(titlePanelLayout.createParallelGroup(Alignment.LEADING)
				.addComponent(titleIconLabel));

		datasourceLabel.setText("Data Source:");

		datasourceComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				datasourceComboBoxActionPerformed(evt);
			}
		});

		aboutButton.setText("About");
		aboutButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				aboutButtonActionPerformed(evt);
			}
		});

		GroupLayout dataSourceLayout = new GroupLayout(datasourcePanel);
		datasourcePanel.setLayout(dataSourceLayout);
		dataSourceLayout.setAutoCreateContainerGaps(false);
		dataSourceLayout.setAutoCreateGaps(true);
		
		dataSourceLayout.setHorizontalGroup(dataSourceLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(dataSourceLayout.createSequentialGroup()
						.addContainerGap()
						.addComponent(datasourceLabel)
						.addComponent(datasourceComboBox, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addGap(20, 20, Short.MAX_VALUE)
						.addComponent(aboutButton)
						.addContainerGap()
				));
		dataSourceLayout.setVerticalGroup(dataSourceLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(dataSourceLayout.createSequentialGroup()
						.addGroup(dataSourceLayout.createParallelGroup(Alignment.BASELINE)
									.addComponent(datasourceLabel)
									.addComponent(aboutButton)
									.addComponent(datasourceComboBox, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE))
				));

		searchButton.setText("Search");
		searchButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				searchButtonActionPerformed();
			}
		});

		cancelButton.setText("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				cancelButtonActionPerformed(evt);
			}
		});

		clearButton.setText("Clear");
		clearButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				clearButtonActionPerformed(evt);
			}
		});

		buttonPanel = LookAndFeelUtil.createOkCancelPanel(searchButton, cancelButton, clearButton);
		
		GroupLayout dataQueryPanelLayout = new GroupLayout(dataQueryPanel);
		dataQueryPanel.setLayout(dataQueryPanelLayout);
		dataQueryPanelLayout.setHorizontalGroup(dataQueryPanelLayout.createParallelGroup(Alignment.LEADING)
				.addGap(0, 461, Short.MAX_VALUE));
		dataQueryPanelLayout.setVerticalGroup(dataQueryPanelLayout.createParallelGroup(Alignment.LEADING)
				.addGap(0, 247, Short.MAX_VALUE));

		queryPanel = new JPanel();
		GroupLayout layout = new GroupLayout(queryPanel);
		queryPanel.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
				.addComponent(titlePanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(datasourcePanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(dataQueryPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(buttonPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(titlePanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(datasourcePanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(dataQueryPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(buttonPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);

		dataQueryPanel.setLayout(new BorderLayout());
	}

	public void prepareForDisplay() {
		// Initialize the selected client GUI here so that we can compute the
		// proper initial bounds of the dialog when it's first displayed.
		readyToShow = true;
		datasourceComboBoxActionPerformed(null);
	}
	
	private void searchButtonActionPerformed() {
		final Object selected = datasourceComboBox.getSelectedItem();
		if (selected == null)
			return;

		WebServiceClient client = null;
		if (selected instanceof WebServiceClient) {
			client = (WebServiceClient) selected;
		} else {
			throw new IllegalStateException("Selected client is not a compatible one.");
		}

		// Set query. Just pass the text in the panel.
		taskManager.execute(client.createTaskIterator(this.queryTextPane.getText()));
	}

	/**
	 * Clear query text field.
	 */
	private void clearButtonActionPerformed(ActionEvent evt) {
		// Just set empty string for the field.
		queryTextPane.setText("");
	}

	private void cancelButtonActionPerformed(ActionEvent evt) {
		// Do nothing. Just hide this window.
		queryTextPane.setText("");
		dispose();
	}

	private void datasourceComboBoxActionPerformed(ActionEvent evt) {
		// This method gets triggered whenever the model changes.  However,
		// we don't want to initialize the GUI until we're actually ready to
		// show the dialog.
		if (!readyToShow) {
			return;
		}
		
		Object selected = datasourceComboBox.getSelectedItem();
		if (selected == null) {
			selected = datasourceComboBox.getItemAt(0);
			if (selected == null)
				return;
		}

		queryTextPane.setText("");

		if (selected instanceof WebServiceClient == false)
			return;

		final WebServiceClient client = (WebServiceClient) selected;

		// Update Panel
		dataQueryPanel.removeAll();
		
		Container gui = getUIPanel(client);

		if (gui != null) {
			// This service has custom panel.
			dataQueryPanel.add(gui, BorderLayout.CENTER);
			// Hide button panel.
			buttonPanel.setVisible(false);
		} else {
			// Otherwise, use the default panel.
			dataQueryPanel.add(mainTabbedPane, BorderLayout.CENTER);
			buttonPanel.setVisible(true);
		}

		pack();
		repaint();
	}

	private Container getUIPanel(WebServiceClient client) {
		Container container = serviceUIPanels.get(client);
		if (container == null && client instanceof WebServiceGUIClient) {
			container = ((WebServiceGUIClient) client).getQueryBuilderGUI();
			if (container != null) {
				serviceUIPanels.put(client, container);
			}
		}
		return container;
	}

	private void aboutButtonActionPerformed(ActionEvent evt) {
		final WebServiceClient wsc = (WebServiceClient) datasourceComboBox.getSelectedItem();

		final String clientName = wsc.getDisplayName();
		final String description = wsc.getDescription();

		Icon icon = null;
		if (icon == null)
			icon = DEF_ICON;
		
		final AboutDialog aboutDialog = new AboutDialog(this, Dialog.ModalityType.APPLICATION_MODAL, openBrowser);
		aboutDialog.showDialog("About " + clientName, icon, description);
	}

	private final class ClientComboBoxCellRenderer extends DefaultListCellRenderer {

		private static final long serialVersionUID = 1494017058040636621L;

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			if (value instanceof WebServiceClient) {
				String displayName = ((WebServiceClient) value).getDisplayName();
				this.setText(displayName);
			}

			return this;

		}
	}
}
