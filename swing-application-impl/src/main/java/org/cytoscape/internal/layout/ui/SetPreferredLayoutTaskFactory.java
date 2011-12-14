package org.cytoscape.internal.layout.ui;


import org.cytoscape.event.CyEventHelper;
import org.cytoscape.property.CyProperty;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.undo.UndoSupport;
import org.cytoscape.view.layout.CyLayoutAlgorithm;

import java.util.Properties; 


public class SetPreferredLayoutTaskFactory implements TaskFactory {

	private final Properties props;
	private final CyLayoutAlgorithm o;
	
	public SetPreferredLayoutTaskFactory(final CyLayoutAlgorithm o, final CyProperty<Properties> p)
	{
		this.o = o;
		this.props       = p.getProperties();		
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(new SetPreferredLayoutTask(o, props));
	}
}
