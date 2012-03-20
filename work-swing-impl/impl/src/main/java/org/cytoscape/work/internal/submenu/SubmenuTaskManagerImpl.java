
package org.cytoscape.work.internal.submenu;

import org.cytoscape.work.AbstractTaskManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.swing.DynamicSubmenuListener;
import org.cytoscape.work.swing.SubmenuTaskManager;

public class SubmenuTaskManagerImpl extends AbstractTaskManager<DynamicSubmenuListener,Object> implements SubmenuTaskManager {
	
	private DialogTaskManager dialogTaskManager;
	private SubmenuTunableMutator stm;

	public SubmenuTaskManagerImpl(SubmenuTunableMutator stm, DialogTaskManager dialogTaskManager) {
		super(stm);
		this.stm = stm;
		this.dialogTaskManager = dialogTaskManager;
	}

	public DynamicSubmenuListener getConfiguration(TaskFactory factory, Object tunableContext) {
		SubmenuListener listener = new SubmenuListener(stm, factory, tunableContext);	
		return listener; 
	}

	public void execute(TaskIterator iterator, Object tunableContext) {
		dialogTaskManager.execute(iterator, tunableContext);
	}
	
	@Override
	public void execute(TaskIterator iterator) {
		dialogTaskManager.execute(iterator);
	}
}
