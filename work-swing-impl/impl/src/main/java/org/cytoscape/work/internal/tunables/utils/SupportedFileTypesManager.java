package org.cytoscape.work.internal.tunables.utils;

/*
 * #%L
 * Cytoscape Work Swing Impl (work-swing-impl)
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


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.CyFileFilterProvider;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.io.write.CyWriterFactory;
import org.cytoscape.util.swing.FileChooserFilter;


/**
 * Provides a list of available file types by consulting all registered
 * <code>InputStreamTaskFactory</code> and  <code>CyWriterFactory</code>instances.
 */
public class SupportedFileTypesManager {
	Set<CyFileFilterProvider> inputFactories;
	Set<CyFileFilterProvider> outputFactories;

	public SupportedFileTypesManager() {
		inputFactories = new HashSet<>();
		outputFactories = new HashSet<>();
	}

	public void addInputStreamTaskFactory(InputStreamTaskFactory factory, Map<?,?> properties) {
		inputFactories.add(factory);
	}

	public void removeInputStreamTaskFactory(InputStreamTaskFactory factory, Map<?,?> properties) {
		inputFactories.remove(factory);
	}

	public void addCyWriterTaskFactory(CyWriterFactory factory, Map<?,?> properties) {
		outputFactories.add(factory);
	}

	public void removeCyWriterTaskFactory(CyWriterFactory factory, Map<?,?> properties) {
		outputFactories.remove(factory);
	}

	public List<FileChooserFilter> getSupportedFileTypes(final DataCategory category, boolean input) {
		if (input)
			return getSupportedFileTypes(category, inputFactories);
		else
			return getSupportedFileTypes(category, outputFactories);
	}

	private List<FileChooserFilter> getSupportedFileTypes(final DataCategory category,
							      final Set<CyFileFilterProvider> factories)
	{
		List<FileChooserFilter> types = new ArrayList<>();

		Set<String> allExtensions = new HashSet<>();
		for (final CyFileFilterProvider factory : factories) {
			CyFileFilter filter = factory.getFileFilter();
			// this is a hack to exclude internal session table format
			if (filter.getExtensions().contains("cytable") || filter.getDataCategory() != category)
				continue;

			String description = filter.getDescription();
			Set<String> filterExtensions = filter.getExtensions();
			String[] extensions = new String[filterExtensions.size()];
			int index = 0;
			for (String extension : filterExtensions) {
				allExtensions.add(extension);
				extensions[index] = extension;
				index++;
			}
			types.add(new FileChooserFilter(description, extensions));
		}

		if (types.isEmpty())
			return types;

		Collections.sort(types, new Comparator<FileChooserFilter>() {
			@Override
			public int compare(FileChooserFilter o1, FileChooserFilter o2) {
				return o1.getDescription().compareTo(o2.getDescription());
			}
		});

		String description = String.format("All %1$s files", category.getDisplayName().toLowerCase());
		types.add(new FileChooserFilter(description,
				new ArrayList<>(allExtensions).toArray(new String[allExtensions.size()])));
		return types;
	}
}
