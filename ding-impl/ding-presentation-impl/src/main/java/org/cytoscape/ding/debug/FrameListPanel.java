package org.cytoscape.ding.debug;

import static javax.swing.GroupLayout.PREFERRED_SIZE;

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
public class FrameListPanel extends BasicCollapsiblePanel {

	private FrameListTablePanel fastPanel;
	private FrameListTablePanel slowPanel;
	private FrameListTablePanel fastBirdPanel;
	private FrameListTablePanel slowBirdPanel;
	
	
	public FrameListPanel() {
		super("Individual Frames");
		createContents();
	}
	
	private void createContents() {
		fastPanel = new FrameListTablePanel("Main Fast (on EDT)");
		slowPanel = new FrameListTablePanel("Main Slow (Async)");
		fastBirdPanel = new FrameListTablePanel("Birds-Eye-View Fast (on EDT)");
		slowBirdPanel = new FrameListTablePanel("Birds-Eye-View Slow (Async)");
		
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
					.addComponent(fastPanel, PREFERRED_SIZE, PREFERRED_SIZE, Short.MAX_VALUE)
					.addComponent(fastBirdPanel, PREFERRED_SIZE, PREFERRED_SIZE, Short.MAX_VALUE)
				)
				.addGroup(layout.createParallelGroup()
					.addComponent(slowPanel, PREFERRED_SIZE, PREFERRED_SIZE, Short.MAX_VALUE)
					.addComponent(slowBirdPanel, PREFERRED_SIZE, PREFERRED_SIZE, Short.MAX_VALUE)
				)
			)
			.addGroup(layout.createSequentialGroup()
				.addComponent(clearButton)
			)
		);
		
		JPanel content = getContentPane();
		content.setLayout(new BorderLayout());
		content.add(BorderLayout.CENTER, panel);
	}
	
	public void clear() {
		fastPanel.clear();
		slowPanel.clear();
		fastBirdPanel.clear();
		slowBirdPanel.clear();
	}
	
	
	private FrameListTablePanel getPanel(DebugFrameType type) {
    if (type == null) return null;
		switch(type) {
			case MAIN_ANNOTAITONS:
			case MAIN_EDGES:
			case MAIN_SELECTED:
			case MAIN_FAST: return fastPanel;
			case MAIN_SLOW: return slowPanel;
			case BEV_ANNOTAITONS:
			case BEV_FAST:  return fastBirdPanel;
			case BEV_SLOW:  return slowBirdPanel;
		}
		return null;
	}
	
	
	public void addFrame(DebugRootFrameInfo frame) {
		ViewUtil.invokeOnEDT(() -> {
      if (frame.getType() != null)
			  getPanel(frame.getType()).addFrame(frame);
		});
	}

}
