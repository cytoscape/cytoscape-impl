

package org.cytoscape.ding.impl.cyannotator.create;

import org.cytoscape.ding.impl.DGraphView;
import javax.swing.JFrame;
import java.util.Map;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.annotations.Annotation;
import org.cytoscape.ding.impl.cyannotator.annotations.ImageAnnotation;
import org.cytoscape.ding.customgraphics.CustomGraphicsManager;

public class ImageAnnotationFactory implements AnnotationFactory {
	private final CustomGraphicsManager customGraphicsManager;

	public ImageAnnotationFactory(CustomGraphicsManager customGraphicsManager) {
		this.customGraphicsManager = customGraphicsManager;
	}

	public JFrame createAnnotationFrame(DGraphView view) {
		return new cImageAnnotation(view,customGraphicsManager);
	}

	public Annotation createAnnotation(String type, CyAnnotator cyAnnotator, DGraphView view, Map<String, String> argMap) {
		if ( type.equals(ImageAnnotation.NAME) ) 
			return new ImageAnnotation(cyAnnotator, view,argMap,customGraphicsManager);
		else 
			return null;
	}
}
