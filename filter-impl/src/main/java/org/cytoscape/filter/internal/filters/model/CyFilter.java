package org.cytoscape.filter.internal.filters.model;

/*
 * #%L
 * Cytoscape Filters Impl (filter-impl)
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


import java.util.BitSet;

import org.cytoscape.model.CyNetwork;

public interface CyFilter /*extends Filter*/{
	public BitSet getNodeBits();
	public BitSet getEdgeBits();

	public void setNegation(boolean pNot);
	public boolean getNegation();
	public String getName();
	public void setName(String pName);

	public void setNetwork(CyNetwork pNetwork);
	public CyNetwork getNetwork();
	
	public void childChanged();
	public CyFilter getParent();	
	public void setParent(CyFilter f);
	
	public void apply();
	public String toString();
}
