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

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.io.read.VizmapReader;
import org.cytoscape.io.read.VizmapReaderManager;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportDefaultVizmapTask extends AbstractTask {

	private static final Logger logger = LoggerFactory.getLogger(ImportDefaultVizmapTask.class);
	private static final String PRESET_VIZMAP_FILE = "default_vizmap.xml";

	private final VisualMappingManager vmm;
	private final VizmapReaderManager vizmapReaderMgr;
	private final RenderingEngineManager renderingEngineMgr;
	private final File vizmapFile;

	public ImportDefaultVizmapTask(final VizmapReaderManager vizmapReaderMgr,
								   final VisualMappingManager vmm,
								   final CyApplicationConfiguration config,
								   final RenderingEngineManager renderingEngineMgr) {
		this.vizmapReaderMgr = vizmapReaderMgr;
		this.vmm = vmm;
		this.renderingEngineMgr = renderingEngineMgr;
		this.vizmapFile = new File(config.getConfigurationDirectoryLocation(), PRESET_VIZMAP_FILE);
	}

	@Override
	public void run(final TaskMonitor taskMonitor) throws Exception {
		final VizmapReader reader;

		if (vizmapFile.exists() == false) {
			// get the file from resource
			final URL url = this.getClass().getClassLoader().getResource(PRESET_VIZMAP_FILE);
			reader = vizmapReaderMgr.getReader(url.toURI(), url.getPath());
		} else {
			reader = vizmapReaderMgr.getReader(vizmapFile.toURI(), vizmapFile.getName());
		}

		logger.debug("Default vizmap file = " + vizmapFile.getName());

		if (reader == null)
			throw new NullPointerException("Failed to find Default Vizmap loader.");

		insertTasksAfterCurrentTask(reader, new AddVisualStylesTask(reader, vmm, renderingEngineMgr));
	}

	private static final class AddVisualStylesTask extends AbstractTask {

		private final VizmapReader reader;
		private final VisualMappingManager vmMgr;
		private final RenderingEngineManager renderingEngineMgr;

		public AddVisualStylesTask(final VizmapReader reader,
								   final VisualMappingManager vmMgr,
								   final RenderingEngineManager renderingEngineMgr) {
			this.reader = reader;
			this.vmMgr = vmMgr;
			this.renderingEngineMgr = renderingEngineMgr;
		}

		@Override
		public void run(TaskMonitor taskMonitor) throws Exception {
			taskMonitor.setTitle("Loading preset Visual Styles...");
			final Set<VisualStyle> styles = reader.getVisualStyles();

			if (styles != null) {
				final VisualStyle defStyle = vmMgr.getDefaultVisualStyle();
				final String DEFAULT_STYLE_NAME = defStyle.getTitle();
				VisualStyle newDefStyle = null;
				int count = 1;
				int total = styles.size();

				for (final VisualStyle vs : styles) {
					if (cancelled)
						break;
					
					if (vs.getTitle().equals(DEFAULT_STYLE_NAME)) {
						newDefStyle = vs; // Don't add another "default" style!
					} else {
						taskMonitor.setStatusMessage(count + " of " + total + ": " + vs.getTitle());
						vmMgr.addVisualStyle(vs);
						taskMonitor.setProgress(count / total);
						count++;
					}
				}

				if (cancelled) {
					// Remove recently added styles
					for (final VisualStyle vs : styles) {
						if (!vs.getTitle().equals(DEFAULT_STYLE_NAME))
							vmMgr.removeVisualStyle(vs);
					}
				} else {
					// Update the current default style, because it can't be replaced or removed.
					// NOTE: Cannot cancel this task from this point forward!
					if (newDefStyle != null) {
						taskMonitor.setStatusMessage(total + " of " + total + ": " + DEFAULT_STYLE_NAME);
						updateVisualStyle(newDefStyle, defStyle);
					}
					
					vmMgr.setCurrentVisualStyle(defStyle);
				}
				
				taskMonitor.setProgress(1.0);
			}
		}
		
		/**
		 * @param source the Visual Style that will provide the new properties and values.
		 * @param target the Visual Style that will be updated.
		 */
		@SuppressWarnings({ "rawtypes", "unchecked" })
		private void updateVisualStyle(final VisualStyle source, final VisualStyle target) {
			// First clean up the target
			final HashSet<VisualMappingFunction<?, ?>> mapingSet = 
					new HashSet<VisualMappingFunction<?, ?>>(target.getAllVisualMappingFunctions());
			
			for (final VisualMappingFunction<?, ?> mapping : mapingSet)
				target.removeVisualMappingFunction(mapping.getVisualProperty());
			
			final Set<VisualPropertyDependency<?>> depList = 
					new HashSet<VisualPropertyDependency<?>>(target.getAllVisualPropertyDependencies());
			
			for (final VisualPropertyDependency<?> dep : depList)
				target.removeVisualPropertyDependency(dep);
			
			// Copy the default visual properties, mappings and dependencies from source to target
			final VisualLexicon lexicon = renderingEngineMgr.getDefaultVisualLexicon();
			final Set<VisualProperty<?>> properties = lexicon.getAllVisualProperties();
			
			for (final VisualProperty vp : properties) {
				if (!vp.equals(BasicVisualLexicon.NETWORK)
						&& !vp.equals(BasicVisualLexicon.NODE)
						&& !vp.equals(BasicVisualLexicon.EDGE))
					target.setDefaultValue(vp, source.getDefaultValue(vp));
			}
			
			for (final VisualPropertyDependency<?> dep : source.getAllVisualPropertyDependencies())
				target.addVisualPropertyDependency(dep);
			
			for (final VisualMappingFunction<?, ?> mapping : source.getAllVisualMappingFunctions())
				target.addVisualMappingFunction(mapping);
		}
	}
}
