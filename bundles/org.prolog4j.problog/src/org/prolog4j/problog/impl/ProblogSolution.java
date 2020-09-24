package org.prolog4j.problog.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.prolog4j.ConversionPolicy;
import org.prolog4j.Solution;
import org.prolog4j.SolutionIterator;
import org.prolog4j.UnknownVariableException;

import com.google.common.base.CharMatcher;

public class ProblogSolution<S> extends Solution<S> {
	
	private List<String> freeVariables;
	private String rawSolutions;
	private Map<String, ProblogResult> variableValues = new HashMap<String, ProblogResult>();
	
	private double overallProbability = 0.0;
	
	private String localDefaultOutputVariable = "";
	
	private ConversionPolicy cp;
	
	public ProblogSolution(List<String> freeVariables, String solutions, ConversionPolicy conversionPolicy) {
		this.freeVariables = freeVariables;
		this.rawSolutions = solutions;
		this.cp = conversionPolicy;
		
		// initialize free variable assignment map
		for(String var : freeVariables) {
			variableValues.put(var, new ProblogResult());
		}
		
		separateSolutions();
	}
	
	private void separateSolutions() {
		List<String> solutions = Arrays.asList(rawSolutions.split("\n"));
		//parse for free Variables
		for(String solution : solutions) {
			solution = solution.replaceFirst("\\s*", "");
			if(solution.startsWith("ParseError:")) {
				overallProbability = 0.0;
				break;
			}
			
			if(solution.startsWith("queryrule(")) {
				String[] tmp = solution.split("\t");
				String probabilityString = tmp[1];
				probabilityString = probabilityString.replaceAll("\\s", "");
				double probability = Double.valueOf(probabilityString);
				overallProbability += probability;
				if(freeVariables.isEmpty()) {
					//if no free variables, create and add a mock
					if(variableValues.containsKey("")) {
						variableValues.get("").addValue("", probability);
					} else {
						ProblogResult result = new ProblogResult();
						result.addValue("", probability);
						variableValues.put("", result);
					} 
					defaultOutputVariable = "";
				} else {
					String predicate = tmp[0];
					String variableString = predicate.substring(predicate.indexOf("(") + 1, predicate.lastIndexOf(")"));
					List<String> variables = parseFreeVariableValues(variableString);
					if(variables.size() != freeVariables.size()) {
						//error?!
					}
					for(int i = 0; i < variables.size() && i < freeVariables.size(); ++i) {
						if(variableValues.containsKey(freeVariables.get(i))) {
							variableValues.get(freeVariables.get(i)).addValue(variables.get(i), probability);
						} else {
							ProblogResult result = new ProblogResult();
							result.addValue(variables.get(i), probability);
							variableValues.put(freeVariables.get(i), result);
						}
					}
					defaultOutputVariable = freeVariables.get(0);
				}
			}
		}
		
		overallProbability = overallProbability / solutions.size();
	}

	@Override
	public boolean isSuccess() {
		// true if probability is 1 (100%)
		boolean success = overallProbability == 1.0 ? true : false;
		return success;
	}

	@Override
	public <A> A get(String variable) {
		if (clazz == null) {	
			// Get values of variable and convert them
			ProblogResult result = variableValues.get(variable);
	
			if (result == null) {
				throw new UnknownVariableException(variable);
			}
			return (A) cp.convertTerm(result.getValue());
		}
		return (A) get(variable, clazz);
	}

	@Override
	public <A> A get(String variable, Class<A> type) {
		ProblogResult result = variableValues.get(variable);
		
		if (result == null) {
			throw new UnknownVariableException(variable);
		}
		
		return cp.convertTerm(result.getValue(), type);
	}

	@Override
	protected boolean fetch() {
		//does not really fetch, since all solutions are already in variableValues
		return variableValues.get(localDefaultOutputVariable).hasNextValue();
	}

	@Override
	public void collect(Collection<?>... collections) {
		SolutionIterator<S> it = iterator();
		while (it.hasNext()) {
			it.next();
			for (int i = 0; i < collections.length && i < freeVariables.size(); ++i) {
				collections[i].add(it.get(freeVariables.get(i)));
			}
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List<?>[] toLists() {
		List<?>[] lists = new List<?>[freeVariables.size() - 1];
		for (int i = 0; i < lists.length; ++i) {
			lists[i] = new ArrayList();
		}
		collect(lists);
		return lists;
	}
	
	@Override
	public <A> Solution<A> on(final String variable) {
		//localDefaultOutputVariable = variable;
		return super.on(variable);
	}

	@Override
	public <A> Solution<A> on(final String variable, final Class<A> clazz) {
		//localDefaultOutputVariable = variable;
		return super.on(variable,clazz);
	}

	@Override
	public SolutionIterator<S> iterator() {
		return new SolutionIterator<S>() {

			@Override
			public boolean hasNext() {
				return ProblogSolution.this.variableValues.get(ProblogSolution.this.defaultOutputVariable).hasNextValue();
			}

			@Override
			public S next() {
				if (!hasNext()) {
					throw new NoSuchElementException();
				}
				return get(ProblogSolution.this.defaultOutputVariable);
			}
			
			@Override
			public S get(String variable) {
				if (clazz == null) {
					return ProblogSolution.this.<S>get(variable);
				}
				return ProblogSolution.this.get(variable, clazz);
			}

			@Override
			public <A> A get(String variable, Class<A> type) {
				return ProblogSolution.this.get(variable, type);
			}
			
		};
	}
	
	// todo gleiche Funktion in PRoblogQuery --> eigene unterklasse mit utils?
	private List<String> parseFreeVariableValues(String freeVariableString) {
		List<String> queries = new ArrayList<>();
		String[] splitQueries = freeVariableString.split(",");
		int openBracketCount = 0;
		int openSquareBracketCount = 0;
		StringBuilder queryBuilder = new StringBuilder();
		for(String queryFragment : splitQueries) {
			openBracketCount += CharMatcher.is('(').countIn(queryFragment);
			openBracketCount -= CharMatcher.is(')').countIn(queryFragment);
			
			openSquareBracketCount += CharMatcher.is('[').countIn(queryFragment);
			openSquareBracketCount -= CharMatcher.is(']').countIn(queryFragment);
			
			if(queryFragment.endsWith(".")) {
				queryFragment = queryFragment.substring(0, queryFragment.length() - 1);
			}
			queryFragment = queryFragment.trim();
			queryBuilder.append(queryFragment);
			
			if(openBracketCount == 0 && openSquareBracketCount == 0) {
				queries.add(queryBuilder.toString());
				queryBuilder = new StringBuilder();
			} else {
				queryBuilder.append(",");
			}
		}
		return queries;
	}
}
