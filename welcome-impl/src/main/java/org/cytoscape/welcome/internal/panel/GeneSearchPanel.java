package org.cytoscape.welcome.internal.panel;

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

import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.welcome.internal.style.IntActXGMMLVisualStyleBuilder;
import org.cytoscape.welcome.internal.task.BuildNetworkBasedOnGenesTaskFactory;
import org.cytoscape.work.swing.DialogTaskManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

public class GeneSearchPanel extends AbstractWelcomeScreenChildPanel implements ActionListener
{
	JComboBox species = new JComboBox();
	JTextArea geneList = new JTextArea();
	JButton buildNetwork = new JButton("Build Network");

	private final DialogTaskManager taskManager;
	private final CyNetworkReaderManager networkReaderManager;
	private final CyNetworkManager networkManager;
	private final CyNetworkViewFactory networkViewFactory;
	private final CyLayoutAlgorithmManager layoutAlgorithmManager;
	private final VisualMappingManager visualMappingManager;
	private final CyNetworkViewManager networkViewManager;
	private final IntActXGMMLVisualStyleBuilder intActVSBuilder;


	public GeneSearchPanel(final DialogTaskManager taskManager, CyNetworkReaderManager networkReaderManager, CyNetworkManager networkManager, CyNetworkViewFactory networkViewFactory, CyLayoutAlgorithmManager layoutAlgorithmManager, VisualMappingManager visualMappingManager, CyNetworkViewManager networkViewManager, IntActXGMMLVisualStyleBuilder intActVSBuilder)
	{
		this.taskManager = taskManager;
		this.networkReaderManager = networkReaderManager;
		this.networkManager = networkManager;
		this.networkViewFactory = networkViewFactory;
		this.layoutAlgorithmManager = layoutAlgorithmManager;
		this.visualMappingManager = visualMappingManager;
		this.networkViewManager = networkViewManager;
		initComponents();
		this.intActVSBuilder = intActVSBuilder;
	}

	private void initComponents()
	{
		JLabel speciesLabel = new JLabel("Species");
		speciesLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		species.setAlignmentX(Component.LEFT_ALIGNMENT);
		JLabel genesLabel = new JLabel("Genes");
		genesLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		geneList.setPreferredSize( new Dimension(200,200));
		JScrollPane sp = new JScrollPane( geneList );
		sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		sp.setAlignmentX(Component.LEFT_ALIGNMENT);
		JPanel buildNetworkPanel = new JPanel();
		buildNetworkPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		buildNetworkPanel.setLayout( new FlowLayout( FlowLayout.RIGHT) );
		buildNetworkPanel.add(buildNetwork);

		species.addItem("human");
		species.addItem("mouse");
		species.addItem("yeast");
		species.addItem("ecoli");
		species.addItem("rat");
		species.addItem("measew");
		species.addItem("caeel");
		species.addItem("trepa");
		species.addItem("i34a1");
		species.addItem("xenla");
		species.addItem("drome");
		species.addItem("arath");
		species.addItem("bacsu");
		species.addItem("hcvco");
		species.addItem("hrsva");
		species.addItem("camje");
		species.addItem("ebvb9");
		species.addItem("hhv11");
		species.addItem("syny3");
		species.addItem("hv1h2");
		species.addItem("9hiv1");
		species.addItem("chick");
		species.addItem("sv40");
		species.addItem("hcvh");
		species.addItem("hpv16");
		species.addItem("bovin");
		species.addItem("theko");
		species.addItem("canen");

		species.addItem("i97a1");
		species.addItem("danre");

		this.setLayout( new BoxLayout(this, BoxLayout.PAGE_AXIS) );
		this.add( speciesLabel );
		this.add( species );
		this.add( genesLabel );
		this.add( sp );
		this.add( buildNetworkPanel );

		buildNetwork.addActionListener(this);

	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		String selectedSpecies = species.getSelectedItem().toString();
		java.util.List<String> geneNames = Arrays.asList(geneList.getText().split("\\s+"));

		closeParentWindow();
		BuildNetworkBasedOnGenesTaskFactory buildNetworkFactory = new BuildNetworkBasedOnGenesTaskFactory(networkReaderManager, networkManager, networkViewFactory, layoutAlgorithmManager, visualMappingManager, networkViewManager, intActVSBuilder, selectedSpecies, geneNames);
		taskManager.execute( buildNetworkFactory.createTaskIterator() );
	}
}
