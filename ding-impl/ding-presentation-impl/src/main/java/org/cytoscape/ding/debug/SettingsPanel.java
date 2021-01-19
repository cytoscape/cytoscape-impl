package org.cytoscape.ding.debug;

import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.event.DebounceTimer;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.PropertyUpdatedEvent;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.BasicCollapsiblePanel;
import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")
public class SettingsPanel extends BasicCollapsiblePanel  {

	private final CyServiceRegistrar registrar;
	private final CyProperty<Properties> cyProps;
	
	private final DebounceTimer debounceTimer = new DebounceTimer();
	
	private PropEditor prop1;
	private PropEditor prop2;
	private PropEditor prop3;
	private PropEditor prop4;
	private PropEditor prop5;
	private PropEditor prop6;
	
	
	@SuppressWarnings("unchecked")
	public SettingsPanel(CyServiceRegistrar registrar) {
		super("Settings");
		this.registrar = registrar;
		this.cyProps = registrar.getService(CyProperty.class, "(cyPropertyName=cytoscape3.props)");
		createContents();
	}
	
	private void createContents() {
		prop1 = new NumberPropEditor("render.coarseDetailThreshold");
		prop2 = new NumberPropEditor("render.nodeBorderThreshold");
		prop3 = new NumberPropEditor("render.nodeLabelThreshold");
		prop4 = new NumberPropEditor("render.edgeArrowThreshold");
		prop5 = new NumberPropEditor("render.edgeLabelThreshold");
		prop6 = new BooleanPropEditor("render.edgeBufferPan");
		
		
		JPanel panel = new JPanel();
		panel.setOpaque(false);
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
			.addGroup(layout.createParallelGroup()
				.addComponent(prop1.getLabel())
				.addComponent(prop2.getLabel())
				.addComponent(prop3.getLabel())
				.addComponent(prop4.getLabel())
				.addComponent(prop5.getLabel())
				.addComponent(prop6.getLabel())
			)
			.addGroup(layout.createParallelGroup()
				.addComponent(prop1.getEditor())
				.addComponent(prop2.getEditor())
				.addComponent(prop3.getEditor())
				.addComponent(prop4.getEditor())
				.addComponent(prop5.getEditor())
				.addComponent(prop6.getEditor())
			)
		);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(prop1.getLabel())
				.addComponent(prop1.getEditor(), PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE)
			)
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(prop2.getLabel())
				.addComponent(prop2.getEditor(), PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE)
			)
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(prop3.getLabel())
				.addComponent(prop3.getEditor(), PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE)
			)
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(prop4.getLabel())
				.addComponent(prop4.getEditor(), PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE)
			)
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(prop5.getLabel())
				.addComponent(prop5.getEditor(), PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE)
			)
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(prop6.getLabel())
				.addComponent(prop6.getEditor(), PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE)
			)
		);
		
		JPanel content = getContentPane();
		content.setLayout(new BorderLayout());
		content.add(BorderLayout.WEST, panel);
	}
	
	public void update() {
		prop1.update();
		prop2.update();
		prop3.update();
		prop4.update();
		prop5.update();
		prop6.update();
	}
	
	
	
	private void setProperty(String propName, String value) {
		cyProps.getProperties().setProperty(propName, value);
		// Need to fire this event for the renderer to see the changes
		CyEventHelper eventHelper = registrar.getService(CyEventHelper.class);
		eventHelper.fireEvent(new PropertyUpdatedEvent(cyProps));
	}
	
	
	private interface PropEditor {
		JComponent getLabel();
		JComponent getEditor();
		void update();
	}
	
	
	private class NumberPropEditor implements PropEditor, ChangeListener {
		
		private final String propName;
		private final SpinnerNumberModel model;
		private final JLabel label;
		private final JSpinner spinner;
		
		public NumberPropEditor(String propName) {
			this.propName = propName;
			label = new JLabel(propName);
			int value = getPropValue();
			model = new SpinnerNumberModel(value, 0, 1000000, 100);
			spinner = new JSpinner(model);
			spinner.addChangeListener(this);
			LookAndFeelUtil.makeSmall(label, spinner);
		}
		
		@Override
		public JComponent getLabel() {
			return label;
		}
		
		@Override
		public JComponent getEditor() {
			return spinner;
		}
		
		@Override
		public void update() {
			spinner.removeChangeListener(this);
			model.setValue(getPropValue());
			spinner.addChangeListener(this);
		}

		private int getPropValue() {
			return Integer.parseInt(cyProps.getProperties().getProperty(propName));
		}
		
		@Override
		public void stateChanged(ChangeEvent e) {
			debounceTimer.debounce(() -> {
				int v = model.getNumber().intValue();
				setProperty(propName, Integer.toString(v));
			});
		}
	}
	
	
	private class BooleanPropEditor implements PropEditor, ActionListener {
		
		private final String propName;
		private final JLabel label;
		private final JCheckBox checkBox;
		
		public BooleanPropEditor(String propName) {
			this.propName = propName;
			label = new JLabel(propName);
			checkBox = new JCheckBox();
			boolean value = getPropValue();
			checkBox.setSelected(value);
			checkBox.addActionListener(this);
			LookAndFeelUtil.makeSmall(label, checkBox);
		}
		
		@Override
		public JComponent getLabel() {
			return label;
		}
		
		@Override
		public JComponent getEditor() {
			return checkBox;
		}
		
		@Override
		public void update() {
			checkBox.removeActionListener(this);
			checkBox.setSelected(getPropValue());
			checkBox.addActionListener(this);
		}

		private boolean getPropValue() {
			return Boolean.valueOf(cyProps.getProperties().getProperty(propName));
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			debounceTimer.debounce(() -> {
				boolean v = checkBox.isSelected();
				setProperty(propName, String.valueOf(v));
			});
		}
	}

}
