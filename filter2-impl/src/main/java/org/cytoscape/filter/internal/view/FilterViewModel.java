package org.cytoscape.filter.internal.view;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComponent;

public class FilterViewModel {
	public final JCheckBox checkBox;
	public final Component view;
	
	public FilterViewModel(final JComponent view, final FilterPanelController controller, final FilterPanel parent) {
		this.view = view;
		this.checkBox = new JCheckBox();
		
		checkBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				controller.handleCheck(parent, checkBox, view);
			}
		});
	}
}