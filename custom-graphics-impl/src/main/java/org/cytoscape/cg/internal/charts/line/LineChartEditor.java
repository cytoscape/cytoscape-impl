package org.cytoscape.cg.internal.charts.line;

import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.cytoscape.cg.internal.charts.AbstractChartEditor;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")
public class LineChartEditor extends AbstractChartEditor<LineChart> {

	private JLabel lineWidthLbl;
	private JTextField lineWidthTxt;
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public LineChartEditor(LineChart chart, CyServiceRegistrar serviceRegistrar) {
		super(chart, Number.class, true, true, false, true, true, false, true, true, serviceRegistrar);
		
		getBorderPnl().setVisible(false);
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================

	// ==[ PRIVATE METHODS ]============================================================================================
	
	@Override
	protected void createLabels() {
		super.createLabels();
		lineWidthLbl = new JLabel("Line Width:");
	}
	
	@Override
	protected JPanel getOtherAdvancedOptionsPnl() {
		var p = super.getOtherAdvancedOptionsPnl();
		p.setVisible(true);
		
		var layout = new GroupLayout(p);
		p.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
				.addGroup(layout.createSequentialGroup()
						.addComponent(lineWidthLbl)
						.addComponent(getLineWidthTxt(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE))
				);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(lineWidthLbl)
						.addComponent(getLineWidthTxt()))
				);
		
		return p;
	}
	
	private JTextField getLineWidthTxt() {
		if (lineWidthTxt == null) {
			lineWidthTxt = new JTextField("" + chart.get(LineChart.LINE_WIDTH, Float.class, 1.0f));
			lineWidthTxt.setInputVerifier(new DoubleInputVerifier());
			lineWidthTxt.setPreferredSize(new Dimension(40, lineWidthTxt.getMinimumSize().height));
			lineWidthTxt.setHorizontalAlignment(JTextField.TRAILING);
			
			lineWidthTxt.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					try {
			            float width = Double.valueOf(lineWidthTxt.getText().trim()).floatValue();
			            chart.set(LineChart.LINE_WIDTH, width);
			        } catch (NumberFormatException ex) {
			        }
				}
			});
		}
		
		return lineWidthTxt;
	}
}
