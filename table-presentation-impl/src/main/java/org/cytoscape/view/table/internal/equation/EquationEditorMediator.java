package org.cytoscape.view.table.internal.equation;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.WindowConstants;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.HyperlinkEvent.EventType;

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
import org.cytoscape.view.table.internal.impl.BrowserTable;
import org.cytoscape.view.table.internal.impl.BrowserTableModel;
import org.cytoscape.view.table.internal.util.TableBrowserUtil;

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
	
	public EquationEditorMediator(CyServiceRegistrar registrar) {
		this.registrar = registrar;
	}
	
	public void openEquationEditorDialog(BrowserTable browserTable) {
		JFrame parent = registrar.getService(CySwingApplication.class).getJFrame();
		JDialog dialog = new JDialog(parent);
		
		EquationEditorPanel builderPanel = new EquationEditorPanel(registrar, browserTable);
		
		wireTogether(dialog, builderPanel, browserTable);
		
		String attribName = getColumnName(browserTable);
		
		dialog.setTitle("Equation Builder for " + attribName);
		dialog.setModal(true);
		dialog.getContentPane().setLayout(new BorderLayout());
		dialog.getContentPane().add(builderPanel, BorderLayout.CENTER);
		dialog.setPreferredSize(new Dimension(550, 430)); 
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.pack();
		dialog.setLocationRelativeTo(parent);
		
		builderPanel.getSyntaxPanel().getSyntaxTextArea().addAncestorListener(new RequestFocusListener());
		
		String formula = getCurrentFormula(browserTable);
		if(formula != null)
			builderPanel.getSyntaxPanel().setText(formula);
		
		dialog.setVisible(true);
	}
	
	
	private static String getCurrentFormula(BrowserTable browserTable) {
		int cellRow = browserTable.getSelectedRow();
		int cellCol = browserTable.getSelectedColumn();
		
		String colName = browserTable.getColumnName(cellCol);
		CyRow row = browserTable.getBrowserTableModel().getCyRow(cellRow);
		
		Object obj = row.getRaw(colName);
		if(obj instanceof Equation) {
			Equation equation = (Equation) obj;
			return equation.toString().trim().substring(1);
		}
		return null;
	}

	private class RequestFocusListener implements AncestorListener {
		public void ancestorAdded(AncestorEvent e) {
			var c = e.getComponent();
			c.requestFocusInWindow();
			c.removeAncestorListener(this);
		}
		public void ancestorRemoved(AncestorEvent event) { }
		public void ancestorMoved(AncestorEvent event) { }
	}
	
	
	private void wireTogether(JDialog dialog, EquationEditorPanel builderPanel, BrowserTable browserTable) {
		initializeTutorialList(builderPanel);
		initializeFunctionList(builderPanel);
		initializeAttributeList(builderPanel, browserTable);
		
		builderPanel.getCloseButton().addActionListener(e -> dialog.dispose());
		
		SyntaxAreaPanel syntaxPanel = builderPanel.getSyntaxPanel();
		syntaxPanel.getUndoButton().addActionListener(e -> syntaxPanel.undo());
		syntaxPanel.getRedoButton().addActionListener(e -> syntaxPanel.redo());
		
		builderPanel.getInfoPanel().getTextArea().addHyperlinkListener(e -> {
			if(e.getEventType() == EventType.ACTIVATED) {
				handleInsert(builderPanel);	
			}
		});
		
		syntaxPanel.getApplyButton().addActionListener(e -> handleApply(builderPanel, browserTable));
	}
	
	
	private void initializeTutorialList(EquationEditorPanel builderPanel) {
		builderPanel.getTutorialPanel().setElements(TutorialItems.getTutorialItems());
		
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
	
	
	private void initializeFunctionList(EquationEditorPanel builderPanel) {
		// Get list of functions
		EquationParser equationParser = registrar.getService(EquationParser.class);
		
		SortedMap<String,Function> functions = new TreeMap<>();
		equationParser.getRegisteredFunctions().forEach(f -> functions.put(f.getName(), f));
		
		builderPanel.getFunctionPanel().setElements(functions.keySet());
		
		JList<String> list = builderPanel.getFunctionPanel().getList();
		list.addListSelectionListener(e -> {
			String name = list.getSelectedValue();
			if(name != null) {
				builderPanel.getAttributePanel().clearSelection();
				builderPanel.getTutorialPanel().clearSelection();
				Function f = functions.get(name);
				String docs = TutorialItems.getFunctionDocs(f);
				builderPanel.getInfoPanel().setText(docs);
			}
		});
	}
	
	
	@SuppressWarnings("serial")
	private void initializeAttributeList(EquationEditorPanel builderPanel, BrowserTable browserTable) {
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
		Collection<CyColumn> columns = table.getColumns();
		List<CyColumn> sortedCols = new ArrayList<>(columns);
		sortedCols.sort(Comparator.comparing(CyColumn::getName));
		builderPanel.getAttributePanel().setElements(sortedCols);
		
		JList<CyColumn> list = builderPanel.getAttributePanel().getList();
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
	
	
	private void handleInsert(EquationEditorPanel builderPanel) {
		SyntaxAreaPanel syntaxPanel = builderPanel.getSyntaxPanel();
		int offset = syntaxPanel.getCaretPosition();
		if(offset < 0)
			return;
		
		String function = builderPanel.getFunctionPanel().getSelectedValue();
		if(function != null) {
			syntaxPanel.insertText(offset, function + "(", ")");
			return;
		}
		
		// MKTODO need to figure out exactly how to reference column names
		CyColumn col = builderPanel.getAttributePanel().getSelectedValue();
		if(col != null) {
			String name = col.getNameOnly();
			String ref = "$" + name;
			syntaxPanel.insertText(offset, ref, null);
			return;
		}
	}
	
	private static String getColumnName(BrowserTable browserTable) {
		BrowserTableModel tableModel = browserTable.getBrowserTableModel();
		int cellCol = browserTable.convertColumnIndexToModel(browserTable.getSelectedColumn());
		String attribName = tableModel.getColumnName(cellCol);
		return attribName;
	}
	
	private void handleApply(EquationEditorPanel builderPanel, BrowserTable browserTable) {
		BrowserTableModel tableModel = browserTable.getBrowserTableModel();
		String formula = builderPanel.getSyntaxPanel().getText();
		String attribName = getColumnName(browserTable);
		CyTable attribs = tableModel.getDataTable();

		EquationCompiler compiler = registrar.getService(EquationCompiler.class);
		StringBuilder errorMessage = new StringBuilder();
		Equation equation = compileEquation(compiler, attribs, attribName, formula, errorMessage);
		if(equation == null) {
			builderPanel.getSyntaxPanel().showError(errorMessage.toString());
			return;
		}
		
		Collection<CyRow> rows = Collections.emptyList();
		
		ApplyScope scope = builderPanel.getSyntaxPanel().getApplyScope();
		switch (scope) {
			case CURRENT_CELL:
				int cellRow = browserTable.convertRowIndexToModel(browserTable.getSelectedRow());
				rows = Collections.singletonList(tableModel.getCyRow(cellRow));
				break;
			case CURRENT_SELECTION:
				rows = tableModel.getDataTable().getMatchingRows(CyNetwork.SELECTED, true);
				break;
			case ENTIRE_COLUMN:
				rows = tableModel.getDataTable().getAllRows();
				break;
		}
		
		for(CyRow row : rows) {
			if(!setAttribute(row, attribName, equation, errorMessage)) {
				builderPanel.getSyntaxPanel().showError(errorMessage.toString());
				break;
			}
		}
		
	}
	
	private Equation compileEquation(EquationCompiler compiler, CyTable table, String attribName, String formula, StringBuilder errorMessage) {
		formula = "=" + formula;
		Map<String,Class<?>> attrNameToTypeMap = TableBrowserUtil.getAttNameToTypeMap(table, attribName);
		if(compiler.compile(formula, attrNameToTypeMap))
			return compiler.getEquation();
		errorMessage.append(compiler.getLastErrorMsg());
		return null;
	}

	private boolean setAttribute(CyRow row, String attribName, Equation newValue, StringBuilder errorMessage) {
		try {
			row.set(attribName, newValue);
			return true;
		} catch(Exception e) {
			errorMessage.append(e.getMessage());
			return false;
		}
	}
	
}
