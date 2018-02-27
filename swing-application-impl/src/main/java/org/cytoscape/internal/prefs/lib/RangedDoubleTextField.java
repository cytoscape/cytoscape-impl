package org.cytoscape.internal.prefs.lib;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

/**
 * JFormattedTextField for use with doubles and verification.
 */
public class RangedDoubleTextField extends JFormattedTextField {

	/**
	 * RangedDoubleTextField: A numerical entry box for decimal numbers supports
	 * arrow keys to increment / decrement the value
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final RangedDoubleVerifier dVerifier;
	private final INumberFormatter f;

	/**
	 * Ranged field. Default prefs formatting.
	 * 
	 */
	public RangedDoubleTextField(double min, double max, INumberFormatter formatter) {
		f = formatter;
		dVerifier = new RangedDoubleVerifier(this, min, max, formatter);
		init(dVerifier);
	}

	/**
	 * Un-Ranged field. Default prefs formatting.
	 * 
	 */
	public RangedDoubleTextField(INumberFormatter formatter) {
		f = formatter;
		dVerifier = new RangedDoubleVerifier(this, formatter);
		init(dVerifier);
	}

	public RangedDoubleTextField(double min, double max, INumberFormatter formatter, Dimension size) {
		this(min, max, formatter);
		AntiAliasedPanel.setSizes(this, size);
	}

	private void init(RangedDoubleVerifier dv) {
		this.setDocument(new DoubleTextFieldDocument());
		this.setInputVerifier(dv);
		setHorizontalAlignment(JTextField.RIGHT);
	}

	/**
	 * Get the cached original value as a Double
	 * 
	 */
	public double getOriginalValue() {		return fOriginal;	}

	public void rememberOriginal() {
		if (Double.isNaN(fOriginal))
			fOriginal = getDouble();
	}// resetBorder();

	public static Border redBorder= BorderFactory.createLineBorder(Color.red);
	public void setEditedBorder() {		setBorder(redBorder);	}

	public void reset() {
		resetBorder();
		setValue(fOriginal);
	}

	public static Border etchedRaised = BorderFactory.createEtchedBorder(EtchedBorder.RAISED, Color.darkGray, Color.lightGray);
	public void resetBorder() 		{		setBorder(etchedRaised);		}

	public Double getDouble() 		{		return f.parseDouble(getText());	}
	public void setDouble(double d) {		setValue(d);	}

	public void setValue(double d) 	{		setText(f.formattedDouble(d));	}

	public void setMin(double d) 	{		dVerifier.setMin(d);	}
	public double getMin() 			{		return dVerifier.getMin();	}

	public void setMax(double d) 	{		dVerifier.setMax(d);	}
	public double getMax() 			{		return dVerifier.getMax();	}

	/**
	 * Get error label Use for 1:1 mapping of field to error label
	 * 
	 */
	public JLabel getErrorLabel() {
		return dVerifier.getErrorLabel();
	}

	/**
	 * Set error label Use for Many:1 mapping of field to error label
	 */
	public void setErrorLabel(JLabel label) {		dVerifier.setErrorLabel(label);	}

	public RangedDoubleVerifier getRangedDoubleVerifier() {		return dVerifier;	}

	@Override	public boolean isEditValid() {		return dVerifier.checkField(this);	}

	private double fOriginal = Double.NaN;
	private double fIncrement = 1;
	private double fCtrlIncrement = .5;
	private double fAltIncrement = .01;
	private double fShiftIncrement = 4;
	private boolean fSupportArrowKeys = false;

	public void supportArrowKeyIncrement() {		fSupportArrowKeys = true;	}

	@Override
	public void processKeyEvent(KeyEvent ev) {
		int code = ev.getKeyCode();
		if (fSupportArrowKeys && (code == KeyEvent.VK_UP || code == KeyEvent.VK_DOWN)) {
			if (ev.getID() != KeyEvent.KEY_PRESSED)
				return;
			int sign = (code == KeyEvent.VK_UP) ? 1 : -1;
			boolean altDown = ev.isAltDown();
			boolean ctrlDown = ev.isControlDown();
			boolean shiftDown = ev.isShiftDown();

			double increment = fIncrement;
			if (ctrlDown)			increment = fCtrlIncrement;
			else if (altDown)		increment = fAltIncrement;
			else if (shiftDown)		increment = fShiftIncrement;
			
			double d = getDouble();
			d += sign * increment;
			setValue(d);
			// SoundUtil.click();
			ev.consume();
			super.processKeyEvent(ev);
			return;
		}
		if (!ev.isConsumed()) {
			if (Character.isLetter(ev.getKeyChar()))
				return;
			super.processKeyEvent(ev);
		}

	}

	public boolean hasText() {
		return getText() != null && getText().length() > 0;
	}


public class RangedDoubleVerifier extends InputVerifier implements FocusListener, KeyListener{
	

//	
//	static DictKey INVALID = new DictKey("string.validnumber");		 
//	static DictKey AND = new DictKey("plat.bool.and");
//	static DictKey INVALID_INPUT = new DictKey("string.invalidinput");
//	
//	
	private RangedDoubleTextField myField;
	private String lastGoodValue;
	private double min,max;
	private INumberFormatter f;
	private Color dFgColor;
	private JLabel error;
	
	/**
	 * Use with ranged doubles.  
	 * Default prefs formattings.
	 * Includes Error JLabel.
	 * 
	 */
	public RangedDoubleVerifier(RangedDoubleTextField field, double minimum, double maximum, INumberFormatter formatter){
		super();
		min = minimum;
		max = maximum;
		f = formatter;
		init(field);
	}
	
	/**
	 * Use with un-ranged doubles. (sets range as neg-infinity<->pos-infinity)
	 * Default prefs formattings.
	 * 
	 */
	public RangedDoubleVerifier(RangedDoubleTextField field, INumberFormatter formatter){
		this(field, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, formatter);
		init(field);
	}
	
	private void init(RangedDoubleTextField field){
		dFgColor = field.getForeground();
		myField=field;
		myField.addFocusListener(this);
		myField.addKeyListener(this);
		lastGoodValue="";
		error = new JLabel();
		setErrorLabel(error);
	}
	
	public void setMin(double d)	{	min = d;	}
	public double getMin()		{	return min;		}
	public void setMax(double d)	{	max = d;	}
	public double getMax()		{	return max;		}
//	public void setRange(Range r)
//	{
//		setMin(r.getMin());
//		setMax(r.getMax());
//	}
	
	@Override public boolean verify(JComponent input) {
        boolean good = checkField(input);
        if(good)
        		lastGoodValue=myField.getText();
        else{
	        	myField.setText(lastGoodValue);
	        	myField.setForeground(dFgColor);
	        	setErrorLabel(error);
	    		error.setVisible(false);
        }
        return good;
    }
	
	/**
	 * Checks validity of field entry as double within range
	 *
	 */
	public boolean checkField(JComponent input){
	
		String inText = myField.getText();
		double myNum;
		myNum = f.parseDouble(inText);
		if (Double.isNaN(myNum))			return false;
		if(myNum<min || myNum>max)		return false;
		return true;		
	}
	
	public JLabel getErrorLabel()	{		return error;			}
	public void setErrorLabel(JLabel label)
	{
//		error=label;
//		if(max==Double.MAX_VALUE && min==Double.MIN_VALUE)
//			error.setText(INVALID_INPUT.lookup()); 						//default error message if unranged values
//		else if(max==Double.MAX_VALUE)
//			error.setText(INVALID.lookup() + " >= "+ f.formattedDouble(min));
//		else if(min==Double.MIN_VALUE)
//			error.setText(INVALID.lookup() + " <= "+ f.formattedDouble(max));
//		else error.setText(INVALID.lookup() + " >= " + f.formattedDouble(min)
//				+" " + AND.lookup() + " <= "+	f.formattedDouble(max));
//		error.setVisible(false);
//		error.setForeground(Color.RED);
	}
	//--------------------------------------------------------------------------------------------

	@Override	public void focusGained(FocusEvent e) {		lastGoodValue=myField.getText();	}
	@Override	public void focusLost(FocusEvent e) {
			double value = myField.getDouble();
			if (!Double.isNaN(value))
				myField.setText(f.formattedDouble(value));		//TODO AM 12/26/12 check behavior for loosing focus with invalid content
			myField.setForeground(dFgColor);
			setErrorLabel(error);
			error.setVisible(false);
//			Action action = (myField.getAction() instanceof Action) ? (Action)myField.getAction() : null;
//			if(action!=null)
//				action.actionPerformed(new ActionEvent(myField, 0, action.getActionCmd()));
	}
//--------------------------------------------------------------------------------------------
	@Override	public void keyPressed(KeyEvent e) {}
	@Override	public void keyTyped(KeyEvent e){}

	@Override	public void keyReleased(KeyEvent e)
	{
		setErrorLabel(error);
		boolean ok = checkField(myField);
		myField.setForeground(ok ? dFgColor : Color.RED);
		error.setVisible(!ok);
	}

}
}
