package org.cytoscape.ding.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import org.cytoscape.ding.impl.cyannotator.create.AnnotationFactoryManager;
import org.cytoscape.ding.customgraphics.CustomGraphicsManager;
import org.cytoscape.ding.impl.DingViewModelFactory;
import org.cytoscape.ding.impl.ViewTaskFactoryListener;
import org.cytoscape.dnd.DropNetworkViewTaskFactory;
import org.cytoscape.dnd.DropNodeViewTaskFactory;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.TableTestSupport;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.spacial.SpacialIndex2DFactory;
import org.cytoscape.spacial.internal.rtree.RTreeFactory;
import org.cytoscape.task.EdgeViewTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.presentation.AbstractRenderingEngineTest;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.swing.SubmenuTaskManager;
import org.cytoscape.work.undo.UndoSupport;
import org.junit.Test;
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


