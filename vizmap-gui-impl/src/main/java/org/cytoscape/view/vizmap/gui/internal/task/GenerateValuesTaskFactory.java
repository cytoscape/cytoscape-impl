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

import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.view.vizmap.gui.internal.util.mapgenerator.NumberSeriesMappingGenerator;
import org.cytoscape.view.vizmap.gui.internal.util.mapgenerator.RandomNumberMappingGenerator;
import org.cytoscape.view.vizmap.gui.internal.view.VisualPropertySheet;
import org.cytoscape.view.vizmap.gui.internal.view.VisualPropertySheetItem;
import org.cytoscape.view.vizmap.gui.internal.view.VizMapperMainPanel;
import org.cytoscape.view.vizmap.gui.util.DiscreteMappingGenerator;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class GenerateValuesTaskFactory extends AbstractTaskFactory {

	private final DiscreteMappingGenerator<?> generator;
	private final VizMapperMainPanel vizMapperPanel;
	private final ServicesUtil servicesUtil;

	public GenerateValuesTaskFactory(final DiscreteMappingGenerator<?> generator,
									 final VizMapperMainPanel vizMapperPanel,
									 final ServicesUtil servicesUtil) {
		this.generator = generator;
		this.vizMapperPanel = vizMapperPanel;
		this.servicesUtil = servicesUtil;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new GenerateValuesTask(generator, vizMapperPanel, servicesUtil));
	}
	
	@Override
	public boolean isReady() {
		final VisualPropertySheet vpSheet = vizMapperPanel.getSelectedVisualPropertySheet();
		
		if (vpSheet != null) {
			for (final VisualPropertySheetItem<?> item : vpSheet.getSelectedItems()) {
				final VisualMappingFunction<?, ?> mapping = item.getModel().getVisualMappingFunction();
				
				if (mapping != null && mapping instanceof DiscreteMapping) {
					final VisualProperty<?> vp = item.getModel().getVisualProperty();
					final Class<?> vpValueType = vp.getRange().getType();
					final Class<?> generatorType = generator.getDataType();
					
					if ( vpValueType.isAssignableFrom(generatorType)
							|| ((generator instanceof NumberSeriesMappingGenerator 
									|| generator instanceof RandomNumberMappingGenerator)
									&& Number.class.isAssignableFrom(vpValueType)) )
						return true;
				}
			}
		}
		
		return false;
	}
}
