package org.cytoscape.ding.impl.cyannotator;

import static java.util.Collections.emptyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.DingGraphLOD;
import org.cytoscape.ding.impl.DingRenderer;
import org.cytoscape.ding.impl.HandleFactoryImpl;
import org.cytoscape.ding.impl.cyannotator.create.GroupAnnotationFactory;
import org.cytoscape.ding.impl.cyannotator.create.ShapeAnnotationFactory;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.GroupAnnotation;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.property.values.HandleFactory;
import org.junit.Before;

public class AbstractAnnotationTest {

	private NetworkViewTestSupport nvTest = new NetworkViewTestSupport();
	protected CyNetworkView graphView;
	protected AnnotationManager annotationManager;
	
	protected ShapeAnnotationFactory shapeFactory;
	protected GroupAnnotationFactory groupFactory;
	
	
	@Before
	public void before() {
		CyServiceRegistrar registrar = mock(CyServiceRegistrar.class);
		DingRenderer dingRenderer = mock(DingRenderer.class);
		
		when(registrar.getService(DingRenderer.class)).thenReturn(dingRenderer);
		when(registrar.getService(CyEventHelper.class)).thenReturn(mock(CyEventHelper.class));
		
		graphView = nvTest.getNetworkView();
		
		DVisualLexicon dingLexicon = new DVisualLexicon();
		AnnotationFactoryManager annMgr = mock(AnnotationFactoryManager.class);
		DingGraphLOD dingGraphLOD = mock(DingGraphLOD.class);
		HandleFactory handleFactory = new HandleFactoryImpl();
		DRenderingEngine re = new DRenderingEngine(graphView, dingLexicon, annMgr, dingGraphLOD, handleFactory, registrar);
		
		when(dingRenderer.getRenderingEngine(graphView)).thenReturn(re);
		
		annotationManager = new AnnotationManagerImpl(registrar);
		
		shapeFactory = new ShapeAnnotationFactory(registrar);
		groupFactory = new GroupAnnotationFactory(registrar);
	}
	
	
	
	protected ShapeAnnotation createShapeAnnotation() {
		return shapeFactory.createAnnotation(ShapeAnnotation.class, graphView, emptyMap());
	}
	
	protected ShapeAnnotation createShapeAnnotation(String name, int zorder, String canvas) {
		return shapeFactory.createAnnotation(ShapeAnnotation.class, graphView, toArgs(name, zorder, canvas));
	}
	
	protected GroupAnnotation createGroupAnnotation() {
		return groupFactory.createAnnotation(GroupAnnotation.class, graphView, emptyMap());
	}
	
	protected GroupAnnotation createGroupAnnotation(String name, int zorder, String canvas) {
		return groupFactory.createAnnotation(GroupAnnotation.class, graphView, toArgs(name, zorder, canvas));
	}
	
	
	private static Map<String, String> toArgs(String name, int zorder, String canvas) {
		Map<String,String> args = new HashMap<>();
		args.put(Annotation.NAME, name);
		args.put(Annotation.Z, String.valueOf(zorder));
		args.put(Annotation.CANVAS, canvas);
		return args;
	}
}
