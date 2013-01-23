package de.mpg.mpi_inf.bioinf.netanalyzer.data.settings;

/*
 * #%L
 * Cytoscape NetworkAnalyzer Impl (network-analyzer-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013
 *   Max Planck Institute for Informatics, Saarbruecken, Germany
 *   The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jdom.Element;
import org.w3c.dom.DOMException;

import de.mpg.mpi_inf.bioinf.netanalyzer.InnerException;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.IntRange;

/**
 * Base class for all NetworkAnalyzer settings.
 * 
 * @author Yassen Assenov
 */
public abstract class Settings implements Cloneable, XMLSerializable {

	/**
	 * Extracts the get methods for all the read/write properties of the given settings instance.
	 * 
	 * @param aSettings Settings instance to query for properties.
	 * @return Array of getter methods for all the read/write properties of the settings instance;
	 *         an empty array if no such properties are found.
	 */
	public static Method[] propertyGetters(Settings aSettings) {
		List<Method> getters = new ArrayList<Method>();
		final Method[] dataMethods = aSettings.getClass().getMethods();
		for (final Method m : dataMethods) {
			final String methodName = m.getName();
			if (methodName.startsWith("get") && m.getParameterTypes().length == 0
					&& (!methodName.equals("getClass"))) {
				getters.add(m);
			}
		}
		final Method[] result = getters.toArray(new Method[getters.size()]);
		Arrays.sort(result, new ReturnTypeComparator());
		return result;
	}

	/**
	 * Extracts the name of a property given its getter or setter method.
	 * 
	 * @param aGetter Getter (or setter) method for the property.
	 * @return Name of the property the given method targets.
	 * @throws InnerException If the method is not a getter or setter method.
	 */
	public static String propertyName(Method aGetter) {
		String methodName = aGetter.getName();
		if (methodName.startsWith("get") || methodName.startsWith("set")) {
			return methodName.substring(3);
		}
		throw new InnerException(null);
	}

	/**
	 * Searches for the setter method for a property given its getter method.
	 * 
	 * @param aGetter Getter method for the property of interest.
	 * @return Setter method for the property of interest, <code>null</code> if none exists.
	 */
	public static Method findSetter(Method aGetter) {
		Class<?>[] sParam = new Class<?>[] { aGetter.getReturnType() };
		try {
			return aGetter.getDeclaringClass().getMethod("set" + propertyName(aGetter), sParam);
		} catch (NoSuchMethodException ex) {
			return null;
		}
	}

	/**
	 * Produces an exact copy of the settings instance.
	 * 
	 * @return Copy of the settings instance.
	 * @see Object#clone()
	 */
	@Override
	public Object clone() {
		try {
			Class<? extends Settings> thisType = getClass();
			ArrayList<Field> fields = getSettingProperties(thisType);
			Object cloned = thisType.newInstance();
			for (final Field f : fields) {
				Class<?> t = f.getType();
				Object value = f.get(this);
				if (t == Boolean.class || t == boolean.class) {
					// Leave the value unchanged
				} else if (t == Color.class) {
					value = new Color(((Color) value).getRGB());
				} else if (t == Double.class || t == double.class) {
					// Leave the value unchanged
				} else if (t == Float.class || t == float.class) {
					// Leave the value unchanged
				} else if (t == Integer.class || t == int.class) {
					// Leave the value unchanged
				} else if (t == IntRange.class) {
					// Leave the value unchanged
				} else if (t == Long.class || t == long.class) {
					// Leave the value unchanged
				} else if (t == PointShape.class) {
					// Leave the value unchanged
				} else if (t == String.class) {
					// Leave the value unchaged
				} else {
					// Unsupported type
					throw new UnsupportedOperationException();
				}
				f.set(cloned, value);
			}
			return cloned;
		} catch (Exception ex) {
			// IllegalAccessException
			// InstantiationException
			// UnsupportedOperationException
			throw new InnerException(ex);
		}
	}

	/**
	 * Saves the data of the visual settings to an XML node.
	 * 
	 * @return The newly created XML node which contains the data of the settings.
	 */
	public Element toXmlNode() {
		try {
			Element root = new Element(getTagName());
			ArrayList<Field> props = getSettingProperties(getClass());
			for (final Field f : props) {
				String tagName = getPropertyTagName(f.getName());
				Element el = new Element(tagName);
				Class<?> t = f.getType();
				Object value = f.get(this);

				try {
					if (t == Boolean.class || t == boolean.class) {
						el.setText(value.toString());
					} else if (t == Color.class) {
						el.setText(String.valueOf(((Color) value).getRGB()));
					} else if (t == Double.class || t == double.class) {
						el.setText(value.toString());
					} else if (t == Float.class || t == float.class) {
						el.setText(value.toString());
					} else if (t == Integer.class || t == int.class) {
						el.setText(value.toString());
					} else if (t == IntRange.class) {
						IntRange range = (IntRange) value;
						if (range.hasMax()) {
							el.setAttribute(MAX_ATTR, range.getMax().toString());
						}
						if (range.hasMin()) {
							el.setAttribute(MIN_ATTR, range.getMin().toString());
						}
					} else if (t == Long.class || t == long.class) {
						el.setText(value.toString());
					} else if (t == PointShape.class) {
						el.setText(value.toString());
					} else if (t == String.class) {
						el.setText(value.toString());
					} else {
						// Unsupported type
						throw new UnsupportedOperationException();
					}
				} catch (Exception ex) {
					throw new DOMException(DOMException.SYNTAX_ERR, tagName);
				}

				root.addContent(el);
			}
			return root;

		} catch (SecurityException ex) {
			throw ex;
		} catch (Exception ex) {
			if (ex instanceof DOMException) {
				throw (DOMException) ex;
			}
			// NoSuchFieldException
			// IllegalAccessException
			// IllegalArgumentException
			// UnsupportedOperationException
			throw new InnerException(ex);
		}
	}

	/**
	 * Gets all the setting properties of the given settings class.
	 * 
	 * @param aClass Settings class, should be extending this class.
	 * @return List of all the settings properties as {@link java.lang.reflect.Field} instances.
	 */
	protected static ArrayList<Field> getSettingProperties(Class<? extends Settings> aClass) {
		Field[] fields = aClass.getDeclaredFields();
		ArrayList<Field> settingPrs = new ArrayList<Field>(fields.length / 2);
		for (int i = 0; i < fields.length; ++i) {
			if (isSettingProperty(fields[i])) {
				settingPrs.add(fields[i]);
			}
		}
		return settingPrs;
	}

	/**
	 * Constructor used in cloning an instance.
	 */
	protected Settings() {
		// This constructor is used only in cloning and does not initialize its fields
	}

	/**
	 * Initializes a new instance of <code>Settings</code>.
	 * 
	 * @param aElement Node in the XML settings file that identifies settings.
	 * @throws DOMException When the given element is not an element node with the expected name or
	 *         when the subtree rooted at <code>aElement</code> does not have the expected
	 *         structure.
	 */
	protected Settings(Element aElement) {
		verifyTagName(aElement.getName());

		ArrayList<Field> props = getSettingProperties(getClass());
		for (final Field f : props) {
			String tagName = getPropertyTagName(f.getName());
			Element tag = aElement.getChild(tagName);
			if (tag == null) {
				throw new DOMException(DOMException.NOT_FOUND_ERR, tagName);
			}
			String text = aElement.getChildText(tagName);
			Object value = null;

			Class<?> t = f.getType();
			try {
				if (t == Boolean.class || t == boolean.class) {
					value = Boolean.valueOf(text);
				} else if (t == Color.class) {
					value = new Color(Integer.parseInt(text));
				} else if (t == Double.class || t == double.class) {
					value = Double.valueOf(text);
				} else if (t == Float.class || t == float.class) {
					value = Float.valueOf(text);
				} else if (t == Integer.class || t == int.class) {
					value = Integer.valueOf(text);
				} else if (t == IntRange.class) {
					Integer min = null;
					if (tag.getAttribute(MIN_ATTR) != null) {
						min = Integer.valueOf(tag.getAttributeValue(MIN_ATTR));
					}
					Integer max = null;
					if (tag.getAttribute(MAX_ATTR) != null) {
						max = Integer.valueOf(tag.getAttributeValue(MAX_ATTR));
					}
					value = new IntRange(min, max);
				} else if (t == Long.class || t == long.class) {
					value = Long.valueOf(text);
				} else if (t == PointShape.class) {
					value = PointShape.parse(text);
				} else if (t == String.class) {
					value = text;
				} else {
					// Unsupported type
					throw new UnsupportedOperationException();
				}
			} catch (Exception ex) {
				throw new DOMException(DOMException.SYNTAX_ERR, tagName);
			}

			try {
				f.set(this, value);
			} catch (Exception ex) {
				// IllegalAccessException
				// IllegalArgumentException
				// UnsupportedOperationException
				throw new InnerException(ex);
			}
		}
	}

	/**
	 * Checks if the given field stores a setting property.
	 * 
	 * @param aField Field to be checked.
	 * @return <code>true</code> if the field modifiers suggest this field is used for a property,
	 *         <code>false</code> if the field is <code>private</code>, <code>protected</code>
	 *         or <code>static</code>.
	 */
	private static boolean isSettingProperty(Field aField) {
		int mods = aField.getModifiers();
		if (Modifier.isStatic(mods) || Modifier.isPrivate(mods) || Modifier.isProtected(mods)) {
			return false;
		}
		return true;
	}

	/**
	 * Name of the attribute that designates the maximum value in an integer range.
	 * 
	 * @see IntRange
	 */
	private static final String MAX_ATTR = "max";

	/**
	 * Name of the attribute that designates the maximum value in an integer range.
	 * 
	 * @see IntRange
	 */
	private static final String MIN_ATTR = "min";

	/**
	 * Gets the name of XML tag identifying the given property.
	 * 
	 * @param aPropName Name of property to find the XML tag for.
	 * @return Name of the XML tag identifying <code>aPropName</code>.
	 * @throws SecurityException If the security manager blocks the attempt to get the value of the
	 *         corresponding field through reflection.
	 */
	private String getPropertyTagName(String aPropName) {
		try {
			String name = getClass().getDeclaredField(aPropName + "Tag").get(null).toString();
			if (name == null) {
				throw new NullPointerException();
			}
			return name;
		} catch (SecurityException ex) {
			throw ex;
		} catch (Exception ex) {
			// IllegalAccessException
			// IllegalArgumentException
			// NoSuchFieldException
			// NullPointerException
			throw new InnerException(ex);
		}
	}

	/**
	 * Gets the name of the XML tag identifying the settings class.
	 * 
	 * @return Name of the XML tag expected by the calling settings type.
	 * @throws IllegalArgumentException If the class calling this method does not define the
	 *         <code>tag</code> field as <code>static</code>.
	 * @throws SecurityException If the security manager blocks the attempt to get the value of the
	 *         <code>tag</code> field.
	 * @throws IllegalAccessException If the <code>tag</code> field is inaccessible.
	 * @throws NoSuchFieldException If the class calling this method does not define a field named
	 *         <code>tag</code>.
	 */
	private String getTagName() throws IllegalArgumentException, SecurityException,
			IllegalAccessException, NoSuchFieldException {
		return getClass().getDeclaredField("tag").get(null).toString();
	}

	/**
	 * Verifies the given name of XML element matches the expected name for the settings class.
	 * 
	 * @param aElementName Name of the XML element to be checked.
	 * @throws SecurityException If the security manager blocks the attempt to get the value of the
	 *         <code>tag</code> field through reflection.
	 * @throws DOMException If the value of the field named <code>tag</code> is different than
	 *         <code>aElementName</code>.
	 * @see #getTagName()
	 */
	private void verifyTagName(String aElementName) {
		String tagName = null;
		try {
			tagName = getTagName();
			if (tagName == null) {
				throw new NullPointerException();
			}
		} catch (SecurityException ex) {
			throw ex;
		} catch (Exception ex) {
			// NoSuchFieldException
			// IllegalAccessException
			// IllegalArgumentException
			// NullPonterException
			throw new InnerException(ex);
		}
		if (!tagName.equals(aElementName)) {
			throw new DOMException(DOMException.NOT_FOUND_ERR, tagName);
		}
	}
}
