package org.cytoscape.ding.impl;


import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.di.util.DIUtil;
import org.cytoscape.property.CyProperty;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;


public class ShowGraphicsDetailTaskFactory implements TaskFactory {
    private final CyApplicationManager appManager;
    private final CyProperty<Properties> defaultProps;
    
    ShowGraphicsDetailTaskFactory(final CyApplicationManager appManager, final CyProperty<Properties> defaultProps) {
	    this.appManager   = DIUtil.stripProxy(appManager);
	    this.defaultProps = DIUtil.stripProxy(defaultProps);
    }
    
    @Override
    public TaskIterator getTaskIterator() {
	return new TaskIterator(new ShowGraphicsDetailTask(defaultProps, appManager));
    }

}
