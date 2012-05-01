package org.cytoscape.work.internal.tunables;


import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.internal.tunables.utils.GUIDefaults;
import org.cytoscape.work.swing.AbstractGUITunableHandler;


/**
 * Handler for the type <i>Boolean</i> of <code>Tunable</code>
 *
 * @author pasteur
 */
public class BooleanHandler extends AbstractGUITunableHandler implements ActionListener {
	private JCheckBox checkBox;
	private boolean horizontal = false;

	/**
	 * Constructs the <code>GUIHandler</code> for the <code>Boolean</code> type
	 *
	 * It creates the Swing component for this Object (JCheckBox) with its description/initial state,  and displays it
	 *
	 * @param f field that has been annotated
	 * @param o object contained in <code>f</code>
	 * @param t tunable associated to <code>f</code>
	 */
	public BooleanHandler(Field f, Object o, Tunable t) {
		super(f, o, t);
		init();
	}

	public BooleanHandler(final Method getter, final Method setter, final Object instance, final Tunable tunable) {
		super(getter, setter, instance, tunable);
		init();
	}

	private void init() {
		//setup GUI
		panel = new JPanel(new BorderLayout(GUIDefaults.hGap, GUIDefaults.vGap));
		checkBox = new JCheckBox();
		checkBox.setSelected(getBoolean());
		JLabel label = new JLabel(getDescription());
		label.setFont(new Font(null, Font.PLAIN, 12));
		label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		checkBox.addActionListener(this);

		if (horizontal) {
			panel.add(label, BorderLayout.NORTH);
			panel.add(checkBox, BorderLayout.SOUTH);
		} else {
			panel.add(label, BorderLayout.WEST);
			panel.add(checkBox, BorderLayout.EAST);
		}
	}

	private boolean getBoolean() {
		try {
			return (Boolean)getValue();
		} catch (final Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	
	public void update(){
		boolean b;
		try{
			b = (Boolean) getValue();
			checkBox.setSelected(b);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * To set the current value represented in the <code>GUIHandler</code> (in a <code>JCheckBox</code>)to the value of this <code>Boolean</code> object
	 */
	public void handle() {
		try {
			final Boolean setting = checkBox.isSelected();
			setValue(setting);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * To get the state of the value of the <code>BooleanHandler</code> : <code>true</code> or <code>false</code>
	 */
	public String getState() {
		return String.valueOf(checkBox.isSelected());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		handle();		
	}
}
