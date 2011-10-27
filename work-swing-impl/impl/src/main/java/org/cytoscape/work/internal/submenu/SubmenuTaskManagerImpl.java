
package org.cytoscape.work.internal.submenu;

import org.cytoscape.work.TunableMutator;
import org.cytoscape.work.AbstractTaskManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.swing.SubmenuTaskManager;
import org.cytoscape.work.swing.DynamicSubmenuListener;

import javax.swing.event.MenuListener;
import java.util.List;

public class SubmenuTaskManagerImpl extends AbstractTaskManager<DynamicSubmenuListener,Object> implements SubmenuTaskManager {
	
	private DialogTaskManager dialogTaskManager;
	private SubmenuTunableMutator stm;

	public SubmenuTaskManagerImpl(SubmenuTunableMutator stm, DialogTaskManager dialogTaskManager) {
		super(stm);
		this.stm = stm;
		this.dialogTaskManager = dialogTaskManager;
	}

	public DynamicSubmenuListener getConfiguration(TaskFactory tf) {
		SubmenuListener listener = new SubmenuListener(stm,tf);	
		return listener; 
	}

	public void execute(TaskFactory tf) {
		dialogTaskManager.execute(tf,false);
	}
}
