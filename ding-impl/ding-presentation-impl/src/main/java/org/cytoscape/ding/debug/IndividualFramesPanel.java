package org.cytoscape.ding.debug;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.cytoscape.ding.internal.util.ViewUtil;
import org.cytoscape.util.swing.BasicCollapsiblePanel;
import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")
public class IndividualFramesPanel extends BasicCollapsiblePanel {

	private FramePanel fastPanel;
	private FramePanel slowPanel;
	private FramePanel fastBirdPanel;
	private FramePanel slowBirdPanel;
	
	
	public IndividualFramesPanel() {
		super("Individual Frames");
		createContents();
	}
	
	private void createContents() {
		fastPanel = new FramePanel("Main Fast (on EDT)");
		slowPanel = new FramePanel("Main Slow (Async)");
		fastBirdPanel = new FramePanel("Birds-Eye-View Fast (on EDT)");
		slowBirdPanel = new FramePanel("Birds-Eye-View Slow (Async)");
		
		final int w = 200, h = 200;
		fastPanel.setPreferredSize(new Dimension(w, h));
		slowPanel.setPreferredSize(new Dimension(w, h));
		fastBirdPanel.setPreferredSize(new Dimension(w, h));
		slowBirdPanel.setPreferredSize(new Dimension(w, h));
		
		JButton clearButton = new JButton("Clear");
		clearButton.addActionListener(e -> clear());
		LookAndFeelUtil.makeSmall(clearButton);
		
		JPanel panel = new JPanel();
		panel.setOpaque(false);
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addGroup(layout.createParallelGroup()
				.addComponent(fastPanel)
				.addComponent(slowPanel)
			)
			.addGroup(layout.createParallelGroup()
				.addComponent(fastBirdPanel)
				.addComponent(slowBirdPanel)
			)
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(clearButton)
			)
		);
		
		layout.setHorizontalGroup(layout.createParallelGroup()
			.addGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup()
					.addComponent(fastPanel)
					.addComponent(fastBirdPanel)
				)
				.addGroup(layout.createParallelGroup()
					.addComponent(slowPanel)
					.addComponent(slowBirdPanel)
				)
			)
			.addGroup(layout.createSequentialGroup()
				.addComponent(clearButton)
//				.addGap(0, Short.MAX_VALUE, Short.MAX_VALUE)
			)
		);
		
		JPanel content = getContentPane();
		content.setLayout(new BorderLayout());
		content.add(BorderLayout.WEST, panel);
	}
	
	public void clear() {
		fastPanel.clear();
		slowPanel.clear();
		fastBirdPanel.clear();
		slowBirdPanel.clear();
	}
	
	
	private FramePanel getPanel(DebugFrameType type) {
		switch(type) {
			case MAIN_ANNOTAITONS:
			case MAIN_EDGES:
			case MAIN_FAST: return fastPanel;
			case MAIN_SLOW: return slowPanel;
			case BEV_FAST:  return fastBirdPanel;
			case BEV_SLOW:  return slowBirdPanel;
		}
		return null;
	}
	
	
	public void addFrame(DebugFrameType type, boolean cancelled, int nodeCount, int edgeCountEstimate, long time) {
		DebugEntry entry = new DebugEntry(time, cancelled, type, nodeCount, edgeCountEstimate);
//		int frameNumber = frameCount.merge(type, 0, (x,y) -> x + 1);
//		if(logCheckbox.isSelected()) { 
//			System.out.println(type + " " + frameNumber + " done (" + time + ")");
//		}
		ViewUtil.invokeOnEDT(() -> {
			getPanel(type).addEntry(entry);
		});
	}

}
