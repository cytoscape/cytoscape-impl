package org.cytoscape.welcome.internal.panel;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.datasource.DataSource;
import org.cytoscape.io.datasource.DataSourceManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.task.analyze.AnalyzeNetworkCollectionTaskFactory;
import org.cytoscape.task.read.LoadNetworkURLTaskFactory;
import org.cytoscape.task.visualize.ApplyPreferredLayoutTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.events.NetworkViewAddedEvent;
import org.cytoscape.view.model.events.NetworkViewAddedListener;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.welcome.internal.VisualStyleBuilder;
import org.cytoscape.welcome.internal.task.AnalyzeAndVisualizeNetworkTask;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateNewNetworkPanel extends AbstractWelcomeScreenChildPanel implements NetworkAddedListener,
		NetworkViewAddedListener {

	private static final long serialVersionUID = -8750909701276867389L;
	private static final Logger logger = LoggerFactory.getLogger(CreateNewNetworkPanel.class);

	private JLabel loadNetwork;
	private JLabel fromDB;
	private JLabel fromWebService;

	private final ButtonGroup gr = new ButtonGroup();
	private final JPanel optionPanel = new JPanel();

	// List of Preset Data
	private JComboBox networkList;

	private final DialogTaskManager guiTaskManager;
	private final BundleContext bc;
	private final LoadNetworkURLTaskFactory importNetworkFromURLTF;
	private final TaskFactory importNetworkFileTF;
	private final DataSourceManager dsManager;
	private final Map<String, String> dataSourceMap;

	private final AnalyzeNetworkCollectionTaskFactory analyzeNetworkCollectionTaskFactory;
	private final VisualStyleBuilder vsBuilder;
	private final VisualMappingManager vmm;

	private Set<CyNetwork> networkToBeAnalyzed;
	private Set<CyNetworkView> networkViews;

	private final ApplyPreferredLayoutTaskFactory applyPreferredLayoutTaskFactory;

	public CreateNewNetworkPanel(final BundleContext bc, final DialogTaskManager guiTaskManager,
			final TaskFactory importNetworkFileTF, final LoadNetworkURLTaskFactory loadTF,
			final CyApplicationConfiguration config, final DataSourceManager dsManager,
			final AnalyzeNetworkCollectionTaskFactory analyzeNetworkCollectionTaskFactory,
			final VisualStyleBuilder vsBuilder, final VisualMappingManager vmm,
			final ApplyPreferredLayoutTaskFactory applyPreferredLayoutTaskFactory) {
		this.bc = bc;
		this.analyzeNetworkCollectionTaskFactory = analyzeNetworkCollectionTaskFactory;
		this.vsBuilder = vsBuilder;
		this.vmm = vmm;
		this.applyPreferredLayoutTaskFactory = applyPreferredLayoutTaskFactory;

		this.importNetworkFromURLTF = loadTF;
		this.importNetworkFileTF = importNetworkFileTF;
		this.guiTaskManager = guiTaskManager;
		this.dsManager = dsManager;
		
		networkToBeAnalyzed = new HashSet<CyNetwork>();
		networkViews = new HashSet<CyNetworkView>();

		this.dataSourceMap = new HashMap<String, String>();
		this.networkList = new JComboBox();
		networkList.setEnabled(false);

		setFromDataSource();
		initComponents();

		// Enable combo box listener here to avoid unnecessary reaction.
		this.networkList.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				loadPreset();
			}
		});

		networkList.setEnabled(true);
	}

	private void setFromDataSource() {
		DefaultComboBoxModel theModel = new DefaultComboBoxModel();

		// Extract the URL entries
		final Collection<DataSource> dataSources = dsManager.getDataSources(DataCategory.NETWORK);
		final SortedSet<String> labelSet = new TreeSet<String>();
		if (dataSources != null) {
			for (DataSource ds : dataSources) {
				String link = null;
				link = ds.getLocation().toString();
				final String sourceName = ds.getName();
				final String provider = ds.getProvider();
				final String sourceLabel = provider + ":" + sourceName;
				dataSourceMap.put(sourceLabel, link);
				labelSet.add(sourceLabel);
			}
		}

		theModel.addElement("Select a network ...");

		for (final String label : labelSet)
			theModel.addElement(label);

		this.networkList.setModel(theModel);
	}

	private void initComponents() {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		this.loadNetwork = new JLabel("Import network from file...");
		loadNetwork.setFont(COMMAND_FONT);
		loadNetwork.setForeground(COMMAND_FONT_COLOR);
		this.loadNetwork.setCursor(new Cursor(Cursor.HAND_CURSOR));

		loadNetwork.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent ev) {
				loadFromFile();
			}
		});

		this.setBorder(new LineBorder(new Color(0, 0, 0, 0), 10));

		this.fromDB = new JLabel("Import network from reference data set:");
		fromDB.setFont(COMMAND_FONT);
		fromDB.setForeground(COMMAND_FONT_COLOR);
		this.fromWebService = new JLabel("Import Network from Public Database...");
		fromWebService.setFont(COMMAND_FONT);
		fromWebService.setForeground(COMMAND_FONT_COLOR);
		fromWebService.setHorizontalAlignment(JLabel.LEFT);
		fromWebService.setHorizontalTextPosition(JLabel.LEFT);
		this.fromWebService.setCursor(new Cursor(Cursor.HAND_CURSOR));
		this.fromWebService.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent ev) {
				// Load network from web service.
				closeParentWindow();
				try {
					execute(bc);
				} catch (InvalidSyntaxException e) {
					logger.error("Could not execute the action", e);
				}
			}
		});

		// Label border
		final Border labelPadding = BorderFactory.createEmptyBorder(2, 10, 2, 8);

		// Remote access
		final JPanel wsPanel = new JPanel();
		wsPanel.setLayout(new GridLayout(1, 1));
		wsPanel.setBorder(BorderFactory.createTitledBorder("Access Remore Service"));
		wsPanel.setOpaque(false);
		final Dimension dbPanelSize = new Dimension(300, 60);
		fromWebService.setMaximumSize(dbPanelSize);
		fromWebService.setBorder(labelPadding);
		wsPanel.setPreferredSize(dbPanelSize);
		wsPanel.setSize(dbPanelSize);
		wsPanel.setMaximumSize(dbPanelSize);
		wsPanel.add(fromWebService);

		final Dimension importPanelSize = new Dimension(300, 120);
		final JPanel importPanel = new JPanel();
		importPanel.setMaximumSize(importPanelSize);
		importPanel.setLayout(new GridLayout(3, 1));
		importPanel.setOpaque(false);
		loadNetwork.setBorder(labelPadding);
		fromDB.setBorder(labelPadding);
		networkList.setFont(REGULAR_FONT);
		networkList.setForeground(REGULAR_FONT_COLOR);

		importPanel.add(loadNetwork);
		importPanel.add(fromDB);
		importPanel.add(networkList);

		final JPanel bottomPanel = new JPanel();
		bottomPanel.setOpaque(false);
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
		bottomPanel.setBorder(BorderFactory.createTitledBorder("Common Workflow"));

		bottomPanel.add(importPanel);
		bottomPanel.add(initOptionPanel());
		this.add(wsPanel);
		this.add(bottomPanel);

		createPresetTasks();
	}

	private JScrollPane initOptionPanel() {

		final JScrollPane optionPane = new JScrollPane();
		optionPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(REGULAR_FONT_COLOR, 1),
				"Optional Tasks after Data Import", TitledBorder.CENTER, TitledBorder.CENTER, REGULAR_FONT,
				REGULAR_FONT_COLOR));

		optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.Y_AXIS));
		optionPanel.setOpaque(false);

		optionPane.setViewportView(optionPanel);
		optionPane.setOpaque(false);
		optionPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		return optionPane;
	}

	private final void loadFromFile() {
		final TaskIterator itr = importNetworkFileTF.createTaskIterator();
		importNetwork(itr);
	}

	private void loadPreset() {
		// Get selected file from the combo box
		final Object file = networkList.getSelectedItem();
		if (file == null)
			return;

		if (!dataSourceMap.containsKey(file))
			return;
		URL url = null;
		try {
			url = new URL(dataSourceMap.get(file));
		} catch (MalformedURLException e) {
			logger.error("Source URL is invalid", e);
		}

		final TaskIterator loadTaskIt = importNetworkFromURLTF.loadCyNetworks(url);
		importNetwork(loadTaskIt);
	}

	private void importNetwork(final TaskIterator loadTaskIt) {

		final ButtonModel selected = gr.getSelection();
		if (selected != null) {
			final TaskIterator optionalTasks = button2taskMap.get(selected);
			if(optionalTasks != null)
				loadTaskIt.append(optionalTasks);
		}

//		if (layoutButton.isSelected()) {
//			loadTaskIt.append(applyPreferredLayoutTaskFactory.createTaskIterator(networkViews));
//		}
//		if (visualizeButton.isSelected()) {
//			loadTaskIt.append(analyzeNetworkCollectionTaskFactory.createTaskIterator(networkToBeAnalyzed));
//			loadTaskIt.append(new AnalyzeAndVisualizeNetworkTask(networkViews, vsBuilder, vmm));
//		}
		guiTaskManager.execute(loadTaskIt);
		closeParentWindow();
	}

	/**
	 * Due to its dependency, we need to import this service dynamically.
	 * 
	 * @throws InvalidSyntaxException
	 */
	private void execute(BundleContext bc) throws InvalidSyntaxException {
		final ServiceReference[] actions = bc.getAllServiceReferences("org.cytoscape.application.swing.CyAction",
				"(id=showImportNetworkFromWebServiceDialogAction)");
		if (actions == null || actions.length != 1) {
			logger.error("Could not find action");
			return;
		}

		final ServiceReference ref = actions[0];
		final CyAction action = (CyAction) bc.getService(ref);
		action.actionPerformed(null);
	}

	@Override
	public void handleEvent(NetworkAddedEvent e) {
		final CyNetwork network = e.getNetwork();
		networkToBeAnalyzed.add(network);
	}

	@Override
	public void handleEvent(NetworkViewAddedEvent e) {
		CyNetworkView networkView = e.getNetworkView();
		if (networkView != null)
			networkViews.add(e.getNetworkView());
	}

	private final Map<ButtonModel, TaskIterator> button2taskMap = new HashMap<ButtonModel, TaskIterator>();

	public void addTaskFactory(final TaskFactory factory, @SuppressWarnings("rawtypes") Map properties) {
		Object workflowID = properties.get("welcomeScreenWorkflowID");
		if (workflowID == null)
			return;

		final Object description = properties.get("welcomeScreenWorkflowDescription");
		final JRadioButton taskButton = new JRadioButton(workflowID.toString());
		taskButton.setFont(REGULAR_FONT);
		taskButton.setForeground(REGULAR_FONT_COLOR);
		gr.add(taskButton);
		optionPanel.add(taskButton);
		button2taskMap.put(taskButton.getModel(), factory.createTaskIterator());

		if (description != null)
			taskButton.setToolTipText(description.toString());
	}

	public void removeTaskFactory(final TaskFactory factory, @SuppressWarnings("rawtypes") Map properties) {

	}

	private void createPresetTasks() {
		final JRadioButton noOptionTaskButton = new JRadioButton("No Optional Task");
		noOptionTaskButton.setFont(REGULAR_FONT);
		noOptionTaskButton.setForeground(REGULAR_FONT_COLOR);
		gr.add(noOptionTaskButton);
		optionPanel.add(noOptionTaskButton);
		
		final JRadioButton visualizeButton = new JRadioButton("Analyze and apply custom Visual Style");
		visualizeButton.setFont(REGULAR_FONT);
		visualizeButton.setForeground(REGULAR_FONT_COLOR);
		gr.add(visualizeButton);
		optionPanel.add(visualizeButton);
		TaskIterator itr = analyzeNetworkCollectionTaskFactory.createTaskIterator(networkToBeAnalyzed);
		itr.append(new AnalyzeAndVisualizeNetworkTask(networkViews, vsBuilder, vmm));
		button2taskMap.put(visualizeButton.getModel(), itr);
		
		gr.setSelected(noOptionTaskButton.getModel(), true);
	}
}
