

package org.cytoscape.ding.impl.cyannotator.create;

import org.cytoscape.ding.impl.DGraphView;
import javax.swing.JFrame;
import java.awt.geom.Point2D;
import java.util.Map;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.api.Annotation;
import org.cytoscape.ding.impl.cyannotator.annotations.TextAnnotationImpl;
import org.cytoscape.ding.impl.cyannotator.dialogs.TextAnnotationDialog;

public class TextAnnotationFactory implements AnnotationFactory {

	public JFrame createAnnotationFrame(DGraphView view, Point2D location) {
		return new TextAnnotationDialog(view, location);
	}

	public Annotation createAnnotation(String type, CyAnnotator cyAnnotator, DGraphView view, Map<String, String> argMap) {
		if ( type.equals(TextAnnotationImpl.NAME) ) {
			Annotation a = new TextAnnotationImpl(cyAnnotator, view, argMap);
			a.update();
			return a;
		} else 
			return null;
	}
}
