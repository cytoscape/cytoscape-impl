package org.cytoscape.view.vizmap.gui.internal.task.generators;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.view.vizmap.gui.SelectedVisualStyleManager;
import org.cytoscape.view.vizmap.gui.util.DiscreteMappingGenerator;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import com.l2fprod.common.propertysheet.PropertySheetPanel;

public class GenerateValuesTaskFactory extends AbstractTaskFactory {

	private final DiscreteMappingGenerator<?> generator;
	private final PropertySheetPanel table;
	private final SelectedVisualStyleManager manager;
	private final CyApplicationManager appManager;

	public GenerateValuesTaskFactory(
			final DiscreteMappingGenerator<?> generator,
			final PropertySheetPanel table,
			final SelectedVisualStyleManager manager,
			final CyApplicationManager appManager) {
		this.generator = generator;
		this.appManager = appManager;
		this.manager = manager;
		this.table = table;
	}

	@Override
	public TaskIterator createTaskIterator() {

		return new TaskIterator(new GenerateValuesTask(generator, table, manager, appManager));
	}
}
