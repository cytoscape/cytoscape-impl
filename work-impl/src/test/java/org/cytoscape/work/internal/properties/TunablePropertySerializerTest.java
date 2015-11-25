package org.cytoscape.work.internal.properties;

/*
 * #%L
 * org.cytoscape.work-impl
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2015 The Cytoscape Consortium
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

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Date;
import java.util.Properties;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.properties.TunablePropertyHandlerFactory;
import org.cytoscape.work.properties.TunablePropertySerializer;
import org.cytoscape.work.util.ListMultipleSelection;
import org.cytoscape.work.util.ListSingleSelection;
import org.junit.Ignore;
import org.junit.Test;

public class TunablePropertySerializerTest {
	
	@Test
	public void testSetTunablesFromProperties() {
		Properties props = new Properties();
        props.setProperty("line.startPoint.x", "5");
        props.setProperty("line.startPoint.y", "10");
        props.setProperty("line.endPoint.x", "15");
        props.setProperty("line.endPoint.y", "20");
        
        props.setProperty("why", "false");
        props.setProperty("str", "This is now a string that has a different value");
        props.setProperty("yesNoMaybe", "MAYBE");
        props.setProperty("integer", "44");
        props.setProperty("primitiveInt", "55");
        
        props.setProperty("listSingleNumbers", "3");
        props.setProperty("listMultipleNames", "Max,Fred");
        props.setProperty("listSingleEnum", "MAYBE");
        props.setProperty("intRange", "1");
        props.setProperty("doubleRange", "0.1");
        
        
        LotsOfTunables tunables = new LotsOfTunables();
        TunablePropertySerializer serializer = getSerializer();
        serializer.setTunables(tunables, props);
        
        
        assertEquals(tunables.line.startPoint.x, 5);
        assertEquals(tunables.line.startPoint.y, 10);
        assertEquals(tunables.line.endPoint.x, 15);
        assertEquals(tunables.line.endPoint.y, 20);
		
        assertEquals(tunables.why, false);
        assertEquals(tunables.str, "This is now a string that has a different value");
        assertEquals(tunables.yesNoMaybe, YesNoMaybe.MAYBE);
        assertEquals(tunables.integer, Integer.valueOf(44));
        assertEquals(tunables.primitiveInt, 55);
        
        assertEquals(tunables.listSingleNumbers.getSelectedValue(), Integer.valueOf(3));
        assertEquals(tunables.listMultipleNames.getSelectedValues(), Arrays.asList("Max","Fred"));
        assertEquals(tunables.listSingleEnum.getSelectedValue(), YesNoMaybe.MAYBE);
        assertEquals(tunables.intRange.getValue(), Integer.valueOf(1));
        assertEquals(tunables.doubleRange.getValue(), Double.valueOf(0.1));
        
        assertEquals(tunables.missingValue, 0);
	}
	
	
	@Test(expected=IllegalArgumentException.class)
	public void testBadFormat() {
		class Tunables {
		    @Tunable public int badFormat;
		};
		
		Properties props = new Properties();
		props.setProperty("badFormat", "this is not an int");
		
		Tunables tunables = new Tunables();
		getSerializer().setTunables(tunables, props);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testMissingValue() {
		class Tunables {
		    @Tunable public int missing;
		};
		
		Properties props = new Properties();
		props.setProperty("missing", "");
		
		Tunables tunables = new Tunables();
		getSerializer().setTunables(tunables, props);
	}
	
	@Test
	public void testMissingProperty() {
		class Tunables {
		    @Tunable public int missing;
		};
		
		Properties props = new Properties();
		
		Tunables tunables = new Tunables();
		getSerializer().setTunables(tunables, props);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testBadListSelection() {
		class Tunables {
		    @Tunable public ListMultipleSelection<String> lms = new ListMultipleSelection<>("A","B","C");
		};
		
		Properties props = new Properties();
		props.setProperty("lms", "A,B,What");
		
		Tunables tunables = new Tunables();
		getSerializer().setTunables(tunables, props);
	}
	
	
	@Test(expected=IllegalArgumentException.class)
	public void testBadEnumName() {
		class Tunables {
		    @Tunable public YesNoMaybe yesNoMaybe = YesNoMaybe.YES;
		};
		// The YesNoMaybe.toString() method must be overridden to return something other than the field name.
		Properties props = new Properties();
		props.setProperty("yesNoMaybe", YesNoMaybe.MAYBE.toString());
		
		Tunables tunables = new Tunables();
		getSerializer().setTunables(tunables, props);
	}
	
	
	@Test
	public void testCreatePropertiesFromTunables() {
		LotsOfTunables tunables = new LotsOfTunables();
		
		tunables.line.startPoint.x = 5;
        tunables.line.startPoint.y = 10;
        tunables.line.endPoint.x = 15;
        tunables.line.endPoint.y = 20;
		
        tunables.why = false;
        tunables.str = "ABCD";
        tunables.yesNoMaybe = YesNoMaybe.MAYBE;
        tunables.integer = 44;
        tunables.primitiveInt = 55;
        
        tunables.listSingleNumbers.setSelectedValue(3);
        tunables.listMultipleNames.setSelectedValues(Arrays.asList("Max","Fred"));
        tunables.listSingleEnum.setSelectedValue(YesNoMaybe.MAYBE);
        tunables.intRange.setValue(1);
        tunables.doubleRange.setValue(0.1);
        
		Properties props = getSerializer().toProperties(tunables);
        
        assertEquals("5", props.getProperty("line.startPoint.x"));
        assertEquals("10", props.getProperty("line.startPoint.y"));
        assertEquals("15", props.getProperty("line.endPoint.x"));
        assertEquals("20", props.getProperty("line.endPoint.y"));
        
        assertEquals("false", props.getProperty("why"));
        assertEquals("ABCD", props.getProperty("str"));
        assertEquals("MAYBE", props.getProperty("yesNoMaybe"));
        assertEquals("44", props.getProperty("integer"));
        assertEquals("55", props.getProperty("primitiveInt"));
        
        assertEquals("3", props.getProperty("listSingleNumbers"));
        assertEquals("Max,Fred", props.getProperty("listMultipleNames"));
        assertEquals("MAYBE", props.getProperty("listSingleEnum"));
        assertEquals("1", props.getProperty("intRange"));
        assertEquals("0.1", props.getProperty("doubleRange"));
	}

	@Ignore
	public void testUnsupportedType() {
		class Tunables {
		    @Tunable public Date date = new Date();
		};
		
		Tunables tunables = new Tunables();
		Properties props = getSerializer().toProperties(tunables);
		assertTrue(props.isEmpty());
	}

	
	private TunablePropertySerializer getSerializer() {
        TunablePropertyHandlerFactory<BasicTypePropertyHandler> simpleHandler = new SimpleTunablePropertyHandlerFactory<>(BasicTypePropertyHandler.class, BasicTypePropertyHandler.supportedTypes());
		TunablePropertyHandlerFactory<ListSinglePropertyHandler> listSingleHandler = new SimpleTunablePropertyHandlerFactory<>(ListSinglePropertyHandler.class, ListSingleSelection.class);
		TunablePropertyHandlerFactory<ListMultiplePropertyHandler> listMultipleHandler = new SimpleTunablePropertyHandlerFactory<>(ListMultiplePropertyHandler.class, ListMultipleSelection.class);
		TunablePropertyHandlerFactory<BoundedPropertyHandler> boundedHandler = new SimpleTunablePropertyHandlerFactory<>(BoundedPropertyHandler.class, BoundedPropertyHandler.supportedTypes());
		
		TunablePropertySerializerFactoryImpl serializerFactory = new TunablePropertySerializerFactoryImpl();
		serializerFactory.addTunableHandlerFactory(simpleHandler, new Properties());
		serializerFactory.addTunableHandlerFactory(listSingleHandler, new Properties());
		serializerFactory.addTunableHandlerFactory(listMultipleHandler, new Properties());
		serializerFactory.addTunableHandlerFactory(boundedHandler, new Properties());
		return serializerFactory.createSerializer();
	}
}
