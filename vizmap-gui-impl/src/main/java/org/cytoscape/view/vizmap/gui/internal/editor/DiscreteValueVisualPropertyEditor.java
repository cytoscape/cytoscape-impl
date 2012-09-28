package org.cytoscape.view.vizmap.gui.internal.editor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;

import org.cytoscape.application.events.SetCurrentRenderingEngineEvent;
import org.cytoscape.application.events.SetCurrentRenderingEngineListener;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.DiscreteRange;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.events.RenderingEngineAddedEvent;
import org.cytoscape.view.presentation.events.RenderingEngineAddedListener;
import org.cytoscape.view.vizmap.gui.editor.ContinuousEditorType;
import org.cytoscape.view.vizmap.gui.editor.ContinuousMappingCellRendererFactory;
import org.cytoscape.view.vizmap.gui.internal.cellrenderer.IconCellRenderer;
import org.cytoscape.view.vizmap.gui.internal.editor.propertyeditor.CyDiscreteValuePropertyEditor;

/**
 * Editor object for all kinds of discrete values such as Node Shape, Line
 * Stroke, etc.
 * 
 * 
 * @param <T>
 */
public class DiscreteValueVisualPropertyEditor<T> extends BasicVisualPropertyEditor<T> implements
		SetCurrentRenderingEngineListener {

	private final Set<T> values;
	private final int iconW;
	private final int iconH;

	public DiscreteValueVisualPropertyEditor(final Class<T> type, final CyDiscreteValuePropertyEditor<T> propEditor,
			ContinuousMappingCellRendererFactory cellRendererFactory, final Set<T> values, final int iconW, final int iconH) {
		super(type, propEditor, ContinuousEditorType.DISCRETE, cellRendererFactory);

		this.values = values;
		this.iconH = iconH;
		this.iconW = iconW;

		discreteTableCellRenderer = REG.getRenderer(type);
	}

	@Override
	public void handleEvent(SetCurrentRenderingEngineEvent e) {
		final RenderingEngine<?> engine = e.getRenderingEngine();
		// CCurrent engine is not ready yet.
		if (engine == null)
			return;

		Map<T, Icon> iconMap = new HashMap<T, Icon>();
		for (T value : values)
			iconMap.put(value, engine.createIcon(null, value, iconW, iconH));

		this.discreteTableCellRenderer = new IconCellRenderer<T>(iconMap);
	}
}
