package org.cytoscape.task.internal.export.network;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.command.StringToModel;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.io.write.CyNetworkViewWriterManager;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.internal.export.TunableAbstractCyWriter;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.Tunable;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2019 The Cytoscape Consortium
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

/**
 * A utility Task implementation specifically for writing a {@link org.cytoscape.model.CyNetwork}.
 */
public final class CyNetworkWriter
		extends TunableAbstractCyWriter<CyNetworkViewWriterFactory, CyNetworkViewWriterManager> {

	// the network to be written
	@Tunable(
			description = "The network to be exported",
			longDescription = StringToModel.CY_NETWORK_LONG_DESCRIPTION,
			exampleStringValue = StringToModel.CY_NETWORK_EXAMPLE_STRING,
			context = "nogui"
	)
	public CyNetwork network;

	/**
	 * @param network The {@link org.cytoscape.model.CyNetwork} to be written out. 
	 */
	public CyNetworkWriter(CyNetwork network, boolean useTunable, CyServiceRegistrar serviceRegistrar) {
		super(
				serviceRegistrar.getService(CyNetworkViewWriterManager.class),
				serviceRegistrar.getService(CyApplicationManager.class)
		);
		this.network = network;

		// Pick SIF as a default file format
		for (String fileTypeDesc : this.getFileFilterDescriptions()) {
			if (fileTypeDesc.contains("SIF")) {
				this.options.setSelectedValue(fileTypeDesc);
				break;
			}
		}

		if (useTunable)
			return;

		if (network == null)
			throw new NullPointerException("Network is null.");

		this.outputFile = getSuggestedFile();
	}

	void setDefaultFileFormatUsingFileExt(File file) {
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
	protected CyWriter getWriter(CyFileFilter filter) throws Exception {
		if (network == null) {
			network = cyApplicationManager.getCurrentNetwork();
			if (network == null) {
				return null;
			}
		}
		
		return writerManager.getWriter(network, filter, outputStream);
	}

	@Tunable(
			description = "File to save network to",
			params = "fileCategory=network;input=false",
			required = true,
			dependsOn = "options!=",
			gravity = 1.1
	)
	@Override
	public File getOutputFile() {
		return outputFile;
	}

	@ProvidesTitle
	public String getTitle() {
		return "Export Network";
	}

	@Override
	protected String getExportName() {
		return network.getRow(network).get(CyNetwork.NAME, String.class);
	}
}
