package org.cytoscape.ding.customgraphicsmgr.internal.ui;

import java.util.List;

import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;

public class CyCustomGraphicsTreeNode extends DefaultMutableTreeTableNode {
	
	private List<Object> data;

	CyCustomGraphicsTreeNode(List<Object> data) {
		super(data);
		this.data = data;
	}
	
	 /* 
     * Inherited 
     */ 
    @Override 
    public int getColumnCount() { 
            return data.size(); 
    } 

    /* 
     * Inherited 
     */ 
    @Override 
    public Object getValueAt(int column) { 
            return data.get(column); 
    } 
}
