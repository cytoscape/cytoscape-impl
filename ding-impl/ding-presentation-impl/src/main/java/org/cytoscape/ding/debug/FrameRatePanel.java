package org.cytoscape.ding.debug;

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

import org.cytoscape.util.swing.BasicCollapsiblePanel;
import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")
public class FrameRatePanel extends BasicCollapsiblePanel {

	private JLabel frameRateLabel;
	private JTable table;
	
	private LinkedList<DebugRootFrameInfo> frames = new LinkedList<>();
	private Timer timer;
	
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
		scrollPane.setPreferredSize(new Dimension(400, 200));
		
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
		double framesPerSecondRaw = (double)frameCount / ((double)frameTime / 1000.0);
		frameRateLabel.setText(String.format("Frame Rate: %.2f per sec", framesPerSecondRaw));
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
		long frameTime = 0;
		int frameCount = 0;
		
		List<DebugFrameInfo> windowFrames = new ArrayList<>();
		synchronized(frames) {
			if(frames.isEmpty())
				return;
			
			long endOfWindow = frames.getLast().getEndTime();
			long startOfWindow = endOfWindow - window;
			// MKTODO what if the last frame is larger than the window
			
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
		}
		updateFrameRateLabel(frameCount, frameTime);
		DebugFrameInfo root = DebugFrameInfo.merge(windowFrames);
		
		TableModel model = createTabelModel(root);
		table.setModel(model);
		table.getColumnModel().getColumn(0).setPreferredWidth(200);
		table.getColumnModel().getColumn(1).setPreferredWidth(100);
		table.getColumnModel().getColumn(2).setPreferredWidth(100);
	}
	
	
	private TableModel createTabelModel(DebugFrameInfo frame) {
		String[] columnNames = {"Render", "Time/" + window + "ms", "%"};
		Object[][] data = computeFrameData(frame);
		DefaultTableModel model = new DefaultTableModel(data, columnNames);
		return model;
	}
	
	private Object[][] computeFrameData(DebugFrameInfo root) {
		int rows = DebugUtil.countNodesInTree(root, t -> t.getSubFrames());
		Object[][] data = new Object[rows + 1][];
		
		double percent = ((double)(window - root.getTime()) / (double)window)  * 100.0;
		String percentText = String.format("%.1f", percent);
		
		data[0] = new Object[] { "Idle/Overhead", window - root.getTime(), percentText };
		flattenAndExtract(1, 0, data, null, root);
		return data;
	}
	
	private int flattenAndExtract(int i, int depth, Object[][] data, DebugFrameInfo parent, DebugFrameInfo frame) {
		data[i] = getDataForRow(parent, frame, depth);
		for(DebugFrameInfo child : frame.getSubFrames()) {
			i = flattenAndExtract(++i, depth+1, data, frame, child);
		}
		return i;
	}
	
	private Object[] getDataForRow(DebugFrameInfo parent, DebugFrameInfo frame, int depth) {
		String indent = "  ".repeat(depth);
		String name = indent + frame.getTask();
		String time = indent + frame.getTime();
		
		double percent;
		if(parent == null) { // root frame
			percent = ((double)frame.getTime() / (double)window)  * 100.0;
		} else {
			percent = ((double)frame.getTime() / (double)parent.getTime()) * 100.0;
		}
		String percentText = indent + String.format("%.1f", percent);
		
		return new Object[] { name, time, percentText };
	}
	
}
