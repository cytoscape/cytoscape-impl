package org.cytoscape.view.table.internal.equation;

import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import java.util.Collection;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")
public class ItemListPanel<T> extends JPanel {

	private final String title;

	private JPanel topPanel;
	private JList<T> list;
	private JScrollPane scrollPane;
	
	public ItemListPanel(String title) {
		this.title = title;
		init();
	}
	
	private void init() {
		setOpaque(!isAquaLAF());
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(getTopPanel())
				.addComponent(getScrollPane()));
		layout.setHorizontalGroup(layout.createParallelGroup()
				.addComponent(getTopPanel())
				.addComponent(getScrollPane()));
	}
	
	public void setElements(Collection<T> items) {
		var model = (DefaultListModel<T>)getList().getModel();
		model.addAll(items);
	}
	
	public void clearSelection() {
		getList().clearSelection();
	}
	
	public T getSelectedValue() {
		return getList().getSelectedValue();
	}
	
	private JPanel getTopPanel() {
		if(topPanel == null) {
			JLabel label = new JLabel(title);
			LookAndFeelUtil.makeSmall(label);
			topPanel = new JPanel();
			topPanel.setOpaque(!LookAndFeelUtil.isAquaLAF()); // Transparent if Aqua
			topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
			topPanel.add(label);
			topPanel.add(Box.createHorizontalGlue());
		}
		return topPanel;
	}
	
	private JScrollPane getScrollPane() {
		if(scrollPane == null) {
			scrollPane = new JScrollPane(getList());
			scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		}
		return scrollPane;
	}
	
	public JList<T> getList() {
		if(list == null) {
			list = new JList<>(new DefaultListModel<>());
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		}
		return list;
	}
	
	
	
}
