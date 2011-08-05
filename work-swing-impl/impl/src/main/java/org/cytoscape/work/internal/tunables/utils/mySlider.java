package org.cytoscape.work.internal.tunables.utils;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;





@SuppressWarnings("serial")
public class mySlider extends JComponent {
    private Number     m_min, m_max, m_value;
    private boolean    m_ignore = false;
    private JSlider    m_slider;
    private JTextField m_field;
	private List<Object>       m_listeners;
	private Number initvalue;
    
    java.text.DecimalFormat df = new java.text.DecimalFormat("##.##");
    
    private Number majortickspace;
    private int m_smin = 0;
    private int m_srange = 100;
    String newline = System.getProperty("line.separator");
    Boolean upper;
    Boolean lower;
    
    
    
    @SuppressWarnings("unchecked")
	public mySlider(String title, Number min, Number max, Number value,Boolean lowerstrict,Boolean upperstrict) {
        m_min    = min;
        m_max    = max;
        m_value  = value;
        upper = upperstrict;
        lower = lowerstrict;
        m_slider = new JSlider();
        m_field  = new JTextField(4);
        m_field.setHorizontalAlignment(JTextField.RIGHT);
        m_listeners = new ArrayList();
        initvalue = value;

        Hashtable labelTable = new Hashtable();
        majortickspace = (max.doubleValue()-min.doubleValue())/5;
        if(m_value instanceof Double || m_value instanceof Float){
        	Float major = new Float(majortickspace.floatValue());
        	
            float i = m_min.floatValue();
	        int j=0;
	        while(i <= m_max.doubleValue()){
	        	JLabel label = new JLabel(df.format(i));
	        	label.setFont(new Font("",Font.BOLD,9));
	        	labelTable.put(j,label);
	        	i+=major;
	        	j+=20;

	        }
	        
        }
        else if(m_value instanceof Long || m_value instanceof Integer){
        	Integer majortick = new Integer(majortickspace.intValue());
        	int i=m_min.intValue();
	        int j=0;
	        while(i <= m_max.intValue()){
	        	JLabel label = new JLabel(df.format(i));
	        	label.setFont(new Font("",Font.BOLD,9));
	        	labelTable.put(j,label);
	        	i+=majortick;
	        	j+=20;   	
	        }
        }
        m_slider.setMajorTickSpacing(20);
        m_slider.setMinorTickSpacing(5);
        m_slider.setLabelTable(labelTable);
        m_slider.setPaintTicks(true);
        m_slider.setPaintLabels(true);
        setSliderValue();
        setFieldValue();
        initUI();
    }
    

    protected void initUI() {
        m_slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if ( m_ignore ) return;
                m_ignore = true;
                // update the value
                m_value = getSliderValue();
                // set text field value
                setFieldValue();
                
                if(upper == true && m_value.doubleValue() == m_max.doubleValue()){
            		m_field.setBackground(Color.red);
                   	JOptionPane.showMessageDialog(null, "Value ("+df.format(m_value.doubleValue())+") can not be equal to upper limit ("+df.format(m_max.doubleValue())+")","Alert",JOptionPane.ERROR_MESSAGE);
                   	m_value = initvalue;
                   	setSliderValue();
                   	setFieldValue();
                   	m_field.setBackground(Color.white);
                   	m_slider.updateUI();
                }
                if(lower == true && m_value.doubleValue() == m_min.doubleValue()){
            		m_field.setBackground(Color.red);
                   	JOptionPane.showMessageDialog(null, "Value ("+df.format(m_value.doubleValue())+") can not be equal to lower limit ("+df.format(m_min.doubleValue())+")","Alert",JOptionPane.ERROR_MESSAGE);
                   	m_value = initvalue;
                   	setSliderValue();
                   	setFieldValue();
                   	m_field.setBackground(Color.white);
                   	m_slider.updateUI();
                }
                
                // fire event
                fireChangeEvent();
                m_ignore = false;
            }
        });
        
//        CaretListener caretupdate = new CaretListener(){
//        	public void caretUpdate(javax.swing.event.CaretEvent e){
//        		JTextField text = (JTextField)e.getSource();
//        		Double test = Double.parseDouble(text.getText());
//        		m_value = test;
//        		setSliderValue();
//        	}
//        };
//        m_field.addCaretListener(caretupdate);

        m_field.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
            	if ( m_ignore ) return;
                m_ignore = true;
                Number v = getFieldValue();
                if ( v != m_value ) {
                    // update the value
                    m_value = v;
                    // set slider value
                    setSliderValue();
                }
                // fire event
                fireChangeEvent();
                m_ignore = false;
            }
        });
//        m_field.addMouseListener(new MouseAdapter() {
//            public void mouseEntered(MouseEvent e) {
//                String s = m_field.getText();
//                if ( isTextObscured(m_field, s) )
//                    m_field.setToolTipText(s);
//            }
//            public void mouseExited(MouseEvent e) {
//                m_field.setToolTipText(null);
//            }
//        });
        
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(m_slider);
        add(m_field);
    }
    

//    private static boolean isTextObscured(JComponent c, String s) {
//        Graphics g = c.getGraphics();
//        FontMetrics fm = g.getFontMetrics(c.getFont());
//        int sw = fm.stringWidth(s);
//        return ( sw > c.getWidth() );
//    }
    

    public Number getValue(){
    	m_value = getFieldValue();
        return m_value;
    }

    public void setValue(Number value) {
        m_value = value;
        setSliderValue();
        setFieldValue();
    }
    
    private Number getSliderValue() {
        if ( m_value instanceof Integer ) {
            int val = m_slider.getValue();
            int min = m_min.intValue();
            int max = m_max.intValue();
            if(upper) max--;
            if(lower) min++;
            return new Integer(min + (val-m_smin)*(max-min)/m_srange);
        } else if ( m_value instanceof Long ) {
            int val = m_slider.getValue();
            long min = m_min.longValue();
            long max = m_max.longValue();
            if(upper) max--;
            if(lower) min++;
            return new Long(min + (val-m_smin)*(max-min)/m_srange);
        } else {
            double f = (m_slider.getValue()-m_smin)/(double)m_srange;
            double min = m_min.doubleValue();
            double max = m_max.doubleValue();
            if(upper) max-=0.0001;
            if(lower) min+=0.0001;
            double val = min + f*(max-min);
            return (m_value instanceof Double ? (Number)new Double(val)
                                              : new Float((float)val));
        }
    }
    
  
    private void setSliderValue() {
        int val;
        if ( m_value instanceof Double || m_value instanceof Float ) {
            double value = m_value.doubleValue();
            double min = m_min.doubleValue();
            double max = m_max.doubleValue();
            val = m_smin + (int)Math.round(m_srange*((value-min)/(max-min)));
            if(upper) max-=0.0001;
            if(lower) min+=0.0001;
        } else {
        	long value = m_value.longValue();
            long min = m_min.longValue();
            long max = m_max.longValue();
            val = m_smin + (int)((m_srange*(value-min))/(max-min));
            if(upper) max--;
            if(lower) min++;
        }
        m_slider.setValue(val);
    }
    
  
    private Number getFieldValue(){
    	Double val = null;
    	try{
    		val = Double.parseDouble(m_field.getText());
    	}catch(NumberFormatException nfe){
    		m_field.setBackground(Color.red);
    		JOptionPane.showMessageDialog(null, "Please enter a Value","Alert", JOptionPane.ERROR_MESSAGE);
    		setFieldValue();
    		m_field.setBackground(Color.white);
    		try{
    			val = m_value.doubleValue();
    		}catch(Exception e){e.printStackTrace();}
    	}
    	
        if ( m_value instanceof Double || m_value instanceof Float ){
        	if ( val < m_min.doubleValue()){
        		m_field.setBackground(Color.red);
            	JOptionPane.showMessageDialog(null, "Value ("+val.doubleValue()+") is less than lower limit ("+df.format(m_min.doubleValue())+")"+newline+"Value will be set to default : "+m_value,"Alert",JOptionPane.ERROR_MESSAGE);
            	setFieldValue();
            	m_field.setBackground(Color.white);
            	return m_value;
            }
            if ( val > m_max.doubleValue()){
            	m_field.setBackground(Color.red);
            	JOptionPane.showMessageDialog(null, "Value ("+val.doubleValue()+") is much than upper limit ("+df.format(m_max.doubleValue())+")"+newline+"Value will be set to default : "+m_value,"Alert",JOptionPane.ERROR_MESSAGE);
            	setFieldValue();
            	m_field.setBackground(Color.white);
            	return m_value;
            }
            return m_value instanceof Double ? (Number)val.doubleValue() : val.floatValue();
        }
        else {
            if ( val < m_min.longValue()){
            	m_field.setBackground(Color.red);
            	JOptionPane.showMessageDialog(null, "Value ("+val.longValue()+") is less than lower limit ("+m_min.longValue()+")"+newline+"Value will be set to default : "+m_value,"Alert",JOptionPane.ERROR_MESSAGE);
            	setFieldValue();
            	m_field.setBackground(Color.white);
            	return m_value;
            }
            if ( val > m_max.longValue()){
            	m_field.setBackground(Color.red);
            	JOptionPane.showMessageDialog(null, "Value ("+val.longValue()+") is much than upper limit ("+m_max.longValue()+")"+newline+"Value will be set to default : "+m_value,"Alert",JOptionPane.ERROR_MESSAGE);
            	setFieldValue();
            	m_field.setBackground(Color.white);
            	return m_value;
            }
            return m_value instanceof Long ? (Number)val.longValue() : val.intValue();
        }
    }
    
 
    private void setFieldValue() {
        String text;
        if ( m_value instanceof Double || m_value instanceof Float )
        	text = stringLib.formatNumber(m_value.doubleValue(),3);
        else
            text = String.valueOf(m_value.longValue());
        m_field.setText(text);
    }
    
	public void addChangeListener(ChangeListener cl) {
        if ( !m_listeners.contains(cl) )
            m_listeners.add(cl);
    }

    public void removeChangeListener(ChangeListener cl) {
        m_listeners.remove(cl);
    }
    
    @SuppressWarnings("unchecked")
	protected void fireChangeEvent() {
        Iterator iter = m_listeners.iterator();
        ChangeEvent evt = new ChangeEvent(this); 
        while ( iter.hasNext() ) {
            ChangeListener cl = (ChangeListener)iter.next();
            cl.stateChanged(evt);
        }
    }   
}