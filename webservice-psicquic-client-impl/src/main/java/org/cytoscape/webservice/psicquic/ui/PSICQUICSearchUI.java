package org.cytoscape.webservice.psicquic.ui;

/*
 * #%L
 * Cytoscape PSIQUIC Web Service Impl (webservice-psicquic-client-impl)
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.cytoscape.application.swing.CyAction;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.create.CreateNetworkViewTaskFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.webservice.psicquic.PSICQUICRestClient;
import org.cytoscape.webservice.psicquic.PSICQUICRestClient.SearchMode;
import org.cytoscape.webservice.psicquic.PSIMI25VisualStyleBuilder;
import org.cytoscape.webservice.psicquic.RegistryManager;
import org.cytoscape.webservice.psicquic.task.SearchRecoredsTask;
import org.cytoscape.webservice.psicquic.ui.SelectorBuilder.Species;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskMonitor;


/**
 * Custom Search UI for PSICQUIC services.
 * 
 */
public class PSICQUICSearchUI extends JPanel {

	private static final long serialVersionUID = 3163269742016489767L;

	private static final Dimension PANEL_SIZE = new Dimension(680, 500);

	// Preset design elements for Search Panel
	private static final Color SEARCH_BORDER_COLOR = new Color(0x1E, 0x90, 0xFF, 200);
	private static final Border SEARCH_BORDER = BorderFactory.createLineBorder(SEARCH_BORDER_COLOR, 2);

	// Color Scheme
	private static final Font STRONG_FONT = new Font("SansSerif", Font.BOLD, 14);

	// Property name for saving selection
	static final String PROP_NAME = "psiqcuic.datasource.selection";

	// Fixed messages
	private static final String MIQL_MODE = "Search by Query Language (MIQL)";
	private static final String INTERACTOR_ID_LIST = "Search by ID (gene/protein/compound ID)";
	private static final String BY_SPECIES = "Import Interactome (this may take long time)";

	private final RegistryManager regManager;
	private final PSICQUICRestClient client;
	private final TaskManager<?, ?> taskManager;
	private final CyNetworkManager networkManager;
	private final CreateNetworkViewTaskFactory createViewTaskFactory;
	private final CyProperty<Properties> props;
	private final CyAction mergeAction;
	
	private JEditorPane queryArea;
	private SourceStatusPanel statesPanel;
	private JScrollPane queryScrollPane;

	private JPanel searchPanel;
	private JLabel modeLabel;
	private JButton searchButton;
	private JButton refreshButton;

	private JPanel speciesPanel;

	private JComboBox searchModeSelector;
	private JComboBox speciesSelector;

	private JPanel searchConditionPanel;

	private SearchMode mode = SearchMode.MIQL;
	private String searchAreaTitle = MIQL_MODE;

	private boolean firstClick = true;

	private Set<String> sourceSet = new HashSet<String>();
	
	private final PSIMI25VisualStyleBuilder vsBuilder;
	private final VisualMappingManager vmm;
	private final PSIMITagManager tagManager;
	
	private final CyServiceRegistrar registrar;


	public PSICQUICSearchUI(final CyNetworkManager networkManager, final RegistryManager regManager,
			final PSICQUICRestClient client, final TaskManager<?, ?> tmManager,
			final CreateNetworkViewTaskFactory createViewTaskFactory, final PSIMI25VisualStyleBuilder vsBuilder,
			final VisualMappingManager vmm, final PSIMITagManager tagManager, final CyProperty<Properties> props, 
			final CyServiceRegistrar registrar, final CyAction mergeAction) {
		this.regManager = regManager;
		this.client = client;
		this.taskManager = tmManager;
		this.networkManager = networkManager;
		this.createViewTaskFactory = createViewTaskFactory;
		this.vmm = vmm;
		this.vsBuilder = vsBuilder;
		this.tagManager = tagManager;
		this.props = props;
		this.registrar = registrar;
		this.mergeAction = mergeAction;
		
		// Load Property if available.
		final Properties cyProp = props.getProperties();
		final String selectionListProp = cyProp.getProperty(PROP_NAME);
		
		if(selectionListProp == null) {
			// Create new one if there is no defaults.
			cyProp.setProperty(PROP_NAME, "");
		} else {
			setSelected(selectionListProp);
		}

		init();
	}

	private void init() {
		// Background (Base Panel) settings
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setBackground(Color.white);
		this.setBorder(new EmptyBorder(10, 10, 10, 10));

		searchConditionPanel = new JPanel();
		searchConditionPanel.setBackground(Color.white);
		searchConditionPanel.setLayout(new BoxLayout(searchConditionPanel, BoxLayout.Y_AXIS));
		final TitledBorder searchConditionPanelBorder = BorderFactory.createTitledBorder(SEARCH_BORDER,
				"1. Enter Search Conditions");
		searchConditionPanelBorder.setTitleFont(STRONG_FONT);
		searchConditionPanel.setBorder(searchConditionPanelBorder);

		createDBlistPanel();
		createQueryPanel();
		createQueryModePanel();
		createSpeciesPanel();

		queryModeChanged();

		this.setSize(PANEL_SIZE);
		this.setPreferredSize(PANEL_SIZE);

		searchConditionPanel.add(queryScrollPane);
		searchConditionPanel.add(searchPanel);

		this.add(searchConditionPanel);
		this.add(statesPanel);
		
	}
	
	private final void setSelected(final String selected) {
		final String[] sources = selected.split(",");
		for(String source:sources) {
			sourceSet.add(source);
		}
	}


	private final void setSelected() {
		this.sourceSet = statesPanel.getSelected();
	}

	private final void createDBlistPanel() {
		// Source Status - list of remote databases
		this.statesPanel = new SourceStatusPanel("", client, regManager, networkManager, null, taskManager, mode,
				createViewTaskFactory, vsBuilder, vmm, tagManager, props, registrar, mergeAction);
		statesPanel.enableComponents(false);
		statesPanel.setSelected(sourceSet);
	}

	private final void createQueryPanel() {
		// Query text area
		queryScrollPane = new JScrollPane();
		queryScrollPane.setBackground(Color.white);
		queryArea = new JEditorPane();

		final TitledBorder border = new TitledBorder(searchAreaTitle);
		border.setTitleFont(STRONG_FONT);
		queryScrollPane.setBorder(border);
		queryScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		queryScrollPane.setPreferredSize(new Dimension(500, 150));
		queryScrollPane.setViewportView(queryArea);
		searchConditionPanel.add(queryScrollPane);

		queryArea.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (firstClick) {
					firstClick = false;
					searchButton.setEnabled(true);
				}
			}
		});
	}

	private final void createQueryModePanel() {
		// Query type selector - Gene ID, MIQL, or species
		modeLabel = new JLabel("Search Mode:");
		this.searchModeSelector = new JComboBox();
		this.searchModeSelector.setPreferredSize(new Dimension(200, 30));
		this.searchModeSelector.addItem(BY_SPECIES);
		this.searchModeSelector.addItem(INTERACTOR_ID_LIST);
		this.searchModeSelector.addItem(MIQL_MODE);
		this.searchModeSelector.setSelectedItem(INTERACTOR_ID_LIST);

		this.searchModeSelector.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				queryModeChanged();
			}
		});

		searchPanel = new JPanel();
		searchPanel.setBackground(Color.white);
		searchPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.X_AXIS));

		searchButton = new JButton("Search");
		searchButton.setPreferredSize(new java.awt.Dimension(90, 28));
		searchButton.setFont(new Font("SansSerif", Font.BOLD, 12));
		searchButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				search();
				statesPanel.enableComponents(true);
			}
		});
		searchButton.setEnabled(false);

		refreshButton = new JButton("Refresh");
		refreshButton.setPreferredSize(new java.awt.Dimension(90, 28));
		refreshButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				refreshButtonActionPerformed();
			}
		});

		searchPanel.add(modeLabel);
		searchPanel.add(searchModeSelector);
		searchPanel.add(searchButton);
		searchPanel.add(refreshButton);

		searchConditionPanel.add(searchPanel);
	}

	private final void createSpeciesPanel() {
		speciesPanel = new JPanel();
		speciesPanel.setBackground(Color.white);
		final JLabel speciesLabel = new JLabel("Select Species:");

		final SelectorBuilder speciesBuilder = new SelectorBuilder();
		speciesSelector = speciesBuilder.getComboBox();
		speciesPanel.setLayout(new BoxLayout(speciesPanel, BoxLayout.X_AXIS));
		speciesPanel.add(speciesLabel);
		speciesPanel.add(speciesSelector);
		searchButton.setEnabled(true);
		
		speciesSelector.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				searchButton.setEnabled(true);
			}
		});
	}

	private void refreshButtonActionPerformed() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				regManager.refresh();
				queryModeChanged();
			}
		});
	}

	private void search() {
		final SearchRecoredsTask searchTask = new SearchRecoredsTask(client, mode);
		final Map<String, String> activeSource = regManager.getActiveServices();
		String query = this.queryArea.getText();

		// Query by species
		if (mode == SearchMode.SPECIES)
			query = buildSpeciesQuery();

		this.setSelected();
		statesPanel.setQuery(query);
		searchTask.setQuery(query);
		searchTask.setTargets(activeSource.values());

		taskManager.execute(new TaskIterator(searchTask, new SetTableTask(searchTask)));
	}

	private final String buildSpeciesQuery() {
		mode = SearchMode.SPECIES;
		final Object selectedItem = this.speciesSelector.getSelectedItem();
		final Species species = (Species) selectedItem;

		return "taxidA:\"" + species.toString() + "\" AND taxidB:\"" + species.toString() + "\"";
	}



	/**
	 * Update table based on returned result
	 *
	 */
	private final class SetTableTask extends AbstractTask {

		final SearchRecoredsTask searchTask;

		public SetTableTask(final SearchRecoredsTask searchTask) {
			this.searchTask = searchTask;
		}

		@Override
		public void run(TaskMonitor taskMonitor) throws Exception {
			final Map<String, Long> result = searchTask.getResult();

			String query;
			// Query by species
			if (mode == SearchMode.SPECIES)
				query = buildSpeciesQuery();
			else {
				query = queryArea.getText();
			}

			statesPanel = new SourceStatusPanel(query, client, regManager, networkManager, result, taskManager, mode,
					createViewTaskFactory, vsBuilder, vmm, tagManager, props, registrar, mergeAction);
			statesPanel.sort();
			updateGUILayout();
			statesPanel.enableComponents(true);
			statesPanel.setSelected(sourceSet);
		}
	}

	private final void updateGUILayout() {
		searchConditionPanel.removeAll();
		removeAll();

		if (mode == SearchMode.SPECIES) {
			searchConditionPanel.add(speciesPanel);
			searchButton.setEnabled(true);
		} else
			searchConditionPanel.add(queryScrollPane);

		searchConditionPanel.add(searchPanel);

		// Add to main panel
		this.add(searchConditionPanel);
		this.add(statesPanel);

		if (getRootPane() != null) {
			Window parentWindow = ((Window) getRootPane().getParent());
			parentWindow.pack();
			repaint();
			parentWindow.toFront();
		}
	}

	private final void queryModeChanged() {
		final Object selectedObject = this.searchModeSelector.getSelectedItem();
		if (selectedObject == null)
			return;

		final String modeString = selectedObject.toString();
		final String query;
		if (modeString.equals(MIQL_MODE)) {
			mode = SearchMode.MIQL;
			searchAreaTitle = MIQL_MODE;
			query = queryArea.getText();
			searchButton.setEnabled(false);
		} else if (modeString.equals(INTERACTOR_ID_LIST)) {
			mode = SearchMode.INTERACTOR;
			searchAreaTitle = INTERACTOR_ID_LIST;
			query = queryArea.getText();
			searchButton.setEnabled(false);
		} else {
			mode = SearchMode.SPECIES;
			searchAreaTitle = BY_SPECIES;
			query = buildSpeciesQuery();
			searchButton.setEnabled(true);
		}

		firstClick = true;

		queryArea.setText("");
		queryScrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		statesPanel = new SourceStatusPanel(query, client, regManager, networkManager, null, taskManager, mode,
				createViewTaskFactory, vsBuilder, vmm, tagManager, props, registrar, mergeAction);
		statesPanel.sort();

		updateGUILayout();
		statesPanel.enableComponents(false);
		statesPanel.setSelected(sourceSet);
	}
}