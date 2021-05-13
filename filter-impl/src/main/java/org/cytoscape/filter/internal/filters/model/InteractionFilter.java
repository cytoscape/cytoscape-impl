package org.cytoscape.filter.internal.filters.model;

/*
 * #%L
 * Cytoscape Filters Impl (filter-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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


import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;


public class InteractionFilter extends CompositeFilter {
	public static int NODE_UNDEFINED = -1;
	public static int NODE_SOURCE = 0;
	public static int NODE_TARGET = 1;
	public static int NODE_SOURCE_TARGET = 3;
	
	protected int nodeType = NODE_UNDEFINED;

	protected boolean isSourceChecked = true;
	protected boolean isTargetChecked = true;
	
	protected CompositeFilter passFilter = new CompositeFilter("None");
	
	public InteractionFilter(CyApplicationManager applicationManager) {
		super(applicationManager);
	}

	public InteractionFilter(String pName, CyApplicationManager applicationManager) {
		super(applicationManager);
		name = pName;
	}

	public void setPassFilter(CompositeFilter pFilter) {
		passFilter = pFilter;
		childChanged = true;
	}

	public CompositeFilter getPassFilter() {
		return passFilter;
	}

	public int getNodeType() {
		return nodeType;
	}

	public void setNodeType(int pNodeType) {
		if (nodeType == pNodeType) {
			return;
		}
		
		nodeType = pNodeType;
		
		if (nodeType == NODE_SOURCE) {
			isSourceChecked = true;
			isTargetChecked = false;
		} else if (nodeType == NODE_TARGET) {
			isSourceChecked = false;
			isTargetChecked = true;
		} else if (nodeType == NODE_SOURCE_TARGET) {
			isSourceChecked = true;
			isTargetChecked = true;
		}
		
		childChanged = true;
	}

	public boolean isSourceChecked() {
		return isSourceChecked;
	}
	
	public boolean isTargetChecked() {
		return isTargetChecked;
	}
	
	public void setSourceChecked(boolean pIsChecked) {
		isSourceChecked =pIsChecked;
		updateNodeType();		
	}

	public void setTargetChecked(boolean pIsChecked) {
		isTargetChecked =pIsChecked;
		updateNodeType();		
	}
	
	@Override
	public void setNetwork(CyNetwork pNetwork) {
		if (network != null && network == pNetwork) {
			return;
		}
		network = pNetwork;
		if (passFilter != null) {
			passFilter.setNetwork(network);			
		}

		childChanged();
	}
	
	@Override
	public String toSerializedForm() {
		String retStr = "<InteractionFilter>\n";
		
		retStr = retStr + "name=" + name + "\n";
		retStr = retStr + advancedSetting.toString() + "\n";
		retStr = retStr + "Negation=" + negation + "\n";
		retStr = retStr + "nodeType=" + nodeType + "\n";

		if (passFilter == null) {
			retStr += "passFilter=null\n";			
		} else {
			retStr += "passFilter=" + passFilter.getName()+"\n";						
		}
		
		retStr += "</InteractionFilter>";

		return retStr;
	}
	
	private void updateNodeType() {
		//update nodeType
		if (isSourceChecked && isTargetChecked) {
			nodeType = NODE_SOURCE_TARGET;
		} else if (isSourceChecked) {
			nodeType = NODE_SOURCE;
		} else if (isTargetChecked) {
			nodeType = NODE_TARGET;
		} else {
			nodeType = NODE_UNDEFINED;
		}
	}
}
