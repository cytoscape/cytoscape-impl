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

import static org.cytoscape.tableimport.internal.reader.TextTableReader.ObjectType.*;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

/**
 * Text table <---> CyAttribute & CyNetwork mapping parameters for network
 * table.
 *
 * @since Cytoscape 2.4
 * @version 0.9
 *
 * @author Keiichiro Ono
 *
 */
public class NetworkTableMappingParameters extends AbstractMappingParameters {
	private static final String DEF_INTERACTION = "pp";
	private final Integer source;
	private final Integer target;
	private final Integer interaction;
	private final String defInteraction;

	/**
	 * Creates a new NetworkTableMappingParameters object.
	 *
	 * @param delimiters  DOCUMENT ME!
	 * @param listDelimiter  DOCUMENT ME!
	 * @param attributeNames  DOCUMENT ME!
	 * @param attributeTypes  DOCUMENT ME!
	 * @param listAttributeTypes  DOCUMENT ME!
	 * @param importFlag  DOCUMENT ME!
	 * @param source  DOCUMENT ME!
	 * @param target  DOCUMENT ME!
	 * @param interaction  DOCUMENT ME!
	 * @param defInteraction  DOCUMENT ME!
	 *
	 * @throws Exception  DOCUMENT ME!
	 */
	public NetworkTableMappingParameters(List<String> delimiters, String listDelimiter,
	                                     String[] attributeNames, Byte[] attributeTypes,
	                                     Byte[] listAttributeTypes,
	                                     boolean[] importFlag,
	                                     Integer source, Integer target, Integer interaction,
	                                     final String defInteraction, 
	                                     int startNumber, String commentChar) throws Exception {
		
		
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

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public Integer getSourceIndex() {
		return source;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public Integer getTargetIndex() {
		return target;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public Integer getInteractionIndex() {
		return interaction;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public String getDefaultInteraction() {
		if (defInteraction == null) {
			return DEF_INTERACTION;
		} else {
			return defInteraction;
		}
	}


	

}
