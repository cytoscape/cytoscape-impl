package org.cytoscape.view.table.internal.equation;

import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.util.swing.TextIcon;
import org.cytoscape.view.table.internal.equation.EquationEditorMediator.ApplyScope;
import org.cytoscape.view.table.internal.impl.BrowserTable;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

@SuppressWarnings("serial")
public class SyntaxAreaPanel extends JPanel {
	
	private final CyServiceRegistrar registrar;
	private final IconManager iconManager;
	private final BrowserTable browserTable;
	
	private JPanel topPanel;
	private RSyntaxTextArea textArea;
	private JScrollPane syntaxAreaScrollPane;
	private JButton undoButton;
	private JButton redoButton;
	
	private JLabel applyLabel;
	private JComboBox<ApplyScope> applyScopeCombo; 
	private JButton applyButton;
	private JButton evalButton;
	private JLabel resultLabel;
	
	
	public SyntaxAreaPanel(CyServiceRegistrar registrar, BrowserTable browserTable) {
		this.registrar = registrar;
		this.browserTable = browserTable;
		this.iconManager = registrar.getService(IconManager.class);
		init();
	}
	
	private void init() {
		setOpaque(!isAquaLAF());
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addComponent(getTopPanel())
			.addComponent(getSyntaxAreaScrollPane())
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(getApplyLabel())
				.addComponent(getApplyScopeCombo())
				.addComponent(getApplyButton())
				.addComponent(getEvalButton())
				.addComponent(getResultLabel())
			)
		);
		
		layout.setHorizontalGroup(layout.createParallelGroup()
			.addComponent(getTopPanel())
			.addComponent(getSyntaxAreaScrollPane())
			.addGroup(Alignment.LEADING, layout.createSequentialGroup()
				.addComponent(getApplyLabel())
				.addComponent(getApplyScopeCombo(), 0, 150, 150)
				.addComponent(getApplyButton())
				.addComponent(getEvalButton())
				.addComponent(getResultLabel())
			)
		);
		
		getUndoButton().addActionListener(e -> undo());
		getRedoButton().addActionListener(e -> redo());
		
		getSyntaxTextArea().addCaretListener(e -> clearResultLabel());
		getSyntaxTextArea().getDocument().addDocumentListener((DocumentListenerAdapter)(e) -> {
			updateApplyButtonEnablement();
		});
		
		updateApplyButtonEnablement();
		setCaret(0); // Want the caret to be visible and flashing
	}
	
	
	public ApplyScope getApplyScope() {
		return (ApplyScope) getApplyScopeCombo().getSelectedItem();
	}
	
	public int getCaretPosition() {
		return getSyntaxTextArea().getCaretPosition();
	}
	
	public void setText(String text) {
		getSyntaxTextArea().setText(text == null ? "" : text);
		updateApplyButtonEnablement();
	}
	
	public String getText() {
		return getSyntaxTextArea().getText();
	}
	
	
	public void insertText(int offset, String text, String post) {
		try {
			getSyntaxTextArea().getDocument().insertString(offset, text, null);
			int caretPos = offset + text.length();
			if(post != null) {
				getSyntaxTextArea().getDocument().insertString(caretPos, post, null);
			}
			setCaret(caretPos);
			getSyntaxTextArea().grabFocus();
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
	
	
	public void showEvalSuccess(int numRows) {
		clearResultLabel();
		if(numRows == 1)
			showResult("1 row updated");
		else if(numRows > 1)
			showResult(numRows + " rows updated");
	}
	
	@SuppressWarnings("deprecation")
	public void showSyntaxError(int location, String message) {
		clearResultLabel();
		
		// Show syntax error as a popup
		JPopupMenu popup = new JPopupMenu();
		popup.add(createErrorPopupLabel(message, true));
		
		// Show popup over the text area in the location where syntax error was encountered.
		try {
			RSyntaxTextArea textArea = getSyntaxTextArea();
			Rectangle rectangle = textArea.modelToView(location);
			textArea.scrollRectToVisible(rectangle);
			rectangle = textArea.modelToView(location);
			popup.show(textArea, rectangle.x, rectangle.y + rectangle.height);
		} catch (BadLocationException e) {
		}
	}
	
	public void showEvalError(int numRows, int numErrors, Collection<String> messages) {
		clearResultLabel();
		// Show syntax error as a popup
		JPanel panel = new JPanel();
		panel.setOpaque(false);
		panel.setLayout(new GridLayout(messages.size(), 1));
		
		if(numRows == 1) {
			panel.setLayout(new GridLayout(1, 1));
			panel.add(createErrorPopupLabel(messages.iterator().next(), true));
		} else {
			panel.setLayout(new GridLayout(messages.size() + 1, 1));
			
			StringBuilder sb = new StringBuilder("Applied to ").append(numRows).append(" rows, ");
			if(numErrors == 1)
				sb.append("1 row had an error");
			else
				sb.append(numErrors).append(" rows had errors");
			
			panel.add(createErrorPopupLabel(sb.toString(), true));
			
			for(String error : messages) {
				panel.add(createErrorPopupLabel(error, false));
			}
		}
		
		JPopupMenu popup = new JPopupMenu();
		popup.add(panel);
		JButton comp = getApplyButton();
		popup.show(comp, 0, comp.getHeight());
	}
	
	private JLabel createErrorPopupLabel(String text, boolean includeIcon) {
		JLabel label = new JLabel(text);
		label.setForeground(Color.RED);
		LookAndFeelUtil.makeSmall(label);
		label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		if(includeIcon)
			label.setIcon(new TextIcon(IconManager.ICON_EXCLAMATION_CIRCLE, iconManager.getIconFont(11f), Color.RED, 10, 10));
		return label;
	}
	
	
	private void setCaret(int offset) {
		getSyntaxTextArea().setCaretPosition(offset);
		getSyntaxTextArea().getCaret().setVisible(true);
	}
	
	public void undo() {
		getSyntaxTextArea().undoLastAction();
	}
	
	public void redo() {
		getSyntaxTextArea().redoLastAction();
	}
	
	
	private JPanel getTopPanel() {
		if(topPanel == null) {
			JLabel label = new JLabel("Equation Editor");
			LookAndFeelUtil.makeSmall(label);
			topPanel = new JPanel();
			topPanel.setOpaque(!LookAndFeelUtil.isAquaLAF()); // Transparent if Aqua
			topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
			topPanel.add(label);
			topPanel.add(Box.createHorizontalGlue());
			topPanel.add(getUndoButton());
			topPanel.add(getRedoButton());
		}
		return topPanel;
	}
	
	private JScrollPane getSyntaxAreaScrollPane() {
		if(syntaxAreaScrollPane == null) {
			syntaxAreaScrollPane = new JScrollPane(getSyntaxTextArea());
		}
		return syntaxAreaScrollPane;
	}
	
	public RSyntaxTextArea getSyntaxTextArea() {
		if(textArea == null) {
			textArea = SyntaxAreaFactory.createEquationTextArea(registrar);
		}
		return textArea;
	}
	
	public JButton getUndoButton() {
		if(undoButton == null) {
			undoButton = createIconButton(IconManager.ICON_UNDO, "Undo");
		}
		return undoButton;
	}
	
	public JButton getRedoButton() {
		if(redoButton == null) {
			redoButton = createIconButton(IconManager.ICON_REPEAT, "Redo");
		}
		return redoButton;
	}
	
	private JLabel getApplyLabel() {
		if(applyLabel == null) {
			applyLabel = new JLabel("Apply to:");
			LookAndFeelUtil.makeSmall(applyLabel);
		}
		return applyLabel;
	}
	
	private JLabel getResultLabel() {
		if(resultLabel == null) {
			resultLabel = new JLabel();
			LookAndFeelUtil.makeSmall(resultLabel);
		}
		return resultLabel;
	}
	
	private void clearResultLabel() {
		getResultLabel().setText("");
	}
	
	private void showResult(String text) {
		getResultLabel().setText(text);
	}
	
	private JComboBox<ApplyScope> getApplyScopeCombo() {
		if(applyScopeCombo == null) {
			ApplyScope[] scopes = ApplyScope.values(tableHasSelected());
			applyScopeCombo = new JComboBox<>(scopes);
			LookAndFeelUtil.makeSmall(applyScopeCombo);
		}
		return applyScopeCombo;
	}
	
	public JButton getApplyButton() {
		if(applyButton == null) {
			applyButton = new JButton("Insert Formula");
			applyButton.setToolTipText("Insert the formula into the selected cells. The formula will be re-evaluated as needed.");
			LookAndFeelUtil.makeSmall(applyButton);
		}
		return applyButton;
	}
	
	public JButton getEvalButton() {
		if(evalButton == null) {
			evalButton = new JButton("Evaluate and Insert Result");
			evalButton.setToolTipText("Evaluate the formula now and insert the result into the selected cells.");
			LookAndFeelUtil.makeSmall(evalButton);
		}
		return evalButton;
	}
	
	private void updateApplyButtonEnablement() {
		boolean enabled = !getText().isBlank();
		getApplyButton().setEnabled(enabled);
		getEvalButton().setEnabled(enabled);
	}
	
	private JButton createIconButton(String icon, String tooltip) {
		IconManager iconManager = registrar.getService(IconManager.class);
		JButton button = new JButton(icon);
		button.setToolTipText(tooltip);
		button.setBorderPainted(false);
		button.setContentAreaFilled(false);
		button.setFocusPainted(false);
		button.setFont(iconManager.getIconFont(14.0f));
		button.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
		return button;
	}
	
	private boolean tableHasSelected() {
		CyColumn selectedColumn = browserTable.getBrowserTableModel().getDataTable().getColumn(CyNetwork.SELECTED);
		return selectedColumn != null && selectedColumn.getType() == Boolean.class;
	}
	

	@FunctionalInterface
	private interface DocumentListenerAdapter extends DocumentListener {
		@Override default void insertUpdate(DocumentEvent e) { update(e); }
		@Override default void removeUpdate(DocumentEvent e) { update(e); }
		@Override default void changedUpdate(DocumentEvent e) { update(e); }
		void update(DocumentEvent e);
	}
	
}
