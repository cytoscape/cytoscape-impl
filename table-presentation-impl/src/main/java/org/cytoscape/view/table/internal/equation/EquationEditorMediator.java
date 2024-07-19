package org.cytoscape.view.table.internal.equation;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.WindowConstants;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

import org.cytoscape.application.swing.CyColumnPresentationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.equations.Equation;
import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.equations.EquationParser;
import org.cytoscape.equations.Function;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.view.table.internal.CyActivator;
import org.cytoscape.view.table.internal.impl.BrowserTable;
import org.cytoscape.view.table.internal.impl.BrowserTableModel;
import org.cytoscape.view.table.internal.util.TableBrowserUtil;

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

/**
 * This mediator is not a singleton, there is one instance per BrowserTable.
 */
public class EquationEditorMediator {
	
	public enum ApplyScope {
		CURRENT_CELL("Current cell only"),
		CURRENT_SELECTION("Current selection"),
		ENTIRE_COLUMN("Entire column");

		private final String text;
		ApplyScope(String text) { this.text = text; }
		@Override public String toString() { return text; }
		
		public static ApplyScope[] values(boolean includeSelected) {
			if(includeSelected)
				return values();
			return new ApplyScope[] { ApplyScope.CURRENT_CELL, ApplyScope.ENTIRE_COLUMN };
		}
	}
	
	
	private final CyServiceRegistrar registrar;
	private final BrowserTable browserTable;
	private final EquationEditorPanel builderPanel;
	
	// The selected column/row can change as the BrowserTable reacts to events, so we need to store these values up front.
	private final CyColumn column;
	private final int currentRowIndex;
	
	private boolean successfulApply = false;
	
	
	private EquationEditorMediator(CyColumn column, BrowserTable browserTable, EquationEditorPanel builderPanel, CyServiceRegistrar registrar) {
		this.browserTable = browserTable;
		this.registrar = registrar;
		this.builderPanel = builderPanel;
		this.column = column;
		this.currentRowIndex = browserTable.convertRowIndexToModel(browserTable.getSelectedRow());
	}
	
	public static String openEquationEditorDialog(BrowserTable browserTable, String equation, CyServiceRegistrar registrar) {
		JFrame parent = registrar.getService(CySwingApplication.class).getJFrame();
		JDialog dialog = new JDialog(parent);
		
		BrowserTableModel tableModel = browserTable.getBrowserTableModel();
		int cellCol = browserTable.convertColumnIndexToModel(browserTable.getSelectedColumn());
		CyColumn column =  tableModel.getCyColumn(cellCol);
		
		var builderPanel = new EquationEditorPanel(registrar, browserTable);
		
		EquationEditorMediator mediator = new EquationEditorMediator(column, browserTable, builderPanel, registrar);
		mediator.wireTogether(dialog);
		
		String attribName = column.getName();
		
		dialog.setTitle("Formula Builder for " + attribName);
		dialog.setModal(true);
		dialog.getContentPane().setLayout(new BorderLayout());
		dialog.getContentPane().add(builderPanel, BorderLayout.CENTER);
		dialog.setPreferredSize(new Dimension(600, 600)); 
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.pack();
		dialog.setLocationRelativeTo(parent);
		
		builderPanel.getSyntaxPanel().getSyntaxTextArea().addAncestorListener(new RequestFocusListener());
		builderPanel.getSyntaxPanel().setText(equation);
		
		dialog.setVisible(true);
		
		// If the equation was successfully applied then we don't need to cache it 
		// in the EquationEditorDialogFactory because it was written to the table cell(s).
		return mediator.successfulApply ? null : builderPanel.getSyntaxPanel().getText();
	}
	
	private static class RequestFocusListener implements AncestorListener {
		@Override
		public void ancestorAdded(AncestorEvent e) {
			var c = e.getComponent();
			c.requestFocusInWindow();
			c.removeAncestorListener(this);
		}
		@Override
		public void ancestorRemoved(AncestorEvent event) { }
		@Override
		public void ancestorMoved(AncestorEvent event) { }
	}
	
	
	private void wireTogether(JDialog dialog) {
		initializeTutorialList();
		initializeFunctionList();
		initializeAttributeList();
		
		builderPanel.getCloseButton().addActionListener(e -> dialog.dispose());
		
		SyntaxAreaPanel syntaxPanel = builderPanel.getSyntaxPanel();
		syntaxPanel.getApplyButton().addActionListener(e -> handleApply(true));
		syntaxPanel.getEvalButton().addActionListener(e -> handleApply(false));
		
		builderPanel.getInfoPanel().getTextArea().addHyperlinkListener(e -> {
			if(e.getEventType() == EventType.ACTIVATED) {
				handleInsert();	
			}
		});
	}
	
	
	private void initializeTutorialList() {
		builderPanel.getTutorialPanel().addElements(TutorialItems.getTutorialItems());
		
		JList<String> list = builderPanel.getTutorialPanel().getList();
		list.addListSelectionListener(e -> {
			String item = list.getSelectedValue();
			if(item != null) {
				builderPanel.getAttributePanel().clearSelection();
				builderPanel.getFunctionPanel().clearSelection();
				String docs = TutorialItems.getTutorialDocs(item);
				builderPanel.getInfoPanel().setText(docs);
			}
		});
	}
	
	
	private void initializeFunctionList() {
		initializeFunctionListContents();
		initializeFunctionListComponents();
	}
	
	
	private SortedMap<String,SortedMap<String,Function>> getFunctionsByCategory() {
		EquationParser equationParser = registrar.getService(EquationParser.class);
		SortedMap<String,SortedMap<String,Function>> categories = new TreeMap<>();
		
		for(Function f : equationParser.getRegisteredFunctions()) {
			for(String category : getCategoriesFor(f)) {
				var functions = categories.computeIfAbsent(category, k -> new TreeMap<>());
				functions.put(f.getName(), f);
			}
		}
		return categories;
	}
	
	private static String[] getCategoriesFor(Function f) {
		String categoryList = f.getCategoryName();
		if(categoryList == null || categoryList.isBlank()) {
			return new String[] { "Other" };
		}
		String[] split = categoryList.split(",");
		if(split == null) {
			return new String[] { "Other" };
		}
		return split;
	}
	
	
	private void initializeFunctionListContents() {
		SortedMap<String,SortedMap<String,Function>> categories = getFunctionsByCategory();
		
		var top = new DefaultMutableTreeNode("Functions");
		
		List<String> topCategories = Arrays.asList("Numeric", "Text", "List", "Logic", "Network");
		
		// First put the top categories at the top
		for(String categoryName : topCategories) {
			var functions = categories.remove(categoryName);
			if(functions != null) {
				var categoryNode = new DefaultMutableTreeNode(FunctionInfo.category(categoryName));
				top.add(categoryNode);
				
				for(var functionEntry : functions.entrySet()) {
					Function f = functionEntry.getValue();
					categoryNode.add(new DefaultMutableTreeNode(FunctionInfo.function(f)));
				}
			}
		}
		
		// Now add the rest
		for(var categoryEntry : categories.entrySet()) {
			String categoryName = categoryEntry.getKey();
			var functions = categoryEntry.getValue();
			
			var categoryNode = new DefaultMutableTreeNode(FunctionInfo.category(categoryName));
			top.add(categoryNode);
			
			for(var functionEntry : functions.entrySet()) {
				Function f = functionEntry.getValue();
				categoryNode.add(new DefaultMutableTreeNode(FunctionInfo.function(f)));
			}
		}
		
		ItemTreePanel<FunctionInfo> functionPanel = builderPanel.getFunctionPanel();
		DefaultTreeModel model = new DefaultTreeModel(top);
		model.setAsksAllowsChildren(false);
		functionPanel.getTree().setModel(model);
	}
	
	
	@SuppressWarnings("serial")
	private void initializeFunctionListComponents() {
		ItemTreePanel<FunctionInfo> functionPanel = builderPanel.getFunctionPanel();
		JTree tree = functionPanel.getTree();
		
		var iconManager = registrar.getService(IconManager.class);
		var funcIcon = iconManager.getIcon(CyActivator.FUNCTION_ICON_SMALL_ID);

		tree.setCellRenderer(new DefaultTreeCellRenderer() {
			{ setLeafIcon(funcIcon); }
			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
				super.getTreeCellRendererComponent(tree, value, false, expanded, leaf, row, hasFocus);
				var node = (DefaultMutableTreeNode) value;
				var userObj = node.getUserObject();
				var name = userObj instanceof FunctionInfo ? ((FunctionInfo) userObj).getName() : ("" + userObj);
				setText(name);
				return this;
			}
		});
		
		tree.addTreeSelectionListener(e -> {
			var node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
			if(node != null) {
				builderPanel.getAttributePanel().clearSelection();
				builderPanel.getTutorialPanel().clearSelection();
				
				var functionInfo = (FunctionInfo) node.getUserObject();
			    if (node.isLeaf()) {
			    	Function f = functionInfo.getFunction();
					String docs = TutorialItems.getFunctionDocs(f);
					builderPanel.getInfoPanel().setText(docs);
			    } else {
			    	builderPanel.getInfoPanel().setText("");
			    }
			}
		});
	}
	

	@SuppressWarnings("serial")
	private void initializeAttributeList() {
		builderPanel.getAttributePanel().getList().setCellRenderer(new DefaultListCellRenderer() {
			CyColumnPresentationManager presentationManager = registrar.getService(CyColumnPresentationManager.class);
			@Override
			@SuppressWarnings("rawtypes") 
		    public Component getListCellRendererComponent(JList list, Object value, int i, boolean selected, boolean hasFocus) {
				super.getListCellRendererComponent(list, value, i, selected, hasFocus);
				CyColumn col = (CyColumn) value;
				presentationManager.setLabel(col.getName(), this);
				return this;
			}
		});
		
		BrowserTableModel model = (BrowserTableModel) browserTable.getModel();
		CyTable table = model.getDataTable();
		
		List<CyColumn> columns = new ArrayList<>(table.getColumns());
		columns.sort(
				comparing((CyColumn col) -> col.isPrimaryKey() && "SUID".equals(col.getName())) // puts "SUID" at the end
				.thenComparing(CyColumn::getNamespace, nullsFirst(naturalOrder()))
				.thenComparing(CyColumn::getNameOnly, naturalOrder()));
		
		builderPanel.getAttributePanel().addElements(columns);
		
		JList<CyColumn> list = builderPanel.getAttributePanel().getList();
		list.addMouseListener(new ListInsertListener());
		
		list.addListSelectionListener(e -> {
			CyColumn col = list.getSelectedValue();
			if(col != null) {
				builderPanel.getFunctionPanel().clearSelection();
				builderPanel.getTutorialPanel().clearSelection();
				String docs = TutorialItems.getColumnDocs(col);
				builderPanel.getInfoPanel().setText(docs);
			}
		});
	}
	
	
	private void handleInsert() {
		SyntaxAreaPanel syntaxPanel = builderPanel.getSyntaxPanel();
		int offset = syntaxPanel.getCaretPosition();
		if(offset < 0)
			return;
		
		// Its important to clear the selection of other panels when selecting in a panel.
		FunctionInfo function = builderPanel.getFunctionPanel().getSelectedValue();
		if(function != null && !function.isCategoryHeader()) {
			syntaxPanel.insertText(offset, function.getName() + "(", ")");
			return;
		}
		
		CyColumn col = builderPanel.getAttributePanel().getSelectedValue();
		if(col != null) {
			String ref = createAttributeReferenceString(col);
			syntaxPanel.insertText(offset, ref, null);
			return;
		}
	}
	
	public static String createAttributeReferenceString(CyColumn column) {
		String name = column.getName();
		
		boolean simple = name.chars().allMatch(ch -> 
			Character.isLetter((char)ch) || Character.isDigit((char)ch) || (char)ch == '_'
		);
		
		if(simple) {
			return "$" + name;
		} else {
			var sb = new StringBuilder("${");
			escapeAttributeName(sb, name);
			sb.append("}");
			return sb.toString();
		}
	}
	
	private static void escapeAttributeName(StringBuilder sb, String name) {
		// escape '}' and ':' but not '::'
		for(int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);
			if(c == ':' && i < name.length()-1 && name.charAt(i+1) == ':') {
				i++;
				sb.append("::");
			} else if(c == ':' || c == '}' || c == '\\') {
				sb.append("\\").append(c);
			} else {
				sb.append(c);
			}
		}
	}
	
	
	private String getEquationText() {
		String equation = builderPanel.getSyntaxPanel().getText();
		equation = equation.trim();
		if(!equation.startsWith("="))
			equation = "=" + equation;
		return equation;
	}
	
	
	private Equation compileEquation(CyColumn col) {
		var tableModel = browserTable.getBrowserTableModel();
		var attribs = tableModel.getDataTable();
		var equationText = getEquationText();
		var compiler = registrar.getService(EquationCompiler.class);
		
		var attribName = col.getName();
		var attrNameToTypeMap = TableBrowserUtil.getAttNameToTypeMap(attribs, attribName);
		boolean success = compiler.compile(equationText, attrNameToTypeMap);
		
		if(!success) {
			int location = compiler.getErrorLocation();
			String msg = compiler.getLastErrorMsg();
			builderPanel.getSyntaxPanel().showSyntaxError(location, msg);
			return null;
		}
		
		return compiler.getEquation();
	}
	
	
	private void handleApply(boolean insert) {
		successfulApply = false;
		
		Equation equation = compileEquation(column);
		
		if(equation != null) {
			Collection<CyRow> rows = getRowsForApply();
			applyToRows(equation, column, rows, insert);
			
			successfulApply = true;
		} 
	}
	
	
	private Collection<CyRow> getRowsForApply() {
		BrowserTableModel tableModel = browserTable.getBrowserTableModel();
		ApplyScope scope = builderPanel.getSyntaxPanel().getApplyScope();
		switch (scope) {
			case CURRENT_CELL:
				return Collections.singletonList(tableModel.getCyRow(currentRowIndex));
			case CURRENT_SELECTION:
				return tableModel.getDataTable().getMatchingRows(CyNetwork.SELECTED, true);
			case ENTIRE_COLUMN:
				return tableModel.getDataTable().getAllRows();
		}
		return Collections.emptyList();
	}
	
	
	private void applyToRows(Equation equation, CyColumn col, Collection<CyRow> rows, boolean insert) {
		var worker = new ApplyWorker(registrar, builderPanel, equation, col, rows, insert);
		worker.execute();
	}
	
	
	private class ListInsertListener extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			if(e.getClickCount() == 2) {
				handleInsert();
			}
		}
		@SuppressWarnings({ "rawtypes", "serial" })
		@Override
		public void mousePressed(MouseEvent e) {
			JList list = (JList) e.getSource();
			if(e.isPopupTrigger()) {
				int index = list.locationToIndex(e.getPoint());
				if(index != -1 && list.getCellBounds(index, index).contains(e.getPoint())) {
					list.setSelectedIndex(index);
					
					JPopupMenu menu = new JPopupMenu();
					menu.add(new AbstractAction("Insert") {
						@Override public void actionPerformed(ActionEvent e) {
							handleInsert();
						}
					});
					menu.show(list, e.getX(), e.getY());
				}
			}
		}
	}
		
}
