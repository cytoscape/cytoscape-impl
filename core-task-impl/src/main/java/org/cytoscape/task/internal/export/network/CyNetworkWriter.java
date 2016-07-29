package org.cytoscape.task.internal.export.network;

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


import org.apache.commons.io.FilenameUtils;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.io.write.CyNetworkViewWriterManager;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.internal.export.TunableAbstractCyWriter;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.Tunable;

import java.io.File;


/**
 * A utility Task implementation specifically for writing a {@link org.cytoscape.model.CyNetwork}.
 */
public final class CyNetworkWriter extends TunableAbstractCyWriter<CyNetworkViewWriterFactory,CyNetworkViewWriterManager> {
	// the network to be written
	private final CyNetwork network;

	/**
	 * @param writerManager The {@link org.cytoscape.io.write.CyNetworkViewWriterManager} used to determine which 
	 * {@link org.cytoscape.io.write.CyNetworkViewWriterFactory} to use to write the file.
	 * @param network The {@link org.cytoscape.model.CyNetwork} to be written out. 
	 */
	public CyNetworkWriter(final CyNetworkViewWriterManager writerManager, final CyNetwork network ) {
		super(writerManager);
		
		if (network == null)
			throw new NullPointerException("Network is null.");
		
		this.network = network;
		// Pick SIF as a default file format
		for(String fileTypeDesc: this.getFileFilterDescriptions()) {
			if(fileTypeDesc.contains("SIF")) {
				this.options.setSelectedValue(fileTypeDesc);
				break;
			}
		}
	}

	void setDefaultFileFormatUsingFileExt(File file) {
		String ext = FilenameUtils.getExtension(file.getName());
		ext = ext.toLowerCase().trim();
		String searchDesc = "*." + ext;
		//Use the EXT to determine the default file format
		for(String fileTypeDesc: this.getFileFilterDescriptions() )
			if(fileTypeDesc.contains(searchDesc) )
			{
				options.setSelectedValue(fileTypeDesc);
				break;
			}
	}


	/**
	 * {@inheritDoc}  
	 */
	@Override
	protected CyWriter getWriter(CyFileFilter filter)  throws Exception{
		return writerManager.getWriter(network,filter,outputStream);
	}
	
	@Tunable(description="Save Network as:", params="fileCategory=network;input=false", dependsOn="options!=", gravity = 1.1)
	public  File getOutputFile() {	
		return outputFile;
	}
	
	@ProvidesTitle
	public String getTitle() {
		return "Export Network";
	}
	

	
	
}
