package de.mpg.mpi_inf.bioinf.netanalyzer.ui;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Icon;
import javax.swing.JPanel;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;

public class ResultPanel extends JPanel implements CytoPanelComponent {
	
	private static final Dimension DEF_PANEL_SIZE = new Dimension(650, 530);
	
	private final String panelTitle;
	
	public ResultPanel(final String panelTitle) {
		this.setPreferredSize(DEF_PANEL_SIZE);
		this.setSize(DEF_PANEL_SIZE);
		this.setMinimumSize(DEF_PANEL_SIZE);
		
		this.panelTitle = panelTitle;
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = -7824516315016600756L;

	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.EAST;
	}

	@Override
	public String getTitle() {
		return panelTitle;
	}

	@Override
	public Icon getIcon() {
		return null;
	}


}
