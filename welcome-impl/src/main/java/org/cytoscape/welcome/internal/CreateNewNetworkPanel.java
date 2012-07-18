package org.cytoscape.welcome.internal;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.Window;
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
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.LineBorder;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.datasource.DataSource;
import org.cytoscape.io.datasource.DataSourceManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.property.CyProperty;
import org.cytoscape.task.analyze.AnalyzeNetworkCollectionTaskFactory;
import org.cytoscape.task.read.LoadNetworkURLTaskFactory;
import org.cytoscape.task.visualize.ApplyPreferredLayoutTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.events.NetworkViewAddedEvent;
import org.cytoscape.view.model.events.NetworkViewAddedListener;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.welcome.internal.task.AnalyzeAndVisualizeNetworkTask;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateNewNetworkPanel extends JPanel implements NetworkAddedListener, NetworkViewAddedListener {

	private static final long serialVersionUID = -8750909701276867389L;
	private static final Logger logger = LoggerFactory.getLogger(CreateNewNetworkPanel.class);

	// Default layout algorithm
	private static final String LAYOUT_ALGORITHM = "force-directed";

	private JLabel loadNetwork;
	private JLabel fromDB;
	private JLabel fromWebService;

	private final JRadioButton importOnlyButton = new JRadioButton("Import only");
	private final JRadioButton layoutButton = new JRadioButton("Import and Layout");
	private final JRadioButton visualizeButton = new JRadioButton("Import, Analyze, and Visualize");

	// List of Preset Data
	private JComboBox networkList;

	// Parent window, usually it's Cytoscape Desktop
	private Window parent;

	private final DialogTaskManager guiTaskManager;
	private final BundleContext bc;
	private final LoadNetworkURLTaskFactory importNetworkFromURLTF;
	private final TaskFactory importNetworkFileTF;
	private final DataSourceManager dsManager;
	private final Map<String, String> dataSourceMap;
	private final CyProperty<Properties> props;

	private final AnalyzeNetworkCollectionTaskFactory analyzeNetworkCollectionTaskFactory;
	private final VisualStyleBuilder vsBuilder;
	private final VisualMappingManager vmm;

	private Set<CyNetwork> networkToBeAnalyzed;
	private Set<CyNetworkView> networkViews;

	private final ApplyPreferredLayoutTaskFactory applyPreferredLayoutTaskFactory;

	CreateNewNetworkPanel(Window parent, final BundleContext bc, final DialogTaskManager guiTaskManager,
			final TaskFactory importNetworkFileTF, final LoadNetworkURLTaskFactory loadTF,
			final CyApplicationConfiguration config, final DataSourceManager dsManager,
			final CyProperty<Properties> props,
			final AnalyzeNetworkCollectionTaskFactory analyzeNetworkCollectionTaskFactory,
			final VisualStyleBuilder vsBuilder, final VisualMappingManager vmm,
			final ApplyPreferredLayoutTaskFactory applyPreferredLayoutTaskFactory) {
		this.parent = parent;
		this.bc = bc;
		this.props = props;
		this.analyzeNetworkCollectionTaskFactory = analyzeNetworkCollectionTaskFactory;
		this.vsBuilder = vsBuilder;
		this.vmm = vmm;
		this.applyPreferredLayoutTaskFactory = applyPreferredLayoutTaskFactory;

		this.importNetworkFromURLTF = loadTF;
		this.importNetworkFileTF = importNetworkFileTF;
		this.guiTaskManager = guiTaskManager;
		this.dsManager = dsManager;

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

		networkToBeAnalyzed = new HashSet<CyNetwork>();
		networkViews = new HashSet<CyNetworkView>();
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
		importOnlyButton.setToolTipText("Just import network data.");
		layoutButton.setToolTipText("Import network and apply preffered layout algorithm.");
		visualizeButton
				.setToolTipText("Import a network and analyze it.  Then visualize the network based on its basic statistics.");

		this.loadNetwork = new JLabel("From file...");
		this.loadNetwork.setCursor(new Cursor(Cursor.HAND_CURSOR));

		loadNetwork.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent ev) {
				loadFromFile();
			}
		});

		this.setBorder(new LineBorder(new Color(0, 0, 0, 0), 10));

		this.fromDB = new JLabel("From Reference Network Data:");

		this.fromWebService = new JLabel("From Public Web Service...");
		this.fromWebService.setCursor(new Cursor(Cursor.HAND_CURSOR));
		this.fromWebService.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent ev) {
				// Load network from web service.
				parent.dispose();
				try {
					execute(bc);
				} catch (InvalidSyntaxException e) {
					logger.error("Could not execute the action", e);
				}
			}
		});

		this.setLayout(new GridLayout(2, 1));

		final JPanel topPanel = new JPanel();
		topPanel.setOpaque(false);
		topPanel.setLayout(new GridLayout(4, 1));
		final JPanel bottomPanel = new JPanel();
		bottomPanel.setOpaque(false);
		bottomPanel.setLayout(new GridLayout(1, 1));

		topPanel.add(fromWebService);
		topPanel.add(loadNetwork);
		topPanel.add(fromDB);
		topPanel.add(networkList);

		bottomPanel.add(initOptionPanel());

		this.add(topPanel);
		this.add(bottomPanel);

	}

	private JPanel initOptionPanel() {
		final JPanel optionPanel = new JPanel();

		optionPanel.setBorder(BorderFactory.createTitledBorder("Options"));
		optionPanel.setLayout(new GridLayout(3, 1));

		final ButtonGroup gr = new ButtonGroup();
		gr.add(importOnlyButton);
		gr.add(layoutButton);
		gr.add(visualizeButton);
		gr.setSelected(importOnlyButton.getModel(), true);

		optionPanel.add(importOnlyButton);
		optionPanel.add(layoutButton);
		optionPanel.add(visualizeButton);
		optionPanel.setOpaque(false);

		return optionPanel;
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

		if (layoutButton.isSelected()) {
			loadTaskIt.append(applyPreferredLayoutTaskFactory.createTaskIterator(networkViews));
		}
		if (visualizeButton.isSelected()) {
			loadTaskIt.append(analyzeNetworkCollectionTaskFactory.createTaskIterator(networkToBeAnalyzed));
			loadTaskIt.append(new AnalyzeAndVisualizeNetworkTask(networkViews, vsBuilder, vmm));
		}
		guiTaskManager.execute(loadTaskIt);
		parent.dispose();
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
}
