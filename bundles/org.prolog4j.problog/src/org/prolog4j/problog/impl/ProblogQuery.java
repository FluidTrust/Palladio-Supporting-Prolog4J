package org.prolog4j.problog.impl;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.parser.IParseResult;
import org.palladiosimulator.supporting.prolog.PrologStandaloneSetup;
import org.palladiosimulator.supporting.prolog.model.prolog.CompoundTerm;
import org.palladiosimulator.supporting.prolog.model.prolog.expressions.Expression;
import org.palladiosimulator.supporting.prolog.parser.antlr.PrologParser;
import org.palladiosimulator.supporting.prolog.services.PrologGrammarAccess;
import org.palladiosimulator.supporting.prolog.services.PrologGrammarAccess.CompoundTermElements;
import org.prolog4j.Query;
import org.prolog4j.Solution;
import org.prolog4j.problog.util.PrologToolProvider;

import com.google.common.base.CharMatcher;
import com.google.inject.Injector;

public class ProblogQuery extends Query {

	private ProblogProver prover;

	protected ProblogQuery(ProblogProver prover, String goalPattern) {
		super(goalPattern);
		this.prover = prover;
	}

	@Override
	public <A> Solution<A> solve(Object... actualArgs) {
		if(getPlaceholderNames().size() != actualArgs.length) {
			// throw exeption?
		}
		// replace placeholders with actual arguments
		String goal = getGoal();
		for(int i = 0; i < actualArgs.length; ++i) {
			String placeholder = getPlaceholderNames().get(i);
			String argument = (String)prover.getConversionPolicy().convertObject(actualArgs[i]);
			goal = goal.replaceFirst(placeholder, argument);


		}

		List<String> queries = parseQueries(goal);
		String knowledgeBase = prover.combineKnowledgeBase();
		StringBuilder queryRuleBuilder = new StringBuilder();
		queryRuleBuilder.append("queryrule(");

		List<String> freeVariables = new ArrayList<String>();

		//get free variables of each query
		StringBuilder freeVarsListBuilder = new StringBuilder();
		if(!queries.isEmpty()) {
			System.out.println("-----------------------------------------------------------");
			System.out.println(queries.get(0));
			List<String> tmp = getFreeVariablesExtensionPoint(queries.get(0));
			freeVariables.addAll(tmp);
			freeVarsListBuilder.append(String.join(",", tmp));
			for(int i = 1; i < queries.size(); ++i) {
				System.out.println("-----------------------------------------------------------");
				System.out.println(queries.get(i));
				tmp = getFreeVariablesExtensionPoint(queries.get(i));
				if(!freeVariables.isEmpty()) {
					freeVarsListBuilder.append(",");
				}
				freeVariables.addAll(tmp);
				freeVarsListBuilder.append(String.join(",", tmp));
			}

		}

		queryRuleBuilder.append(freeVarsListBuilder.toString()).append(")");
		String queryRulePredicate = queryRuleBuilder.toString();
		queryRuleBuilder.append(" :- ").append(String.join(",", queries)).append(".");

		//create full output
		StringBuilder problogQueryBuilder = new StringBuilder();
		problogQueryBuilder.append(knowledgeBase).append(queryRuleBuilder.toString()).append("\nquery(").append(queryRulePredicate).append(").");

		// run jproblog
		String solution = this.prover.getJProblog().getProcessor().apply(problogQueryBuilder.toString());

		return new ProblogSolution<A>(freeVariables, solution, this.prover.getConversionPolicy());
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
		PrologToolProvider toolProvider = new PrologToolProvider();
		PrologParser parser = toolProvider.getParser();
		PrologGrammarAccess grammar = toolProvider.getGrammarAccess();

		Reader targetReader = new StringReader(query);

		IParseResult result = parser.parse(grammar.getExpression_1100_xfyRule(), targetReader);

		EObject root = result.getRootASTElement();
		if(root != null && root instanceof CompoundTerm) {
			CompoundTerm elements = (CompoundTerm) root;
			for(Expression expr : elements.getArguments()) {
				if(expr instanceof CompoundTerm) {
					CompoundTerm argument = (CompoundTerm)expr;
					//todo: maybe check if argument list is null before calling isEmpty
					//this currently has not and should not result in an error
					if(argument.getArguments().isEmpty() && argument.getValue().length() == 1) {
						if(Character.isUpperCase(argument.getValue().charAt(0))) {
							freeVariables.add(argument.getValue());
						}
					}
				}
			}
		}

		return freeVariables;
	}

	// parse Queries and seperate them at ',' 
	private List<String> parseQueries(String concatenatedQueries) {
		List<String> queries = new ArrayList<>();
		String[] splitQueries = concatenatedQueries.split(",");
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

	//	private void getFreeVariablesStandalone(String query) {
	//	PrologStandaloneSetup setup = new PrologStandaloneSetup();
	//	Injector inject = setup.createInjectorAndDoEMFRegistration();
	//	PrologParser parser = inject.getInstance(PrologParser.class);
	//	parser.doParse(query);
	//}

}
