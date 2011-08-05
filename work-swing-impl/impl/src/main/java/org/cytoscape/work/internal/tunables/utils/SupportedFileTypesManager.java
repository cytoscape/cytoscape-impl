package org.cytoscape.work.internal.tunables.utils;


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
		inputFactories = new HashSet<CyFileFilterProvider>();
		outputFactories = new HashSet<CyFileFilterProvider>();
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
		List<FileChooserFilter> types = new ArrayList<FileChooserFilter>();

		Set<String> allExtensions = new HashSet<String>();
		for (final CyFileFilterProvider factory : factories) {
			CyFileFilter filter = factory.getCyFileFilter();
			if (filter.getDataCategory() != category)
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

		String description = String.format("All %1$s files", category.toString().toLowerCase());
		types.add(new FileChooserFilter(description,
						new ArrayList<String>(allExtensions).toArray(new String[allExtensions.size()])));
		return types;
	}
}
