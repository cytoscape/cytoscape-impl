package org.cytoscape.internal.prefs.lib;

import java.text.DecimalFormatSymbols;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;


/**
 * For use with RangedDoubleTextField.java
 * Limits input to document by keystroke.  Allows only numeric
 * values and limited localized formatting symbols.
 * @author Seth
 *
 */
public class DoubleTextFieldDocument extends PlainDocument {
	private static final long	serialVersionUID	= 1L;
	//localized format symbols
	private final char locDecimal;
	private final char locMinus;
	private final String locExponentSeparator;
	
	char[] match;
	
	/**
	 * For use with RangedDoubleTextField.java
	 * Limits input to document by keystroke.  Allows only numeric
	 * values and limited localized formatting symbols.
	*/
	public DoubleTextFieldDocument(){
		super();
		DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance();  // Intl.getInstanceDecimalFormatSymbols();
		locDecimal = dfs.getDecimalSeparator();
		locMinus = dfs.getMinusSign();
		locExponentSeparator = dfs.getExponentSeparator();
		
		String s = Character.toString(locDecimal)+Character.toString(locMinus)+locExponentSeparator;
		match = s.toCharArray();
	}

	@Override	public void insertString(int offset, String str, AttributeSet a) 
	throws BadLocationException {

		char[] source = str.toCharArray();
		char[] result = new char[source.length];
		int j = 0;
		int k = 0;
		
		for (int i=0+k; i < result.length; i++) {			
			if (Character.isDigit(source[i]) || isFormatting(source[i])) {
		        result[j++] = source[i];
		    } 
		}
		try {
//			String i = new StringBuffer(this.getText(0, this.getLength()))
//				.insert(offset, new String(result, 0, j)).toString();
			super.insertString(offset, new String(result, 0, j), a);
		} 
		catch (NumberFormatException e) {//enter nothing}
		}
	}
	private boolean isFormatting(char c){
		boolean ret=false;
		
		for(int i=0;i<match.length;i++){
			if(match[i]==c){
				ret=true;
				return ret;
			}
		}
		return ret;
	}
}
