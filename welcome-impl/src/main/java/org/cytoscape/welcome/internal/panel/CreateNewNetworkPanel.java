package org.cytoscape.welcome.internal.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import org.cytoscape.application.swing.CyAction;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.datasource.DataSource;
import org.cytoscape.io.datasource.DataSourceManager;
import org.cytoscape.task.read.LoadNetworkURLTaskFactory;
import org.cytoscape.welcome.internal.WelcomeScreenDialog;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.swing.DialogTaskManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateNewNetworkPanel extends AbstractWelcomeScreenChildPanel {

	private static final long serialVersionUID = -8750909701276867389L;
	private static final Logger logger = LoggerFactory.getLogger(CreateNewNetworkPanel.class);
	
	private static final Icon NEW_ICON;
	private static final Icon PRESET_ICON;
	private static final Icon DATABASE_ICON;
	private static final Icon OPEN_ICON;
	
	private static final Pattern METATAG = Pattern.compile("(.*?)<meta>(.+?)</meta>(.*?)");

	static {
		BufferedImage newImage = null;
		BufferedImage databaseImage = null;
		BufferedImage loadImage = null;
		BufferedImage presetImage = null;

		try {
			newImage = ImageIO.read(WelcomeScreenDialog.class.getClassLoader().getResource("images/Icons/empty.png"));
		} catch (IOException e) {
			logger.warn("Could not create Icon.", e);
		}

		try {
			databaseImage = ImageIO.read(WelcomeScreenDialog.class.getClassLoader().getResource(
					"images/Icons/remote.png"));
		} catch (IOException e) {
			logger.warn("Could not create Icon.", e);
		}
		try {
			loadImage = ImageIO.read(WelcomeScreenDialog.class.getClassLoader().getResource("images/Icons/open.png"));
		} catch (IOException e) {
			logger.warn("Could not create Icon.", e);
		}
		
		try {
			presetImage = ImageIO.read(WelcomeScreenDialog.class.getClassLoader().getResource("images/Icons/logo48.png"));
		} catch (IOException e) {
			logger.warn("Could not create Icon.", e);
		}

		if (newImage != null)
			NEW_ICON = new ImageIcon(newImage);
		else
			NEW_ICON = null;

		if (databaseImage != null)
			DATABASE_ICON = new ImageIcon(databaseImage);
		else
			DATABASE_ICON = null;

		if (loadImage != null)
			OPEN_ICON = new ImageIcon(loadImage);
		else
			OPEN_ICON = null;
		
		if (presetImage != null)
			PRESET_ICON = new ImageIcon(presetImage);
		else
			PRESET_ICON = null;
	}
	
	public static final String WORKFLOW_ID = "welcomeScreenWorkflowID";
	public static final String WORKFLOW_NAME = "welcomeScreenWorkflowName";
	public static final String WORKFLOW_DESCRIPTION = "welcomeScreenWorkflowDescription";

	private final DialogTaskManager guiTaskManager;
	private final BundleContext bc;
	private final LoadNetworkURLTaskFactory importNetworkFromURLTF;
	private final TaskFactory importNetworkFileTF;
	private final DataSourceManager dsManager;
	private final Map<String, String> dataSourceMap;

	private final Map<ButtonModel, TaskFactory> button2taskMap = new HashMap<ButtonModel, TaskFactory>();
	private JRadioButton noOptionTaskButton;

	private List<JRadioButton> buttonList;
	private JPanel sourceButtons;
	private ButtonGroup bGroup;

	public CreateNewNetworkPanel(final BundleContext bc, final DialogTaskManager guiTaskManager,
			final TaskFactory importNetworkFileTF, final LoadNetworkURLTaskFactory loadTF,
			final DataSourceManager dsManager) {
		this.bc = bc;

		this.importNetworkFromURLTF = loadTF;
		this.importNetworkFileTF = importNetworkFileTF;
		this.guiTaskManager = guiTaskManager;
		this.dsManager = dsManager;
		this.dataSourceMap = new HashMap<String, String>();

		setFromDataSource();
		initComponents();
	}
	
	private void setFromDataSource() {
		buttonList = new ArrayList<JRadioButton>();
		
		// Extract the URL entries
		final Collection<DataSource> dataSources = dsManager.getDataSources(DataCategory.NETWORK);
		final SortedSet<String> labelSet = new TreeSet<String>();
		if (dataSources != null) {
			for (DataSource ds : dataSources) {
				String link = null;
				link = ds.getLocation().toString();
				final String sourceName = ds.getName();
				String description = ds.getDescription();
				Matcher match = METATAG.matcher(description);
				boolean found = match.matches();
				if (!found)
					continue;

				final String tags = match.group(2);
			
				final String tooltip = match.group(3);
				final String[] res = tags.split(",");

				if (res != null) {
					final List<String> tagList = Arrays.asList(res);
					if (tagList.contains("preset")) {
						final String sourceLabel = sourceName;
						dataSourceMap.put(sourceLabel, link);

						final JRadioButton button = new JRadioButton(sourceLabel);
						button.setToolTipText(tooltip);
						buttonList.add(button);
						labelSet.add(sourceLabel);
					}
				}
			}
		}

		bGroup = new ButtonGroup();
		sourceButtons = new JPanel();
		sourceButtons.setOpaque(false);
		
		// Determine Size of Grid
		int rowCount = buttonList.size()/2;
		int mod = buttonList.size()%2;
		sourceButtons.setLayout(new GridLayout(rowCount+mod, 2));
		for(JRadioButton rb: buttonList) {
			sourceButtons.add(rb);
			sourceButtons.setOpaque(false);
			bGroup.add(rb);
		}
	}

	private void initComponents() {
		// Basic layout of this panel (2 rows)
		this.setLayout(new GridLayout(2,1));
		
		// Label border
		this.setBorder(new LineBorder(new Color(0, 0, 0, 0), 10));

		final JButton createEmptySessionButton = new JButton();
		createEmptySessionButton.setText("New/Empty Network");
		createEmptySessionButton.setIcon(NEW_ICON);
		createEmptySessionButton.setHorizontalAlignment(SwingConstants.LEFT);
		createEmptySessionButton.setIconTextGap(20);
		createEmptySessionButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent ev) {
				closeParentWindow();
			}
		});
		
		final JButton importFromFileButton = new JButton();
		importFromFileButton.setText("From Network File...");
		importFromFileButton.setIcon(OPEN_ICON);
		importFromFileButton.setHorizontalAlignment(SwingConstants.LEFT);
		importFromFileButton.setIconTextGap(20);
		importFromFileButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				loadFromFile();
			}
		});

		JPanel dbButtonPanel = new JPanel();
		dbButtonPanel.setLayout(new GridLayout(1,1));
		dbButtonPanel.setOpaque(false);
		JButton dbButton = new JButton("From Network Database...");
		dbButton.setIcon(DATABASE_ICON);
		dbButton.setIconTextGap(20);
		dbButton.setHorizontalAlignment(SwingConstants.LEFT);
		dbButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// Load network from web service.
				closeParentWindow();
				try {
					execute(bc);
				} catch (InvalidSyntaxException ise) {
					logger.error("Could not execute the action", e);
				}
			}
			
		});
		
		final JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(3, 1));
		buttonPanel.add(createEmptySessionButton);
		buttonPanel.add(importFromFileButton);
		buttonPanel.add(dbButton);
		this.add(buttonPanel);

		final JPanel presetPanel = new JPanel();
		presetPanel.setBorder(BorderFactory.createTitledBorder("From Preset Network"));
		presetPanel.setOpaque(false);
		presetPanel.setLayout(new BorderLayout());
		JScrollPane buttonScrollPane = new JScrollPane();
		buttonScrollPane.setViewportView(sourceButtons);
		presetPanel.add(buttonScrollPane, BorderLayout.CENTER);
		final JButton importPresetButton = new JButton("Load Preset Network");
		importPresetButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				loadPreset();
			}
			
		});
		presetPanel.add(importPresetButton, BorderLayout.SOUTH);

		this.add(presetPanel);
		
		createPresetTasks();
	}

	private final void loadFromFile() {
		final TaskIterator itr = importNetworkFileTF.createTaskIterator();
		importNetwork(itr);
	}

	private void loadPreset() {
		// Get selected file from the combo box
		Object file = null;
		for(JRadioButton button: buttonList) {
			if(button.isSelected()) {
				file = button.getText();
				break;
			}
		}
		
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
		loadTaskIt.append(new ResetTask());
		closeParentWindow();
		guiTaskManager.execute(loadTaskIt);
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

	public void addTaskFactory(final TaskFactory factory, @SuppressWarnings("rawtypes") Map properties) {
		final Object workflowID = properties.get(WORKFLOW_ID);
		if (workflowID == null)
			return;

		Object workflowName = properties.get(WORKFLOW_NAME);
		if (workflowName == null)
			workflowName = workflowID;
		final Object description = properties.get(WORKFLOW_DESCRIPTION);
		final JRadioButton taskButton = new JRadioButton(workflowName.toString());
		taskButton.setFont(REGULAR_FONT);
		taskButton.setForeground(REGULAR_FONT_COLOR);
		button2taskMap.put(taskButton.getModel(), factory);

		if (description != null)
			taskButton.setToolTipText(description.toString());
	}

	public void removeTaskFactory(final TaskFactory factory, @SuppressWarnings("rawtypes") Map properties) {

	}

	private void createPresetTasks() {
		noOptionTaskButton = new JRadioButton("No Optional Task");
		noOptionTaskButton.setFont(REGULAR_FONT);
		noOptionTaskButton.setForeground(REGULAR_FONT_COLOR);
	}

	private final class ResetTask extends AbstractTask {

		@Override
		public void run(TaskMonitor taskMonitor) throws Exception {

//			networkList.setSelectedIndex(0);
		}
	}
}
