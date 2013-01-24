package org.cytoscape.network.merge.internal.task;

/*
 * #%L
 * Cytoscape Merge Impl (network-merge-impl)
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

import org.cytoscape.network.merge.internal.conflict.*;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jj
 */
public class HandleConflictsTask extends AbstractTask {
	private AttributeConflictCollector conflictCollector;

	/**
	 * Constructor.<br>
	 *
	 */
	public HandleConflictsTask(final AttributeConflictCollector conflictCollector) {
		this.conflictCollector = conflictCollector;
	}

	/**
	 * Executes Task
	 *
	 * @throws
	 * @throws Exception
	 */
	//@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setStatusMessage("Handle conflicts.\n\nIt may take a while.\nPlease wait...");
		taskMonitor.setProgress(0.0d);

		int nBefore = conflictCollector.getMapToGOAttr().size();

		List<AttributeConflictHandler> conflictHandlers = new ArrayList<AttributeConflictHandler>();

		AttributeConflictHandler conflictHandler;

		//             if (idMapping!=null) {
		//                conflictHandler = new IDMappingAttributeConflictHandler(idMapping);
		//                conflictHandlers.add(conflictHandler);
		//             }

		conflictHandler = new DefaultAttributeConflictHandler();
		conflictHandlers.add(conflictHandler);

		AttributeConflictManager conflictManager = new AttributeConflictManager(conflictCollector,conflictHandlers);
		conflictManager.handleConflicts();

		int nAfter = conflictCollector.getMapToGOAttr().size();

		taskMonitor.setProgress(1.0d);
		taskMonitor.setStatusMessage("Successfully handled " + (nBefore-nAfter) + " table column conflicts. "
					      + nAfter+" conflicts remains.");
	}
}
