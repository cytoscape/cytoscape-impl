package org.cytoscape.ding.impl.cyannotator.tasks;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */


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
