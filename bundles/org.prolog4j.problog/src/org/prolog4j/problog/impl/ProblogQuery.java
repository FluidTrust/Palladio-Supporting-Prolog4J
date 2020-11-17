package org.prolog4j.problog.impl;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.parser.IParseResult;
import org.palladiosimulator.supporting.prolog.model.prolog.CompoundTerm;
import org.palladiosimulator.supporting.prolog.model.prolog.expressions.Expression;
import org.palladiosimulator.supporting.prolog.model.prolog.expressions.LogicalAnd;
import org.palladiosimulator.supporting.prolog.model.prolog.expressions.Unification;
import org.palladiosimulator.supporting.prolog.parser.antlr.PrologParser;
import org.prolog4j.Query;
import org.prolog4j.Solution;
import org.prolog4j.base.PrologAPIWrapper;
import org.prolog4j.base.QueryReplacer;
import org.prolog4j.problog.ProblogExecutable;

public class ProblogQuery extends Query {

	private final ProblogProver prover;
	private final PrologParser parser;
	private final QueryReplacer queryReplacer;
	private final PrologAPIWrapper prologAPIWrapper;
	private final ProblogExecutable executable;
	
	protected ProblogQuery(ProblogProver prover, PrologAPIWrapper prologAPIWrapper, String goalPattern, ProblogExecutable executable) {
		super(goalPattern);
		this.prover = prover;
		this.prologAPIWrapper = prologAPIWrapper;
		this.parser = prologAPIWrapper.getPrologApi().getParser();
		this.queryReplacer = new QueryReplacer(prover.getConversionPolicy(), prologAPIWrapper, goalPattern);
		this.executable = executable;
	}

	@Override
	public <A> Solution<A> solve(Object... actualArgs) {
		String newGoal = queryReplacer.getQueryString(actualArgs);
		
		List<String> queries = parseQueries(newGoal);
		
		//get free variables from each query and create list
		List<String> freeVariables = new ArrayList<String>();
		for(int i = 0; i < queries.size(); ++i) {
			List<String> freeVarsOfQuery = getFreeVariablesExtensionPoint(queries.get(i));
			freeVariables.addAll(freeVarsOfQuery);
		}

		//create queryrule
		StringBuilder queryRuleBuilder = new StringBuilder();
		if(freeVariables.isEmpty()) {
			queryRuleBuilder.append("queryrule");
		} else {
			String freeVariableString = String.join(",", freeVariables);
			queryRuleBuilder.append("queryrule(").append(freeVariableString).append(")");
		}
		
		String queryRulePredicate = queryRuleBuilder.toString();
		queryRuleBuilder.append(" :- ").append(String.join(",", queries)).append(".");

		//create full output
		String knowledgeBase = prover.combineKnowledgeBase();
		StringBuilder problogQueryBuilder = new StringBuilder();
		problogQueryBuilder.append(knowledgeBase).append(queryRuleBuilder.toString()).append("\nquery(").append(queryRulePredicate).append(").");

		// run ProbLog
		String solution = executable.execute(problogQueryBuilder.toString());
		
		// run jproblog old --> delete
		//String solution = this.prover.getJProblog().getProcessor().apply(problogQueryBuilder.toString());

		return new ProblogSolution<A>(freeVariables, solution, this.prover.getConversionPolicy(), this.parser);
	}

	@Override
	public Query bind(int argument, Object value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Query bind(String variable, Object value) {
		// TODO Auto-generated method stub
		return null;
	}

	private List<String> getFreeVariablesExtensionPoint(String query) {
		List<String> freeVariables = new ArrayList<String>();
		IParseResult result = this.parser.parse(this.parser.getGrammarAccess().getExpression_1100_xfyRule(), new StringReader(query));

		EObject root = result.getRootASTElement();
		if(root != null) {
			if(root instanceof CompoundTerm) {
				CompoundTerm elements = (CompoundTerm) root;
				for(Expression expr : elements.getArguments()) {
					if(expr instanceof CompoundTerm) {
						CompoundTerm argument = (CompoundTerm)expr;
						//TODO: maybe check if argument list is null before calling isEmpty
						//this currently has not and should not result in an error
						if(argument.getArguments().isEmpty() && !argument.getValue().isEmpty()) {
							if(Character.isUpperCase(argument.getValue().charAt(0))) {
								freeVariables.add(argument.getValue());
							}
						}
					}
				}
			} else if(root instanceof Unification) {
				Unification unif = (Unification) root;
				if(unif.getLeft() instanceof CompoundTerm) {
					CompoundTerm left = (CompoundTerm) unif.getLeft();
					if(left.getArguments().isEmpty()) {
						if(Character.isUpperCase(left.getValue().charAt(0))) {
							freeVariables.add(left.getValue());
						}
					}
				}
			}
		}

		return freeVariables;
	}

	// parse Queries and seperate them
	private List<String> parseQueries(String concatenatedQueries) {
		List<String> queries = new ArrayList<>();
		IParseResult parseResult = this.parser.parse(this.parser.getGrammarAccess().getExpression_1100_xfyRule(), new StringReader(concatenatedQueries));
		
		if(parseResult.getRootASTElement() instanceof LogicalAnd) {
			LogicalAnd and = (LogicalAnd) parseResult.getRootASTElement();
			queries.addAll(parseQueries(and.getLeft()));
			queries.addAll(parseQueries(and.getRight()));
		} else if(parseResult.getRootASTElement() instanceof Expression) {
			String query = prologAPIWrapper.serializeExpression((Expression)parseResult.getRootASTElement());
			queries.add(query);
		}
		
		return queries;
	}
	
	// Recursive call for parsing and seperating the queries
	private List<String> parseQueries(Expression expr) {
		List<String> queries = new ArrayList<>();
		
		if(expr instanceof LogicalAnd) {
			LogicalAnd and = (LogicalAnd) expr;
			queries.addAll(parseQueries(and.getLeft()));
			queries.addAll(parseQueries(and.getRight()));
		} else if(expr instanceof Expression) {
			String query = prologAPIWrapper.serializeExpression((Expression)expr);
			queries.add(query);
		}
		
		return queries;
	}
}
