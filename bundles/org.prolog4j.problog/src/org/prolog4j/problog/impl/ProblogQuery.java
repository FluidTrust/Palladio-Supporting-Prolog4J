package org.prolog4j.problog.impl;

import java.util.ArrayList;
import java.util.List;

import org.prolog4j.Query;
import org.prolog4j.Solution;

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
			Object argument = actualArgs[i];
			goal.replaceFirst(getPlaceholderNames().get(i), argument.toString());
		}
		// add knowledge base
		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append(prover.combineKnowledgeBase());
		// add queries
		List<String> queries = parseQueries(goal);
		for(String query : queries) {
			queryBuilder.append("query(").append(query).append(").").append('\n');
		}
		// run jproblog
		String solution = this.prover.getJProblog().getProcessor().apply(queryBuilder.toString());
		return new ProblogSolution<A>(solution);
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
	
	private List<String> parseQueries(String concatenatedQueries) {
		List<String> queries = new ArrayList<>();
		String[] splitQueries = concatenatedQueries.split(",");
		int openBracketCount = 0;
		int openSquareBracketCount = 0;
		StringBuilder queryBuilder = new StringBuilder();
		for(String queryFragment : splitQueries) {
			openBracketCount += queryFragment.contains("(") ? 1:0;
			openBracketCount -= queryFragment.contains(")") ? 1:0;
			
			openSquareBracketCount += queryFragment.contains("[") ? 1:0;
			openSquareBracketCount -= queryFragment.contains("]") ? 1:0;
			
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
