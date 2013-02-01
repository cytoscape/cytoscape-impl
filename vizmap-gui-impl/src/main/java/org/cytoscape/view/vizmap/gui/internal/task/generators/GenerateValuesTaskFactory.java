package org.cytoscape.view.vizmap.gui.internal.task.generators;

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

import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.gui.util.DiscreteMappingGenerator;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import com.l2fprod.common.propertysheet.PropertySheetPanel;

public class GenerateValuesTaskFactory extends AbstractTaskFactory {

	private final DiscreteMappingGenerator<?> generator;
	private final PropertySheetPanel table;
	private final VisualMappingManager vmm;

	public GenerateValuesTaskFactory(final DiscreteMappingGenerator<?> generator, final PropertySheetPanel table,
			final VisualMappingManager vmm) {
		this.generator = generator;
		this.table = table;
		this.vmm = vmm;
	}

	@Override
	public TaskIterator createTaskIterator() {

		return new TaskIterator(new GenerateValuesTask(generator, table, vmm));
	}
}
