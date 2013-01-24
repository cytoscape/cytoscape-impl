package org.cytoscape.io.webservice.biomart;

/*
 * #%L
 * Cytoscape Biomart Webservice Impl (webservice-biomart-client-impl)
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


public final class BiomartQuery {
	
	private final String xmlQuery;
	private final String keyColumnName;
	private final String tableName;
	
	public BiomartQuery(final String xmlQuery, final String keyColumnName, final String tableName) {
		this.keyColumnName = keyColumnName;
		this.xmlQuery = xmlQuery;
		this.tableName = tableName;
	}
	
	public String getKeyColumnName () {
		return this.keyColumnName;
	}


	public String getQueryString() {
		return xmlQuery;
	}
	
	public String getTableName() {
		return tableName;
	}

}
