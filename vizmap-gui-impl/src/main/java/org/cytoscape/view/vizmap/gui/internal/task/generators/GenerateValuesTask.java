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

import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.internal.VizMapperProperty;
import org.cytoscape.view.vizmap.gui.internal.event.CellType;
import org.cytoscape.view.vizmap.gui.util.DiscreteMappingGenerator;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.propertysheet.PropertySheetPanel;
import com.l2fprod.common.propertysheet.PropertySheetTableModel.Item;

public class GenerateValuesTask extends AbstractTask {

	private final DiscreteMappingGenerator<?> generator;

	private final PropertySheetPanel table;
	private final VisualMappingManager vmm;

	public GenerateValuesTask(final DiscreteMappingGenerator<?> generator, final PropertySheetPanel table, final VisualMappingManager vmm) {
		this.generator = generator;
		this.table = table;
		this.vmm = vmm;
	}

	@Override
	public void run(TaskMonitor monitor) throws Exception {

		int selectedRow = table.getTable().getSelectedRow();

		// If not selected, do nothing.
		if (selectedRow < 0)
			return;

		final Item value = (Item) table.getTable().getValueAt(selectedRow, 0);

		if (value.isProperty()) {
			final VizMapperProperty<?, ?, ?> prop = (VizMapperProperty<?, ?, ?>) value.getProperty();

			if (prop.getCellType() == CellType.VISUAL_PROPERTY_TYPE) {
				final VisualProperty<?> vp = (VisualProperty<?>) prop.getKey();
				final Class<?> vpValueType = vp.getRange().getType();
				final Class<?> generatorType = generator.getDataType();

				// TODO: is this safe?
				if (generatorType.isAssignableFrom(vpValueType) || vpValueType.isAssignableFrom(generatorType))
					generateMapping(prop, prop.getValue().toString(), vp);
			}
		}

		value.toggle();
	}

	private void generateMapping(final VizMapperProperty<?, ?, ?> prop, final String attrName,
			final VisualProperty<?> vp) {

		final Property[] subProps = prop.getSubProperties();
		final VisualStyle style = vmm.getCurrentVisualStyle();
		final VisualMappingFunction<?, ?> mapping = style.getVisualMappingFunction(vp);

		if (mapping == null)
			return;

		final DiscreteMapping<Object, Object> discMapping = (DiscreteMapping) mapping;

		final SortedSet<Object> keySet = new TreeSet<Object>();

		for (Property p : subProps) {
			final VizMapperProperty<?, ?, ?> vmp = (VizMapperProperty<?, ?, ?>) p;
			if (vmp.getCellType().equals(CellType.DISCRETE)) {
				keySet.add(vmp.getKey());
			}
		}

		Map<Object, ?> map = generator.generateMap(keySet);

		discMapping.putAll(map);

		table.removeProperty(prop);
		prop.clearSubProperties();

		for (Property p : subProps) {
			final VizMapperProperty<?, ?, ?> vmp = (VizMapperProperty<?, ?, ?>) p;
			if (vmp.getCellType().equals(CellType.DISCRETE)) {
				vmp.setValue(discMapping.getMapValue(vmp.getKey()));
			}
		}

		prop.addSubProperties(subProps);
		table.addProperty(prop);
		table.repaint();
	}

}
