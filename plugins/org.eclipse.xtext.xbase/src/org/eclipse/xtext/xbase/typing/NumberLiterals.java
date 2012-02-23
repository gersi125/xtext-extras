/*******************************************************************************
 * Copyright (c) 2012 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.xbase.typing;

import static org.eclipse.xtext.util.Strings.*;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.eclipse.xtext.common.types.util.TypeReferences;
import org.eclipse.xtext.xbase.XNumberLiteral;

import com.google.inject.Inject;

/**
 * @author Jan Koehnlein - Initial contribution and API
 */
public class NumberLiterals {

	@Inject
	private TypeReferences typeReferences;

	@Inject
	private ITypeProvider typeProvider;

	public int getBase(XNumberLiteral literal) {
		String valueAsLowerCase = literal.getValue().toLowerCase();
		if (valueAsLowerCase.startsWith("0x"))
			return 16;
		else if (valueAsLowerCase.startsWith("0") && valueAsLowerCase.length() > 1 && valueAsLowerCase.indexOf('.') == -1)
			return 8;
		else
			return 10;
	}

	protected String getTypeQualifier(XNumberLiteral literal) {
		String valueAsLowerCase = literal.getValue().toLowerCase();
		switch(getBase(literal)) {
			case 16:
				int index = valueAsLowerCase.indexOf('#');
				if(index != -1) 
					return valueAsLowerCase.substring(index + 1);
				else 
					return "";
			case 10:
			case 8:
				if(valueAsLowerCase.endsWith("bi") || valueAsLowerCase.endsWith("bd"))
					return valueAsLowerCase.substring(valueAsLowerCase.length()-2);
				char lastChar = valueAsLowerCase.charAt(literal.getValue().length()-1);
				switch(lastChar) {
					case 'l':
					case 'd':
					case 'f':
						return Character.toString(lastChar);
					default: 
						return "";
				}
			default:
				throw new IllegalArgumentException("Invalid number literal base " + getBase(literal));
		}
	}

	public String toJavaLiteral(XNumberLiteral literal) {
		if(getJavaType(literal).isPrimitive())
			return literal.getValue().replace("_", "").replace("#", "");
		else 
			return null;
	}
	
	public String getDigits(XNumberLiteral literal) {
		return getXbaseDigits(literal).replace("_", "");
	}
	
	protected String getXbaseDigits(XNumberLiteral literal) {
		String value = literal.getValue();
		int length = value.length();
		String typeQualifier = getTypeQualifier(literal);
		switch (getBase(literal)) {
			case 8:
				return value.substring(1, length - typeQualifier.length());
			case 10:
				return value.substring(0, length - typeQualifier.length());
			case 16:
				if(equal("", typeQualifier)) 
					return value.substring(2, length - typeQualifier.length());
				else 
					return value.substring(2, length - typeQualifier.length()-1);
			default:
				throw new IllegalArgumentException("Invalid number literal base " + getBase(literal));
		}
	}
	

	protected boolean isFloatingPoint(XNumberLiteral literal) {
		if (literal.getValue().indexOf('.') != -1) {
			return true;
		}
		String lowerCaseValue = literal.getValue().toLowerCase();
		switch (getBase(literal)) {
			case 8:
			case 16:
				return false;
			case 10:
				if(lowerCaseValue.indexOf('e') != -1)
					return true;
				char lastChar = lowerCaseValue.charAt(literal.getValue().length()-1);
				return lastChar == 'd' || lastChar == 'f';
			default:
				throw new IllegalArgumentException("Invalid number literal base " + getBase(literal));
		}
	}
	
	protected Class<? extends Number> getExplicitJavaType(XNumberLiteral literal) {
		String typeQualifier = getTypeQualifier(literal);
		if(equal("", typeQualifier))
			return null;
		else if (equal("f", typeQualifier))
			return Float.TYPE;
		else if (equal("l", typeQualifier))
			return Long.TYPE;
		else if (equal("d", typeQualifier))
			return Double.TYPE;
		else if (equal("bi", typeQualifier))
			return BigInteger.class;
		else if (equal("bd", typeQualifier))
			return BigDecimal.class;
		else
			throw new IllegalArgumentException("Invalid type qualifier " + typeQualifier);
	}
	
	public Class<? extends Number> getJavaType(XNumberLiteral literal) {
 		Class<? extends Number> explicitType = getExplicitJavaType(literal);
		return (explicitType == null) 
			? (isFloatingPoint(literal)) ? Double.TYPE : Integer.TYPE
			: explicitType;
	}

	public Number numberValue(XNumberLiteral literal, Class<? extends Number> numberType) {
		if(numberType == Integer.TYPE || numberType == Integer.class) {
			BigInteger asBigInt = toBigInteger(literal);
			BigInteger shiftRight = asBigInt.shiftRight(32);
			if(shiftRight.getLowestSetBit() != -1 || asBigInt.testBit(31) && getBase(literal) == 10) 
				throw new NumberFormatException("Integer literal is out of range: " + literal.getValue());
			return asBigInt.intValue();
		}
		else if(numberType == Double.TYPE || numberType == Double.class)
			return Double.parseDouble(getDigits(literal));
		else if(numberType == Long.TYPE || numberType == Long.class) {
			BigInteger asBigInt = toBigInteger(literal);
			BigInteger shiftRight = asBigInt.shiftRight(64);
			if(shiftRight.getLowestSetBit() != -1 || asBigInt.testBit(63) && getBase(literal) == 10) 
				throw new NumberFormatException("Long literal is out of range: " + literal.getValue());
			return asBigInt.longValue();
		} else if(numberType == Float.TYPE || numberType == Float.class)
			return Float.parseFloat(getDigits(literal));
		else if(numberType == BigInteger.class)
			return toBigInteger(literal);
		else if(numberType == BigDecimal.class)
			return toBigDecimal(literal);
		else 
			throw new IllegalArgumentException("Cannot convert number literal to type" + numberType.getCanonicalName());
	}

	protected BigInteger toBigInteger(XNumberLiteral literal) {
		if (isFloatingPoint(literal))
			return toBigDecimal(literal).toBigInteger();
		else
			return new BigInteger(getDigits(literal), getBase(literal));
	}

	protected BigDecimal toBigDecimal(XNumberLiteral literal) {
		if (isFloatingPoint(literal)) {
			return new BigDecimal(getDigits(literal));
		} else {
			int base = getBase(literal);
			switch (base) {
				case 16:
				case 8:
					return new BigDecimal(new BigInteger(getDigits(literal), base));
				case 10:
					return new BigDecimal(getDigits(literal));
				default:
					throw new IllegalArgumentException("Invalid number literal base " + base);
			}
		}
	}
}
