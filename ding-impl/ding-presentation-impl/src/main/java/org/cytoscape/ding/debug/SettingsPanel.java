package org.cytoscape.ding.debug;

import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.DingRenderer;
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
	
	private List<PropEditor> props;
	private JButton cacheStatsButton;
	
	
	@SuppressWarnings("unchecked")
	public SettingsPanel(CyServiceRegistrar registrar) {
		super("Settings");
		this.registrar = registrar;
		this.cyProps = registrar.getService(CyProperty.class, "(cyPropertyName=cytoscape3.props)");
		createContents();
	}
	
	
	private void createContents() {
		props = Arrays.asList(
			new NumberPropEditor("render.coarseDetailThreshold", "coarseDetailThreshold"),
			new NumberPropEditor("render.nodeBorderThreshold", "nodeBorderThreshold"),
			new NumberPropEditor("render.nodeLabelThreshold", "nodeLabelThreshold"),
			new NumberPropEditor("render.edgeArrowThreshold", "edgeArrowThreshold"),
			new NumberPropEditor("render.edgeLabelThreshold", "edgeLabelThreshold"),
			new BooleanPropEditor("render.edgeBufferPan", "edgeBufferPan"),
			new BooleanPropEditor("render.labelCache", "labelCache"),
			new BooleanPropEditor("render.selectedOnly", "selectedOnly"),
			new BooleanPropEditor("render.hidpi", "hidpi")
		);
		
		cacheStatsButton = new JButton("show stats");
		cacheStatsButton.addActionListener(e-> showCacheStats());
		LookAndFeelUtil.makeSmall(cacheStatsButton);
		
		JPanel panel = new JPanel();
		panel.setOpaque(false);
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		ParallelGroup horizontalLabelGroup = layout.createParallelGroup();
		ParallelGroup horizontalEditorGroup = layout.createParallelGroup();
		SequentialGroup verticalGroup = layout.createSequentialGroup();
		
		for(PropEditor prop : props) {
			horizontalLabelGroup.addComponent(prop.getLabel());
			horizontalEditorGroup.addComponent(prop.getEditor());
			verticalGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(prop.getLabel())
				.addComponent(prop.getEditor(), PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE)
			);
		}
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
			.addGroup(horizontalLabelGroup)
			.addGroup(horizontalEditorGroup)
			.addGroup(layout.createParallelGroup().addComponent(cacheStatsButton))
		);
		
		layout.setVerticalGroup(verticalGroup);
		verticalGroup.addComponent(cacheStatsButton);
		
		JPanel content = getContentPane();
		content.setLayout(new BorderLayout());
		content.add(BorderLayout.WEST, panel);
	}
	
	
	public void update() {
		props.forEach(PropEditor::update);
	}
	
	
	private void showCacheStats() {
		var renderer = registrar.getService(DingRenderer.class);
		var appManager = registrar.getService(CyApplicationManager.class);
		
		var netView = appManager.getCurrentNetworkView();
		if(netView == null)
			return;
		
		DRenderingEngine re = renderer.getRenderingEngine(netView);
		var labelCache = re.getLabelCache();
		String stats = labelCache.getStats();
		
		JOptionPane.showMessageDialog(this, stats);
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
		
		public NumberPropEditor(String propName, String labelText) {
			this.propName = propName;
			label = new JLabel(labelText);
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
		
		public BooleanPropEditor(String propName, String labelText) {
			this.propName = propName;
			label = new JLabel(labelText);
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
