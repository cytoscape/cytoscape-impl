package org.cytoscape.ding.debug;

import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.BorderLayout;
import java.util.Properties;

import javax.swing.GroupLayout;
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
	
	
	@SuppressWarnings("unchecked")
	public SettingsPanel(CyServiceRegistrar registrar) {
		super("Settings");
		this.registrar = registrar;
		this.cyProps = registrar.getService(CyProperty.class, "(cyPropertyName=cytoscape3.props)");
		createContents();
	}
	
	private void createContents() {
		prop1 = new PropEditor("render.coarseDetailThreshold");
		prop2 = new PropEditor("render.nodeBorderThreshold");
		prop3 = new PropEditor("render.nodeLabelThreshold");
		prop4 = new PropEditor("render.edgeArrowThreshold");
		prop5 = new PropEditor("render.edgeLabelThreshold");
		
		JPanel panel = new JPanel();
		panel.setOpaque(false);
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
			.addGroup(layout.createParallelGroup()
				.addComponent(prop1.label)
				.addComponent(prop2.label)
				.addComponent(prop3.label)
				.addComponent(prop4.label)
				.addComponent(prop5.label)
			)
			.addGroup(layout.createParallelGroup()
				.addComponent(prop1.spinner)
				.addComponent(prop2.spinner)
				.addComponent(prop3.spinner)
				.addComponent(prop4.spinner)
				.addComponent(prop5.spinner)
			)
		);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addGroup(layout.createParallelGroup()
				.addComponent(prop1.label)
				.addComponent(prop1.spinner, PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE)
			)
			.addGroup(layout.createParallelGroup()
				.addComponent(prop2.label)
				.addComponent(prop2.spinner, PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE)
			)
			.addGroup(layout.createParallelGroup()
				.addComponent(prop3.label)
				.addComponent(prop3.spinner, PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE)
			)
			.addGroup(layout.createParallelGroup()
				.addComponent(prop4.label)
				.addComponent(prop4.spinner, PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE)
			)
			.addGroup(layout.createParallelGroup()
				.addComponent(prop5.label)
				.addComponent(prop5.spinner, PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE)
			)
		);
		
		JPanel content = getContentPane();
		content.setLayout(new BorderLayout());
		content.add(BorderLayout.WEST, panel);
	}
	
	
	private void setProperty(String propName, String value) {
		cyProps.getProperties().setProperty(propName, value);
		// Need to fire this event for the renderer to see the changes
		CyEventHelper eventHelper = registrar.getService(CyEventHelper.class);
		eventHelper.fireEvent(new PropertyUpdatedEvent(cyProps));
	}
	
	
	public void update() {
		prop1.update();
		prop2.update();
		prop3.update();
		prop4.update();
		prop5.update();
	}
	
	
	private class PropEditor implements ChangeListener {
		
		private final String propName;
		private final SpinnerNumberModel model;
		public final JLabel label;
		public final JSpinner spinner;
		
		public PropEditor(String propName) {
			this.propName = propName;
			label = new JLabel(propName);
			int value = getPropValue();
			model = new SpinnerNumberModel(value, 0, 1000000, 100);
			spinner = new JSpinner(model);
			spinner.addChangeListener(this);
			LookAndFeelUtil.makeSmall(label, spinner);
		}
		
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

}
