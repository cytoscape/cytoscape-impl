package org.cytoscape.ding.impl;


import org.cytoscape.model.CyNetwork;

import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.presentation.AbstractRenderingEngineTest;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.ding.*;


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


