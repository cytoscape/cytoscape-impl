package org.cytoscape.app.internal.util;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Test;

/*
 * #%L
 * Cytoscape App Impl (app-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2008 - 2021 The Cytoscape Consortium
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

public class AppUtilTest {

	@Test
	public void simpleTest() {
		String list = "A, B, Sin'gle quote, \"Quoted\" String, \"Quo,ted\" comma, \"A\" and \"B,C\", etc";
		ArrayList<String> packages = AppUtil.splitByChar(list, ',');
		assertEquals(packages.size(), 7);
		assertEquals(packages.get(4), " \"Quo,ted\" comma");
		assertEquals(packages.get(5), " \"A\" and \"B,C\"");
	}
	
	@Test
	public void packageList() {
		String list = "com.fasterxml.jackson.core;resolution:=optional;groupId=\"!org.cytoscape\";version=\"[2.8,3)\",com.fasterxml.jackson.databind;resolution:=optional;groupId=\"!org.cytoscape\";version=\"[2.8,3)\",com.github.luben.zstd;resolution:=optional;groupId=\"!org.cytoscape\",com.sun.javafx.scene.control.skin;resolution:=optional;groupId=\"!org.cytoscape\",com.sun.javafx.scene.traversal;resolution:=optional;groupId=\"!org.cytoscape\",com.sun.javafx.tk;resolution:=optional;groupId=\"!org.cytoscape\",io.swagger.annotations;resolution:=optional;groupId=\"!org.cytoscape\";version=\"[1.5,2)\",javafx.animation;resolution:=optional;groupId=\"!org.cytoscape\",javafx.application;resolution:=optional;groupId=\"!org.cytoscape\",javafx.beans.property;resolution:=optional;groupId=\"!org.cytoscape\",javafx.beans.value;resolution:=optional;groupId=\"!org.cytoscape\",javafx.collections;resolution:=optional;groupId=\"!org.cytoscape\",javafx.embed.swing;resolution:=optional;groupId=\"!org.cytoscape\",javafx.event;resolution:=optional;groupId=\"!org.cytoscape\",javafx.geometry;resolution:=optional;groupId=\"!org.cytoscape\",javafx.scene;resolution:=optional;groupId=\"!org.cytoscape\",javafx.scene.canvas;resolution:=optional;groupId=\"!org.cytoscape\",javafx.scene.control;resolution:=optional;groupId=\"!org.cytoscape\",javafx.scene.image;resolution:=optional;groupId=\"!org.cytoscape\",javafx.scene.input;resolution:=optional;groupId=\"!org.cytoscape\",javafx.scene.layout;resolution:=optional;groupId=\"!org.cytoscape\",javafx.scene.paint;resolution:=optional;groupId=\"!org.cytoscape\",javafx.scene.text;resolution:=optional;groupId=\"!org.cytoscape\",javafx.scene.transform;resolution:=optional;groupId=\"!org.cytoscape\",javafx.stage;resolution:=optional;groupId=\"!org.cytoscape\",javafx.util;resolution:=optional;groupId=\"!org.cytoscape\",javax.crypto;resolution:=optional;groupId=\"!org.cytoscape\",javax.crypto.spec;resolution:=optional;groupId=\"!org.cytoscape\",javax.print;resolution:=optional;groupId=\"!org.cytoscape\",javax.print.attribute;resolution:=optional;groupId=\"!org.cytoscape\",javax.security.auth.x500;resolution:=optional;groupId=\"!org.cytoscape\",javax.swing;resolution:=optional;groupId=\"!org.cytoscape\",javax.swing.border;resolution:=optional;groupId=\"!org.cytoscape\",javax.swing.event;resolution:=optional;groupId=\"!org.cytoscape\",javax.swing.filechooser;resolution:=optional;groupId=\"!org.cytoscape\",javax.ws.rs;resolution:=optional;groupId=\"!org.cytoscape\";version=\"[2.0,3)\",javax.ws.rs.core;resolution:=optional;groupId=\"!org.cytoscape\";version=\"[2.0,3)\",javax.xml.bind;resolution:=optional;groupId=\"!org.cytoscape\",org.apache.commons.codec.binary;resolution:=optional;groupId=\"!org.cytoscape\";version=\"[1.10,2)\",org.apache.commons.io;resolution:=optional;groupId=\"!org.cytoscape\";version=\"[1.4,2)\",org.apache.http;resolution:=optional;groupId=\"!org.cytoscape\",org.apache.http.client.methods;resolution:=optional;groupId=\"!org.cytoscape\",org.apache.http.entity;resolution:=optional;groupId=\"!org.cytoscape\",org.apache.http.impl.client;resolution:=optional;groupId=\"!org.cytoscape\",org.apache.http.message;resolution:=optional;groupId=\"!org.cytoscape\",org.apache.http.util;resolution:=optional;groupId=\"!org.cytoscape\",org.brotli.dec;resolution:=optional;groupId=\"!org.cytoscape\",org.cxio.core.interfaces;resolution:=optional;groupId=\"!org.cytoscape\",org.cxio.metadata;resolution:=optional;groupId=\"!org.cytoscape\",org.cytoscape.app.swing;resolution:=optional;groupId=\"!org.cytoscape\";version=\"[3.6,4)\",org.cytoscape.application;resolution:=optional;groupId=\"!org.cytoscape\";version=\"[3.6,4)\",org.cytoscape.application.swing;resolution:=optional;groupId=\"!org.cytoscape\";version=\"[3.6,4)\",org.cytoscape.application.swing.search;resolution:=optional;groupId=\"!org.cytoscape\";version=\"[3.6,4)\",org.cytoscape.ci;resolution:=optional;groupId=\"!org.cytoscape\";version=\"[3.6,4)\",org.cytoscape.ci.model;resolution:=optional;groupId=\"!org.cytoscape\";version=\"[3.6,4)\",org.cytoscape.cyndex2.errors;resolution:=optional;groupId=\"!org.cytoscape\",org.cytoscape.group;resolution:=optional;groupId=\"!org.cytoscape\";version=\"[3.6,4)\",org.cytoscape.io.internal.cx_reader;resolution:=optional;groupId=\"!org.cytoscape\";version=\"[3.6,4)\",org.cytoscape.io.internal.cx_writer;resolution:=optional;groupId=\"!org.cytoscape\";version=\"[3.6,4)\",org.cytoscape.io.internal.cxio;resolution:=optional;groupId=\"!org.cytoscape\";version=\"[3.6,4)\",org.cytoscape.io.read;resolution:=optional;groupId=\"!org.cytoscape\";version=\"[3.6,4)\",org.cytoscape.io.util;resolution:=optional;groupId=\"!org.cytoscape\";version=\"[3.6,4)\",org.cytoscape.io.write;resolution:=optional;groupId=\"!org.cytoscape\";version=\"[3.6,4)\",org.cytoscape.model;resolution:=optional;groupId=\"!org.cytoscape\";version=\"[3.6,4)\",org.cytoscape.model.events;resolution:=optional;groupId=\"!org.cytoscape\";version=\"[3.6,4)\",org.cytoscape.model.subnetwork;resolution:=optional;groupId=\"!org.cytoscape\";version=\"[3.6,4)\",org.cytoscape.property;resolution:=optional;groupId=\"!org.cytoscape\";version=\"[3.6,4)\",org.cytoscape.service.util;resolution:=optional;groupId=\"!org.cytoscape\";version=\"[3.6,4)\",org.cytoscape.session;resolution:=optional;groupId=\"!org.cytoscape\";version=\"[3.6,4)\",org.cytoscape.task;resolution:=optional;groupId=\"!org.cytoscape\";version=\"[3.6,4)\",org.cytoscape.view.layout;resolution:=optional;groupId=\"!org.cytoscape\";version=\"[3.6,4)\",org.cytoscape.view.model;resolution:=optional;groupId=\"!org.cytoscape\";version=\"[3.6,4)\",org.cytoscape.view.presentation;resolution:=optional;groupId=\"!org.cytoscape\";version=\"[3.6,4)\",org.cytoscape.view.presentation.property;resolution:=optional;groupId=\"!org.cytoscape\";version=\"[3.6,4)\",org.cytoscape.view.vizmap;resolution:=optional;groupId=\"!org.cytoscape\";version=\"[3.6,4)\",org.cytoscape.work;resolution:=optional;groupId=\"!org.cytoscape\";version=\"[3.6,4)\",org.cytoscape.work.swing;resolution:=optional;groupId=\"!org.cytoscape\";version=\"[3.6,4)\",org.ndexbio.model.cx;resolution:=optional;groupId=\"!org.cytoscape\";version=\"[3.6,4)\",org.ndexbio.model.exceptions;resolution:=optional;groupId=\"!org.cytoscape\";version=\"[3.6,4)\",org.ndexbio.model.object;resolution:=optional;groupId=\"!org.cytoscape\";version=\"[3.6,4)\",org.ndexbio.model.object.network;resolution:=optional;groupId=\"!org.cytoscape\";version=\"[3.6,4)\",org.ndexbio.rest.client;resolution:=optional;groupId=\"!org.cytoscape\";version=\"[3.6,4)\",org.osgi.framework;resolution:=optional;groupId=\"!org.cytoscape\";version=\"[1.5,2)\",org.osgi.util.tracker;resolution:=optional;groupId=\"!org.cytoscape\";version=\"[1.4,2)\",org.slf4j;resolution:=optional;groupId=\"!org.cytoscape\";version=\"[1.5,2)\",sun.awt;resolution:=optional;groupId=\"!org.cytoscape\"";
		ArrayList<String> packages = AppUtil.splitByChar(list, ',');
		assertEquals(packages.get(0), "com.fasterxml.jackson.core;resolution:=optional;groupId=\"!org.cytoscape\";version=\"[2.8,3)\"");
		int n = packages.size();
		assertEquals(packages.get(n-2), "org.slf4j;resolution:=optional;groupId=\"!org.cytoscape\";version=\"[1.5,2)\"");
		assertEquals(packages.get(n-1), "sun.awt;resolution:=optional;groupId=\"!org.cytoscape\"");
	}
}
