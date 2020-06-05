package org.cytoscape.view.vizmap.gui.internal.view;

import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import org.cytoscape.model.CyIdentifiable;

public interface VisualPropertySheetContainer {

	Set<VisualPropertySheet> getVisualPropertySheets();
	
	VisualPropertySheet getVisualPropertySheet(Class<? extends CyIdentifiable> targetDataType);
	
	void setSelectedVisualPropertySheet(VisualPropertySheet vpSheet);
	
	void addVisualPropertySheet(VisualPropertySheet vpSheet);
	
	VisualPropertySheet getSelectedVisualPropertySheet();
	
	JComponent getComponent();

	JPopupMenu getContextMenu();

	JMenu getMapValueGeneratorsSubMenu();

	
}
