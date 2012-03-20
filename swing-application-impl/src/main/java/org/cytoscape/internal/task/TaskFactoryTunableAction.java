/*
 File: TaskFactoryTunableAction.java

 Copyright (c) 2006, 2010, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.internal.task;

import java.awt.event.ActionEvent;
import java.util.Map;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.swing.DialogTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskFactoryTunableAction extends AbstractCyAction {

    private static final long serialVersionUID = 8009915597814265396L;

    private final static Logger logger = LoggerFactory.getLogger(TaskFactoryTunableAction.class);

    final protected TaskFactory factory;
    final protected DialogTaskManager manager;
	final protected CyApplicationManager applicationManager;

    public TaskFactoryTunableAction(final DialogTaskManager manager, final TaskFactory factory, final Map<String, String> serviceProps,
	    final CyApplicationManager applicationManager) {
	super(serviceProps, applicationManager, factory);
	this.manager = manager;
	this.applicationManager = applicationManager;
	this.factory = factory;
    }

    public void actionPerformed(ActionEvent a) {
	logger.debug("About to execute task from factory: " + factory.toString());
	
	// execute the task(s) in a separate thread
	manager.execute(factory.createTaskIterator());
    }
}
