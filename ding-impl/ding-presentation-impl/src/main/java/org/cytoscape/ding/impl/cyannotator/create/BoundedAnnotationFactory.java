

package org.cytoscape.ding.impl.cyannotator.create;

import org.cytoscape.ding.impl.DGraphView;
import javax.swing.JFrame;
import java.util.Map;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.annotations.Annotation;
import org.cytoscape.ding.impl.cyannotator.annotations.BoundedAnnotation;

public class BoundedAnnotationFactory implements AnnotationFactory {

	public JFrame createAnnotationFrame(DGraphView view) {
		return new cBoundedAnnotation(view);
	}

	public Annotation createAnnotation(String type, CyAnnotator cyAnnotator, DGraphView view, Map<String, String> argMap) {
		if ( type.equals(BoundedAnnotation.NAME) ) 
			return new BoundedAnnotation(cyAnnotator, view,argMap);
		else 
			return null;
	}
}
