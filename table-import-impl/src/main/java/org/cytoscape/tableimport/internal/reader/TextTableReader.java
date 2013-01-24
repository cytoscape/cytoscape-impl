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

import java.io.IOException;

import java.util.List;

import org.cytoscape.model.CyTable;

/**
 * Interface of all text table readers.<br>
 * 
 * @since Cytoscape 2.4
 * @version 1.0
 * @author kono
 * 
 */
public interface TextTableReader {
	
	public enum ObjectType {
		NODE, EDGE, NETWORK;
	}

	public void readTable(CyTable table) throws IOException;

	public List<String> getColumnNames();

	/**
	 * Report the result of import as a string.
	 * 
	 * @return Description of
	 */
	public String getReport();
	
	public MappingParameter getMappingParameter();
}
