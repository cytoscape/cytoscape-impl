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

import java.util.HashSet;
import java.util.Set;

import javax.swing.SwingUtilities;

import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.AbstractCyEdit;
import org.cytoscape.work.undo.UndoSupport;

/**
 * Removes {@link VisualMappingFunction} objects from a {@link VisualStyle}.
 */
public class RemoveVisualMappingsTask extends AbstractTask {

	private final Set<VisualMappingFunction<?, ?>> mappings;
	private final Set<VisualMappingFunction<?, ?>> validMappings;
	private final VisualStyle style;
	private final ServicesUtil servicesUtil;

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public RemoveVisualMappingsTask(final Set<VisualMappingFunction<?, ?>> mappings, final VisualStyle style,
			final ServicesUtil servicesUtil) {
		this.mappings = mappings;
		this.style = style;
		this.servicesUtil = servicesUtil;
		validMappings = new HashSet<VisualMappingFunction<?,?>>();
	}

	// ==[ PUBLIC METHODS ]=============================================================================================
	
	@Override
	public void run(final TaskMonitor monitor) throws Exception {
		if (mappings != null && !mappings.isEmpty()) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					for (final VisualMappingFunction<?, ?> vm : mappings) {
						// Make sure the current style has these visual mappings
						if (vm != null && vm.equals(style.getVisualMappingFunction(vm.getVisualProperty())))
							validMappings.add(vm);
					}
					
					if (!validMappings.isEmpty()) {
						removeMappings();
						
						final UndoSupport undo = servicesUtil.get(UndoSupport.class);
						undo.postEdit(new RemoveVisualMappingEdit());
					}
				}
			});
		}
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
	private void removeMappings() {
		for (final VisualMappingFunction<?, ?> vm : validMappings)
			style.removeVisualMappingFunction(vm.getVisualProperty());
	}
	
	// ==[ CLASSES ]====================================================================================================
	
	private class RemoveVisualMappingEdit extends AbstractCyEdit {

		public RemoveVisualMappingEdit() {
			super("Remove " + (validMappings.size()) + " Visual Mapping" + (validMappings.size() > 1 ? "s" : ""));
		}

		@Override
		public void undo() {
			for (final VisualMappingFunction<?, ?> vm : validMappings)
				style.addVisualMappingFunction(vm);
		}

		@Override
		public void redo() {
			removeMappings();
		}
	}
}
