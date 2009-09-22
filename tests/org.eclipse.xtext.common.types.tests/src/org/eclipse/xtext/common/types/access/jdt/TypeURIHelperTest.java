/*******************************************************************************
 * Copyright (c) 2009 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.common.types.access.jdt;

import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.emf.common.util.URI;
import org.eclipse.jdt.core.Signature;

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 */
public class TypeURIHelperTest extends TestCase {

	private TypeURIHelper uriHelper;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		uriHelper = new TypeURIHelper();
	}
	
	@Override
	protected void tearDown() throws Exception {
		uriHelper = null;
		super.tearDown();
	}
	
	public void testCreateResourceURI_01() {
		URI uri = uriHelper.createResourceURI(Signature.createTypeSignature("int", true));
		assertEquals("java:/Primitives", uri.toString());
	}
	
	public void testCreateResourceURI_02() {
		URI uri = uriHelper.createResourceURI(Signature.createTypeSignature("java.lang.String", true));
		assertEquals("java:/Objects/java.lang.String", uri.toString());
	}
	
	public void testCreateResourceURI_03() {
		URI uri = uriHelper.createResourceURI(Signature.createTypeSignature("java.lang.String[]", true));
		assertEquals("java:/Objects/java.lang.String", uri.toString());
	}
	
	public void testCreateResourceURI_04() {
		URI uri = uriHelper.createResourceURI(Signature.createTypeSignature("int[]", true));
		assertEquals("java:/Primitives", uri.toString());
	}
	
	public void testCreateResourceURI_05() {
		URI uri = uriHelper.createResourceURI(Signature.createTypeSignature("java.util.Map$Entry", true));
		assertEquals("java:/Objects/java.util.Map", uri.toString());
	}
	
	public void testCreateResourceURI_06() {
		URI uri = uriHelper.createResourceURI(Signature.createTypeSignature("java.util.Map$Entry[]", true));
		assertEquals("java:/Objects/java.util.Map", uri.toString());
	}
	
	public void testCreateResourceURI_07() {
		URI uri = uriHelper.createResourceURIFromString("Something");
		assertEquals("java:Something", uri.toString());
	}
	
	public void testFragmentURI_01() {
		String fragment = uriHelper.getFragment(Signature.createTypeSignature("int", true));
		assertEquals(int.class.getName(), fragment);
	}
	
	public void testFragmentURI_02() {
		String fragment = uriHelper.getFragment(Signature.createTypeSignature("java.lang.String", true));
		assertEquals(String.class.getName(), fragment);
	}
	
	public void testFragmentURI_03() {
		String fragment = uriHelper.getFragment(Signature.createTypeSignature("int[]", true));
		assertEquals("int[]", fragment);
	}
	
	public void testFragmentURI_04() {
		String fragment = uriHelper.getFragment(Signature.createTypeSignature("java.lang.String[]", true));
		assertEquals(String.class.getName() + "[]", fragment);
	}

	public void testFragmentURI_05() {
		String fragment = uriHelper.getFragment(Signature.createTypeSignature("java.util.Map$Entry", true));
		assertEquals(Map.Entry.class.getName(), fragment);
	}
	
	public void testFragmentURI_06() {
		String fragment = uriHelper.getFragment(Signature.createTypeSignature("java.util.Map$Entry[]", true));
		assertEquals(Map.Entry.class.getName() + "[]", fragment);
	}

}
