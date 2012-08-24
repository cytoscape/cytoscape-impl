package org.cytoscape.session;

import static org.junit.Assert.*;

import java.awt.Color;
import java.awt.Paint;
import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import javax.swing.plaf.basic.BasicViewportUI;

import org.cytoscape.io.internal.util.session.model.SelectedNodes;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import org.cytoscape.work.TaskIterator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;

@RunWith(JUnit4TestRunner.class)
@ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class Cy3SimpleSessionLodingTest extends BasicIntegrationTest {

	@Before
	public void setup() throws Exception {
		sessionFile = new File("./src/test/resources/testData/session3x/", "smallSession.cys");
		checkBasicConfiguration();
	}

	@Test
	public void testLoadSession() throws Exception {
		final TaskIterator ti = openSessionTF.createTaskIterator(sessionFile);
		tm.execute(ti);
		confirm();
	}

	private void confirm() {
		// test overall status of current session.
		checkGlobalStatus();
		
		Set<CyNetwork> networks = networkManager.getNetworkSet();
		final Iterator<CyNetwork> itr = networks.iterator();
		CyNetwork net1 = itr.next();
		CyNetwork net2 = itr.next();

		// Pick galFiltered.sif
		if(net1.getDefaultNetworkTable().getRow(net1.getSUID()).get(CyNetwork.NAME, String.class).equals("galFiltered.sif")) {
			checkNetwork(net1);
			checkChildNetwork(net2);
		} else {
			checkNetwork(net2);
			checkChildNetwork(net1);
		}
		
		checkTables();
	}
	
	private void checkGlobalStatus() {
		assertEquals(2, networkManager.getNetworkSet().size());
		assertEquals(2, viewManager.getNetworkViewSet().size());
		
		// Since this test runs in headless mode, this should be zero.
		assertEquals(0, renderingEngineManager.getAllRenderingEngines().size());
		
		assertEquals(36, tableManager.getAllTables(true).size());
		
		// 3 tables per network
		assertEquals(6, tableManager.getAllTables(false).size());
	}
	
	private void checkNetwork(final CyNetwork network) {
		assertEquals(331, network.getNodeCount());
		assertEquals(362, network.getEdgeCount());
		
		// Selection state
		Collection<CyRow> selectedNodes = network.getDefaultNodeTable().getMatchingRows(CyNetwork.SELECTED, true);
		Collection<CyRow> selectedEdges = network.getDefaultEdgeTable().getMatchingRows(CyNetwork.SELECTED, true);
		assertEquals(43, selectedNodes.size());
		assertEquals(51, selectedEdges.size());
		
		// View test
		Collection<CyNetworkView> views = viewManager.getNetworkViews(network);
		assertEquals(1, views.size());
		
		final CyNetworkView view = views.iterator().next();
		assertEquals(331, view.getNodeViews().size());
		assertEquals(362, view.getEdgeViews().size());
		
		// Visual Style
		assertEquals(8, vmm.getAllVisualStyles().size());
		final VisualStyle style = vmm.getVisualStyle(view);
		checkVisualStyle(style);
		
		final Color backgroungColor = (Color) view.getVisualProperty(BasicVisualLexicon.NETWORK_BACKGROUND_PAINT);
		assertEquals(Color.WHITE, backgroungColor);
		
		// All nodes should have same width:

		// TODO: This check seems wrong because by this point the visual style for the network has
		// already been applied.  This happens in CySessionManager.setCurrentSession, which is called
		// from the open session task.
//		Double nodeWidth = view.getNodeView(network.getNodeList().iterator().next()).getVisualProperty(BasicVisualLexicon.NODE_WIDTH);
//		assertEquals(Double.valueOf(60.0d), nodeWidth);
		
		// Apply the given style
		style.apply(view);
		
		Double nodeWidth = view.getNodeView(network.getNodeList().iterator().next()).getVisualProperty(BasicVisualLexicon.NODE_WIDTH);
		assertEquals(Double.valueOf(70.0d), nodeWidth);
	}
	
	private void checkVisualStyle(final VisualStyle style) {
		assertNotNull(style);
		assertEquals(vmm.getDefaultVisualStyle(), style);
		
		Collection<VisualMappingFunction<?, ?>> mappings = style.getAllVisualMappingFunctions();
		assertEquals(1, mappings.size());
		VisualMappingFunction<?, ?> labelMapping = mappings.iterator().next();
		assertTrue(labelMapping instanceof PassthroughMapping);
		assertEquals(BasicVisualLexicon.NODE_LABEL, labelMapping.getVisualProperty());
		assertEquals(CyNetwork.NAME, labelMapping.getMappingColumnName());
		assertEquals(String.class, labelMapping.getMappingColumnType());
	}
	
	private void checkChildNetwork(final CyNetwork network) {
		assertEquals(43, network.getNodeCount());
		assertEquals(34, network.getEdgeCount());
	}
	
	private void checkTables() {
		// Check SUID-type columns
		final Set<CyTable> globalTables = tableManager.getGlobalTables();
		// TODO
	}
}
