

package org.cytoscape.ding.impl.cyannotator.create;

import org.cytoscape.ding.impl.DGraphView;
import javax.swing.JFrame;
import java.awt.geom.Point2D;
import java.util.Map;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.api.Annotation;
import org.cytoscape.ding.impl.cyannotator.annotations.ShapeAnnotationImpl;
import org.cytoscape.ding.impl.cyannotator.dialogs.ShapeAnnotationDialog;

public class ShapeAnnotationFactory implements AnnotationFactory {

	public JFrame createAnnotationFrame(DGraphView view, Point2D location) {
		return new ShapeAnnotationDialog(view, location);
	}

	public Annotation createAnnotation(String type, CyAnnotator cyAnnotator, DGraphView view, Map<String, String> argMap) {
		if ( type.equals(ShapeAnnotationImpl.NAME) ) {
			Annotation a = new ShapeAnnotationImpl(cyAnnotator, view,argMap);
			a.update();
			return a;
		} else 
			return null;
	}
}
