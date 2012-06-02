

package org.cytoscape.ding.impl.cyannotator.create;

import org.cytoscape.ding.impl.DGraphView;
import javax.swing.JFrame;
import java.awt.geom.Point2D;
import java.util.Map;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.api.Annotation;
import org.cytoscape.ding.impl.cyannotator.annotations.ImageAnnotationImpl;
import org.cytoscape.ding.customgraphics.CustomGraphicsManager;

public class ImageAnnotationFactory implements AnnotationFactory {
	private final CustomGraphicsManager customGraphicsManager;

	public ImageAnnotationFactory(CustomGraphicsManager customGraphicsManager) {
		this.customGraphicsManager = customGraphicsManager;
	}

	public JFrame createAnnotationFrame(DGraphView view, Point2D location) {
		return new cImageAnnotation(view, location, customGraphicsManager);
	}

	public Annotation createAnnotation(String type, CyAnnotator cyAnnotator, DGraphView view, Map<String, String> argMap) {
		if ( type.equals(ImageAnnotationImpl.NAME) ) {
			Annotation a = new ImageAnnotationImpl(cyAnnotator, view,argMap,customGraphicsManager);
			a.update();
			return a;
		} else 
			return null;
	}
}
