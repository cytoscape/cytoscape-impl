package org.cytoscape.ding.impl.cyannotator.tasks;

import java.awt.Component;

import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.api.ImageAnnotation;
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
		Component[] annotations=cyAnnotator.getForeGroundCanvas().getComponents();
		for(int i=0;i<annotations.length;i++){
			if(annotations[i] instanceof ImageAnnotation && !canceled) {
				((ImageAnnotation)annotations[i]).reloadImage();
			}
		}

		annotations=cyAnnotator.getBackGroundCanvas().getComponents();
		for(int i=0;i<annotations.length;i++){
			if(annotations[i] instanceof ImageAnnotation && !canceled)
				((ImageAnnotation)annotations[i]).reloadImage();
		}
	}

	@Override
	public void cancel() {
		canceled = true;
	}
}
