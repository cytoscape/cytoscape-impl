package org.cytoscape.browser.internal.equation;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.WindowConstants;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.cytoscape.application.swing.CyColumnPresentationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.browser.internal.view.TableRenderer;
import org.cytoscape.equations.EquationParser;
import org.cytoscape.equations.Function;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;

public class EquationEditorMediator {
	
	public enum ApplyScope {
		CURRENT_CELL("Current cell only"),
		CURRENT_SELECTION("Current selection"),
		ENTIRE_COLUMN("Entire column");

		private final String text;
		ApplyScope(String text) { this.text = text; }
		@Override public String toString() { return text; }
	}
	
	
	private final CyServiceRegistrar registrar;
	
	public EquationEditorMediator(CyServiceRegistrar registrar) {
		this.registrar = registrar;
	}
	
	public void openEquationEditorDialog(TableRenderer tableRenderer) {
		JFrame parent = registrar.getService(CySwingApplication.class).getJFrame();
		JDialog dialog = new JDialog(parent);
		EquationEditorPanel builderPanel = new EquationEditorPanel(registrar);
		
		wireTogether(dialog, builderPanel, tableRenderer);
		
		dialog.setTitle("Equation Builder");
		dialog.getContentPane().setLayout(new BorderLayout());
		dialog.getContentPane().add(builderPanel, BorderLayout.CENTER);
		dialog.setPreferredSize(new Dimension(550, 500));
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.pack();
		dialog.setLocationRelativeTo(parent);
		
		builderPanel.getSyntaxPanel().getSyntaxTextArea().addAncestorListener(new RequestFocusListener());
		
		dialog.setVisible(true);
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
	
	
	private void wireTogether(JDialog dialog, EquationEditorPanel builderPanel, TableRenderer tableRenderer) {
		initializeTutorialList(builderPanel);
		initializeFunctionList(builderPanel);
		initializeAttributeList(builderPanel, tableRenderer);
		
		builderPanel.getCloseButton().addActionListener(e -> dialog.dispose());
		
		SyntaxAreaPanel syntaxPanel = builderPanel.getSyntaxPanel();
		syntaxPanel.getUndoButton().addActionListener(e -> syntaxPanel.undo());
		syntaxPanel.getRedoButton().addActionListener(e -> syntaxPanel.redo());
		
		builderPanel.getInfoPanel().getInsertButton().addActionListener(e -> handleInsert(builderPanel));
		
		syntaxPanel.getApplyButton().addActionListener(e -> handleApply(builderPanel, tableRenderer));
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
				builderPanel.getInfoPanel().getInsertButton().setEnabled(false);
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
				builderPanel.getInfoPanel().getInsertButton().setEnabled(true);
			}
		});
	}
	
	
	@SuppressWarnings("serial")
	private void initializeAttributeList(EquationEditorPanel builderPanel, TableRenderer tableRenderer) {
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
		
		CyTable table = tableRenderer.getDataTable();
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
				builderPanel.getInfoPanel().getInsertButton().setEnabled(true);
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
	
	private void handleApply(EquationEditorPanel builderPanel, TableRenderer tableRenderer) {
		String formula = builderPanel.getSyntaxPanel().getText();
//		String attribName = tableRenderer.getRenderingEngine().getSelectedColumn().getModel().getName();
		// MKTODO
		
	}
	

}
