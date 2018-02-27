package org.cytoscape.internal.prefs.lib;

import java.awt.Dimension;
import java.awt.Font;

public class RangedIntegerTextField extends RangedDoubleTextField
{
	private static final long	serialVersionUID	= 1L;
	
	public RangedIntegerTextField()
	{
		this(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}
	
	public RangedIntegerTextField(int min, int max)
	{   
		super(min, max, new IntegerFormatter());
	}
	
	public RangedIntegerTextField(int min, int max, Dimension size)
	{
		this(min, max);
		AntiAliasedPanel.setSizes(this, size);
	}
	
	public RangedIntegerTextField(int min, int max, Dimension size, Font font)
	{
		this(min, max, size);
		setFont(font);
	}

	public int getInt()
	{
		double v = getDouble();
		if (Double.isNaN(v))
			return (int) getMin();
		return (int) v;
	}
	public void setInt(int i)						{ 		setValue(i);						}
	@Override public void setValue(double d)		{		super.setValue((int) d);			}
	
	protected INumberFormatter makeFormatter()		{	return new IntegerFormatter();	}
	
	
	public static class IntegerFormatter implements INumberFormatter
	{
		@Override public String formattedDouble(Double d) {
			if (d == null) return "";
			return String.valueOf(d.longValue());
		}

		@Override public String formattedPercentDouble(double d) {
			return String.valueOf((int) (d * 100));
		}

		@Override public double parseDouble(String s) {
			try {
			return Integer.parseInt(s);
			} catch (NumberFormatException ex) {}
			return Double.NaN;
		}
		
	}
}