package org.cytoscape.welcome.internal.view;

/*
 * #%L
 * Cytoscape Welcome Screen Impl (welcome-impl)
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
import static javax.swing.GroupLayout.Alignment.LEADING;

import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;

import org.cytoscape.application.swing.CyAction;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.datasource.DataSource;
import org.cytoscape.io.datasource.DataSourceManager;
import org.cytoscape.task.create.NewEmptyNetworkViewFactory;
import org.cytoscape.task.read.LoadNetworkURLTaskFactory;
import org.cytoscape.util.swing.LookAndFeelUtil;
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

public class NewNetworkPanel extends AbstractWelcomeScreenChildPanel {

	private static final long serialVersionUID = -8750909701276867389L;
	private static final Logger logger = LoggerFactory.getLogger(NewNetworkPanel.class);
	
	private static final Icon NEW_ICON;
	private static final Icon DATABASE_ICON;
	private static final Icon OPEN_ICON;
	
	private static final Map<String, Icon> SPECIES_ICON = new HashMap<>();
	
	private static final Pattern METATAG = Pattern.compile("(.*?)<meta>(.+?)</meta>(.*?)");

	static {
		BufferedImage newImage = null;
		BufferedImage databaseImage = null;
		BufferedImage loadImage = null;

		try {
			newImage = ImageIO.read(WelcomeScreenDialog.class.getClassLoader().getResource("images/Icons/new-empty-32.png"));
		} catch (IOException e) {
			logger.warn("Could not create Icon.", e);
		}

		try {
			databaseImage = ImageIO.read(WelcomeScreenDialog.class.getClassLoader().getResource("images/Icons/import-net-db-32.png"));
		} catch (IOException e) {
			logger.warn("Could not create Icon.", e);
		}
		try {
			loadImage = ImageIO.read(WelcomeScreenDialog.class.getClassLoader().getResource("images/Icons/import-net-32.png"));
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
		
		// Species ICON
		try {
			SPECIES_ICON.put("H. sapiens", new ImageIcon(ImageIO.read(
					WelcomeScreenDialog.class.getClassLoader().getResource("images/Icons/taxonomy/human-32.png"))));
			SPECIES_ICON.put("S. cerevisiae", new ImageIcon(ImageIO.read(
					WelcomeScreenDialog.class.getClassLoader().getResource("images/Icons/taxonomy/yeast-32.png"))));
			SPECIES_ICON.put("D. melanogaster", new ImageIcon(ImageIO.read(
					WelcomeScreenDialog.class.getClassLoader().getResource("images/Icons/taxonomy/fly-32.png"))));
			SPECIES_ICON.put("M. musculus", new ImageIcon(ImageIO.read(
					WelcomeScreenDialog.class.getClassLoader().getResource("images/Icons/taxonomy/mouse-32.png"))));
			SPECIES_ICON.put("C. elegans", new ImageIcon(ImageIO.read(
					WelcomeScreenDialog.class.getClassLoader().getResource("images/Icons/taxonomy/worm-32.png"))));
			SPECIES_ICON.put("A. thaliana", new ImageIcon(ImageIO.read(
					WelcomeScreenDialog.class.getClassLoader().getResource("images/Icons/taxonomy/plant-32.png"))));
			SPECIES_ICON.put("D. rerio", new ImageIcon(ImageIO.read(
					WelcomeScreenDialog.class.getClassLoader().getResource("images/Icons/taxonomy/fish-32.png"))));
			SPECIES_ICON.put("E. coli", new ImageIcon(ImageIO.read(
					WelcomeScreenDialog.class.getClassLoader().getResource("images/Icons/taxonomy/bacteria-32.png"))));
		} catch (IOException e) {
			logger.warn("Could not create Icon.", e);
		}
	}
	
	public static final String WORKFLOW_ID = "welcomeScreenWorkflowID";
	public static final String WORKFLOW_NAME = "welcomeScreenWorkflowName";
	public static final String WORKFLOW_DESCRIPTION = "welcomeScreenWorkflowDescription";

	private final DialogTaskManager guiTaskManager;
	private final BundleContext bc;
	private final LoadNetworkURLTaskFactory importNetworkFromURLTF;
	private final NewEmptyNetworkViewFactory newEmptyNetworkViewFactory;
	private final TaskFactory importNetworkFileTF;
	private final DataSourceManager dsManager;
	
	private final Map<String, String> dataSourceMap;
	private final Map<ButtonModel, TaskFactory> button2taskMap;
	private final List<JButton> dataSourceButtons;

	private ButtonGroup bGroup;

	public NewNetworkPanel(
			final BundleContext bc,
			final DialogTaskManager guiTaskManager,
			final TaskFactory importNetworkFileTF,
			final LoadNetworkURLTaskFactory loadTF,
			final DataSourceManager dsManager,
			final NewEmptyNetworkViewFactory newEmptyNetworkViewFactory
	) {
		super("Start New Session");
		this.bc = bc;
		this.importNetworkFromURLTF = loadTF;
		this.importNetworkFileTF = importNetworkFileTF;
		this.newEmptyNetworkViewFactory = newEmptyNetworkViewFactory;
		this.guiTaskManager = guiTaskManager;
		this.dsManager = dsManager;
		
		button2taskMap = new HashMap<>();
		dataSourceMap = new HashMap<>();
		dataSourceButtons = new ArrayList<>();

		createDataSourceButtons();
		initComponents();
	}
	
	private void createDataSourceButtons() {
		final SortedMap<String, JButton> buttonMap = new TreeMap<>();
		// Extract the URL entries
		final Collection<DataSource> dataSources = dsManager.getDataSources(DataCategory.NETWORK);
		
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
					// Add preset buttons
					final List<String> tagList = Arrays.asList(res);
					
					if (tagList.contains("preset")) {
						final String sourceLabel = sourceName;
						dataSourceMap.put(sourceLabel, link);

						final JButton button = new JButton(sourceLabel);
						buttonMap.put(sourceLabel, button);
						button.setHorizontalAlignment(SwingConstants.LEFT);
						button.setToolTipText(tooltip);
						button.setFont(button.getFont().deriveFont(LookAndFeelUtil.INFO_FONT_SIZE));

						if (sourceLabel.contains("."))
							button.setFont(button.getFont().deriveFont(Font.ITALIC));

						final Icon icon = SPECIES_ICON.get(sourceLabel);

						if (icon != null)
							button.setIcon(icon);
						
						button.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								loadPreset(button);
							}
						});
					}
				}
			}
		}

		dataSourceButtons.addAll(buttonMap.values());
		bGroup = new ButtonGroup();
		
		for (JButton btn : dataSourceButtons)
			bGroup.add(btn);
	}

	private void initComponents() {
		final JButton emptyNetButton = new JButton("With Empty Network", NEW_ICON);
		emptyNetButton.setHorizontalAlignment(SwingConstants.LEFT);
		emptyNetButton.setIconTextGap(20);
		emptyNetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				closeParentWindow();
				guiTaskManager.execute(((TaskFactory)newEmptyNetworkViewFactory).createTaskIterator());
			}
		});
		
		final JButton fileImportButton = new JButton("From Network File...", OPEN_ICON);
		fileImportButton.setHorizontalAlignment(SwingConstants.LEFT);
		fileImportButton.setIconTextGap(20);
		fileImportButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				loadFromFile();
			}
		});
		
		final JButton dbButton = new JButton("From Network Database...", DATABASE_ICON);
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
		
		final JPanel dsButtonPanel = new JPanel();
		
		if (LookAndFeelUtil.isAquaLAF())
			dsButtonPanel.setOpaque(false);
		
		int rowCount = dataSourceButtons.size() / 2;
		int mod = dataSourceButtons.size() % 2;
		dsButtonPanel.setLayout(new GridLayout(rowCount + mod, 2));
		
		for (JButton btn: dataSourceButtons) {
			dsButtonPanel.add(btn);
			bGroup.add(btn);
		}
		
		final GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
		
		layout.setHorizontalGroup(layout.createParallelGroup(LEADING, true)
				.addComponent(emptyNetButton, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(fileImportButton, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(dbButton, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(dsButtonPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addComponent(emptyNetButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(fileImportButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(dbButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addComponent(dsButtonPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
	}

	private final void loadFromFile() {
		final TaskIterator itr = importNetworkFileTF.createTaskIterator();
		importNetwork(itr);
	}

	private void loadPreset(JButton button) {
		// Get selected file from the combo box
		Object file = button.getText();
		
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
		button2taskMap.put(taskButton.getModel(), factory);

		if (description != null)
			taskButton.setToolTipText(description.toString());
	}

	public void removeTaskFactory(final TaskFactory factory, @SuppressWarnings("rawtypes") Map properties) {

	}

	private final class ResetTask extends AbstractTask {

		@Override
		public void run(TaskMonitor taskMonitor) throws Exception {
			// TODO
//			networkList.setSelectedIndex(0);
		}
	}
}
