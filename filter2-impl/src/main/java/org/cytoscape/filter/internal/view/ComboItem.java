package org.cytoscape.filter.internal.view;

/**
 * Simple value holder class to be used in combo boxes and other list controls.
 * Holds the actual value and a string that will display in the UI.
 * 
 * Note: 
 * The hashCode() and equals() methods only use the 'value' field.
 * This allows you can select the value in the combo box without needing the label field.
 * Eg)
 * comboBox.setSelectedItem(new ComboItem<>(value))
 */
public class ComboItem<V> {

	private final V value;
	private final String label;
	
	
	public ComboItem(V value, String label) {
		this.value = value;
		this.label = label;
	}
	
	public ComboItem(V value) {
		this(value, value.toString());
	}
	
	public V getValue() {
		return value;
	}
	
	public String getLabel() {
		return label;
	}
	
	@Override
	public String toString() {
		return String.valueOf(label);
	}

	public static <V> ComboItem<V> of(V value) {
		return new ComboItem<V>(value);
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ComboItem other = (ComboItem) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	
	
	
}
