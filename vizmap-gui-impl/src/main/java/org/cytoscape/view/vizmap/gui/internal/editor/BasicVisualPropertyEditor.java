package org.cytoscape.view.vizmap.gui.internal.editor;

import java.beans.PropertyEditor;

import org.cytoscape.view.vizmap.gui.editor.AbstractVisualPropertyEditor;
import org.cytoscape.view.vizmap.gui.editor.ContinuousEditorType;

import com.l2fprod.common.propertysheet.PropertyEditorRegistry;
import com.l2fprod.common.propertysheet.PropertyRendererRegistry;

public abstract class BasicVisualPropertyEditor<T> extends
		AbstractVisualPropertyEditor<T> {

	protected static final PropertyRendererRegistry REG = new PropertyRendererRegistry();

	static {
		REG.registerDefaults();
	}

	public BasicVisualPropertyEditor(Class<T> type, PropertyEditor propertyEditor, ContinuousEditorType editorType) {
		super(type, propertyEditor, editorType);
	}
}
