package org.prolog4j.problog.impl;

import java.util.ArrayList;
import java.util.List;

/**
 * A single result of a free variable in Problog, 
 * containing the value and probability.
 * 
 * @author Nicolas Boltz
 */
public class ProblogResult {
	
	private final List<String> values = new ArrayList<String>();
	private final List<Double> probabilities = new ArrayList<Double>();
	private int currentValuePos = 0;
	
	public ProblogResult() {}
	
	public String getValue() {
		if(hasNextValue()) {
			String result = values.get(currentValuePos);
			currentValuePos++;
			return result;
		} else {
			return null;
		}
	}
	
	public void addValue(String value, double probability) {
		values.add(value);
		probabilities.add(probability);
	}
	
	public boolean hasNextValue() {
		if(currentValuePos < values.size()) {
			return true;
		} else {
			return false;
		}
	}
}
