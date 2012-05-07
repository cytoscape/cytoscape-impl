package org.cytoscape.view.vizmap.gui.internal.bypass;

import java.awt.Component;

import javax.swing.SwingUtilities;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.gui.SelectedVisualStyleManager;
import org.cytoscape.view.vizmap.gui.editor.ValueEditor;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

/**
 * Task to execute visual style bypass.
 * 
 * @param <T>
 *            Type of Graph object: CyNode or CyEdge.
 */
public class BypassTask<T extends CyIdentifiable> extends AbstractTask {

	// Target view object. Node or Edge.
	private final View<T> view;
	private final VisualProperty<Object> vp;
	private final ValueEditor<Object> editor;

	private final Component parent;
	private final CyNetworkView networkView;

	private final SelectedVisualStyleManager selectedManager;

	@SuppressWarnings("unchecked")
	BypassTask(Component parent, ValueEditor<?> editor, final VisualProperty<?> vp, final View<T> view,
			final CyNetworkView networkView, final SelectedVisualStyleManager selectedManager) {
		this.view = view;
		this.vp = (VisualProperty<Object>) vp;
		this.editor = (ValueEditor<Object>) editor;
		this.parent = parent;

		this.networkView = networkView;
		this.selectedManager = selectedManager;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		final boolean lock = view.isValueLocked(vp);
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				update(lock);
			}
		});

		
	}
	
	private final void update(final boolean lock) {
		if (!lock) {
			final Object newValue = editor.showEditor(parent, view.getVisualProperty(vp));
			view.setLockedValue(vp, newValue);
		} else {
			// Unlock it
			view.clearValueLock(vp);
		}

		final CyRow row = networkView.getModel().getRow(view.getModel());
		selectedManager.getCurrentVisualStyle().apply(row, view);
		networkView.updateView();
	}
}
