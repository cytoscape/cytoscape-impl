package org.cytoscape.task.internal.export.vizmap;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
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
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.io.write.VizmapWriterFactory;
import org.cytoscape.io.write.VizmapWriterManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.internal.export.TunableAbstractCyWriter;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListMultipleSelection;

public class VizmapWriter extends TunableAbstractCyWriter<VizmapWriterFactory, VizmapWriterManager> {

	@ProvidesTitle
	public String getTitle() {
		return "Export Styles";
	}
	
	@Tunable(description = "Select Styles:")
	public ListMultipleSelection<VisualStyle> styles;
	
	@Tunable(description="Save Styles as:", params="fileCategory=vizmap;input=false")
	@Override
	public File getOutputFile() {
		return outputFile;
	}
	
	public VizmapWriter(final VizmapWriterManager writerManager, final CyServiceRegistrar serviceRegistrar) {
		super(writerManager);
		
		// Initialize Visual Style selector
		final VisualMappingManager vmMgr = serviceRegistrar.getService(VisualMappingManager.class);
		final List<VisualStyle> allStyles = new ArrayList<>(vmMgr.getAllVisualStyles());
		
		final Collator collator = Collator.getInstance(Locale.getDefault());
		
		Collections.sort(allStyles, (vs1, vs2) -> collator.compare(vs1.getTitle(), vs2.getTitle()));
		
		styles = new ListMultipleSelection<>(allStyles);
		// Select the current style by default
		styles.setSelectedValues(Collections.singletonList(vmMgr.getCurrentVisualStyle()));
	}
	
	void setDefaultFileFormatUsingFileExt(final File file) {
		String ext = FilenameUtils.getExtension(file.getName());
		ext = ext.toLowerCase().trim();
		String searchDesc = "*." + ext;
		
		// Use the EXT to determine the default file format
		for (String fileTypeDesc : this.getFileFilterDescriptions())
			if (fileTypeDesc.contains(searchDesc)) {
				options.setSelectedValue(fileTypeDesc);
				break;
			}
	}

	@Override
	protected CyWriter getWriter(final CyFileFilter filter, File file) throws Exception {
		if (!fileExtensionIsOk(file))
			file = addOrReplaceExtension(outputFile);

		final Set<VisualStyle> selectedStyles = new LinkedHashSet<>(styles.getSelectedValues());

		return writerManager.getWriter(selectedStyles, filter, file);
	}
	
	@Override
	public ValidationState getValidationState(final Appendable msg) {
		if (styles.getSelectedValues().isEmpty()) {
			try {
				msg.append("Select at least one Style.");
			} catch (final Exception e) {
				/* Intentionally empty. */
			}
			
			return ValidationState.INVALID;
		}
		
		return super.getValidationState(msg);
	}
}
