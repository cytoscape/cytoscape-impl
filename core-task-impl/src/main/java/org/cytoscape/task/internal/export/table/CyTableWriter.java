package org.cytoscape.task.internal.export.table;

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
import java.util.ArrayList;
import java.util.List;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyTableWriterManager;
import org.cytoscape.io.write.CyTableWriterFactory;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyTable;
import org.cytoscape.task.internal.export.TunableAbstractCyWriter;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

/**
 * A utility Task implementation specifically for writing {@link org.cytoscape.model.CyTable} objects.
 */
public final class CyTableWriter extends TunableAbstractCyWriter<CyTableWriterFactory,CyTableWriterManager> {

	private final CyTable table;

	/**
	 * @param writerManager The {@link org.cytoscape.io.write.CyTableWriterManager} used to determine which 
	 * {@link org.cytoscape.io.write.CyTableWriterFactory} to use to write the file.
	 * @param table The {@link org.cytoscape.model.CyTable} to be written out. 
 	 */
	public CyTableWriter(CyTableWriterManager writerManager, CyTable table ) {
		super(writerManager);
		if ( table == null )
			throw new NullPointerException("Table is null");
		this.table = table;
		final List<String> availableFormats = new ArrayList<String>();
		for(String format: options.getPossibleValues()){
			if(!format.contains(".cytable")) {
				availableFormats.add(format);
			}
		}
		options = new ListSingleSelection<String>(availableFormats);
	}

	/**
	 * {@inheritDoc}
	 */
	protected CyWriter getWriter(CyFileFilter filter, File file)  throws Exception{
		if (!fileExtensionIsOk(file))
			file = addOrReplaceExtension(outputFile);

		return writerManager.getWriter(table,filter,file);
	}

	@Tunable(description="Save Table As:", params="fileCategory=table;input=false", dependsOn="options!=")
	public File getOutputFile() {
		return outputFile;
	}
	
	@ProvidesTitle
	public String getTitle() {
		return "Export Table";
	}
}
