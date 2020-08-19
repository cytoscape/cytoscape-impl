package org.cytoscape.browser.internal.equation;

import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import java.awt.Dimension;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.cytoscape.model.CyColumn;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")
public class EquationEditorPanel extends JPanel {
	
	private final CyServiceRegistrar registrar;
	
	private SyntaxAreaPanel syntaxPanel;
	private ItemListPanel<String> tutorialPanel;
	private ItemListPanel<String> functionPanel;
	private ItemListPanel<CyColumn> attributePanel;
	private InfoPanel infoPanel;
	
	// Maybe these should be part of the dialog?
	private JPanel buttonPanel;
	private JButton closeButton;
	private JButton applyCellButton;
	private JButton applyAllButton;
	
	
	public EquationEditorPanel(CyServiceRegistrar registrar) {
		this.registrar = registrar;
		init();
	}
	
	private void init() {
		setOpaque(!isAquaLAF());
		final GroupLayout layout = new GroupLayout(this);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		setLayout(layout);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addComponent(getSyntaxPanel())
			.addGap(20)
			.addGroup(layout.createParallelGroup()
				.addComponent(getTutorialPanel(),  0, 80, Short.MAX_VALUE)
				.addComponent(getFunctionPanel(),  0, 80, Short.MAX_VALUE)
				.addComponent(getAttributePanel(), 0, 80, Short.MAX_VALUE)
			)
			.addComponent(getInfoPanel())
			.addGap(20)
			.addComponent(getButtonPanel())
		);
		
		layout.setHorizontalGroup(layout.createParallelGroup()
			.addComponent(getSyntaxPanel())
			.addGroup(layout.createSequentialGroup()
				.addComponent(getTutorialPanel(),  0, 200, Short.MAX_VALUE)
				.addComponent(getFunctionPanel(),  0, 200, Short.MAX_VALUE)
				.addComponent(getAttributePanel(), 0, 200, Short.MAX_VALUE)
			)
			.addComponent(getInfoPanel())
			.addComponent(getButtonPanel())
		);
	}

	
	public SyntaxAreaPanel getSyntaxPanel() {
		if(syntaxPanel == null) {
			syntaxPanel = new SyntaxAreaPanel(registrar);
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
	
	public ItemListPanel<String> getFunctionPanel() {
		if(functionPanel == null) {
			functionPanel = new ItemListPanel<>("Functions");
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
			buttonPanel = LookAndFeelUtil.createOkCancelPanel(null, getCloseButton(), help, getApplyCellButton(), getApplyAllButton());
		}
		return buttonPanel;
	}
	
	public JButton getCloseButton() {
		if(closeButton == null) {
			closeButton = new JButton("Close");
		}
		return closeButton;
	}
	
	private JButton getApplyCellButton() {
		if(applyCellButton == null) {
			applyCellButton = new JButton("Apply to Current Cell");
		}
		return applyCellButton;
	}
	
	private JButton getApplyAllButton() {
		if(applyAllButton == null) {
			applyAllButton = new JButton("Apply to Entire Column");
		}
		return applyAllButton;
	}
	
}

