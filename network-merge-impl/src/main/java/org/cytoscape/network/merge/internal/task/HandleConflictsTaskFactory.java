
package org.cytoscape.network.merge.internal.task;

import org.cytoscape.network.merge.internal.conflict.AttributeConflictCollector;

import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskFactory;

/**
 *
 * @author jj
 */
public class HandleConflictsTaskFactory implements TaskFactory {
    private AttributeConflictCollector conflictCollector;

    /**
     * Constructor.<br>
     *
     */
    public HandleConflictsTaskFactory(final AttributeConflictCollector conflictCollector) {
            this.conflictCollector = conflictCollector;
    }
    
    @Override
    public TaskIterator getTaskIterator() {
        return new TaskIterator(new HandleConflictsTask(conflictCollector));
    }
}
