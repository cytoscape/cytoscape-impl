

package org.cytoscape.ding.impl.cyannotator.create;

import org.cytoscape.ding.impl.DGraphView;
import javax.swing.JFrame;
import java.awt.geom.Point2D;
import java.util.Map;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.api.Annotation;
import org.cytoscape.ding.impl.cyannotator.annotations.BoundedTextAnnotationImpl;
import org.cytoscape.ding.impl.cyannotator.dialogs.BoundedTextAnnotationDialog;

public class BoundedTextAnnotationFactory implements AnnotationFactory {

	public JFrame createAnnotationFrame(DGraphView view, Point2D location) {
		return new BoundedTextAnnotationDialog(view, location);
	}

	public Annotation createAnnotation(String type, CyAnnotator cyAnnotator, DGraphView view, Map<String, String> argMap) {
		if ( type.equals(BoundedTextAnnotationImpl.NAME) ) {
			Annotation a = new BoundedTextAnnotationImpl(cyAnnotator, view, argMap);
			a.update();
			return a;
		} else 
			return null;
	}
}
