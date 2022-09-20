package org.cytoscape.view.table.internal.equation;

import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import java.awt.Dimension;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.cytoscape.model.CyColumn;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.table.internal.impl.BrowserTable;

/*
 * #%L
 * Cytoscape Table Presentation Impl (table-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2021 The Cytoscape Consortium
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
public class EquationEditorPanel extends JPanel {
	
	private final CyServiceRegistrar registrar;
	private final BrowserTable browserTable;
	
	private SyntaxAreaPanel syntaxPanel;
	private ItemTreePanel<FunctionInfo> functionPanel;
	private ItemListPanel<CyColumn> attributePanel;
	private ItemListPanel<String> tutorialPanel;
	private InfoPanel infoPanel;
	
	// Maybe these should be part of the dialog?
	private JPanel buttonPanel;
	private JButton closeButton;
	
	
	public EquationEditorPanel(CyServiceRegistrar registrar, BrowserTable browserTable) {
		this.registrar = registrar;
		this.browserTable = browserTable;
		init();
	}
	
	private void init() {
		setOpaque(!isAquaLAF());
		final GroupLayout layout = new GroupLayout(this);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		setLayout(layout);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addComponent(getSyntaxPanel(), 0, 120, 120)
			.addGap(20)
			.addGroup(layout.createParallelGroup()
				.addComponent(getFunctionPanel(),  0, 80, Short.MAX_VALUE)
				.addComponent(getAttributePanel(), 0, 80, Short.MAX_VALUE)
				.addComponent(getTutorialPanel(),  0, 80, Short.MAX_VALUE)
			)
			.addComponent(getInfoPanel())
			.addGap(20)
			.addComponent(getButtonPanel())
		);
		
		layout.setHorizontalGroup(layout.createParallelGroup()
			.addComponent(getSyntaxPanel())
			.addGroup(layout.createSequentialGroup()
				.addComponent(getFunctionPanel(),  0, 250, Short.MAX_VALUE)
				.addComponent(getAttributePanel(), 0, 250, Short.MAX_VALUE)
				.addComponent(getTutorialPanel(),  0, 250, Short.MAX_VALUE)
			)
			.addComponent(getInfoPanel())
			.addComponent(getButtonPanel())
		);
	}

	
	public SyntaxAreaPanel getSyntaxPanel() {
		if(syntaxPanel == null) {
			syntaxPanel = new SyntaxAreaPanel(registrar, browserTable);
			Dimension p = syntaxPanel.getPreferredSize();
			syntaxPanel.setPreferredSize(new Dimension(p.width, 50));
		}
		return syntaxPanel;
	}
	
	public ItemListPanel<String> getTutorialPanel() {
		if(tutorialPanel == null) {
			tutorialPanel = new ItemListPanel<>("Syntax");
		}
		return tutorialPanel;
	}
	
	public ItemTreePanel<FunctionInfo> getFunctionPanel() {
		if(functionPanel == null) {
			functionPanel = new ItemTreePanel<>("Functions");
		}
		return functionPanel;
	}
	
	public ItemListPanel<CyColumn> getAttributePanel() {
		if(attributePanel == null) {
			attributePanel = new ItemListPanel<>("Attributes");
		}
		return attributePanel;
	}
	
	public InfoPanel getInfoPanel() {
		if(infoPanel == null) {
			infoPanel = new InfoPanel();
		}
		return infoPanel;
	}
	
	private JPanel getButtonPanel() {
		if(buttonPanel == null) {
			String help = "http://manual.cytoscape.org/en/stable/Column_Data_Functions_and_Equations.html";
			buttonPanel = LookAndFeelUtil.createOkCancelPanel(null, getCloseButton(), help);
		}
		return buttonPanel;
	}
	
	public JButton getCloseButton() {
		if(closeButton == null) {
			closeButton = new JButton("Close");
		}
		return closeButton;
	}
	
}

