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

import javax.swing.SwingUtilities;

import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.internal.VizMapperProperty;
import org.cytoscape.view.vizmap.gui.internal.event.CellType;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.l2fprod.common.propertysheet.PropertySheetTable;
import com.l2fprod.common.propertysheet.PropertySheetTableModel.Item;

public class DeleteMappingFunctionTask extends AbstractTask {

	private final PropertySheetTable table;
	private final VisualMappingManager vmm;

	public DeleteMappingFunctionTask(final PropertySheetTable table, final VisualMappingManager vmm) {
		this.table = table;
		this.vmm = vmm;
	}


	@Override
	public void run(TaskMonitor monitor) throws Exception {

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				final int selectedRow = table.getSelectedRow();

				// If not selected, do nothing.
				if (selectedRow < 0)
					return;

				final Item value = (Item) table.getValueAt(selectedRow, 0);

				if (value.isProperty()) {
					final VizMapperProperty<?, ?, ?> prop = (VizMapperProperty<?, ?, ?>) value.getProperty();

					if (prop.getCellType() == CellType.VISUAL_PROPERTY_TYPE) {
						final VisualProperty<?> vp = (VisualProperty<?>) prop.getKey();
						removeMapping(vmm.getCurrentVisualStyle(), vp);
//						updatePropertySheet(prop, vp);
					}
				}

			}
		});

	}

	private final void removeMapping(final VisualStyle style, final VisualProperty<?> vp) {
		style.removeVisualMappingFunction(vp);
	}

}
