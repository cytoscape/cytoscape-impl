package org.cytoscape.browser.internal.view.tools;

import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;
import static org.cytoscape.util.swing.LookAndFeelUtil.makeSmall;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;

import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.table.CyTableView;
import org.cytoscape.view.model.table.CyTableViewManager;

/*
 * #%L
 * Cytoscape Table Browser Impl (table-browser-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

@SuppressWarnings("serial")
public abstract class AbstractToolBarControl extends JPanel {
	
	private JLabel titleLabel;
	private JPanel contentPane;
	
	protected String title;
	protected CyTable currentTable;
	
	protected final CyServiceRegistrar serviceRegistrar;
	
	/**
	 * Create a panel without a title.
	 */
	protected AbstractToolBarControl(CyServiceRegistrar serviceRegistrar) {
		this(null, serviceRegistrar);
	}
	
	protected AbstractToolBarControl(String title, CyServiceRegistrar serviceRegistrar) {
		this.title = title;
		this.serviceRegistrar = serviceRegistrar;
		
		setOpaque(!isAquaLAF());
		setLayout(new BorderLayout());
		
		if (!isAquaLAF())
			setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
		
		if (title != null)
			add(getTitleLabel(), BorderLayout.NORTH);
		
		add(getContentPane(), BorderLayout.CENTER);
	}
	
	public void setCurrentTable(CyTable currentTable) {
		this.currentTable = currentTable;
		update();
	}
	
	protected CyTableView getTableView() {
		if (currentTable == null)
			return null;
		
		var tableViewManager = serviceRegistrar.getService(CyTableViewManager.class);
		
		return tableViewManager.getTableView(currentTable);
	}
	
	protected <T> void apply(VisualProperty<T> vp, T value) {
		var view = getTableView();
		
		if (view != null)
			view.setVisualProperty(vp, value);
	}
	
	protected JLabel getTitleLabel() {
		if (titleLabel == null) {
			titleLabel = new JLabel(title + ":");
			titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
			makeSmall(titleLabel);
			setAquaStyle(titleLabel);
		}
		
		return titleLabel;
	}
	
	protected JPanel getContentPane() {
		if (contentPane == null) {
			contentPane = new JPanel();
			contentPane.setOpaque(!isAquaLAF());
		}
		
		return contentPane;
	}
	
	protected abstract void update();
	
	static void setAquaStyle(JToggleButton... buttons) {
		if (isAquaLAF()) {
			int count = 0;
			
			for (var btn : buttons) {
				if (btn instanceof JCheckBox || btn instanceof JRadioButton) {
					setAquaStyle((JComponent) btn);
					continue;
				}
				
				btn.putClientProperty("JComponent.sizeVariant", "mini");
				btn.putClientProperty("JButton.buttonType", "square");
				
//				if (buttons.length == 1) {
//					btn.putClientProperty("JButton.segmentPosition", "only");
//				} else {
//					if (count == 0)
//						btn.putClientProperty("JButton.segmentPosition", "first");
//					else if (count == buttons.length - 1)
//						btn.putClientProperty("JButton.segmentPosition", "last");
//					else
//						btn.putClientProperty("JButton.segmentPosition", "middle");
//				}
				
				count++;
			}
		}
	}

	static void setAquaStyle(JComponent... components) {
		if (isAquaLAF()) {
			for (var c : components) {
				c.putClientProperty("JComponent.sizeVariant", "mini");

				if (c instanceof JCheckBox || c instanceof JRadioButton) {
					// Don't do anything else...
				} else if (c instanceof JButton || c instanceof JToggleButton) {
					c.putClientProperty("JButton.buttonType", "square");
				}
			}
		}
	}
}
