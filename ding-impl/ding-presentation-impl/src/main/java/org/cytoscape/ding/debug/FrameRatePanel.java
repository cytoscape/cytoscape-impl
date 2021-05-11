package org.cytoscape.ding.debug;

import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.cytoscape.ding.impl.canvas.SelectionCanvas;
import org.cytoscape.util.swing.BasicCollapsiblePanel;
import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")
public class FrameRatePanel extends BasicCollapsiblePanel {

	private JLabel frameRateLabel;
	private JTable table;
	
	private LinkedList<DebugRootFrameInfo> frames = new LinkedList<>();
	private Timer timer;
	
	private DebugFrameInfo root;
	private int lastProcessFrameNumber = 0;
	private double lastFrameRate = 0;
	
	private final long window = 5000; // five seconds
	
	public FrameRatePanel() {
		super("Frame Rate");
		createContents();
		timer = new Timer(1000, e -> updateFrameRate());
		timer.setRepeats(true);
	}

	
	private void createContents() {
		frameRateLabel = new JLabel("Frame Rate: ");
		LookAndFeelUtil.makeSmall(frameRateLabel);
		table = new JTable();
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
			.addComponent(scrollPane, PREFERRED_SIZE, PREFERRED_SIZE, Short.MAX_VALUE)
		);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addComponent(frameRateLabel)
			.addComponent(scrollPane)
		);
		
		
		JPanel content = getContentPane();
		content.setLayout(new BorderLayout());
		content.add(BorderLayout.CENTER, panel);
	}
	
	private double calcFrameRate(int frameCount, long frameTime) {
		return (double)frameCount / ((double)frameTime / 1000.0);
	}
	
	private void updateFrameRateLabel(double frameRate) {
		frameRateLabel.setText(String.format("Frame Rate: %.2f per sec", frameRate));
	}
	
	
	public void addFrame(DebugRootFrameInfo frame) {
		synchronized(frames) {
			// MKTODO maybe there's a better way to handle cancelled frames
			if(frame.getType() == DebugFrameType.MAIN_FAST && !frame.isCancelled()) {
				frames.addLast(frame);
			}
		}
		if(!timer.isRunning()) {
			timer.start();
		}
	}
	
	private void updateFrameRate() {
		List<DebugFrameInfo> windowFrames = new ArrayList<>();
		synchronized(frames) {
			if(frames.isEmpty())
				return;
			
			int currentFrameNumber = frames.getLast().getFrameNumber();
			if(lastProcessFrameNumber == 0 || currentFrameNumber != lastProcessFrameNumber) {
				lastProcessFrameNumber = currentFrameNumber;
				
				long endOfWindow = frames.getLast().getEndTime();
				long startOfWindow = endOfWindow - window;
				// MKTODO what if the last frame is larger than the window
				
				long frameTime = 0;
				int frameCount = 0;
				
				ListIterator<DebugRootFrameInfo> listIterator = frames.listIterator(frames.size());
				
				while(listIterator.hasPrevious()) {
					var frame = listIterator.previous();
					if(frame.getStartTime() < startOfWindow) {
						break;
					}
					windowFrames.add(frame);
					frameTime += frame.getTime();
					frameCount++;
				}
				while(listIterator.hasPrevious()) {
					listIterator.previous();
					listIterator.remove();
				}
				
				root = DebugFrameInfo.merge(windowFrames);
				lastFrameRate = calcFrameRate(frameCount, frameTime);
			}
		}
		
		updateFrameRateLabel(lastFrameRate);
		TableModel model = createTabelModel(root);
		table.setModel(model);
		table.getColumnModel().getColumn(0).setPreferredWidth(150);
		table.getColumnModel().getColumn(1).setPreferredWidth(100);
		table.getColumnModel().getColumn(2).setPreferredWidth(100);
		table.getColumnModel().getColumn(3).setPreferredWidth(100);
	}
	
	
	private TableModel createTabelModel(DebugFrameInfo frame) {
		String[] columnNames = {"Render", "Time/" + window + "ms", "% rel", "% frame"};
		Object[][] data = computeFrameData(frame);
		DefaultTableModel model = new DefaultTableModel(data, columnNames);
		return model;
	}
	
	private Object[][] computeFrameData(DebugFrameInfo root) {
		int rows = DebugUtil.countNodesInTree((DebugFrameInfo)root, t -> t.getSubFrames());
		Object[][] data = new Object[rows + 1 -1][]; // :) add a row for "idle", and remove annotation selection row
		
		double percent = ((double)(window - root.getTime()) / (double)window)  * 100.0;
		String percentText = String.format("%.1f", percent);
		
		data[0] = new Object[] { "Idle/Overhead", window - root.getTime(), percentText, ""};
		flattenAndExtract(1, 0, root.getTime(), data, null, root);
		return data;
	}
	
	private int flattenAndExtract(int i, int depth, long frameTot, Object[][] data, DebugFrameInfo parent, DebugFrameInfo frame) {
		data[i] = getDataForRow(parent, frame, depth, frameTot);
		for(DebugFrameInfo child : frame.getSubFrames()) {
			if(!SelectionCanvas.DEBUG_NAME.equals(child.getTask())) {
				i = flattenAndExtract(++i, depth+1, frameTot, data, frame, child);
			}
		}
		return i;
	}
	
	private Object[] getDataForRow(DebugFrameInfo parent, DebugFrameInfo frame, int depth, long frameTot) {
		String indent = "  ".repeat(depth);
		String name = indent + frame.getTask();
		String time = indent + frame.getTime();
		
		double percentFrame = ((double)frame.getTime() / (double)frameTot)  * 100.0;
		
		double percentRel;
		if(parent == null) { // root frame
			percentRel = ((double)frame.getTime() / (double)window)  * 100.0;;
		} else {
			percentRel = ((double)frame.getTime() / (double)parent.getTime()) * 100.0;
		}
		
		String percentRelText = indent + String.format("%.1f", percentRel);
		String percentFrameText = indent + String.format("%.1f", percentFrame);
		
		return new Object[] { name, time, percentRelText, percentFrameText };
	}
	
}
