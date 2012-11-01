package org.cytoscape.webservice.psicquic.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.task.create.CreateNetworkViewTaskFactory;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.webservice.psicquic.PSICQUICRestClient;
import org.cytoscape.webservice.psicquic.PSIMI25VisualStyleBuilder;
import org.cytoscape.webservice.psicquic.PSICQUICRestClient.SearchMode;
import org.cytoscape.webservice.psicquic.QueryMode;
import org.cytoscape.webservice.psicquic.RegistryManager;
import org.cytoscape.webservice.psicquic.task.SearchRecoredsTask;
import org.cytoscape.webservice.psicquic.ui.SelectorBuilder.Species;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskMonitor;

public class PSICQUICSearchUI extends JPanel {

	private static final long serialVersionUID = 3163269742016489767L;

	private static final String MIQL_REFERENCE_PAGE_URL = "http://code.google.com/p/psicquic/wiki/MiqlReference";

	// Color Scheme
	private static final Color MIQL_COLOR = new Color(0x7f, 0xff, 0xd4);
	private static final Color ID_LIST_COLOR = new Color(0xff, 0xa5, 0x00);
	private static final Font STRONG_FONT = new Font("SansSerif", Font.BOLD, 14);

	// Fixed messages
	private static final String MIQL_MODE = "Search by Query Language (MIQL)";
	private static final String INTERACTOR_ID_LIST = "Search by ID (gene/protein/compound ID)";
	private static final String BY_SPECIES = "Search by Species";

	private static final String MIQL_QUERY_AREA_MESSAGE_STRING = "Please enter search query (MIQL) here.  "
			+ "Currently the result table shows number of all binary interactions available in the database.  "
			+ "\nIf you need help, please click Syntax Help button below.";
	private static final String INTERACTOR_LIST_AREA_MESSAGE_STRING = "Please enter list of genes/proteins/compounds, separated by space.  "
			+ "Currently the result table shows number of all binary interactions available in the database.";

	private final RegistryManager regManager;
	private final PSICQUICRestClient client;
	private final TaskManager<?, ?> taskManager;
	private final CyNetworkManager networkManager;
	private final CreateNetworkViewTaskFactory createViewTaskFactory;

	private JEditorPane queryArea;
	private SourceStatusPanel statesPanel;
	private JScrollPane queryScrollPane;

	private JPanel searchPanel;
	private JLabel modeLabel;
	private JButton helpButton;
	private JButton searchButton;
	private JButton refreshButton;

	private JPanel queryBuilderPanel;
	
	private JPanel speciesPanel;
	
	private JComboBox searchModeSelector;
	// private JComboBox speciesSelector;
	private ButtonGroup bg;

	private SearchMode mode = SearchMode.MIQL;
	private String searchAreaTitle = MIQL_MODE;

	private boolean firstClick = true;

	private final OpenBrowser openBrowserUtil;

	private final PSIMI25VisualStyleBuilder vsBuilder;
	private final VisualMappingManager vmm;

	public PSICQUICSearchUI(final CyNetworkManager networkManager, final RegistryManager regManager,
			final PSICQUICRestClient client, final TaskManager<?, ?> tmManager,
			final CreateNetworkViewTaskFactory createViewTaskFactory, final OpenBrowser openBrowserUtil,
			final PSIMI25VisualStyleBuilder vsBuilder, final VisualMappingManager vmm) {
		this.regManager = regManager;
		this.client = client;
		this.taskManager = tmManager;
		this.networkManager = networkManager;
		this.createViewTaskFactory = createViewTaskFactory;
		this.openBrowserUtil = openBrowserUtil;
		this.vmm = vmm;
		this.vsBuilder = vsBuilder;

		init();
	}

	private void init() {
		// Background (Base Panel) settings
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setBackground(Color.white);
		this.setBorder(new EmptyBorder(10, 10, 10, 10));

		createDBlistPanel();
		createQueryPanel();
		createQueryModePanel();
		createSpeciesPanel();
		
		queryModeChanged();
		

		// this.queryBuilderPanel = new JPanel();
		// queryBuilderPanel.setBackground(Color.white);
		// queryBuilderPanel.setLayout(new BoxLayout(queryBuilderPanel,
		// BoxLayout.X_AXIS));
		// queryBuilderPanel.setBorder(new TitledBorder("Query Helper"));
		//
		// // Help menu
		// this.helpButton = new JButton("Syntax Help");
		// helpButton.setToolTipText("Show MIQL Syntax Reference in Web Browser...");
		// helpButton.addActionListener(new ActionListener() {
		// @Override
		// public void actionPerformed(ActionEvent e) {
		// openBrowserUtil.openURL(MIQL_REFERENCE_PAGE_URL);
		// }
		// });
		//
		// final JLabel speciesLabel = new JLabel("Species:");
		// final SelectorBuilder speciesBuilder = new SelectorBuilder();
		// speciesSelector = speciesBuilder.getComboBox();
		// speciesSelector.addActionListener(new ActionListener() {
		// @Override
		// public void actionPerformed(ActionEvent e) {
		// addSpeciesQuery();
		// }
		// });
		//
		// queryBuilderPanel.add(speciesLabel);
		// queryBuilderPanel.add(speciesSelector);
		// queryBuilderPanel.add(helpButton);
		//
		// final JPanel basePanel = new JPanel();
		// basePanel.setBackground(Color.WHITE);
		// basePanel.setLayout(new BoxLayout(basePanel, BoxLayout.Y_AXIS));
		// basePanel.add(queryBuilderPanel);
		//
		// this.add(basePanel, BorderLayout.SOUTH);
		//
		//
		// queryBuilderPanel.setMaximumSize(new Dimension(950, 60));
	}

	private final void createDBlistPanel() {
		// Source Status - list of remote databases
		this.statesPanel = new SourceStatusPanel("", client, regManager, networkManager, null, taskManager, mode,
				createViewTaskFactory, vsBuilder, vmm);
		statesPanel.enableComponents(false);
		this.add(statesPanel);
	}

	private final void createQueryPanel() {
		// Query text area
		queryScrollPane = new JScrollPane();
		queryScrollPane.setBackground(Color.white);
		queryArea = new JEditorPane();

		final TitledBorder border = new TitledBorder(searchAreaTitle);
		border.setTitleColor(MIQL_COLOR);
		border.setTitleFont(STRONG_FONT);
		queryScrollPane.setBorder(border);
		queryScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		queryScrollPane.setPreferredSize(new Dimension(500, 150));
		queryScrollPane.setViewportView(queryArea);
		this.add(queryScrollPane);
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

		this.add(searchPanel);
	}

	private final void createSpeciesPanel() {
		speciesPanel = new JPanel();
		speciesPanel.setBackground(Color.white);
		final JLabel speciesLabel = new JLabel("Select Species:");

		final SelectorBuilder speciesBuilder = new SelectorBuilder();
		JComboBox speciesSelector = speciesBuilder.getComboBox();
		speciesPanel.setLayout(new BoxLayout(speciesPanel, BoxLayout.X_AXIS));
		speciesPanel.add(speciesLabel);
		speciesPanel.add(speciesSelector);
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

	private void addSpeciesQuery() {

		// final Object selectedItem = speciesSelector.getSelectedItem();
		// final Species species = (Species) selectedItem;
		//
		// String currentQuery = this.queryArea.getText();
		// if (currentQuery.contains(MIQL_QUERY_AREA_MESSAGE_STRING)) {
		// currentQuery = "";
		// queryArea.setText("");
		// }
		//
		// final String newQuery;
		// if (species == Species.ALL) {
		// newQuery = currentQuery.replaceAll("species:\".+\"", "");
		// } else {
		//
		// final String speciesEntry = "species:\"" + species.toString() + "\"";
		//
		// if (currentQuery.contains("species:")) {
		// newQuery = currentQuery.replaceAll("species:\".+\"", speciesEntry);
		// } else {
		// newQuery = currentQuery + " " + speciesEntry;
		// }
		//
		// this.queryArea.setText(newQuery);
		// }
	}

	private void search() {
		final SearchRecoredsTask searchTask = new SearchRecoredsTask(client, mode);
		final Map<String, String> activeSource = regManager.getActiveServices();
		final String query = this.queryArea.getText();
		searchTask.setQuery(query);
		searchTask.setTargets(activeSource.values());

		taskManager.execute(new TaskIterator(searchTask, new SetTableTask(searchTask)));
	}

	private final class SetTableTask extends AbstractTask {

		final SearchRecoredsTask searchTask;

		public SetTableTask(final SearchRecoredsTask searchTask) {
			this.searchTask = searchTask;
		}

		@Override
		public void run(TaskMonitor taskMonitor) throws Exception {
			final Map<String, Long> result = searchTask.getResult();
			statesPanel = new SourceStatusPanel(queryArea.getText(), client, regManager, networkManager, result,
					taskManager, mode, createViewTaskFactory, vsBuilder, vmm);
			statesPanel.sort();
			updateGUILayout();
		}
	}

	private final void updateGUILayout() {	
		removeAll();
		
		add(statesPanel);
		if(mode == null)
			add(speciesPanel);
		else
			add(queryScrollPane);
		
		add(searchPanel);
		
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
		final Color borderColor;
		if (modeString.equals(MIQL_MODE)) {
			mode = SearchMode.MIQL;
			searchAreaTitle = MIQL_MODE;
			// speciesSelector.setEnabled(true);
			// helpButton.setEnabled(true);
			queryArea.setText(MIQL_QUERY_AREA_MESSAGE_STRING);
			borderColor = MIQL_COLOR;
		} else if (modeString.equals(INTERACTOR_ID_LIST)) {
			mode = SearchMode.INTERACTOR;
			searchAreaTitle = INTERACTOR_ID_LIST;
			// speciesSelector.setEnabled(false);
			// helpButton.setEnabled(false);
			queryArea.setText(INTERACTOR_LIST_AREA_MESSAGE_STRING);
			borderColor = ID_LIST_COLOR;
		} else {
			mode = null;
			searchAreaTitle = BY_SPECIES;
			borderColor = MIQL_COLOR;
		}

		firstClick = true;

		final TitledBorder border = new TitledBorder(searchAreaTitle);
		border.setTitleColor(borderColor);
		border.setTitleFont(STRONG_FONT);
		queryScrollPane.setBorder(border);
		
		statesPanel = new SourceStatusPanel(queryArea.getText(), client, regManager, networkManager, null,
				taskManager, mode, createViewTaskFactory, vsBuilder, vmm);
		statesPanel.sort();
		
		updateGUILayout();
		statesPanel.setEnabled(false);
	}
}