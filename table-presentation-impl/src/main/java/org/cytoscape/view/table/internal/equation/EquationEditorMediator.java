package org.cytoscape.view.table.internal.equation;

import static java.util.Comparator.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
	
	
	private EquationEditorMediator(CyColumn column, BrowserTable browserTable, EquationEditorPanel builderPanel, CyServiceRegistrar registrar) {
		this.browserTable = browserTable;
		this.registrar = registrar;
		this.builderPanel = builderPanel;
		this.column = column;
		this.currentRowIndex = browserTable.convertRowIndexToModel(browserTable.getSelectedRow());
	}
	
	public static void openEquationEditorDialog(BrowserTable browserTable, String equation, CyServiceRegistrar registrar) {
		JFrame parent = registrar.getService(CySwingApplication.class).getJFrame();
		JDialog dialog = new JDialog(parent);
		
		BrowserTableModel tableModel = browserTable.getBrowserTableModel();
		int cellCol = browserTable.convertColumnIndexToModel(browserTable.getSelectedColumn());
		CyColumn column =  tableModel.getColumn(cellCol);
		
		var builderPanel = new EquationEditorPanel(registrar, browserTable);
		
		EquationEditorMediator mediator = new EquationEditorMediator(column, browserTable, builderPanel, registrar);
		mediator.wireTogether(dialog);
		
		String attribName = column.getName();
		
		dialog.setTitle("Equation Builder for " + attribName);
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
	}
	
	private static class RequestFocusListener implements AncestorListener {
		public void ancestorAdded(AncestorEvent e) {
			var c = e.getComponent();
			c.requestFocusInWindow();
			c.removeAncestorListener(this);
		}
		public void ancestorRemoved(AncestorEvent event) { }
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
	
	
	@SuppressWarnings("serial")
	private void initializeFunctionList() {
		
		
		EquationParser equationParser = registrar.getService(EquationParser.class);
		
		SortedMap<String,SortedMap<String,Function>> categories = new TreeMap<>();
		
		for(Function f : equationParser.getRegisteredFunctions()) {
			String category = f.getCategoryName();
			if(category == null)
				category = "Other";
			
			Map<String,Function> functions = categories.computeIfAbsent(category, k -> new TreeMap<>());
			functions.put(f.getName(), f);
		}
		
		ItemListPanel<FunctionInfo> functionPanel = builderPanel.getFunctionPanel();
		
		List<String> topCategories = Arrays.asList("Numeric", "Text", "List", "Logic", "Network");
		
		// First put the top categories at the top
		for(String categoryName : topCategories) {
			var functions = categories.remove(categoryName);
			if(functions != null) {
				functionPanel.addElement(FunctionInfo.category(categoryName));
				
				for(var functionEntry : functions.entrySet()) {
					Function f = functionEntry.getValue();
					functionPanel.addElement(FunctionInfo.function(f));
				}
			}
		}
		
		// Now add the rest
		for(var categoryEntry : categories.entrySet()) {
			String categoryName = categoryEntry.getKey();
			var functions = categoryEntry.getValue();
			
			functionPanel.addElement(FunctionInfo.category(categoryName));
			
			for(var functionEntry : functions.entrySet()) {
				Function f = functionEntry.getValue();
				functionPanel.addElement(FunctionInfo.function(f));
			}
		}
		
		JList<FunctionInfo> list = functionPanel.getList();
		
		list.setCellRenderer(new DefaultListCellRenderer() {
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				
				FunctionInfo functionInfo = (FunctionInfo) value;
				
				if(functionInfo.isCategoryHeader()) {
					Font font = getFont();
					Font bold = font.deriveFont(Font.BOLD);
					setFont(bold);
					setText(functionInfo.getName());
					setBackground(slightlyDarker(getBackground()));
				} else {
					setText("  " + functionInfo.getName());
				}
				return this;
			};
		});
		
		list.addListSelectionListener(e -> {
			FunctionInfo functionInfo = list.getSelectedValue();
			if(functionInfo != null) {
				builderPanel.getAttributePanel().clearSelection();
				builderPanel.getTutorialPanel().clearSelection();
				if(functionInfo.isCategoryHeader()) {
					builderPanel.getInfoPanel().setText("");
				} else {
					Function f = functionInfo.getFunction();
					String docs = TutorialItems.getFunctionDocs(f);
					builderPanel.getInfoPanel().setText(docs);
				}
			}
		});
	}

	private static Color slightlyDarker(Color color) {
		double f = 0.9;
		return new Color(
			Math.max((int) (color.getRed()   * f), 0), 
			Math.max((int) (color.getGreen() * f), 0),
			Math.max((int) (color.getBlue()  * f), 0)
		);
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
		
		FunctionInfo function = builderPanel.getFunctionPanel().getSelectedValue();
		if(function != null && !function.isCategoryHeader()) {
			syntaxPanel.insertText(offset, function.getName() + "(", ")");
			return;
		}
		
		// MKTODO need to figure out exactly how to reference column names
		CyColumn col = builderPanel.getAttributePanel().getSelectedValue();
		if(col != null) {
			String ref = getAttributeReference(col);
			syntaxPanel.insertText(offset, ref, null);
			return;
		}
	}
	
	public static String getAttributeReference(CyColumn column) {
		String name = column.getName();
		boolean simple = name.chars().allMatch(Character::isAlphabetic);
		if(simple)
			return "$" + name;
		else
			return "${" + name + "}";
	}
	
	
	private String getEquationText() {
		String equation = builderPanel.getSyntaxPanel().getText();
		equation = equation.trim();
		if(!equation.startsWith("="))
			equation = "=" + equation;
		return equation;
	}
	
	private void handleApply(boolean insert) {
		Equation equation = compileEquation(column);
		if(equation != null) {
			Collection<CyRow> rows = getRowsForApply();
			applyToRows(equation, column, rows, insert);
		}
		
	}
	
	private Equation compileEquation(CyColumn col) {
		BrowserTableModel tableModel = browserTable.getBrowserTableModel();
		CyTable attribs = tableModel.getDataTable();
		String equationText = getEquationText();
		EquationCompiler compiler = registrar.getService(EquationCompiler.class);
		
		String attribName = col.getName();
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
		
}
