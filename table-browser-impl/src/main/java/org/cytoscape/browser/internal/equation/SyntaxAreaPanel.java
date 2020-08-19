package org.cytoscape.browser.internal.equation;

import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.BadLocationException;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

@SuppressWarnings("serial")
public class SyntaxAreaPanel extends JPanel {
	
	private final CyServiceRegistrar registrar;
	
	private JPanel topPanel;
	private RSyntaxTextArea textArea;
	private JScrollPane syntaxAreaScrollPane;
	private JButton undoButton;
	private JButton redoButton;
	
	
	public SyntaxAreaPanel(CyServiceRegistrar registrar) {
		this.registrar = registrar;
		init();
	}
	
	private void init() {
		setOpaque(!isAquaLAF());
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(getTopPanel())
				.addComponent(getSyntaxAreaScrollPane()));
		layout.setHorizontalGroup(layout.createParallelGroup()
				.addComponent(getTopPanel())
				.addComponent(getSyntaxAreaScrollPane()));
		
		// Want the caret to be visible and flasing
		setCaret(0);
	}
	
	public int getCaretPosition() {
		return getSyntaxTextArea().getCaretPosition();
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

}
