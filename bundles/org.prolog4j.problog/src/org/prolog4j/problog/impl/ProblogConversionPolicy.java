package org.prolog4j.problog.impl;

import org.prolog4j.ConversionPolicy;

public class ProblogConversionPolicy extends ConversionPolicy {

	@Override
	public boolean match(Object term1, Object term2) {
		
		return false;
	}

	@Override
	public boolean isInteger(Object term) {
		return term instanceof Integer;
	}

	@Override
	public boolean isDouble(Object term) {
		// TODO Auto-generated method stub
		return term instanceof Double;
	}

	@Override
	public boolean isAtom(Object term) {
		return false;
	}

	@Override
	public boolean isCompound(Object term) {
		return false;
	}

	@Override
	public Object term(int value) {
		return value;
	}

	@Override
	public Object term(double value) {
		return value;
	}

	@Override
	public Object term(String name) {
		return name;
	}

	@Override
	public Object term(String pattern, Object... args) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int intValue(Object term) {
		return ((int) term);
	}

	@Override
	public double doubleValue(Object term) {
		return ((double) term);
	}

	@Override
	protected String getName(Object compound) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getArity(Object compound) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object getArg(Object compound, int index) {
		// TODO Auto-generated method stub
		return null;
	}

}
