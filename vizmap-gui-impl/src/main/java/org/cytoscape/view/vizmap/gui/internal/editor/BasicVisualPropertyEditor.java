package org.cytoscape.view.vizmap.gui.internal.editor;

import java.beans.PropertyEditor;

import org.cytoscape.view.vizmap.gui.editor.AbstractVisualPropertyEditor;

import com.l2fprod.common.propertysheet.PropertyRendererRegistry;

public class BasicVisualPropertyEditor<T> extends
		AbstractVisualPropertyEditor<T> {

	protected static final PropertyRendererRegistry REG = new PropertyRendererRegistry();

	static {
		REG.registerDefaults();
	}

	public BasicVisualPropertyEditor(Class<T> type, PropertyEditor propertyEditor) {
		super(type, propertyEditor);
	}

}
