package org.prolog4j.problog.impl;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.xtext.parser.IParseResult;
import org.palladiosimulator.supporting.prolog.model.prolog.AtomicDouble;
import org.palladiosimulator.supporting.prolog.model.prolog.AtomicNumber;
import org.palladiosimulator.supporting.prolog.model.prolog.CompoundTerm;
import org.palladiosimulator.supporting.prolog.model.prolog.expressions.Expression;
import org.palladiosimulator.supporting.prolog.model.prolog.expressions.ModuleCall;
import org.palladiosimulator.supporting.prolog.parser.antlr.PrologParser;
import org.prolog4j.ConversionPolicy;
import org.prolog4j.Solution;
import org.prolog4j.SolutionIterator;
import org.prolog4j.UnknownVariableException;

public class ProblogSolution<S> extends Solution<S> {
	
	private List<String> freeVariables;
	private String rawSolutions;
	private Map<String, ProblogResult> variableValues = new HashMap<String, ProblogResult>();
	
	private double overallProbability = 0.0;
	
	private ConversionPolicy cp;
	private PrologParser parser;
	
	public ProblogSolution(List<String> freeVariables, String solutions, ConversionPolicy conversionPolicy, PrologParser parser) {
		this.freeVariables = freeVariables;
		this.rawSolutions = solutions;
		this.cp = conversionPolicy;
		this.parser = parser;
		
		// initialize free variable assignment map
		for(String var : freeVariables) {
			variableValues.put(var, new ProblogResult());
		}
		
		separateSolutions();
	}
	
	private void separateSolutions() {
		List<String> solutions = Arrays.asList(rawSolutions.split("\n"));
		int validSolutions = 0;
		//parse for free Variables

		for(String solution : solutions) {
			IParseResult parseResult = this.parser.parse(this.parser.getGrammarAccess().getExpression_1100_xfyRule(), new StringReader(solution));
			
			if(parseResult.hasSyntaxErrors()) {
				overallProbability = 0.0;
				break;
			} else if(parseResult.getRootASTElement() instanceof ModuleCall) {
				ModuleCall call = (ModuleCall) parseResult.getRootASTElement();
				double probability = 0.0;
				
				//right element is the probability of this solution
				if(call.getRight() instanceof AtomicNumber) {
					AtomicNumber probabilityElement = (AtomicNumber) call.getRight();
					probability = probabilityElement.getValue();
				} else if(call.getRight() instanceof AtomicDouble) {
					AtomicDouble probabilityElement = (AtomicDouble) call.getRight();
					probability = probabilityElement.getValue();
				}
				
				//left is the query with the actual allocation of the free variables
				if(call.getLeft() instanceof CompoundTerm) {
					CompoundTerm queryRuleElement = (CompoundTerm) call.getLeft();
					if(queryRuleElement.getValue().equals("queryrule")) {
						List<Expression> queryRuleArgumentAllocation = queryRuleElement.getArguments();
						
						if(freeVariables.isEmpty()) {
							addValueToMap("", null, probability);
							this.on("");
						} else {
							for(int i = 0; i < queryRuleArgumentAllocation.size() && i < freeVariables.size(); ++i) {
								addValueToMap(freeVariables.get(i), queryRuleArgumentAllocation.get(i), probability);
							}
							
							this.on(freeVariables.get(0));
						}
						validSolutions++;
						overallProbability += probability;
					}
				}
			}
		}
		
		overallProbability = overallProbability / validSolutions;
	}
	
	private void addValueToMap(String key, Expression value, double probability) {
		if(variableValues.containsKey(key)) {
			variableValues.get(key).addValue(value, probability);
		} else {
			ProblogResult result = new ProblogResult();
			result.addValue(value, probability);
			variableValues.put(key, result);
		}
	}

	@Override
	public boolean isSuccess() {
		// true if probability is 1 (100%)
		boolean success = overallProbability == 1.0 ? true : false;
		return success;
	}

	@SuppressWarnings("unchecked")
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
		if(freeVariables.isEmpty()) {
			return variableValues.get("").hasNextValue();
		} else {
			//TODO: Produziert einen Fehler im Zusammenhang mit on(), da hier jetzt die falsche Variable
			//gefetched wird, da es aber bei mehrfachen freien vars immer die maximale Anzahl an Ergebnissen für jede
			//Var gibt, könnte es trotzdem passen.
			return variableValues.get(freeVariables.get(0)).hasNextValue();
		}
		
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
}
