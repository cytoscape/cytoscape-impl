package org.cytoscape.ding.impl.cyannotator;

import static java.util.Collections.emptyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.cyannotator.create.GroupAnnotationFactory;
import org.cytoscape.ding.impl.cyannotator.create.ShapeAnnotationFactory;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.service.util.CyServiceRegistrar;
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
	
	protected GroupAnnotation createGroupAnnotation() {
		return groupFactory.createAnnotation(GroupAnnotation.class, graphView, emptyMap());
	}
}
