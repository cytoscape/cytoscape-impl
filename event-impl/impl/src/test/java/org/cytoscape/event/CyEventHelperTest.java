package org.cytoscape.event;

/*
 * #%L
 * Cytoscape Event Impl (event-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2008 - 2013 The Cytoscape Consortium
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

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.cytoscape.event.internal.*;

import org.osgi.framework.*;

import org.springframework.osgi.mock.*;

import java.lang.RuntimeException;

import java.util.List;

import org.junit.After;
import org.junit.Before;


/**
 * DOCUMENT ME!
  */
public class CyEventHelperTest extends AbstractCyEventHelperTest {

	private ServiceReference stubServiceRef;
	private ServiceReference fakeServiceRef;
	private ServiceReference payloadServiceRef;
	private BundleContext bc;
	private CyEventHelperImpl helperImpl;

	/**
	 *  DOCUMENT ME!
	 */
	@Before
	public void setUp() {
		service = new StubCyListenerImpl();
		payloadService = new StubCyPayloadListenerImpl();

		stubServiceRef = new MockServiceReference();
		fakeServiceRef = new MockServiceReference();
		payloadServiceRef = new MockServiceReference();

		bc = new MockBundleContext() {
				public ServiceReference getServiceReference(String clazz) {
					if ( clazz.equals( FakeCyListener.class.getName() ) )
						return fakeServiceRef;
					else if ( clazz.equals( StubCyListener.class.getName() ) )
						return stubServiceRef;
					else if ( clazz.equals( StubCyPayloadListener.class.getName() ) )
						return payloadServiceRef;
					else
						return null;
				}

				public ServiceReference[] getServiceReferences(String clazz, String filter)
				    throws InvalidSyntaxException {
					if ( clazz.equals( FakeCyListener.class.getName() ) )
						return new ServiceReference[] { fakeServiceRef };
					else if ( clazz.equals( StubCyListener.class.getName() ) )
						return new ServiceReference[] { stubServiceRef };
					else if ( clazz.equals( StubCyPayloadListener.class.getName() ) )
						return new ServiceReference[] { payloadServiceRef };
					else
						return null;
				}

				public Object getService(ServiceReference ref) {
					if ( ref == stubServiceRef )
						return service;
					else if ( ref == payloadServiceRef )
						return payloadService;
					else 
						return null;
				}
			};

		CyListenerAdapter la = new CyListenerAdapter(bc);

		helperImpl = new CyEventHelperImpl(la);
		helper = helperImpl;
	}
	
	@After
	public void cleanup() {
		helperImpl.cleanup();
	}
}
