package org.cytoscape.view.vizmap.gui.internal.task;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
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

import java.util.Set;

import javax.swing.SwingUtilities;

import org.cytoscape.view.vizmap.gui.internal.view.VisualPropertySheetItem;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class DeleteVisualMappingsTask extends AbstractTask {

	private final Set<VisualPropertySheetItem<?>> items;

	public DeleteVisualMappingsTask(final Set<VisualPropertySheetItem<?>> items) {
		this.items = items;
	}


	@Override
	public void run(TaskMonitor monitor) throws Exception {

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (items != null) {
					for (final VisualPropertySheetItem<?> item : items) {
						if (item.getModel().getVisualMappingFunction() != null)
							item.getModel().setVisualMappingFunction(null);
					}
				}
			}
		});

	}
}
