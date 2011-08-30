
package org.cytoscape.network.merge.internal.task;

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
		taskMonitor.setProgress(0.0);

		try {
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

			taskMonitor.setProgress(1.0);
			taskMonitor.setStatusMessage("Successfully handled " + (nBefore-nAfter) + " attribute conflicts. "
					      + nAfter+" conflicts remains.");
		} catch(Exception e) {
			throw new Exception(e);
		}

	}
}
