package org.cytoscape.ding.impl.cyannotator.tasks;

import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.annotations.ImageAnnotationImpl;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

public class ReloadImagesTask implements Task {
	
	private final CyAnnotator cyAnnotator;
	private boolean canceled;

	public ReloadImagesTask(CyAnnotator cyAnnotator) {
		this.cyAnnotator = cyAnnotator;
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		tm.setTitle("Reload Images");

		var annotations = cyAnnotator.getAnnotations();
		
		for (var a : annotations) {
			if (a instanceof ImageAnnotationImpl && !canceled)
				((ImageAnnotationImpl) a).reloadImage();
		}
	}

	@Override
	public void cancel() {
		canceled = true;
	}
}
