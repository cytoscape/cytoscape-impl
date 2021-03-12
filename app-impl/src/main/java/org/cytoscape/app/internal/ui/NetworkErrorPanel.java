package org.cytoscape.app.internal.ui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")
public class NetworkErrorPanel extends JPanel {
	
	private final IconManager iconManager;
	
	private List<String> warningMessages = new ArrayList<>();
	
	
	public NetworkErrorPanel(IconManager iconManager) {
		this.iconManager = iconManager;
		initComponents();
	}
	
	
	private void initComponents() {
		removeAll();
		
		JLabel warnIcon = new JLabel(IconManager.ICON_WARNING);
		warnIcon.setFont(iconManager.getIconFont(14.0f));
		warnIcon.setForeground(LookAndFeelUtil.getWarnColor());
		
		GroupLayout layout = new GroupLayout(this);
	    setLayout(layout);
	    layout.setAutoCreateContainerGaps(true);
	    layout.setAutoCreateGaps(true);
	    
	    SequentialGroup verticalLabelGroup = layout.createSequentialGroup();
	    ParallelGroup horizonalLabelGroup = layout.createParallelGroup();
	    
	    for(String message : warningMessages) {
	    	JLabel label = new JLabel(message);
	    	verticalLabelGroup.addComponent(label);
	    	horizonalLabelGroup.addComponent(label);
	    }
	    
	    layout.setHorizontalGroup(layout.createSequentialGroup()
	    	.addComponent(warnIcon)
	    	.addGroup(horizonalLabelGroup)
	    );
		
	    layout.setVerticalGroup(layout.createParallelGroup()
	    	.addComponent(warnIcon)
	    	.addGroup(verticalLabelGroup)
	    );
	}
	
	public void addMessage(String message) {
		warningMessages.add(message);
		initComponents();
	}
	
	public void clearMessages() {
		warningMessages.clear();
		initComponents();
	}

}
