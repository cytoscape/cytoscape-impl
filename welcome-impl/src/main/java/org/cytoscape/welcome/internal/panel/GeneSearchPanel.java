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
import org.cytoscape.io.webservice.WebServiceClient;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.*;
import org.cytoscape.work.swing.DialogTaskManager;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

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
	private final WebServiceClient webServiceClient;
	private final NetworkTaskFactory edgeBundlerTaskFactory;
	private final CyServiceRegistrar serviceRegistrar;


	public GeneSearchPanel(final DialogTaskManager taskManager, CyNetworkReaderManager networkReaderManager, CyNetworkManager networkManager, CyNetworkViewFactory networkViewFactory, CyLayoutAlgorithmManager layoutAlgorithmManager, VisualMappingManager visualMappingManager, CyNetworkViewManager networkViewManager, WebServiceClient webServiceClient, NetworkTaskFactory edgeBundlerTaskFactory, CyServiceRegistrar serviceRegistrar)
	{
		this.taskManager = taskManager;
		this.networkReaderManager = networkReaderManager;
		this.networkManager = networkManager;
		this.networkViewFactory = networkViewFactory;
		this.layoutAlgorithmManager = layoutAlgorithmManager;
		this.visualMappingManager = visualMappingManager;
		this.networkViewManager = networkViewManager;
		this.webServiceClient = webServiceClient;
		this.edgeBundlerTaskFactory = edgeBundlerTaskFactory;
		this.serviceRegistrar = serviceRegistrar;
		initComponents();
	}

	private void initComponents()
	{
		this.setBorder(BorderFactory.createEmptyBorder(5, 5, 3, 5));
		species.setOpaque(true);
		species.setBackground(PANEL_COLOR);
		buildNetwork.setOpaque(true);
		buildNetwork.setBackground(PANEL_COLOR);
		
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
		sp.setPreferredSize(new Dimension(sp.getPreferredSize().width, 320));
		
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

		final GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
						.addComponent(speciesLabel)
						.addComponent(species, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(genesLabel)
						.addGroup(layout.createParallelGroup(Alignment.TRAILING, true)
										.addComponent(sp, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(buildNetwork)
						)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
						.addComponent(speciesLabel)
						.addComponent(species)
						.addComponent(genesLabel)
						.addComponent(sp)
						.addComponent(buildNetwork)
		);
		
		buildNetwork.addActionListener(this);

	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		String selectedSpecies = species.getSelectedItem().toString();
		java.util.List<String> geneNames = Arrays.asList(geneList.getText().split("\\s+"));

		String query = "species:" + selectedSpecies;
		if( geneNames != null && !geneNames.isEmpty() )
		{
			query += " AND ";
			if( geneNames.size() > 1 )
				query += "( ";
			for( int i = 0; i < geneNames.size() - 1; i++ )
			{
				String geneName = geneNames.get(i);
				query += "alias:" + geneName + " OR ";
			}
			query += "alias:" + geneNames.get(geneNames.size()-1);
			if( geneNames.size() > 1 )
				query += " )";
		}
		System.out.println("Query = " + query);

		closeParentWindow();
		taskManager.execute( webServiceClient.createTaskIterator(query), new TaskObserver()
		{
			CyNetwork network;

			@Override
			public void taskFinished(ObservableTask task)
			{
				Object networks = task.getResults(Object.class);
				if( networks instanceof Set )
				{
					Set networkSet = (Set)networks;
					for( Object o : networkSet )
					{
						System.out.println("Hello there FOO BAR");
						if( o instanceof CyNetwork )
							network = (CyNetwork)o;
						return;
					}
				}

			}

			@Override
			public void allFinished(FinishStatus finishStatus)
			{
				Collection<CyNetworkView> views = networkViewManager.getNetworkViews(network);
				CyLayoutAlgorithm layoutAlgorithm = layoutAlgorithmManager.getLayout("force-directed");

				for( final CyNetworkView view : views )
				{
					Object ctx = layoutAlgorithm.getDefaultLayoutContext();
					TaskIterator ti = layoutAlgorithm.createTaskIterator(view, ctx, CyLayoutAlgorithm.ALL_NODE_VIEWS, "");

					Task styleTask = new AbstractTask()
					{
						@Override
						public void run(TaskMonitor taskMonitor) throws Exception
						{
							VisualStyle defaultStyle = visualMappingManager.getDefaultVisualStyle();

							visualMappingManager.setVisualStyle(defaultStyle, view);

							defaultStyle.apply(view);
							view.updateView();
						}
					};
					ti.append(styleTask);


					TaskIterator edgeBundleTaskIterator = edgeBundlerTaskFactory.createTaskIterator(view.getModel());
					//Task edgeBundleTask = edgeBundleTaskIterator.next();
					Map<String, Object> settings = new HashMap<String, Object>();
					settings.put("numNubs", 3);
					settings.put("K", 0.003);
					settings.put("COMPATABILITY_THRESHOLD", 0.3);
					settings.put("maxIterations", 5000);
					TunableSetter setter = serviceRegistrar.getService(TunableSetter.class);;
					//setter.applyTunables(edgeBundleTask, settings);

					ti.append( setter.createTaskIterator(edgeBundleTaskIterator, settings) );



					taskManager.execute(ti);
				}
			}
		});
	}
}
