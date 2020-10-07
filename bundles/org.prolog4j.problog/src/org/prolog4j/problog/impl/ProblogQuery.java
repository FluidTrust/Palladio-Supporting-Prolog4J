package org.prolog4j.problog.impl;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.parser.IParseResult;
import org.eclipse.xtext.resource.XtextResource;
import org.palladiosimulator.supporting.prolog.api.PrologAPI;
import org.palladiosimulator.supporting.prolog.model.prolog.CompoundTerm;
import org.palladiosimulator.supporting.prolog.model.prolog.PrologFactory;
import org.palladiosimulator.supporting.prolog.model.prolog.expressions.Expression;
import org.palladiosimulator.supporting.prolog.model.prolog.expressions.LogicalAnd;
import org.palladiosimulator.supporting.prolog.parser.antlr.PrologParser;
import org.prolog4j.Query;
import org.prolog4j.Solution;

public class ProblogQuery extends Query {

	private final ProblogProver prover;
	private final PrologParser parser;
	private final QueryReplacer queryReplacer;
	private final PrologAPI prologAPI;

	protected ProblogQuery(ProblogProver prover, PrologAPI prologAPI, String goalPattern) {
		super(goalPattern);
		this.prover = prover;
		this.prologAPI = prologAPI;
		this.parser = prologAPI.getParser();
		this.queryReplacer = new QueryReplacer(prover.getConversionPolicy(), prologAPI, goalPattern);
	}

	@Override
	public <A> Solution<A> solve(Object... actualArgs) {
		//TODO: The QueryReplacer has been straight up copied from the swicli-bundle
		// there are a lot of similarities --> maybe centralized classes for text based prolog apis
		String newGoal = queryReplacer.getQueryString(actualArgs);
		
		List<String> queries = parseQueries(newGoal);
		
		//get free variables from each query and create list
		List<String> freeVariables = new ArrayList<String>();
		for(int i = 0; i < queries.size(); ++i) {
			List<String> freeVarsOfQuery = getFreeVariablesExtensionPoint(queries.get(i));
			freeVariables.addAll(freeVarsOfQuery);
		}
		String freeVariableString = String.join(",", freeVariables);

		//create queryrule
		StringBuilder queryRuleBuilder = new StringBuilder();
		queryRuleBuilder.append("queryrule(").append(freeVariableString).append(")");
		String queryRulePredicate = queryRuleBuilder.toString();
		queryRuleBuilder.append(" :- ").append(String.join(",", queries)).append(".");

		//create full output
		String knowledgeBase = prover.combineKnowledgeBase();
		StringBuilder problogQueryBuilder = new StringBuilder();
		problogQueryBuilder.append(knowledgeBase).append(queryRuleBuilder.toString()).append("\nquery(").append(queryRulePredicate).append(").");

		// run jproblog
		String solution = this.prover.getJProblog().getProcessor().apply(problogQueryBuilder.toString());

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
		if(root != null && root instanceof CompoundTerm) {
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
		}

		return freeVariables;
	}

	// parse Queries and seperate them at ',' 
	private List<String> parseQueries(String concatenatedQueries) {
		List<String> queries = new ArrayList<>();
		IParseResult parseResult = this.parser.parse(this.parser.getGrammarAccess().getExpression_1100_xfyRule(), new StringReader(concatenatedQueries));
		
		if(parseResult.getRootASTElement() instanceof LogicalAnd) {
			LogicalAnd and = (LogicalAnd) parseResult.getRootASTElement();
			queries.addAll(parseQueries(and.getLeft()));
			queries.addAll(parseQueries(and.getRight()));
		} else if(parseResult.getRootASTElement() instanceof Expression) {
			String query = serializeExpression((Expression)parseResult.getRootASTElement());
			queries.add(query);
		}
		
		return queries;
	}
	
	private List<String> parseQueries(Expression expr) {
		List<String> queries = new ArrayList<>();
		
		if(expr instanceof LogicalAnd) {
			LogicalAnd and = (LogicalAnd) expr;
			queries.addAll(parseQueries(and.getLeft()));
			queries.addAll(parseQueries(and.getRight()));
		} else if(expr instanceof Expression) {
			String query = serializeExpression((Expression)expr);
			queries.add(query);
		}
		
		return queries;
	}
	
	public String serializeExpression(Expression expr) {
    	var program = PrologFactory.eINSTANCE.createProgram();
        var rule = PrologFactory.eINSTANCE.createRule();
        program.getClauses().add(rule);
        var head = PrologFactory.eINSTANCE.createCompoundTerm();
        rule.setHead(head);
        head.setValue("test");
        rule.setBody(expr);
        var r = new XtextResource();
        r.getContents().add(program);
        String termString = prologAPI.getSerializer()
            .serialize(expr);
        return termString;
    }
}
