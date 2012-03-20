
package org.cytoscape.network.merge.internal.task;

import org.cytoscape.network.merge.internal.conflict.AttributeConflictCollector;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

/**
 *
 * @author jj
 */
public class HandleConflictsTaskFactory extends AbstractTaskFactory {
    private AttributeConflictCollector conflictCollector;

    /**
     * Constructor.<br>
     *
     */
    public HandleConflictsTaskFactory(final AttributeConflictCollector conflictCollector) {
            this.conflictCollector = conflictCollector;
    }
    
    @Override
    public TaskIterator createTaskIterator() {
        return new TaskIterator(new HandleConflictsTask(conflictCollector));
    }
}
