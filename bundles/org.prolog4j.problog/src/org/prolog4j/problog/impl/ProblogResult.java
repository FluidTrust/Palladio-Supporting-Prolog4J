package org.prolog4j.problog.impl;

import java.util.ArrayList;
import java.util.List;

import org.palladiosimulator.supporting.prolog.model.prolog.expressions.Expression;

/**
 * A single result of a free variable in Problog, 
 * containing the value and probability.
 * 
 * @author Nicolas Boltz
 */
public class ProblogResult {
	private class Result {
		private final Expression value;
		private final Double probability;
		
		private Result(Expression value, Double probability) {
			this.value = value;
			this.probability = probability;
		}
		
		private Expression getValue() {
			return this.value;
		}
		
		private Double getProbability() {
			return this.probability;
		}
	}
	
	private final List<Result> results = new ArrayList<Result>();
	private int currentResult = 0;
	
	private Result getResult() {
		if(results.isEmpty()) {
			return null;
		}
		return results.get(currentResult);
	}
	
	public Expression getValue() {
		return getResult().getValue();
	}
	
	public Double getProbability() {
		return getResult().getProbability();
	}
	
	public void addResult(Expression value, double probability) {
		results.add(new Result(value, probability));
	}
	
	public boolean fetch() {
		if(hasNextValue()) {
			currentResult++;
			return true;
		} else {
			return false;
		}
	}
	
	private boolean hasNextValue() {
		if(currentResult < results.size() - 1) {
			return true;
		} else {
			return false;
		}
	}
}
