package org.cytoscape.ding.impl;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
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


import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.presentation.AbstractRenderingEngineTest;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineFactory;


public class DGraphViewTest extends AbstractRenderingEngineTest {

	protected DGraphView graphView;
	
	
	//protected int numberOfVP;

	public DGraphViewTest() {
		
		
		NetworkViewTestSupport nvTest = new NetworkViewTestSupport();
		graphView = (DGraphView) nvTest.getNetworkView();
			
			
		this.factory = new RenderingEngineFactory<CyNetwork>() {
			
			
			@Override
			public VisualLexicon getVisualLexicon() {
				// TODO Auto-generated method stub
				return graphView.getVisualLexicon();
			}
			
			@Override
			public RenderingEngine<CyNetwork> createRenderingEngine(
					Object visualizationContainer, View<CyNetwork> viewModel) {
				// TODO Auto-generated method stub
				return graphView;
			}
		};
		
		this.networkView = graphView;
		this.numberOfVP = graphView.getVisualLexicon().getAllVisualProperties().size(); 
		
	}
	
	
	
}


