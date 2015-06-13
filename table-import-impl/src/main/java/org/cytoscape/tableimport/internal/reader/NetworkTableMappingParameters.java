package org.cytoscape.tableimport.internal.reader;

/*
 * #%L
 * Cytoscape Table Import Impl (table-import-impl)
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

import java.io.InputStream;
import java.util.List;

/**
 * Text table <---> CyAttribute & CyNetwork mapping parameters for network table.
 */
public class NetworkTableMappingParameters extends AbstractMappingParameters {
	
	private static final String DEF_INTERACTION = "pp";
	
	private final Integer source;
	private final Integer target;
	private final Integer interaction;
	private final String defInteraction;

	/**
	 * Creates a new NetworkTableMappingParameters object.
	 */
	public NetworkTableMappingParameters(
			List<String> delimiters,
			String listDelimiter,
			String[] attributeNames,
			Byte[] attributeTypes,
			Byte[] listAttributeTypes,
			boolean[] importFlag,
			Integer source,
			Integer target,
			Integer interaction,
			final String defInteraction,
			int startNumber,
			String commentChar
	) throws Exception {
		super(delimiters, listDelimiter,attributeNames , attributeTypes, listAttributeTypes, importFlag, startNumber, commentChar);

		this.source = source;
		this.target = target;
		this.interaction = interaction;
		this.defInteraction = defInteraction;
	}

	public NetworkTableMappingParameters(InputStream is, String fileType) throws Exception {
		super(is, fileType);
		this.source = -1;
		this.target = -1;
		this.interaction = -1;
		this.defInteraction = "";
	}

	public Integer getSourceIndex() {
		return source;
	}

	public Integer getTargetIndex() {
		return target;
	}

	public Integer getInteractionIndex() {
		return interaction;
	}

	public String getDefaultInteraction() {
		return defInteraction == null ? DEF_INTERACTION : defInteraction;
	}
}
