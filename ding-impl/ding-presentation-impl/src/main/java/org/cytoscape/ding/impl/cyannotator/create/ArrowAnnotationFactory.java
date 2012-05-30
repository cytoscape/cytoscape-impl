

package org.cytoscape.ding.impl.cyannotator.create;

import org.cytoscape.ding.impl.DGraphView;
import javax.swing.JFrame;
import java.awt.geom.Point2D;
import java.util.Map;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.api.Annotation;
// import org.cytoscape.ding.impl.cyannotator.annotations.ArrowAnnotationImpl;

public class ArrowAnnotationFactory implements AnnotationFactory {

	public JFrame createAnnotationFrame(DGraphView view, Point2D location) {
		System.err.println("Not yet implemented");
		return new JFrame("Not Yet Implemented!");
	}

	public Annotation createAnnotation(String type, CyAnnotator cyAnnotator, DGraphView view, Map<String, String> argMap) {
/*
		if ( type.equals(ArrowAnnotation.NAME) ) 
			return new ArrowAnnotationImpl(cyAnnotator, view,argMap);
		else 
*/
			return null;

	}
}
