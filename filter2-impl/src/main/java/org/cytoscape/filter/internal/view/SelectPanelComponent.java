package org.cytoscape.filter.internal.view;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JProgressBar;

public interface SelectPanelComponent {
	Component getComponent();
	Component getEditPanel();
	JComponent getApplyButton();
	JComponent getCancelApplyButton();
	JProgressBar getProgressBar();
	void setStatus(String status);
}
