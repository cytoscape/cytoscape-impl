package org.cytoscape.internal.prefs.lib;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.cytoscape.view.presentation.property.BasicVisualLexicon;

public class CheckList extends JList<JCheckBox> {
	private static final long serialVersionUID = 1L;
	public CheckList()
	{
		super(new DefaultListModel<JCheckBox>());
		setCellRenderer(new CheckboxListCellRenderer());
//		MyMouseAdaptor myMouseAdaptor = new MyMouseAdaptor(this);
//		addMouseListener(myMouseAdaptor);
//		addMouseMotionListener(myMouseAdaptor);
	}
	public CheckList(String[] names)
	{
		this();
		DefaultListModel<JCheckBox> model = (DefaultListModel<JCheckBox>) getModel();
		setCellRenderer(new CheckboxListCellRenderer());
		for (String line : names)
		{
			JCheckBox ck = new JCheckBox(line);
			model.addElement(ck);
		}
	}
	public class CheckboxListCellRenderer extends JCheckBox implements ListCellRenderer {

	    public Component getListCellRendererComponent(JList list, Object value, int index, 
	            boolean isSelected, boolean cellHasFocus) {

	        setComponentOrientation(list.getComponentOrientation());
	        setFont(list.getFont());
	        setBackground(list.getBackground());
	        setForeground(list.getForeground());
	        setSelected(isSelected);
	        setEnabled(list.isEnabled());
	        	if (value instanceof JCheckBox)
			setText(((JCheckBox)value).getText());
			else
	        setText(value == null ? "" : value.toString());  

	        return this;
	    }
	}	static String SPACE = " ";
	public void setValues(String valList, String prefix)
	{
		DefaultListModel<JCheckBox> model = (DefaultListModel<JCheckBox>) getModel();
		String[] lines = valList.split(",");
		for (String line : lines)
		{
//			String userString = BasicVisualLexicon.lookup(line);
			
//			if (line.startsWith(prefix))
//				line = SPACE + line.substring(prefix.length());
//			JCheckBox ck = new JCheckBox(line);
//			model.addElement(ck);
		}
	}
	public String getValues(String prefix)
	{
		DefaultListModel<JCheckBox> model = (DefaultListModel<JCheckBox>) getModel();
		JCheckBox[] boxes = (JCheckBox[]) model.toArray();
		StringBuilder builder = new StringBuilder();
		for (JCheckBox s : boxes)
		{
			if (s.isSelected())
			{
				String str = s.getText();
				if (str.startsWith(SPACE))
					str = str.replace(SPACE , prefix);
				builder.append(str + ",");
			}
		}
		int len = builder.length();
		if (len == 0)  return "";
		builder.setLength(builder.length() - 1);
		return builder.toString();
	}
}

