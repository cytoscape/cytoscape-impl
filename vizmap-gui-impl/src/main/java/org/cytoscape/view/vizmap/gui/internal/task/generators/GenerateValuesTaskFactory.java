package org.cytoscape.view.vizmap.gui.internal.task.generators;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.gui.util.DiscreteMappingGenerator;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import com.l2fprod.common.propertysheet.PropertySheetPanel;

public class GenerateValuesTaskFactory extends AbstractTaskFactory {

	private final DiscreteMappingGenerator<?> generator;
	private final PropertySheetPanel table;
	private final VisualMappingManager vmm;
	private final CyApplicationManager appManager;

	public GenerateValuesTaskFactory(final DiscreteMappingGenerator<?> generator, final PropertySheetPanel table,
			final VisualMappingManager vmm, final CyApplicationManager appManager) {
		this.generator = generator;
		this.appManager = appManager;
		this.table = table;
		this.vmm = vmm;
	}

	@Override
	public TaskIterator createTaskIterator() {

		return new TaskIterator(new GenerateValuesTask(generator, table, appManager, vmm));
	}
}
