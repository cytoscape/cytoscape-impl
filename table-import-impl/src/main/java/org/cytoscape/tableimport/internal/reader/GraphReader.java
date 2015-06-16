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

import org.cytoscape.model.CyNetwork;



/**
* Interface for Reading in Cytoscape Graphs.
*
* @author Cytoscape Development Group.
*/
public interface GraphReader {
	
	/**
	 * Reads/imports the graph.
	 *
	 * @throws IOException IO Errors.
	 */
	public void read() throws IOException;

	/**
	 * Gets the name of the network.
	 * @return network name.
	 */
	public String getNetworkName();
	
	public void setNetwork(CyNetwork network);
}
