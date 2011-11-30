/*
 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package org.cytoscape.webservice.internal.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle;

import org.cytoscape.io.webservice.NetworkImportWebServiceClient;
import org.cytoscape.io.webservice.SearchWebServiceClient;
import org.cytoscape.io.webservice.WebServiceClient;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default GUI component for network import web service clients.
 * 
 */
public class UnifiedNetworkImportDialog extends JDialog {

	private static final long serialVersionUID = 3333726113970459078L;
	private static final Logger logger = LoggerFactory.getLogger(UnifiedNetworkImportDialog.class);
	
	private static final String NO_CLIENT = "No Service Client";
	
	// Default icon for about dialog
	private static final Icon DEF_ICON = new ImageIcon(UnifiedNetworkImportDialog.class
					.getResource("/images/stock_internet-32.png"));

	private static final Icon NETWORK_IMPORT_ICON = new ImageIcon(
			UnifiedNetworkImportDialog.class
					.getResource("/images/networkImportIcon.png"));

	// Registered web service clients
	private Set<NetworkImportWebServiceClient> clients;

	// Key is display name, value is URI of service.
	private Map<String, String> clientNames;

	// Client-Dependent GUI panels
	private Map<NetworkImportWebServiceClient, Container> serviceUIPanels = new HashMap<NetworkImportWebServiceClient, Container>();
	private int numClients;
	
	private final TaskManager taskManager;

	/**
	 * Creates new form NetworkImportDialog
	 */
	public UnifiedNetworkImportDialog(final TaskManager taskManager) {
		super();
		if(taskManager == null)
			throw new NullPointerException("TaskManager is null.");
		
		this.taskManager = taskManager;
		
		numClients = 0;
		setModal(false);
		this.clients = new HashSet<NetworkImportWebServiceClient>();

		initGUI();
		
		datasourceComboBox.addItem(NO_CLIENT);
		setComponentsEnabled(false);
	}

	
	/**
	 * Will be used by Spring DM.
	 * 
	 * @param client
	 * @param props
	 */
	public void addNetworkImportClient(
			final NetworkImportWebServiceClient client, @SuppressWarnings("rawtypes") Map props) {
		if(this.numClients == 0)
			this.datasourceComboBox.removeAllItems();
		
		datasourceComboBox.addItem(client);
		this.clients.add(client);
		numClients++;
		setComponentsEnabled(true);
		
		if (client instanceof WebServiceClient) {
			WebServiceClient service = (WebServiceClient) client;
			Container container = service.getQueryBuilderGUI();
			if (container != null) {
				serviceUIPanels.put(client, container);
			}
		}
		logger.info("New network import client registered: " + client);
	}

	
	/**
	 * Will be used by Spring DM
	 * 
	 * @param client
	 * @param props
	 */
	public void removeNetworkImportClient(
			final NetworkImportWebServiceClient client, @SuppressWarnings("rawtypes") Map props) {
		
		datasourceComboBox.removeItem(client);
		this.clients.remove(client);
		serviceUIPanels.remove(client);
		numClients--;
		
		if(numClients == 0) {
			this.datasourceComboBox.removeAllItems();
			this.datasourceComboBox.addItem(NO_CLIENT);
			setComponentsEnabled(false);
		}
	}
	
	
	private void setComponentsEnabled(boolean enable) {
		datasourceComboBox.setEnabled(enable);
		this.searchButton.setEnabled(enable);
		this.aboutButton.setEnabled(enable);
		this.cancelButton.setEnabled(enable);
	}

	private void initGUI() {
		clientNames = new HashMap<String, String>();

		initComponents();
		setDatasource();

		// If we have no data sources, show the install panel
		getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(queryPanel, BorderLayout.CENTER);
		this.pack();
		
		setProperty(clientNames.get(datasourceComboBox.getSelectedItem()));

		// Initialize GUI panel.
		datasourceComboBoxActionPerformed(null);
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	private void initComponents() {
		mainTabbedPane = new javax.swing.JTabbedPane();
		searchTermScrollPane = new javax.swing.JScrollPane();
		queryTextPane = new javax.swing.JTextPane();
		propertyPanel = new javax.swing.JPanel();

		queryTextPane.setFont(new java.awt.Font("SansSerif", 0, 12));
		queryTextPane.setText("Please enter search terms...");
		searchTermScrollPane.setViewportView(queryTextPane);

		mainTabbedPane.addTab("Query", searchTermScrollPane);

		GroupLayout propertyPanelLayout = new GroupLayout(propertyPanel);
		propertyPanel.setLayout(propertyPanelLayout);
		propertyPanelLayout.setHorizontalGroup(propertyPanelLayout
				.createParallelGroup(GroupLayout.Alignment.LEADING).addGap(0,
						408, Short.MAX_VALUE));
		propertyPanelLayout.setVerticalGroup(propertyPanelLayout
				.createParallelGroup(GroupLayout.Alignment.LEADING).addGap(0,
						303, Short.MAX_VALUE));

		propertyScrollPane = new JScrollPane();
		propertyScrollPane.setViewportView(propertyPanel);
		mainTabbedPane.addTab("Search Property", propertyScrollPane);

		titlePanel = new javax.swing.JPanel();
		titleIconLabel = new javax.swing.JLabel();
		datasourcePanel = new javax.swing.JPanel();
		datasourceLabel = new javax.swing.JLabel();
		datasourceComboBox = new javax.swing.JComboBox();
		datasourceComboBox.setRenderer(new ClientComboBoxCellRenderer());
		aboutButton = new javax.swing.JButton();
		buttonPanel = new javax.swing.JPanel();
		searchButton = new javax.swing.JButton();
		cancelButton = new javax.swing.JButton();
		clearButton = new javax.swing.JButton();
		dataQueryPanel = new javax.swing.JPanel();

		setTitle("Import Network from Database");

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

		titlePanel.setBackground(new java.awt.Color(0, 0, 0));

		titleIconLabel.setIcon(NETWORK_IMPORT_ICON); // NOI18N

		GroupLayout titlePanelLayout = new GroupLayout(titlePanel);
		titlePanel.setLayout(titlePanelLayout);
		titlePanelLayout.setHorizontalGroup(titlePanelLayout
				.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(titleIconLabel, GroupLayout.PREFERRED_SIZE, 461,
						GroupLayout.PREFERRED_SIZE));
		titlePanelLayout.setVerticalGroup(titlePanelLayout.createParallelGroup(
				GroupLayout.Alignment.LEADING).addComponent(titleIconLabel));

		datasourceLabel.setFont(new java.awt.Font("SansSerif", 0, 12));
		datasourceLabel.setText("Data Source");

		datasourceComboBox
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						datasourceComboBoxActionPerformed(evt);
					}
				});

		aboutButton.setText("About");
		aboutButton.setMargin(new java.awt.Insets(2, 5, 2, 5));
		aboutButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				aboutButtonActionPerformed(evt);
			}
		});

		GroupLayout datasourcePanelLayout = new GroupLayout(datasourcePanel);
		datasourcePanel.setLayout(datasourcePanelLayout);
		datasourcePanelLayout.setHorizontalGroup(datasourcePanelLayout
				.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
						datasourcePanelLayout
								.createSequentialGroup()
								.addContainerGap()
								.addComponent(datasourceLabel)
								.addPreferredGap(
										LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(datasourceComboBox, 0, 301,
										Short.MAX_VALUE)
								.addPreferredGap(
										LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(aboutButton).addContainerGap()));
		datasourcePanelLayout
				.setVerticalGroup(datasourcePanelLayout
						.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addGroup(
								datasourcePanelLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												datasourcePanelLayout
														.createParallelGroup(
																GroupLayout.Alignment.BASELINE)
														.addComponent(
																datasourceLabel)
														.addComponent(
																aboutButton)
														.addComponent(
																datasourceComboBox,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE))
										.addContainerGap(
												GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)));

		buttonPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

		searchButton.setText("Search");
		searchButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				searchButtonActionPerformed();
			}
		});

		cancelButton.setText("Cancel");
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cancelButtonActionPerformed(evt);
			}
		});

		clearButton.setText("Clear");
		clearButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				clearButtonActionPerformed(evt);
			}
		});

		GroupLayout buttonPanelLayout = new GroupLayout(buttonPanel);
		buttonPanel.setLayout(buttonPanelLayout);
		buttonPanelLayout.setHorizontalGroup(buttonPanelLayout
				.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
						GroupLayout.Alignment.TRAILING,
						buttonPanelLayout
								.createSequentialGroup()
								.addContainerGap()
								.addComponent(clearButton)
								.addPreferredGap(
										LayoutStyle.ComponentPlacement.RELATED,
										225, Short.MAX_VALUE)
								.addComponent(cancelButton)
								.addPreferredGap(
										LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(searchButton).addContainerGap()));
		buttonPanelLayout
				.setVerticalGroup(buttonPanelLayout
						.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addGroup(
								GroupLayout.Alignment.TRAILING,
								buttonPanelLayout
										.createSequentialGroup()
										.addContainerGap(
												GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)
										.addGroup(
												buttonPanelLayout
														.createParallelGroup(
																GroupLayout.Alignment.BASELINE)
														.addComponent(
																searchButton)
														.addComponent(
																cancelButton)
														.addComponent(
																clearButton))
										.addContainerGap()));

		GroupLayout dataQueryPanelLayout = new GroupLayout(dataQueryPanel);
		dataQueryPanel.setLayout(dataQueryPanelLayout);
		dataQueryPanelLayout.setHorizontalGroup(dataQueryPanelLayout
				.createParallelGroup(GroupLayout.Alignment.LEADING).addGap(0,
						461, Short.MAX_VALUE));
		dataQueryPanelLayout.setVerticalGroup(dataQueryPanelLayout
				.createParallelGroup(GroupLayout.Alignment.LEADING).addGap(0,
						247, Short.MAX_VALUE));

		queryPanel = new JPanel();
		GroupLayout layout = new GroupLayout(queryPanel);
		queryPanel.setLayout(layout);
		layout.setHorizontalGroup(layout
				.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(titlePanel, GroupLayout.DEFAULT_SIZE,
						GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(datasourcePanel, GroupLayout.DEFAULT_SIZE,
						GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(buttonPanel, GroupLayout.DEFAULT_SIZE,
						GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(dataQueryPanel, GroupLayout.DEFAULT_SIZE,
						GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
		layout.setVerticalGroup(layout.createParallelGroup(
				GroupLayout.Alignment.LEADING)
				.addGroup(
						layout.createSequentialGroup()
								.addComponent(titlePanel,
										GroupLayout.PREFERRED_SIZE,
										GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(
										LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(datasourcePanel,
										GroupLayout.PREFERRED_SIZE,
										GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(
										LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(dataQueryPanel,
										GroupLayout.DEFAULT_SIZE,
										GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE)
								.addPreferredGap(
										LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(buttonPanel,
										GroupLayout.PREFERRED_SIZE,
										GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE)));

		dataQueryPanel.setLayout(new BorderLayout());
	} // </editor-fold>


	/**
	 * Execute the search task.
	 * 
	 * @param evt
	 */
	private void searchButtonActionPerformed() {
		final Object selected = datasourceComboBox.getSelectedItem();
		if(selected == null)
			return;
		
		WebServiceClient client = null;
		if(selected instanceof WebServiceClient) {
			client = (WebServiceClient) selected;
		} else {
			throw new IllegalStateException("Selected cleint is not a compatible client.");
		}
		
		// Set query.  Just pass the text in the panel.
		client.setQuery(this.queryTextPane.getText());
		taskManager.execute(client);
		
	}

	private void aboutButtonActionPerformed(ActionEvent evt) {
		
		final WebServiceClient wsc = (WebServiceClient)datasourceComboBox.getSelectedItem();
		
		final String clientName = wsc.getDisplayName();
		final String description = wsc.getDescription();
		
		// FIXME
		Icon icon = null;
		if (icon == null) {
			icon = DEF_ICON;
		}
		//AboutDialog.showDialog(clientName, icon, description);
	}
	
	private final class ImportNetworkTaskFactory implements TaskFactory {

		private final TaskFactory tFactory;
		
		ImportNetworkTaskFactory(TaskFactory tFactory) {
			this.tFactory = tFactory;
		}
		
		@Override
		public TaskIterator createTaskIterator() {
			final TaskIterator itr = new TaskIterator();
			itr.insertTasksAfter(new RegisterNetworkTask(), tFactory.createTaskIterator().next());
			return itr;
		}
		
	}
	
	private final class RegisterNetworkTask extends AbstractTask {

		@Override
		public void run(TaskMonitor taskMonitor) throws Exception {
			logger.debug("Executing register task ------------------");
			logger.info("DONE!");
		}
		
	}

	/**
	 * Clear query text field.
	 */
	private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {
		// Just set empty string for the field.
		queryTextPane.setText("");
	}

	private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
		// Do nothing. Just hide this window.
		queryTextPane.setText("");
		dispose();
	}

	private void datasourceComboBoxActionPerformed(ActionEvent evt) {
		
		final Object selected = datasourceComboBox.getSelectedItem();
		if(selected == null)
			return;
		
		queryTextPane.setText("");
		setProperty(clientNames.get(datasourceComboBox.getSelectedItem()));
			
		if(selected instanceof WebServiceClient == false)
			return;
		
		final WebServiceClient client = (WebServiceClient) selected;
		
		// Update Panel
		dataQueryPanel.removeAll();

		final Container gui = serviceUIPanels.get(client);
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

	private void setProperty(String clientID) {
//		final WebServiceClient<?> client = WebServiceClientManager
//				.getClient(clientID);
//
//		if (client == null)
//			return;
//
//		ModuleProperties props = client.getProps();
//		List<Tunable> tunables = props.getTunables();
//		propertyPanel = new JPanel(new GridLayout(0, 1));
//
//		for (Tunable tu : tunables) {
//			JPanel p = tu.getPanel();
//			p.setBackground(Color.white);
//
//			if (p != null)
//				propertyPanel.add(p);
//		}
//
//		propertyScrollPane.setViewportView(propertyPanel);
//		pack();
//		repaint();
	}

	private void setDatasource() {
//		List<WebServiceClient<?>> clients = WebServiceClientManager
//				.getAllClients();
//		for (WebServiceClient<?> client : clients) {
//			if (client instanceof NetworkImportWebServiceClient) {
//				this.datasourceComboBox.addItem(client.getDisplayName());
//				this.clientNames.put(client.getDisplayName(),
//						client.getClientID());
//
//				if (client instanceof WebServiceClientGUI
//						&& (((WebServiceClientGUI) client).getGUI() != null)) {
//					serviceUIPanels.put(client.getClientID(),
//							((WebServiceClientGUI) client).getGUI());
//				}
//				numDataSources++;
//			}
//		}
	}

//	private CyWebServiceEvent<String> buildEvent() {
//		final String clientID = clientNames.get(datasourceComboBox
//				.getSelectedItem());
//
//		// Update props here.
//		WebServiceClientManager.getClient(clientID).getProps().updateValues();
//
//		return new CyWebServiceEvent<String>(clientID,
//				WSEventType.SEARCH_DATABASE, queryTextPane.getText(),
//				WSEventType.IMPORT_NETWORK);
//	}

	// Variables declaration - do not modify
	private javax.swing.JButton cancelButton;
	private javax.swing.JComboBox datasourceComboBox;
	private javax.swing.JLabel datasourceLabel;
	private javax.swing.JTabbedPane mainTabbedPane;
	private javax.swing.JPanel propertyPanel;
	private JScrollPane propertyScrollPane;
	private javax.swing.JButton searchButton;
	private javax.swing.JScrollPane searchTermScrollPane;
	private javax.swing.JTextPane queryTextPane;
	private javax.swing.JButton aboutButton;
	private javax.swing.JPanel buttonPanel;
	private JPanel queryPanel;
	private javax.swing.JButton clearButton;
	private javax.swing.JPanel dataQueryPanel;
	private javax.swing.JPanel datasourcePanel;
	private javax.swing.JLabel titleIconLabel;
	private javax.swing.JPanel titlePanel;

//	/**
//	 * Task to import network from web service.
//	 * 
//	 * @author kono
//	 * 
//	 */
//	private class WSNetworkImportTask implements Task {
//		private String serviceName;
//		private CyWebServiceEvent<String> evt;
//		private TaskMonitor taskMonitor;
//
//		public WSNetworkImportTask(final String serviceName,
//				final CyWebServiceEvent<String> evt) {
//			this.evt = evt;
//			this.serviceName = serviceName;
//		}
//
//		public String getTitle() {
//			return "Loading network from web service...";
//		}
//
//		public void halt() {
//
//			cancelFlag = true;
//			Thread.currentThread().interrupt();
//			taskMonitor.setPercentCompleted(100);
//
//			// Kill the import task.
//			CyWebServiceEvent<String> cancelEvent = new CyWebServiceEvent<String>(
//					serviceName, WSEventType.CANCEL, null, null);
//			try {
//				WebServiceClientManager.getCyWebServiceEventSupport()
//						.fireCyWebServiceEvent(cancelEvent);
//			} catch (CyWebServiceException e) {
//				taskMonitor.setException(e, "Cancel Failed.");
//			}
//		}
//
//		public void run() {
//			cancelFlag = false;
//			taskMonitor.setStatus("Loading network from " + serviceName);
//			taskMonitor.setPercentCompleted(-1);
//
//			try {
//				WebServiceClientManager.getCyWebServiceEventSupport()
//						.fireCyWebServiceEvent(evt);
//			} catch (Exception e) {
//				taskMonitor.setException(e,
//						"Failed to load network from web service.");
//				return;
//			}
//			taskMonitor.setPercentCompleted(100);
//			taskMonitor.setStatus("Network successfully loaded.");
//		}
//
//		public void setTaskMonitor(TaskMonitor arg0)
//				throws IllegalThreadStateException {
//			this.taskMonitor = arg0;
//		}
//
//		protected TaskMonitor getTaskMonitor() {
//			return taskMonitor;
//		}
//	}
//
//	/**
//	 * Listening to events fired by service clients.
//	 * 
//	 * @param evt
//	 *            DOCUMENT ME!
//	 */
//	public void propertyChange(PropertyChangeEvent evt) {
//
//		if (cancelFlag)
//			return;
//
//		Object resultObject = evt.getNewValue();
//
//		if (evt.getPropertyName().equals(
//				WSResponseType.SEARCH_FINISHED.toString())
//				&& (resultObject != null)
//				&& resultObject instanceof DatabaseSearchResult) {
//			DatabaseSearchResult result = (DatabaseSearchResult) resultObject;
//
//			if (result.getNextMove().equals(WSEventType.IMPORT_NETWORK)) {
//				logger.info("Got search result from: " + evt.getSource()
//						+ ", Num result = " + result.getResultSize()
//						+ ", Source name = " + evt.getOldValue());
//
//				String[] message = {
//						result.getResultSize() + " records found in "
//								+ selectedClientID,
//						"Do you want to create new network from the search result?" };
//				int value = JOptionPane.showConfirmDialog(
//						Cytoscape.getDesktop(), message, "Import network",
//						JOptionPane.YES_NO_OPTION);
//
//				if (value == JOptionPane.YES_OPTION) {
//					CyWebServiceEvent<Object> evt2 = new CyWebServiceEvent<Object>(
//							evt.getOldValue().toString(),
//							WSEventType.IMPORT_NETWORK, result.getResult());
//
//					try {
//						WebServiceClientManager.getCyWebServiceEventSupport()
//								.fireCyWebServiceEvent(evt2);
//					} catch (CyWebServiceException e) {
//						// TODO Auto-generated catch block
//						if (task.getTaskMonitor() != null) {
//							task.getTaskMonitor().setException(e,
//									"Database search failed.");
//						}
//					}
//				}
//			}
//		} else if (evt.getPropertyName().equals(
//				WSResponseType.DATA_IMPORT_FINISHED.toString())) {
//
//			// If result is empty, just ignore it.
//			if (evt.getNewValue() == null)
//				return;
//
//			// VisualStyle style = ((NetworkImportWebServiceClient)
//			// WebServiceClientManager
//			// .getClient(selectedClientID)).getDefaultVisualStyle();
//			// if (style == null) {
//			// style = Cytoscape.getVisualMappingManager().getVisualStyle();
//			// }
//			//
//			// if (Cytoscape.getVisualMappingManager().getCalculatorCatalog()
//			// .getVisualStyle(style.getName()) == null)
//			// Cytoscape.getVisualMappingManager().getCalculatorCatalog()
//			// .addVisualStyle(style);
//			//
//			// Cytoscape.getVisualMappingManager().setVisualStyle(style);
//
//			// Name the network
//			final String[] message = {
//					"Network Loaded from " + selectedClientID,
//					"Please enter title for new network:" };
//			String value = JOptionPane.showInputDialog(Cytoscape.getDesktop(),
//					message, "Name new network", JOptionPane.QUESTION_MESSAGE);
//			if (value == null || value.length() == 0)
//				value = selectedClientID + " Network";
//
//			final CyNetwork cyNetwork = Cytoscape.getCurrentNetwork();
//			Cytoscape.getCurrentNetwork().setTitle(value);
//			Cytoscape.getDesktop().getNetworkPanel().updateTitle(cyNetwork);
//		}
//	}

	private final class ClientComboBoxCellRenderer extends
			DefaultListCellRenderer {
		
		private static final long serialVersionUID = -7944343422335332051L;

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			
			if(value instanceof WebServiceClient) {
				String displayName = ((WebServiceClient) value).getDisplayName();
				this.setText(displayName);
			}
			
			return this;

		}
	}
}
