
/*
 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/

package org.cytoscape.filter.internal.filters;

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
		}
		else if (nodeType == NODE_TARGET) {
			isSourceChecked = false;
			isTargetChecked = true;			
		}
		else if (nodeType == NODE_SOURCE_TARGET) {
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
	
	private void updateNodeType() {
		//update nodeType
		if (isSourceChecked && isTargetChecked) {
			nodeType = NODE_SOURCE_TARGET;
		}
		else if (isSourceChecked) {
			nodeType = NODE_SOURCE;
		}
		else if (isTargetChecked) {
			nodeType = NODE_TARGET;
		}
		else {
			nodeType = NODE_UNDEFINED;
		}
	}
	
				
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
	
	
	public String toString() {
		String retStr = "<InteractionFilter>\n";
		
		retStr = retStr + "name=" + name + "\n";
		retStr = retStr + advancedSetting.toString() + "\n";
		retStr = retStr + "Negation=" + negation + "\n";
		retStr = retStr + "nodeType=" + nodeType + "\n";

		if (passFilter == null) {
			retStr += "passFilter=null\n";			
		}
		else {
			retStr += "passFilter=" + passFilter.getName()+"\n";						
		}
		
		retStr += "</InteractionFilter>";

		return retStr;
	}
}
