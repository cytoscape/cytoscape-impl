package org.cytoscape.filter.internal.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComponent;

public class TransformerElementViewModel<V extends SelectPanelComponent> {
	public final JCheckBox checkBox;
	public final JComponent view;
	
	public TransformerElementViewModel(final JComponent view, final AbstractPanelController<?, V> controller, final V parent) {
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