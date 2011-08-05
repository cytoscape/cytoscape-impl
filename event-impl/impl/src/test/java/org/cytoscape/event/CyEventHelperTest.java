
/*
 Copyright (c) 2008, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/

package org.cytoscape.event;

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
