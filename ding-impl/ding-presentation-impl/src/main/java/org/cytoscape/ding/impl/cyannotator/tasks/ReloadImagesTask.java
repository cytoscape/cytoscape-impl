package org.cytoscape.ding.impl.cyannotator.tasks;

import java.util.List;

import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.ding.impl.cyannotator.annotations.ImageAnnotationImpl;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReloadImagesTask implements Task {
	private final CyAnnotator cyAnnotator; 
	private boolean canceled = false;

	private static final Logger logger = LoggerFactory.getLogger(ReloadImagesTask.class);
	
	
	public ReloadImagesTask(CyAnnotator cyAnnotator) {
		this.cyAnnotator = cyAnnotator;
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		tm.setTitle("Reload Images");
		
		List<DingAnnotation> annotations = cyAnnotator.getForeGroundCanvas().getAnnotations();
		for(DingAnnotation a : annotations) {
			if(a instanceof ImageAnnotationImpl && !canceled) {
				((ImageAnnotationImpl)a).reloadImage();
			}
		}

		annotations = cyAnnotator.getBackGroundCanvas().getAnnotations();
		for(DingAnnotation a : annotations) {
			if(a instanceof ImageAnnotationImpl && !canceled) {
				((ImageAnnotationImpl)a).reloadImage();
			}
		}
	}

	@Override
	public void cancel() {
		canceled = true;
	}
}
