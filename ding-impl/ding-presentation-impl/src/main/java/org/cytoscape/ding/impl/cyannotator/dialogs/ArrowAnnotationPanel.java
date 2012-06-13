package org.cytoscape.ding.impl.cyannotator.dialogs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.AbstractListModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.awt.Component;

import org.cytoscape.ding.impl.cyannotator.api.ArrowAnnotation;
import org.cytoscape.ding.impl.cyannotator.api.ArrowAnnotation.ArrowType;
import org.cytoscape.ding.impl.cyannotator.api.ArrowAnnotation.ArrowEnd;
import org.cytoscape.ding.impl.cyannotator.annotations.ArrowAnnotationImpl;

public class ArrowAnnotationPanel extends javax.swing.JPanel {
	private int WIDTH = 500;
	private int HEIGHT = 475;
	private int TOP = 10;
	private int LEFT = 10;
	private int COLUMN1 = 175;
	private int COLUMN2 = 250;
	private int RIGHT = WIDTH-10;
	private int ARROWHEIGHT = 175;

	private ArrowAnnotation preview;
	private PreviewPanel previewPanel;
	
	private ArrowAnnotation mAnnotation;

	public ArrowAnnotationPanel(ArrowAnnotation mAnnotation, PreviewPanel previewPanel, int width, int height) {
		this.mAnnotation=mAnnotation;
		this.previewPanel = previewPanel;
		this.preview=(ArrowAnnotation)previewPanel.getPreviewAnnotation();
		this.WIDTH = width;
		this.HEIGHT = height;
		initComponents();
		setSize(width,height);
	}

	private void initComponents() {

		setMaximumSize(new Dimension(WIDTH, HEIGHT));
		setMinimumSize(new Dimension(WIDTH, HEIGHT));
		setLayout(null);
		setBorder(BorderFactory.createLoweredBevelBorder());

		// Upper left components
		//
		int y = TOP;

		final JCheckBox lineColor = new JCheckBox();
		final JButton sLineColorButton = new JButton();
		final JSlider lineOValue = new JSlider(0, 100);

		// Line color
		{
			lineColor.setText("Line Color");
			if (mAnnotation.getLineColor() != null) 
				lineColor.setSelected(true);

			lineColor.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					if(lineColor.isSelected()) {
						sLineColorButton.setEnabled(true);
					} else {
						sLineColorButton.setEnabled(false);
						preview.setLineColor(null);
					}
					previewPanel.repaint();
				}
			});		
			add(lineColor);
			lineColor.setBounds(LEFT, y, lineColor.getPreferredSize().width, 20);

			sLineColorButton.setText("Select Line Color");
			if(lineColor.isSelected())
				sLineColorButton.setEnabled(true);
			else
				sLineColorButton.setEnabled(false);

			sLineColorButton.setBounds(COLUMN2, y, sLineColorButton.getPreferredSize().width, 20);
			sLineColorButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
			    sLineColorButtonActionPerformed(evt, lineOValue.getValue());
				}
			});	  

			add(sLineColorButton);
		}

		// Line opacity
		{
			y += 25;
			final JLabel lineOLabel = new JLabel("Line Opacity");
			lineOLabel.setBounds(LEFT, y, lineOLabel.getPreferredSize().width, 20);
			add(lineOLabel);

			lineOValue.setMajorTickSpacing(100);
			lineOValue.setPaintTicks(true);
			lineOValue.setPaintLabels(true);
			lineOValue.setValue(100);
			lineOValue.setBounds(COLUMN2, y, RIGHT-COLUMN2, lineOValue.getPreferredSize().height);
			if (lineColor.isSelected())
				lineOValue.setEnabled(true);
			else
				lineOValue.setEnabled(false);
			lineOValue.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent evt) {
					preview.setLineColor(mixColor(preview.getLineColor(),lineOValue.getValue()));
					previewPanel.repaint();
				}
			});

			add(lineOValue);
		}

		// Line width
		{
			y = y+50;
			JLabel jLabel6 = new JLabel();
			jLabel6.setText("Line Thickness");
			jLabel6.setBounds(LEFT, y, jLabel6.getPreferredSize().width, 14);
			add(jLabel6);

			final JComboBox eThickness = new JComboBox();
			eThickness.setModel(new DefaultComboBoxModel(new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13" }));
			eThickness.setSelectedIndex(1);
			for(int i=0;i<eThickness.getModel().getSize();i++){
				if( ((int)mAnnotation.getLineWidth())==Integer.parseInt((String)eThickness.getModel().getElementAt(i)) ){
					eThickness.setSelectedIndex(i);
				break;
				}
			}
			eThickness.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
						preview.setLineWidth( Integer.parseInt( (String)(eThickness.getModel().getSelectedItem()) ) );
						previewPanel.repaint();
				}
			});	 
			eThickness.setBounds(COLUMN2, y, 42, 20);
			add(eThickness);
		}

		y += 25;
		JPanel sourcePanel = getArrowPanel(ArrowEnd.SOURCE);
		add(sourcePanel);
		// sourcePanel.setLocation(LEFT, y);
		sourcePanel.setBounds(LEFT, y, WIDTH-15, ARROWHEIGHT);
		
		y += ARROWHEIGHT+10;
		JPanel targetPanel = getArrowPanel(ArrowEnd.TARGET);
		add(targetPanel);
		// targetPanel.setLocation(LEFT, y);
		targetPanel.setBounds(LEFT, y, WIDTH-15, ARROWHEIGHT);
	}
	
	public ArrowAnnotation getPreview() {
		return preview;
	}

	private JPanel getArrowPanel(final ArrowEnd end) {
		// Source arrow
		JPanel arrowPanel = new JPanel();
		arrowPanel.setLayout(null);
		TitledBorder title = BorderFactory.createTitledBorder("Source Arrow");
		if (end == ArrowEnd.TARGET)
			title = BorderFactory.createTitledBorder("Target Arrow");
		arrowPanel.setBorder(title);

		int arrowY = 30;

		// Arrow label
		{
			JLabel jLabel5 = new JLabel();
			jLabel5.setFont(new Font("Tahoma", 1, 12));
			jLabel5.setText("Arrow Type:");
			jLabel5.setBounds(LEFT, arrowY, jLabel5.getPreferredSize().width, 20);
			arrowPanel.add(jLabel5);
		}

		// Arrow list
		{
			final JComboBox arrowList = new JComboBox();
			arrowList.setModel(new DefaultComboBoxModel(mAnnotation.getSupportedArrows()));
			arrowList.setSelectedItem(mAnnotation.getArrowType(end));
			arrowList.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
						preview.setArrowType(end, (ArrowType)arrowList.getSelectedItem());
						previewPanel.repaint();
				}
			});		    
			arrowList.setBounds(COLUMN2, arrowY, 125, 20);
			arrowPanel.add(arrowList);
		}

		arrowY += 25;

		// Arrow color
		final JCheckBox arrowColor = new JCheckBox();
		final JButton sArrowColorButton = new JButton();
		final JSlider arrowOValue = new JSlider(0, 100);
		{
			arrowColor.setText("Arrow Color");
			if (mAnnotation.getArrowColor(end) != null) 
				arrowColor.setSelected(true);
			arrowColor.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					if(arrowColor.isSelected()) {
						sArrowColorButton.setEnabled(true);
					} else {
						sArrowColorButton.setEnabled(false);
						preview.setArrowColor(end, null);
					}
				}
			});		
			arrowPanel.add(arrowColor);
			arrowColor.setBounds(LEFT, arrowY, arrowColor.getPreferredSize().width, 20);

			if (end == ArrowEnd.SOURCE)
				sArrowColorButton.setText("Select Source Arrow Color");
			else
				sArrowColorButton.setText("Select Target Arrow Color");

			if(arrowColor.isSelected())
				sArrowColorButton.setEnabled(true);
			else
				sArrowColorButton.setEnabled(false);

			sArrowColorButton.setBounds(COLUMN2, arrowY, sArrowColorButton.getPreferredSize().width, 20);
			sArrowColorButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
			    sArrowColorButtonActionPerformed(evt, end, arrowOValue.getValue());
				}
			});	  
	
			arrowPanel.add(sArrowColorButton);
		}


		// Arrow opacity
		{
			arrowY += 25;
			JLabel fillOLabel = new JLabel("Arrowhead Opacity");
			fillOLabel.setBounds(LEFT, arrowY, fillOLabel.getPreferredSize().width, 20);
			arrowPanel.add(fillOLabel);

			arrowOValue.setMajorTickSpacing(100);
			arrowOValue.setPaintTicks(true);
			arrowOValue.setPaintLabels(true);
			arrowOValue.setValue(100);
			arrowOValue.setBounds(COLUMN2, arrowY, RIGHT-COLUMN2-20, arrowOValue.getPreferredSize().height);
			arrowOValue.setEnabled(false);
			arrowOValue.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent evt) {
					preview.setArrowColor(end, mixColor(preview.getArrowColor(end),arrowOValue.getValue()));
					previewPanel.repaint();
				}
			});
			arrowPanel.add(arrowOValue);
			
		}	

		// Arrow size
		{
			arrowY += 50;
			JLabel jLabel6 = new JLabel();
			jLabel6.setText("Arrow Size");
			jLabel6.setBounds(LEFT, arrowY, jLabel6.getPreferredSize().width, 14);
			arrowPanel.add(jLabel6);

			final JComboBox aSize = new JComboBox();
			aSize.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13" }));
			aSize.setSelectedIndex(1);
			for(int i=0;i<aSize.getModel().getSize();i++){
				if( ((int)mAnnotation.getArrowSize(end))==Integer.parseInt((String)aSize.getModel().getElementAt(i)) ){
					aSize.setSelectedIndex(i);
				break;
				}
			}
			aSize.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
						preview.setArrowSize(end, Integer.parseInt(aSize.getModel().getSelectedItem().toString()));
						previewPanel.repaint();
				}
			});	 
			aSize.setBounds(COLUMN2, arrowY, 42, 20);
			arrowPanel.add(aSize);
		}

		return arrowPanel;
	}
	
	private void sArrowColorButtonActionPerformed(ActionEvent evt, final ArrowEnd end, final int opacity) {
		//Set Line Color Button
		final SelectColor sLineSelectColor=new SelectColor(mAnnotation.getArrowColor(end));
		sLineSelectColor.setOKListener( new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				Color clr = sLineSelectColor.getColor();
				preview.setArrowColor(end, mixColor(clr,opacity));
				previewPanel.repaint();
			}
		});
	
		sLineSelectColor.setVisible(true);
		sLineSelectColor.setSize(435, 420);
	}

	private void sLineColorButtonActionPerformed(ActionEvent evt, final int opacity) {
		//Set Line Color Button
		final SelectColor sLineSelectColor=new SelectColor(mAnnotation.getLineColor());
		sLineSelectColor.setOKListener( new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				Color clr = sLineSelectColor.getColor();
				preview.setLineColor(mixColor(clr,opacity));
				previewPanel.repaint();
			}
		});
	
		sLineSelectColor.setVisible(true);
		sLineSelectColor.setSize(435, 420);
	}

	private Paint mixColor(Paint p, int value) {
		if (p == null || !(p instanceof Color)) return p;
		Color c = (Color)p;
		return new Color(c.getRed(), c.getGreen(), c.getBlue(), value*255/100);
	}

}

