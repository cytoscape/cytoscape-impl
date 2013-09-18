package org.cytoscape.cpath2.internal;

/*
 * #%L
 * Cytoscape CPath2 Impl (cpath2-impl)
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

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.cpath2.internal.schemas.summary_response.BasicRecordType;
import org.cytoscape.cpath2.internal.task.CPathNetworkImportTask;
import org.cytoscape.cpath2.internal.task.ExecuteGetRecordByCPathIdTaskFactory;
import org.cytoscape.cpath2.internal.task.MergeNetworkTaskFactory;
import org.cytoscape.cpath2.internal.util.NetworkMergeUtil;
import org.cytoscape.cpath2.internal.util.NetworkUtil;
import org.cytoscape.cpath2.internal.view.DownloadDetails;
import org.cytoscape.cpath2.internal.view.InteractionBundleModel;
import org.cytoscape.cpath2.internal.view.InteractionBundlePanel;
import org.cytoscape.cpath2.internal.view.MergePanel;
import org.cytoscape.cpath2.internal.view.PathwayTableModel;
import org.cytoscape.cpath2.internal.view.PhysicalEntityDetailsPanel;
import org.cytoscape.cpath2.internal.view.SearchBoxPanel;
import org.cytoscape.cpath2.internal.view.SearchDetailsPanel;
import org.cytoscape.cpath2.internal.view.SearchHitsPanel;
import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.undo.AbstractCyEdit;
import org.cytoscape.work.undo.UndoSupport;

// TODO: This is a "God" object.  Probably shouldn't exist, but it's better than having to
//       propagate all of the injected dependencies throughout all the implementation classes.
//       Lesser of two evils.
public class CPathFactory {
	private final CySwingApplication application;
	private final TaskManager taskManager;
	private final OpenBrowser openBrowser;
	private final CyNetworkManager networkManager;
	private final CyApplicationManager applicationManager;
	private final CyNetworkViewManager networkViewManager;
	private final CyNetworkReaderManager networkViewReaderManager;
	private final CyNetworkNaming naming;
	private final CyNetworkFactory networkFactory;
	private final CyLayoutAlgorithmManager layoutManager;
	private final UndoSupport undoSupport;
	private final VisualMappingManager mappingManager;
	
	public CPathFactory(CySwingApplication application, TaskManager taskManager, OpenBrowser openBrowser, CyNetworkManager networkManager, CyApplicationManager applicationManager, CyNetworkViewManager networkViewManager, CyNetworkReaderManager networkViewReaderManager, CyNetworkNaming naming, CyNetworkFactory networkFactory, CyLayoutAlgorithmManager layouts, UndoSupport undoSupport, VisualMappingManager mappingManager) {
		this.application = application;
		this.taskManager = taskManager;
		this.openBrowser = openBrowser;
		this.networkManager = networkManager;
		this.applicationManager = applicationManager;
		this.networkViewManager = networkViewManager;
		this.networkViewReaderManager = networkViewReaderManager;
		this.naming = naming;
		this.layoutManager = layouts;
		this.networkFactory = networkFactory;
		this.undoSupport = undoSupport;
		this.mappingManager = mappingManager;
	}
	
	public ExecuteGetRecordByCPathIdTaskFactory createExecuteGetRecordByCPathIdTaskFactory(CPathWebService webApi, long[] ids, CPathResponseFormat format, String networkTitle, CyNetwork networkToMerge) {
		networkTitle = naming.getSuggestedNetworkTitle(networkTitle);
		return new ExecuteGetRecordByCPathIdTaskFactory(webApi, ids, format, networkTitle, networkToMerge, this, mappingManager);
	}

	public ExecuteGetRecordByCPathIdTaskFactory createExecuteGetRecordByCPathIdTaskFactory(
			CPathWebService webApi, long[] ids, CPathResponseFormat format, String title) {
		title = naming.getSuggestedNetworkTitle(title);
		return new ExecuteGetRecordByCPathIdTaskFactory(webApi, ids, format, title, null, this, mappingManager);
	}

	public SearchBoxPanel createSearchBoxPanel(CPathWebService webApi) {
		return new SearchBoxPanel(webApi, this);
	}

	public OpenBrowser getOpenBrowser() {
		return openBrowser;
	}

	public SearchHitsPanel createSearchHitsPanel(
			InteractionBundleModel interactionBundleModel,
			PathwayTableModel pathwayTableModel, CPathWebService webApi) {
		return new SearchHitsPanel(interactionBundleModel, pathwayTableModel, webApi, this);
	}

	public MergePanel createMergePanel() {
		return new MergePanel(this);
	}

	public CySwingApplication getCySwingApplication() {
		return application;
	}

	public TaskManager getTaskManager() {
		return taskManager;
	}

	public DownloadDetails createDownloadDetails(List<BasicRecordType> passedRecordList, String physicalEntityName) {
		return new DownloadDetails(passedRecordList, physicalEntityName, this);
	}

	public JPanel createInteractionBundlePanel(InteractionBundleModel interactionBundleModel) {
		return new InteractionBundlePanel(interactionBundleModel, this);
	}

	public InteractionBundlePanel createInteractionBundlePanel(
			InteractionBundleModel interactionBundleModel, CyNetwork network,
			JDialog dialog) {
		return new InteractionBundlePanel(interactionBundleModel, network, dialog, this);
	}

	public NetworkMergeUtil getNetworkMergeUtil() {
		return new NetworkMergeUtil(this);
	}

	public PhysicalEntityDetailsPanel createPhysicalEntityDetailsPanel(SearchHitsPanel searchHitsPanel) {
		return new PhysicalEntityDetailsPanel(searchHitsPanel, this);
	}

	public SearchDetailsPanel createSearchDetailsPanel(
			InteractionBundleModel interactionBundleModel,
			PathwayTableModel pathwayTableModel) {
		return new SearchDetailsPanel(interactionBundleModel, pathwayTableModel, this);
	}

	public CyNetworkManager getNetworkManager() {
		return networkManager;
	}

	public CyApplicationManager getCyApplicationManager() {
		return applicationManager;
	}

	public CyNetworkViewManager getCyNetworkViewManager() {
		return networkViewManager;
	}

	public CyNetworkReaderManager getCyNetworkViewReaderManager() {
		return networkViewReaderManager;
	}

	public CyNetworkNaming getCyNetworkNaming() {
		return naming;
	}

	public Task createNetworkUtil(String cpathRequest, CyNetwork network, boolean merging) {
		return new NetworkUtil(cpathRequest, network, merging, this);
	}

	public CyNetworkFactory getCyNetworkFactory() {
		return networkFactory;
	}

	public UndoSupport getUndoSupport() {
		return undoSupport;
	}

	public TaskFactory createMergeNetworkTaskFactory(URL cpathURL, CyNetwork cyNetwork) {
		return new MergeNetworkTaskFactory(cpathURL, cyNetwork, this);
	}

	public AbstractCyEdit createMergeNetworkEdit(CyNetwork cyNetwork, Collection<CyNode> cyNodes, Set<CyEdge> cyEdges) {
		return new MergeNetworkEdit(cyNetwork, cyNodes, cyEdges, this);
	}

	public CPathNetworkImportTask createCPathNetworkImportTask(String query, CPathWebService client, CPathResponseFormat format) {
		return new CPathNetworkImportTask(query, client, format, this);
	}

	public CyNetworkManager getCyNetworkManager() {
		return networkManager;
	}

	public CyLayoutAlgorithmManager getCyLayoutAlgorithmManager() {
		return layoutManager;
	}
	
	public static JScrollPane createConfigPanel() {
		JPanel configPanel = new JPanel();
		configPanel.setBorder(new TitledBorder("Retrieval Options"));
		configPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		final JRadioButton button1 = new JRadioButton("Full Model");

		JTextArea textArea1 = new JTextArea();
		textArea1.setLineWrap(true);
		textArea1.setWrapStyleWord(true);
		textArea1.setEditable(false);
		textArea1.setOpaque(false);
		Font font = textArea1.getFont();
		Font smallerFont = new Font(font.getFamily(), font.getStyle(), font.getSize() - 2);
		textArea1.setFont(smallerFont);
		textArea1.setText("Retrieve the full model, as stored in the original BioPAX "
				+ "representation.  In this representation, nodes within a network can "
				+ "refer to physical entities and interactions.");
		textArea1.setBorder(new EmptyBorder(5, 20, 0, 0));

		JTextArea textArea2 = new JTextArea(3, 20);
		textArea2.setLineWrap(true);
		textArea2.setWrapStyleWord(true);
		textArea2.setEditable(false);
		textArea2.setOpaque(false);
		textArea2.setFont(smallerFont);
		textArea2.setText("Retrieve a simplified binary network, as inferred from the original "
				+ "BioPAX representation.  In this representation, nodes within a network refer "
				+ "to physical entities only, and edges refer to inferred interactions.");
		textArea2.setBorder(new EmptyBorder(5, 20, 0, 0));


		final JRadioButton button2 = new JRadioButton("Simplified Binary Model");
		button2.setSelected(true);
		ButtonGroup group = new ButtonGroup();
		group.add(button1);
		group.add(button2);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;

		c.gridx = 0;
		c.gridy = 0;
		configPanel.add(button2, c);

		c.gridy = 1;
		configPanel.add(textArea2, c);

		c.gridy = 2;
		configPanel.add(button1, c);

		c.gridy = 3;
		configPanel.add(textArea1, c);

		//  Add invisible filler to take up all remaining space
		c.gridy = 4;
		c.weighty = 1.0;
		JPanel panel = new JPanel();
		configPanel.add(panel, c);

		button1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				CPathProperties config = CPathProperties.getInstance();
				config.setDownloadMode(CPathProperties.DOWNLOAD_FULL_BIOPAX);
			}
		});
		button2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				CPathProperties config = CPathProperties.getInstance();
				config.setDownloadMode(CPathProperties.DOWNLOAD_REDUCED_BINARY_SIF);
			}
		});
		JScrollPane scrollPane = new JScrollPane(configPanel);
		return scrollPane;
	}

}
