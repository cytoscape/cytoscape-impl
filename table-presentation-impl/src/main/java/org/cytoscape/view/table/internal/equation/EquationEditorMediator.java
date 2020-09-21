package org.cytoscape.view.table.internal.equation;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;
import static java.util.stream.Collectors.toList;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
	
	
	private EquationEditorMediator(BrowserTable browserTable, EquationEditorPanel builderPanel, CyServiceRegistrar registrar) {
		this.browserTable = browserTable;
		this.registrar = registrar;
		this.builderPanel = builderPanel;
	}
	
	public static void openEquationEditorDialog(BrowserTable browserTable, String equation, CyServiceRegistrar registrar) {
		JFrame parent = registrar.getService(CySwingApplication.class).getJFrame();
		JDialog dialog = new JDialog(parent);
		
		var builderPanel = new EquationEditorPanel(registrar, browserTable);
		
		EquationEditorMediator mediator = new EquationEditorMediator(browserTable, builderPanel, registrar);
		mediator.wireTogether(dialog);
		
		String attribName = mediator.getColumn().getName();
		
		dialog.setTitle("Equation Builder for " + attribName);
		dialog.setModal(true);
		dialog.getContentPane().setLayout(new BorderLayout());
		dialog.getContentPane().add(builderPanel, BorderLayout.CENTER);
		dialog.setPreferredSize(new Dimension(550, 430)); 
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
		syntaxPanel.getApplyButton().addActionListener(e -> handleApply());
		
		builderPanel.getInfoPanel().getTextArea().addHyperlinkListener(e -> {
			if(e.getEventType() == EventType.ACTIVATED) {
				handleInsert();	
			}
		});
	}
	
	
	private void initializeTutorialList() {
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
	
	
	private void initializeFunctionList() {
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
		
		List<CyColumn> colsToShow = table.getColumns().stream()
				.filter(col -> !"SUID".equals(col.getName()))
				.sorted(comparing(CyColumn::getNamespace, nullsFirst(naturalOrder())).thenComparing(CyColumn::getNameOnly))
				.collect(toList());
		
		builderPanel.getAttributePanel().setElements(colsToShow);
		
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
		
		String function = builderPanel.getFunctionPanel().getSelectedValue();
		if(function != null) {
			syntaxPanel.insertText(offset, function + "(", ")");
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
	
	private CyColumn getColumn() {
		BrowserTableModel tableModel = browserTable.getBrowserTableModel();
		int cellCol = browserTable.convertColumnIndexToModel(browserTable.getSelectedColumn());
		return tableModel.getColumn(cellCol);
	}
	
	private String getEquationText() {
		String equation = builderPanel.getSyntaxPanel().getText();
		equation = equation.trim();
		if(!equation.startsWith("="))
			equation = "=" + equation;
		return equation;
	}
	
	private void handleApply() {
		CyColumn col = getColumn();
		Equation equation = compileEquation(col);
		if(equation != null) {
			Collection<CyRow> rows = getRowsForApply();
			applyToRows(equation, col, rows);
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
				int cellRow = browserTable.convertRowIndexToModel(browserTable.getSelectedRow());
				return Collections.singletonList(tableModel.getCyRow(cellRow));
			case CURRENT_SELECTION:
				return tableModel.getDataTable().getMatchingRows(CyNetwork.SELECTED, true);
			case ENTIRE_COLUMN:
				return tableModel.getDataTable().getAllRows();
		}
		return Collections.emptyList();
	}
	
	private void applyToRows(Equation equation, CyColumn col, Collection<CyRow> rows) {
		Set<String> errors = new HashSet<>();
		int numErrors = 0;
		
		for(CyRow row : rows) {
			row.set(col.getName(), equation);
			
			// We really need a better way to detect if an Equation evaluation results in an error.
			// Note, this is NOT thread safe.
			Object x = row.get(col.getName(), col.getType());
			if(x == null) {
				String error = row.getTable().getLastInternalError();
				if(error != null) {
					// There 
					numErrors++;
					errors.add(error);
				}
			}
		}
		
		if(errors.isEmpty()) {
			builderPanel.getSyntaxPanel().showEvalSuccess(rows.size());
		} else {
			builderPanel.getSyntaxPanel().showEvalError(rows.size(), numErrors, errors);
		}
	}

}
