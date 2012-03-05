package org.cytoscape.welcome.internal;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.datasource.DataSource;
import org.cytoscape.datasource.DataSourceManager;
import org.cytoscape.io.DataCategory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.property.CyProperty;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.task.creation.ImportNetworksTaskFactory;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskMonitor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateNewNetworkPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = -8750909701276867389L;

	private static final Logger logger = LoggerFactory.getLogger(CreateNewNetworkPanel.class);

	private static final String LAYOUT_ALGORITHM = "force-directed";
	private static final String VIEW_THRESHOLD = "viewThreshold";
	private static final int DEF_VIEW_THRESHOLD = 3000;

	private static final String ICON_OPEN = "images/Icons/net_file_import_small.png";
	private static final String ICON_DATABASE = "images/Icons/net_db_import_small.png";

	private JLabel loadNetwork;
	private JLabel fromDB;
	private JLabel fromWebService;

	private JComboBox networkList;
	private JCheckBox layout;

	private final TaskManager guiTaskManager;

	private Window parent;

	private final BundleContext bc;

	private final ImportNetworksTaskFactory importNetworkFromURLTF;
	private final TaskFactory importNetworkFileTF;
	private final NetworkTaskFactory createViewTaskFactory;

	private final DataSourceManager dsManager;
	private final Map<String, String> dataSourceMap;

	private final int viewThreshold;

	private boolean firstSelection = false;
	
	private final CyProperty<Properties> props;

	CreateNewNetworkPanel(Window parent, final BundleContext bc, final TaskManager guiTaskManager,
			final TaskFactory importNetworkFileTF, final ImportNetworksTaskFactory loadTF,
			final NetworkTaskFactory createViewTaskFactory, final CyApplicationConfiguration config,
			final DataSourceManager dsManager, final CyProperty<Properties> props) {
		this.parent = parent;
		this.bc = bc;
		this.props = props;

		this.importNetworkFromURLTF = loadTF;
		this.createViewTaskFactory = createViewTaskFactory;
		this.importNetworkFileTF = importNetworkFileTF;
		this.guiTaskManager = guiTaskManager;
		this.dsManager = dsManager;
		this.viewThreshold = getViewThreshold(props.getProperties());

		this.dataSourceMap = new HashMap<String, String>();
		this.networkList = new JComboBox();

		setFromDataSource();

		initComponents();

		// Enable combo box listener here to avoid unnecessary reaction.
		this.networkList.addActionListener(this);
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

		BufferedImage openIconImg = null;
		BufferedImage databaseIconImg = null;
		try {
			openIconImg = ImageIO.read(WelcomeScreenDialog.class.getClassLoader().getResource(ICON_OPEN));
			databaseIconImg = ImageIO.read(WelcomeScreenDialog.class.getClassLoader().getResource(ICON_DATABASE));
		} catch (IOException e) {
			logger.error("Could not load icons", e);
		}

		ImageIcon openIcon = new ImageIcon(openIconImg);
		ImageIcon databaseIcon = new ImageIcon(databaseIconImg);

		this.layout = new JCheckBox();
		layout.setText("Apply default layout");
		layout.setToolTipText("Note: This option may take minutes to finish for large networks!");

		this.loadNetwork = new JLabel("From file...");
		this.loadNetwork.setIcon(openIcon);

		loadNetwork.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent ev) {
				// Load network from file.
				parent.dispose();
				guiTaskManager.execute(importNetworkFileTF);
			}
		});
		this.setBorder(new LineBorder(new Color(0, 0, 0, 0), 10));

		this.fromDB = new JLabel("From Reference Network Data:");
		this.fromDB.setIcon(databaseIcon);

		this.fromWebService = new JLabel("From Public Web Service...");
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

		this.setLayout(new GridLayout(5, 1));
		this.add(loadNetwork);
		this.add(fromWebService);
		this.add(fromDB);
		this.add(networkList);
		this.add(layout);

	}

	private void loadNetwork() throws URISyntaxException, MalformedURLException {

		// Get selected file from the combo box
		final Object file = networkList.getSelectedItem();
		if (file == null)
			return;

		if (!dataSourceMap.containsKey(file))
			return;
		final URL url = new URL(dataSourceMap.get(file));

		parent.dispose();

		if(layout.isSelected())
			props.getProperties().setProperty(CyLayoutAlgorithmManager.DEFAULT_LAYOUT_PROPERTY_NAME, LAYOUT_ALGORITHM);
				
		// TODO REFACTOR!!!!!!!!!!!!!!!!!!!
		guiTaskManager.execute(new TaskFactory() {

			@Override
			public TaskIterator createTaskIterator() {
				return new TaskIterator(2,
						new CreateNetworkViewTask(url, importNetworkFromURLTF, createViewTaskFactory));
			}
		});

	}

	private int getViewThreshold(final Properties props) {
		final String vts = props.getProperty(VIEW_THRESHOLD);
		int threshold;
		try {
			threshold = Integer.parseInt(vts);
		} catch (Exception e) {
			threshold = DEF_VIEW_THRESHOLD;
		}

		return threshold;
	}

	private final class CreateNetworkViewTask extends AbstractTask {

		private final ImportNetworksTaskFactory loadNetworkFileTF;
		private final NetworkTaskFactory createViewTaskFactory;

		private final URL url;

		public CreateNetworkViewTask(final URL url, final ImportNetworksTaskFactory loadNetworkFileTF,
				final NetworkTaskFactory createViewTaskFactory) {
			this.loadNetworkFileTF = loadNetworkFileTF;
			this.createViewTaskFactory = createViewTaskFactory;
			this.url = url;
		}

		@Override
		public void run(TaskMonitor taskMonitor) throws Exception {
			taskMonitor.setTitle("Loading network...");
			taskMonitor.setStatusMessage("Loading network.  Please wait...");
			taskMonitor.setProgress(0.01d);

			final Set<CyNetwork> networks = this.loadNetworkFileTF.loadCyNetworks(url);
			taskMonitor.setProgress(1.0d);
			
			props.getProperties().setProperty(CyLayoutAlgorithmManager.DEFAULT_LAYOUT_PROPERTY_NAME, CyLayoutAlgorithmManager.DEFAULT_LAYOUT_NAME);
		}

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			loadNetwork();
		} catch (Exception ex) {
			logger.error("Could not load network.", ex);
		}
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
}
