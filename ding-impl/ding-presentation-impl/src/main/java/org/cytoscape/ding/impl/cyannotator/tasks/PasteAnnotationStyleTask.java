package org.cytoscape.ding.impl.cyannotator.tasks;

import java.util.Collection;

import org.cytoscape.ding.impl.cyannotator.AnnotationClipboard;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.work.AbstractTask;
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

public class PasteAnnotationStyleTask extends AbstractTask {

	private final Collection<DingAnnotation> annotations;
	private final AnnotationClipboard clipboard;

	public PasteAnnotationStyleTask(Collection<DingAnnotation> annotations, AnnotationClipboard clipboard) {
		this.annotations = annotations;
		this.clipboard = clipboard;
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		tm.setTitle("Paste Annotation Style");
		
		if (annotations != null && !annotations.isEmpty() && !clipboard.isEmpty()) {
			tm.setStatusMessage("Pasting settings to at least " + annotations.size() + " annotation(s)...");
			
			var annotator = annotations.iterator().next().getCyAnnotator();
			annotator.markUndoEdit("Paste Style to Annotations");
			
			clipboard.pasteStyle(annotations);
			
			annotator.postUndoEdit();
		}
	}
}
