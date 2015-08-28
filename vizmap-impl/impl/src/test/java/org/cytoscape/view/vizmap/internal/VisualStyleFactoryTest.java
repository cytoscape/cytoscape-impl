package org.cytoscape.view.vizmap.internal;

/*
 * #%L
 * Cytoscape VizMap Impl (vizmap-impl)
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

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_LABEL_COLOR;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_LABEL_FONT_SIZE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_BACKGROUND_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_FILL_COLOR;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_HEIGHT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_SIZE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_WIDTH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Color;
import java.awt.Paint;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.NetworkViewRenderer;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NullVisualProperty;
import org.cytoscape.view.vizmap.AbstractVisualStyleFactoryTest;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.internal.mappings.DiscreteMappingImpl;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class VisualStyleFactoryTest extends AbstractVisualStyleFactoryTest {

	private CyServiceRegistrar serviceRegistrar;
	private CyEventHelper eventHelper;
	private CyApplicationManager applicationMgr;
	private NetworkViewRenderer netViewRenderer;
	private RenderingEngineFactory<CyNetwork> engineFactory;
	private VisualLexicon lexicon = 
			new BasicVisualLexicon(new NullVisualProperty("MINIMAL_ROOT", "Minimal Root Visual Property"));

	@Before
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void setUp() throws Exception {
		final Set lexiconSet = Collections.singleton(lexicon);
		
		final VisualMappingManager vmMgr = mock(VisualMappingManager.class);
		when(vmMgr.getAllVisualLexicon()).thenReturn(lexiconSet);
		
		final VisualMappingFunctionFactory ptFactory = mock(VisualMappingFunctionFactory.class);
		
		serviceRegistrar = mock(CyServiceRegistrar.class);
		eventHelper = mock(CyEventHelper.class);
		
		engineFactory = mock(RenderingEngineFactory.class);
		when(engineFactory.getVisualLexicon()).thenReturn(lexicon);
		
		netViewRenderer = mock(NetworkViewRenderer.class);
		when(netViewRenderer.getRenderingEngineFactory(Mockito.anyString())).thenReturn(engineFactory);
		
		applicationMgr = mock(CyApplicationManager.class);
		when(applicationMgr.getCurrentNetworkViewRenderer()).thenReturn(netViewRenderer);
		
		when(serviceRegistrar.getService(CyEventHelper.class)).thenReturn(eventHelper);
		when(serviceRegistrar.getService(VisualMappingManager.class)).thenReturn(vmMgr);
		when(serviceRegistrar.getService(CyApplicationManager.class)).thenReturn(applicationMgr);
		
		factory = new VisualStyleFactoryImpl(serviceRegistrar, ptFactory);
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testCopyVisualStyle() {
		final VisualStyle vs1 = new VisualStyleImpl("Style 1", serviceRegistrar);
		// Add a few default values
		vs1.setDefaultValue(NODE_FILL_COLOR, Color.LIGHT_GRAY);
		vs1.setDefaultValue(NODE_SIZE, 64.0);
		vs1.setDefaultValue(EDGE_LABEL_COLOR, Color.GREEN);
		vs1.setDefaultValue(EDGE_LABEL_FONT_SIZE, 32);
		vs1.setDefaultValue(NETWORK_BACKGROUND_PAINT, Color.YELLOW);
		// Add a mapping
		final DiscreteMapping<String, Paint> dm1 = 
				new DiscreteMappingImpl<>("myattr1", String.class, NODE_FILL_COLOR, eventHelper);
		dm1.putMapValue("a", Color.CYAN);
		dm1.putMapValue("b", Color.BLUE);
		vs1.addVisualMappingFunction(dm1);
		// Add a dependency
		final Set<VisualProperty<Double>> depProps = new HashSet<>();
		depProps.add(NODE_WIDTH);
		depProps.add(NODE_HEIGHT);
		final VisualPropertyDependency<Double> dep1 = 
				new VisualPropertyDependency<>("nodeSizeDep", "Lock Node W/H", depProps, lexicon);
		dep1.setDependency(false);
		vs1.addVisualPropertyDependency(dep1);
		
		// Copy the style and test it
		final VisualStyle vs2 = factory.createVisualStyle(vs1);
		assertNotEquals(vs1, vs2);
		assertEquals(vs1.getTitle(), vs2.getTitle());
		
		assertEquals(Color.LIGHT_GRAY, vs2.getDefaultValue(NODE_FILL_COLOR));
		assertEquals(new Double(64.0), vs2.getDefaultValue(NODE_SIZE));
		assertEquals(Color.GREEN, vs2.getDefaultValue(EDGE_LABEL_COLOR));
		assertEquals(new Integer(32), vs2.getDefaultValue(EDGE_LABEL_FONT_SIZE));
		assertEquals(Color.YELLOW, vs2.getDefaultValue(NETWORK_BACKGROUND_PAINT));
		
		final DiscreteMapping<String, Paint> dm2 = 
				(DiscreteMapping<String, Paint>) vs2.getVisualMappingFunction(NODE_FILL_COLOR);
		assertEquals("myattr1", dm2.getMappingColumnName());
		assertEquals(2, dm2.getAll().size());
		assertEquals(Color.CYAN, dm2.getMapValue("a"));
		assertEquals(Color.BLUE, dm2.getMapValue("b"));
		
		final Set<VisualPropertyDependency<?>> depSet2 = vs2.getAllVisualPropertyDependencies();
		assertEquals(vs1.getAllVisualPropertyDependencies().size(), depSet2.size());
		
		boolean depFound = false;
		
		for (final VisualPropertyDependency<?> dep : depSet2) {
			if (dep.getIdString().equals("nodeSizeDep")) {
				depFound = true;
				assertTrue("The copied dependency is not a new instance", dep != dep1);
				assertEquals(dep1.getDisplayName(), dep.getDisplayName());
				assertEquals(dep1.getParentVisualProperty(), dep.getParentVisualProperty());
				assertFalse(dep.isDependencyEnabled());
				assertEquals(2, dep.getVisualProperties().size());
				assertTrue(dep.getVisualProperties().contains(NODE_WIDTH));
				assertTrue(dep.getVisualProperties().contains(NODE_HEIGHT));
				break;
			}
		}
		
		assertTrue("VisualPropertyDependency was not copied.", depFound);
	}
}
