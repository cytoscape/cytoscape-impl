package org.cytoscape.ding.impl.cyannotator;

import static java.util.Collections.emptyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.cyannotator.create.GroupAnnotationFactory;
import org.cytoscape.ding.impl.cyannotator.create.ShapeAnnotationFactory;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.GroupAnnotation;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.junit.Before;

public class AnnotationTest {

	private NetworkViewTestSupport nvTest = new NetworkViewTestSupport();
	protected DGraphView graphView;
	protected AnnotationManager annotationManager;
	
	protected ShapeAnnotationFactory shapeFactory;
	protected GroupAnnotationFactory groupFactory;
	
	
	@Before
	public void before() {
		CyServiceRegistrar registrar = mock(CyServiceRegistrar.class);
		when(registrar.getService(CyEventHelper.class)).thenReturn(mock(CyEventHelper.class));
		
		graphView = (DGraphView) nvTest.getNetworkView();
		annotationManager = new AnnotationManagerImpl(registrar);
		
		shapeFactory = new ShapeAnnotationFactory(registrar);
		groupFactory = new GroupAnnotationFactory(registrar);
	}
	
	
	
	protected ShapeAnnotation createShapeAnnotation() {
		return shapeFactory.createAnnotation(ShapeAnnotation.class, graphView, emptyMap());
	}
	
	protected ShapeAnnotation createShapeAnnotation(String name, int zorder) {
		return shapeFactory.createAnnotation(ShapeAnnotation.class, graphView, toArgs(name, zorder));
	}
	
	protected GroupAnnotation createGroupAnnotation() {
		return groupFactory.createAnnotation(GroupAnnotation.class, graphView, emptyMap());
	}
	
	protected GroupAnnotation createGroupAnnotation(String name, int zorder) {
		return groupFactory.createAnnotation(GroupAnnotation.class, graphView, toArgs(name, zorder));
	}
	
	
	private static Map<String, String> toArgs(String name, int zorder) {
		Map<String,String> args = new HashMap<>();
		args.put(Annotation.NAME, name);
		args.put(Annotation.Z, String.valueOf(zorder));
		return args;
	}
}
