

package org.cytoscape.ding.impl.cyannotator.create;

import org.cytoscape.ding.impl.DGraphView;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.annotations.Annotation;

public class AnnotationFactoryManager {

	List<AnnotationFactory> annotationFactories;

	public AnnotationFactoryManager() {
		annotationFactories = new ArrayList<AnnotationFactory>();
	}

	public Annotation getAnnotation(String type, CyAnnotator ann, DGraphView view, Map<String,String> argMap) {
		Annotation annotation = null;
		for ( AnnotationFactory factory : annotationFactories ) {
			annotation = factory.createAnnotation(type,ann,view,argMap);
			if ( annotation != null )
				break;
		}
		return annotation;
	}

    public void addAnnotationFactory(AnnotationFactory factory, Map props) {
        if ( factory != null )
            annotationFactories.add(factory);
    }

    public void removeAnnotationFactory(AnnotationFactory factory, Map props) {
        if ( factory != null )
            annotationFactories.remove(factory);
    }
}
