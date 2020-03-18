package org.cytoscape.ding.debug;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.table.AbstractTableModel;

import org.cytoscape.util.swing.BasicCollapsiblePanel;
import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")
public class FrameRatePanel extends BasicCollapsiblePanel {

	private JLabel frameRateLabel;
	private JTable table;
	private FrameRateTableModel model;
	
	private LinkedList<DebugFrameInfo> frames = new LinkedList<>();
	private Timer timer;
	private long window = 5000; // five seconds
	
	public FrameRatePanel() {
		super("Frame Rate");
		createContents();
		timer = new Timer(1000, e -> updateFrameRate());
		timer.setRepeats(true);
	}

	
	private void createContents() {
		frameRateLabel = new JLabel("Frame Rate: ");
		LookAndFeelUtil.makeSmall(frameRateLabel);
		model = new FrameRateTableModel();
		table = new JTable(model);
		table.setShowGrid(true);
		
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setPreferredSize(new Dimension(300, 200));
		
		JPanel panel = new JPanel();
		panel.setOpaque(false);
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup()
			.addComponent(frameRateLabel)
			.addComponent(scrollPane)
		);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addComponent(frameRateLabel)
			.addComponent(scrollPane)
		);
		
		
		JPanel content = getContentPane();
		content.setLayout(new BorderLayout());
		content.add(BorderLayout.WEST, panel);
	}
	
	private void updateFrameRateLabel(int frameCount, long frameTime) {
//		System.out.println("frameCount: " + frameCount + ", frameTime: " + frameTime);
		double framesPerSecondRaw = (double)frameCount / ((double)frameTime / 1000.0);
		frameRateLabel.setText(String.format("Frame Rate: %.2f per sec", framesPerSecondRaw));
	}
	
	
	public void addFrame(DebugFrameInfo frame) {
		synchronized(frames) {
			frames.addLast(frame);
		}
		if(!timer.isRunning()) {
			timer.start();
		}
	}
	
	private void updateFrameRate() {
		long frameTime = 0;
		int frameCount = 0;
		
		synchronized(frames) {
			if(frames.isEmpty())
				return;
			
			System.out.println("frames in list: " + frames.size());
			
			long endOfWindow = frames.getLast().getEndTime();
			long startOfWindow = endOfWindow - window;
			// MKTODO what if the last frame is larger than the window
			
			ListIterator<DebugFrameInfo> listIterator = frames.listIterator(frames.size());
			while(listIterator.hasPrevious()) {
				var frame = listIterator.previous();
				if(frame.getStartTime() < startOfWindow) {
					break;
				}
				frameTime += frame.getTime();
				frameCount++;
			}
			while(listIterator.hasPrevious()) {
				listIterator.previous();
				listIterator.remove();
			}
			
			System.out.println("frames in list: " + frames);
		}
		updateFrameRateLabel(frameCount, frameTime);
	}
	
	
	
	private List<DebugRootProgressMonitor> pms = new LinkedList<>();
	
	private class InfoNode {
		
		long time;
		
	}
	
	
	
	private class FrameRateTableModel extends AbstractTableModel {
		
		@Override
		public int getRowCount() {
			return 2;
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public String getColumnName(int col) {
			switch(col) {
				case 0:  return "Render";
				case 1:  return "%";
				default: return null;
			}
		}
		
		@Override
		public Class<?> getColumnClass(int col) {
			return String.class;
		}
		
		@Override
		public Object getValueAt(int row, int col) {
			if(col == 0) {
				if(row == 0) {
					return "Nodes:";
				} else if(row == 1) {
					return "Edges";
				}
				
			} else if(col == 1) {
				return "99%";
			}
			return null;
		}
	}
	

}
